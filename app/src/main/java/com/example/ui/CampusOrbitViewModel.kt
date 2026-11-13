package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AttendanceRecord
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.CampusOrbitRepository
import com.example.data.DemoSeedData
import com.example.data.FacultyRecord
import com.example.data.GrievanceRecord
import com.example.data.OrbitRole
import com.example.data.StudentRecord
import com.example.data.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class OrbitSection(val title: String, val subtitle: String) {
    OVERVIEW("Command Center", "Real-time institutional KPIs, analytics, and role insights"),
    STUDENTS("Student Management", "Enrollment, profiles, CGPA, attendance, and bulk CSV import/export"),
    FACULTY("Faculty Portal", "Professor directory, subject allocation, qualifications, and workload"),
    ACADEMICS("Depts, Courses & Timetable", "Departments, credit courses, and conflict-free weekly timetable"),
    ATTENDANCE_QR("Smart QR Attendance", "Live QR session generator, student QR scanner, and attendance logs"),
    EXAMS_ASSIGNMENTS("Exams, Results & LMS", "SGPA/CGPA grading, hall tickets, assignments, and study materials"),
    FINANCE_GRIEVANCES("Finance & Grievances", "Tuition fee invoices, digital receipts, and grievance redressal"),
    COLLABORATION_HUB("Campus Collaboration Hub", "Interdisciplinary project discovery, teams, tasks, showcase & faculty mentorship"),
    CAMPUS_LIFE("Facilities, Certs & Career", "Resource booking, certificates, events, study groups, and placements"),
    PARENT_PORTAL("Parent & Guardian Hub", "Verified ward oversight, attendance alerts, fees, and mentor connect"),
    SUPER_ADMIN_GOVERNANCE("Governance & Audit", "Institution configuration, RBAC matrix, and security audit trail")
}

data class GlobalSearchResult(
    val category: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val targetSection: OrbitSection
)

class CampusOrbitViewModel(
    private val repository: CampusOrbitRepository?,
    private val currentUserId: String,
    initialPreferredRole: OrbitRole,
    val isDemoMode: Boolean = false
) : ViewModel() {

    private val _selectedSection = MutableStateFlow(defaultSectionForRole(initialPreferredRole))
    val selectedSection: StateFlow<OrbitSection> = _selectedSection.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(
        if (isDemoMode) "Signed in with Demo Account: ${initialPreferredRole.demoName} (${initialPreferredRole.label})" else null
    )
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _activeQrToken = MutableStateFlow("ORB-CS601-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-LIVE")
    val activeQrToken: StateFlow<String> = _activeQrToken.asStateFlow()

    private val _activeQrSubject = MutableStateFlow("CS601 • Distributed Cloud Systems")
    val activeQrSubject: StateFlow<String> = _activeQrSubject.asStateFlow()

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    // In-memory backing states used when isDemoMode == true
    private val demoProfileFlow = MutableStateFlow<UserProfile?>(DemoSeedData.buildDemoProfile(initialPreferredRole))
    private val demoStudentsFlow = MutableStateFlow(DemoSeedData.initialStudents(currentUserId))
    private val demoFacultyFlow = MutableStateFlow(DemoSeedData.initialFaculty(currentUserId))
    private val demoAttendanceFlow = MutableStateFlow(DemoSeedData.initialAttendance(currentUserId))
    private val demoGrievancesFlow = MutableStateFlow(DemoSeedData.initialGrievances(currentUserId))
    private val demoCampusItemsFlow = MutableStateFlow(DemoSeedData.initialCampusItems(currentUserId))

    // Real-time Flows (Firestore when authenticated, in-memory DemoSeedData when isDemoMode == true)
    val profileState: StateFlow<UiState<UserProfile?>> = if (isDemoMode || repository == null) {
        demoProfileFlow
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(demoProfileFlow.value))
    } else {
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing profile", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load user profile"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    }

    val studentsState: StateFlow<UiState<List<StudentRecord>>> = if (isDemoMode || repository == null) {
        demoStudentsFlow
            .map<List<StudentRecord>, UiState<List<StudentRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(demoStudentsFlow.value))
    } else {
        repository.observeStudents(currentUserId)
            .map<List<StudentRecord>, UiState<List<StudentRecord>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing students", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe students"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    }

    val facultyState: StateFlow<UiState<List<FacultyRecord>>> = if (isDemoMode || repository == null) {
        demoFacultyFlow
            .map<List<FacultyRecord>, UiState<List<FacultyRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(demoFacultyFlow.value))
    } else {
        repository.observeFaculty(currentUserId)
            .map<List<FacultyRecord>, UiState<List<FacultyRecord>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing faculty", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe faculty"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    }

    val attendanceState: StateFlow<UiState<List<AttendanceRecord>>> = if (isDemoMode || repository == null) {
        demoAttendanceFlow
            .map<List<AttendanceRecord>, UiState<List<AttendanceRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(demoAttendanceFlow.value))
    } else {
        repository.observeAttendance(currentUserId)
            .map<List<AttendanceRecord>, UiState<List<AttendanceRecord>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing attendance", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe attendance"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    }

    val grievancesState: StateFlow<UiState<List<GrievanceRecord>>> = if (isDemoMode || repository == null) {
        demoGrievancesFlow
            .map<List<GrievanceRecord>, UiState<List<GrievanceRecord>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(demoGrievancesFlow.value))
    } else {
        repository.observeGrievances(currentUserId)
            .map<List<GrievanceRecord>, UiState<List<GrievanceRecord>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing grievances", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe grievances"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    }

    val campusItemsState: StateFlow<UiState<List<CampusModuleItem>>> = if (isDemoMode || repository == null) {
        demoCampusItemsFlow
            .map<List<CampusModuleItem>, UiState<List<CampusModuleItem>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(demoCampusItemsFlow.value))
    } else {
        repository.observeCampusItems(currentUserId)
            .map<List<CampusModuleItem>, UiState<List<CampusModuleItem>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing campus items", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe campus items"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    }

    init {
        if (!isDemoMode && repository != null) {
            initializeWorkspace(initialPreferredRole)
        }
    }

    private fun initializeWorkspace(preferredRole: OrbitRole) {
        val repo = repository ?: return
        viewModelScope.launch {
            _isSeeding.value = true
            val user = Firebase.auth.currentUser
            repo.ensureOrUpdateUserProfile(
                fullName = user?.displayName ?: preferredRole.demoName,
                email = user?.email ?: preferredRole.demoEmail,
                activeRole = preferredRole
            )
            repo.seedInstitutionalDataIfEmpty()
            _isSeeding.value = false
        }
    }

    fun selectSection(section: OrbitSection) {
        _selectedSection.value = section
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun switchRole(role: OrbitRole) {
        if (isDemoMode || repository == null) {
            val existing = demoProfileFlow.value ?: DemoSeedData.buildDemoProfile(role)
            demoProfileFlow.value = existing.copy(
                fullName = role.demoName,
                email = role.demoEmail,
                activeRole = role.name,
                linkedStudentCode = role.demoLinkedStudentCode,
                updatedAt = Timestamp.now()
            )
            _selectedSection.value = defaultSectionForRole(role)
            _statusBanner.value = "Switched Demo Account to ${role.demoName} (${role.label})"
            return
        }

        viewModelScope.launch {
            repository.switchActiveRole(role).onSuccess {
                _statusBanner.value = "Switched active role portal to ${role.label}"
                _selectedSection.value = defaultSectionForRole(role)
            }.onFailure {
                _statusBanner.value = "Failed to switch role: ${it.localizedMessage}"
            }
        }
    }

    fun updateInstitutionConfig(institutionName: String, linkedStudentCode: String, activeRole: OrbitRole) {
        if (isDemoMode || repository == null) {
            val existing = demoProfileFlow.value ?: DemoSeedData.buildDemoProfile(activeRole)
            demoProfileFlow.value = existing.copy(
                institutionName = institutionName.trim().ifBlank { existing.institutionName },
                linkedStudentCode = linkedStudentCode.trim().ifBlank { existing.linkedStudentCode },
                activeRole = activeRole.name,
                updatedAt = Timestamp.now()
            )
            _statusBanner.value = "Institutional settings updated in Demo Workspace."
            return
        }

        viewModelScope.launch {
            repository.switchActiveRole(
                role = activeRole,
                institutionName = institutionName,
                linkedStudentCode = linkedStudentCode
            ).onSuccess {
                _statusBanner.value = "Institutional settings updated in Cloud Firestore."
            }.onFailure {
                _statusBanner.value = "Error updating settings: ${it.localizedMessage}"
            }
        }
    }

    // --- STUDENT OPERATIONS ---

    fun addStudent(
        studentCode: String,
        fullName: String,
        email: String,
        department: String,
        semester: Long,
        cgpa: Double,
        attendancePct: Long,
        feeStatus: String,
        guardianName: String,
        guardianPhone: String,
        mentorName: String
    ) {
        if (isDemoMode || repository == null) {
            val now = Timestamp.now()
            val newRec = StudentRecord(
                id = "demo_stu_${UUID.randomUUID().toString().take(8)}",
                userId = currentUserId,
                studentCode = studentCode.trim(),
                fullName = fullName.trim(),
                email = email.trim(),
                department = department.trim(),
                semester = semester.coerceIn(1L, 12L),
                cgpa = cgpa.coerceIn(0.0, 10.0),
                attendancePct = attendancePct.coerceIn(0L, 100L),
                feeStatus = feeStatus,
                guardianName = guardianName.trim(),
                guardianPhone = guardianPhone.trim(),
                mentorName = mentorName.trim(),
                status = "ACTIVE",
                createdAt = now,
                updatedAt = now
            )
            demoStudentsFlow.value = (demoStudentsFlow.value + newRec).sortedBy { it.studentCode }
            _statusBanner.value = "Student $fullName ($studentCode) enrolled."
            return
        }

        viewModelScope.launch {
            repository.createStudent(
                studentCode = studentCode,
                fullName = fullName,
                email = email,
                department = department,
                semester = semester,
                cgpa = cgpa,
                attendancePct = attendancePct,
                feeStatus = feeStatus,
                guardianName = guardianName,
                guardianPhone = guardianPhone,
                mentorName = mentorName
            ).onSuccess {
                _statusBanner.value = "Student $fullName ($studentCode) enrolled in Firestore."
            }.onFailure {
                _statusBanner.value = "Enrollment error: ${it.localizedMessage}"
            }
        }
    }

    fun importStudentsFromCsv(csvText: String) {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("studentCode", ignoreCase = true) }
        if (lines.isEmpty()) {
            _statusBanner.value = "CSV input is empty. Provide rows as: Code,Name,Email,Dept,Sem,CGPA,Attendance%"
            return
        }

        if (isDemoMode || repository == null) {
            val now = Timestamp.now()
            val added = mutableListOf<StudentRecord>()
            for (line in lines) {
                val cols = line.split(",").map { it.trim() }
                if (cols.size >= 4) {
                    val code = cols[0].ifBlank { "STU-${UUID.randomUUID().toString().take(5).uppercase()}" }
                    val name = cols[1].ifBlank { "Imported Scholar" }
                    val email = cols[2].ifBlank { "${code.lowercase()}@campusorbit.edu" }
                    val dept = cols[3].ifBlank { "Computer Science & Engineering" }
                    val sem = cols.getOrNull(4)?.toLongOrNull() ?: 4L
                    val cgpa = cols.getOrNull(5)?.toDoubleOrNull() ?: 8.5
                    val att = cols.getOrNull(6)?.toLongOrNull() ?: 88L
                    added.add(
                        StudentRecord(
                            id = "demo_stu_${UUID.randomUUID().toString().take(8)}",
                            userId = currentUserId,
                            studentCode = code,
                            fullName = name,
                            email = email,
                            department = dept,
                            semester = sem,
                            cgpa = cgpa,
                            attendancePct = att,
                            feeStatus = "PAID",
                            guardianName = "Verified Guardian",
                            guardianPhone = "+1-555-0199",
                            mentorName = "Dr. Elena Rostova",
                            status = "ACTIVE",
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
            }
            demoStudentsFlow.value = (demoStudentsFlow.value + added).sortedBy { it.studentCode }
            _statusBanner.value = "Bulk CSV import completed: ${added.size} student records added."
            return
        }

        viewModelScope.launch {
            var importedCount = 0
            for (line in lines) {
                val cols = line.split(",").map { it.trim() }
                if (cols.size >= 4) {
                    val code = cols[0].ifBlank { "STU-${UUID.randomUUID().toString().take(5).uppercase()}" }
                    val name = cols[1].ifBlank { "Imported Scholar" }
                    val email = cols[2].ifBlank { "${code.lowercase()}@campusorbit.edu" }
                    val dept = cols[3].ifBlank { "Computer Science & Engineering" }
                    val sem = cols.getOrNull(4)?.toLongOrNull() ?: 4L
                    val cgpa = cols.getOrNull(5)?.toDoubleOrNull() ?: 8.5
                    val att = cols.getOrNull(6)?.toLongOrNull() ?: 88L
                    val res = repository.createStudent(
                        studentCode = code,
                        fullName = name,
                        email = email,
                        department = dept,
                        semester = sem,
                        cgpa = cgpa,
                        attendancePct = att,
                        feeStatus = "PAID",
                        guardianName = "Verified Guardian",
                        guardianPhone = "+1-555-0199",
                        mentorName = "Dr. Elena Rostova"
                    )
                    if (res.isSuccess) importedCount++
                }
            }
            _statusBanner.value = "Bulk CSV import completed: $importedCount student records saved to Firestore."
        }
    }

    fun updateStudentFeeOrAttendance(student: StudentRecord, newFeeStatus: String, newAttendancePct: Long) {
        if (isDemoMode || repository == null) {
            demoStudentsFlow.value = demoStudentsFlow.value.map {
                if (it.id == student.id) {
                    it.copy(
                        feeStatus = newFeeStatus,
                        attendancePct = newAttendancePct.coerceIn(0L, 100L),
                        updatedAt = Timestamp.now()
                    )
                } else it
            }
            _statusBanner.value = "Updated ${student.fullName}'s profile."
            return
        }

        viewModelScope.launch {
            repository.updateStudent(
                student.copy(
                    feeStatus = newFeeStatus,
                    attendancePct = newAttendancePct.coerceIn(0L, 100L)
                )
            ).onSuccess {
                _statusBanner.value = "Updated ${student.fullName}'s profile."
            }.onFailure {
                _statusBanner.value = "Update failed: ${it.localizedMessage}"
            }
        }
    }

    fun removeStudent(studentId: String, studentName: String) {
        if (isDemoMode || repository == null) {
            demoStudentsFlow.value = demoStudentsFlow.value.filterNot { it.id == studentId }
            _statusBanner.value = "Removed student record: $studentName"
            return
        }

        viewModelScope.launch {
            repository.deleteStudent(studentId).onSuccess {
                _statusBanner.value = "Removed student record: $studentName"
            }
        }
    }

    // --- FACULTY OPERATIONS ---

    fun addFaculty(
        facultyCode: String,
        fullName: String,
        email: String,
        department: String,
        designation: String,
        qualification: String,
        assignedSubjects: String,
        workloadHours: Long,
        experienceYears: Long
    ) {
        if (isDemoMode || repository == null) {
            val now = Timestamp.now()
            val rec = FacultyRecord(
                id = "demo_fac_${UUID.randomUUID().toString().take(8)}",
                userId = currentUserId,
                facultyCode = facultyCode.trim(),
                fullName = fullName.trim(),
                email = email.trim(),
                department = department.trim(),
                designation = designation.trim(),
                qualification = qualification.trim(),
                assignedSubjects = assignedSubjects.trim(),
                workloadHours = workloadHours,
                experienceYears = experienceYears,
                createdAt = now,
                updatedAt = now
            )
            demoFacultyFlow.value = (demoFacultyFlow.value + rec).sortedBy { it.facultyCode }
            _statusBanner.value = "Faculty profile $fullName ($facultyCode) added."
            return
        }

        viewModelScope.launch {
            repository.createFaculty(
                facultyCode = facultyCode,
                fullName = fullName,
                email = email,
                department = department,
                designation = designation,
                qualification = qualification,
                assignedSubjects = assignedSubjects,
                workloadHours = workloadHours,
                experienceYears = experienceYears
            ).onSuccess {
                _statusBanner.value = "Faculty profile $fullName ($facultyCode) added."
            }.onFailure {
                _statusBanner.value = "Failed to add faculty: ${it.localizedMessage}"
            }
        }
    }

    fun removeFaculty(facultyId: String, name: String) {
        if (isDemoMode || repository == null) {
            demoFacultyFlow.value = demoFacultyFlow.value.filterNot { it.id == facultyId }
            _statusBanner.value = "Removed faculty record: $name"
            return
        }

        viewModelScope.launch {
            repository.deleteFaculty(facultyId).onSuccess {
                _statusBanner.value = "Removed faculty record: $name"
            }
        }
    }

    // --- QR ATTENDANCE & MANUAL ATTENDANCE ---

    fun generateNewQrAttendanceSession(subjectCode: String, subjectName: String) {
        val dateStr = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val randomSuffix = UUID.randomUUID().toString().take(4).uppercase()
        _activeQrToken.value = "ORB-$subjectCode-$dateStr-$randomSuffix"
        _activeQrSubject.value = "$subjectCode • $subjectName"
        _statusBanner.value = "New live QR attendance token generated for $subjectCode"
    }

    fun recordStudentAttendance(
        studentCode: String,
        studentName: String,
        subjectCode: String,
        subjectName: String,
        department: String,
        semester: Long,
        status: String,
        method: String,
        sessionToken: String
    ) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val existingList = (attendanceState.value as? UiState.Success)?.data ?: emptyList()
        val duplicate = existingList.any {
            it.studentCode.equals(studentCode.trim(), ignoreCase = true) &&
                it.subjectCode.equals(subjectCode.trim(), ignoreCase = true) &&
                it.sessionDate == today &&
                it.sessionToken == sessionToken.trim()
        }
        if (duplicate) {
            _statusBanner.value = "Duplicate prevented: $studentName already marked for session $sessionToken"
            return
        }

        if (isDemoMode || repository == null) {
            val now = Timestamp.now()
            val newRec = AttendanceRecord(
                id = "demo_att_${UUID.randomUUID().toString().take(8)}",
                userId = currentUserId,
                studentCode = studentCode.trim(),
                studentName = studentName.trim(),
                subjectCode = subjectCode.trim(),
                subjectName = subjectName.trim(),
                department = department.trim(),
                semester = semester,
                sessionDate = today,
                status = status,
                method = method,
                sessionToken = sessionToken.trim(),
                createdAt = now,
                updatedAt = now
            )
            demoAttendanceFlow.value = listOf(newRec) + demoAttendanceFlow.value
            _statusBanner.value = "Attendance ($status via $method) recorded for $studentName."
            return
        }

        viewModelScope.launch {
            repository.markAttendance(
                studentCode = studentCode,
                studentName = studentName,
                subjectCode = subjectCode,
                subjectName = subjectName,
                department = department,
                semester = semester,
                sessionDate = today,
                status = status,
                method = method,
                sessionToken = sessionToken
            ).onSuccess {
                _statusBanner.value = "Attendance ($status via $method) recorded for $studentName."
            }.onFailure {
                _statusBanner.value = "Attendance error: ${it.localizedMessage}"
            }
        }
    }

    fun toggleAttendanceStatus(record: AttendanceRecord) {
        val nextStatus = when (record.status) {
            "PRESENT" -> "LATE"
            "LATE" -> "ABSENT"
            else -> "PRESENT"
        }
        if (isDemoMode || repository == null) {
            demoAttendanceFlow.value = demoAttendanceFlow.value.map {
                if (it.id == record.id) it.copy(status = nextStatus, updatedAt = Timestamp.now()) else it
            }
            _statusBanner.value = "Updated ${record.studentName} attendance to $nextStatus"
            return
        }

        viewModelScope.launch {
            repository.updateAttendanceStatus(record, nextStatus).onSuccess {
                _statusBanner.value = "Updated ${record.studentName} attendance to $nextStatus"
            }
        }
    }

    // --- GRIEVANCE OPERATIONS ---

    fun fileGrievance(
        title: String,
        category: String,
        description: String,
        priority: String,
        submittedBy: String
    ) {
        val ticket = "GRV-2026-${(100..999).random()}"
        if (isDemoMode || repository == null) {
            val now = Timestamp.now()
            val grv = GrievanceRecord(
                id = "demo_grv_${UUID.randomUUID().toString().take(8)}",
                userId = currentUserId,
                ticketNumber = ticket,
                title = title.trim(),
                category = category.trim(),
                description = description.trim(),
                priority = priority.trim(),
                status = "SUBMITTED",
                submittedBy = submittedBy.trim(),
                assignedTo = "Dean of Student Welfare",
                resolutionNotes = "Awaiting initial review by grievance cell.",
                createdAt = now,
                updatedAt = now
            )
            demoGrievancesFlow.value = listOf(grv) + demoGrievancesFlow.value
            _statusBanner.value = "Grievance $ticket submitted to Institutional Redressal Cell."
            return
        }

        viewModelScope.launch {
            repository.submitGrievance(
                ticketNumber = ticket,
                title = title,
                category = category,
                description = description,
                priority = priority,
                submittedBy = submittedBy
            ).onSuccess {
                _statusBanner.value = "Grievance $ticket submitted to Institutional Redressal Cell."
            }.onFailure {
                _statusBanner.value = "Failed to submit grievance: ${it.localizedMessage}"
            }
        }
    }

    fun advanceGrievanceStatus(grievance: GrievanceRecord, resolutionNotes: String) {
        val nextStatus = when (grievance.status) {
            "SUBMITTED" -> "UNDER_REVIEW"
            "UNDER_REVIEW" -> "IN_PROGRESS"
            "ASSIGNED" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "RESOLVED"
            else -> "RESOLVED"
        }
        if (isDemoMode || repository == null) {
            demoGrievancesFlow.value = demoGrievancesFlow.value.map {
                if (it.id == grievance.id) {
                    it.copy(
                        status = nextStatus,
                        resolutionNotes = resolutionNotes,
                        updatedAt = Timestamp.now()
                    )
                } else it
            }
            _statusBanner.value = "Ticket ${grievance.ticketNumber} moved to $nextStatus."
            return
        }

        viewModelScope.launch {
            repository.updateGrievanceStatus(
                grievance = grievance,
                newStatus = nextStatus,
                resolutionNotes = resolutionNotes
            ).onSuccess {
                _statusBanner.value = "Ticket ${grievance.ticketNumber} moved to $nextStatus."
            }
        }
    }

    // --- UNIFIED CAMPUS MODULE OPERATIONS ---

    fun addCampusModuleItem(
        moduleType: CampusModuleType,
        code: String,
        title: String,
        subtitle: String,
        department: String,
        semester: Long,
        status: String,
        category: String,
        primaryMetric: Double,
        secondaryMetric: Long,
        metadataText: String,
        ownerName: String
    ) {
        val existingItems = (campusItemsState.value as? UiState.Success)?.data ?: emptyList()
        if (moduleType == CampusModuleType.TIMETABLE) {
            val conflict = existingItems.any {
                it.moduleType == CampusModuleType.TIMETABLE.name &&
                    it.category.equals(category.trim(), ignoreCase = true) &&
                    it.title.take(10).equals(title.trim().take(10), ignoreCase = true) &&
                    (it.ownerName.equals(ownerName.trim(), ignoreCase = true) ||
                        it.subtitle.equals(subtitle.trim(), ignoreCase = true))
            }
            if (conflict) {
                _statusBanner.value = "Scheduling Conflict Detected: Faculty or Classroom already allocated at that slot!"
                return
            }
        }
        if (moduleType == CampusModuleType.FACILITY_BOOKING) {
            val bookingConflict = existingItems.any {
                it.moduleType == CampusModuleType.FACILITY_BOOKING.name &&
                    it.title.equals(title.trim(), ignoreCase = true) &&
                    it.subtitle.equals(subtitle.trim(), ignoreCase = true) &&
                    it.status != "REJECTED"
            }
            if (bookingConflict) {
                _statusBanner.value = "Booking Conflict Detected: $title is already reserved for $subtitle!"
                return
            }
        }

        if (isDemoMode || repository == null) {
            val now = Timestamp.now()
            val item = CampusModuleItem(
                id = "demo_item_${UUID.randomUUID().toString().take(8)}",
                userId = currentUserId,
                moduleType = moduleType.name,
                code = code.trim(),
                title = title.trim(),
                subtitle = subtitle.trim(),
                department = department.trim(),
                semester = semester,
                status = status.trim(),
                category = category.trim(),
                primaryMetric = primaryMetric,
                secondaryMetric = secondaryMetric,
                metadataText = metadataText.trim(),
                ownerName = ownerName.trim(),
                createdAt = now,
                updatedAt = now
            )
            demoCampusItemsFlow.value = listOf(item) + demoCampusItemsFlow.value
            _statusBanner.value = "${moduleType.title}: '$title' published."
            return
        }

        viewModelScope.launch {
            repository.createCampusItem(
                moduleType = moduleType,
                code = code,
                title = title,
                subtitle = subtitle,
                department = department,
                semester = semester,
                status = status,
                category = category,
                primaryMetric = primaryMetric,
                secondaryMetric = secondaryMetric,
                metadataText = metadataText,
                ownerName = ownerName
            ).onSuccess {
                _statusBanner.value = "${moduleType.title}: '$title' published to Cloud Firestore."
            }.onFailure {
                _statusBanner.value = "Operation failed: ${it.localizedMessage}"
            }
        }
    }

    fun updateCampusItemStatus(item: CampusModuleItem, newStatus: String, updatedMeta: String = item.metadataText) {
        if (isDemoMode || repository == null) {
            demoCampusItemsFlow.value = demoCampusItemsFlow.value.map {
                if (it.id == item.id) {
                    it.copy(
                        status = newStatus,
                        metadataText = updatedMeta,
                        updatedAt = Timestamp.now()
                    )
                } else it
            }
            _statusBanner.value = "Updated '${item.title}' status to $newStatus."
            return
        }

        viewModelScope.launch {
            repository.updateCampusItem(
                item.copy(
                    status = newStatus,
                    metadataText = updatedMeta
                )
            ).onSuccess {
                _statusBanner.value = "Updated '${item.title}' status to $newStatus."
            }
        }
    }

    fun updateCampusItemProgressAndStatus(
        item: CampusModuleItem,
        newStatus: String,
        newProgress: Double,
        updatedMeta: String = item.metadataText
    ) {
        if (isDemoMode || repository == null) {
            demoCampusItemsFlow.value = demoCampusItemsFlow.value.map {
                if (it.id == item.id) {
                    it.copy(
                        status = newStatus,
                        primaryMetric = newProgress.coerceIn(0.0, 100.0),
                        metadataText = updatedMeta,
                        updatedAt = Timestamp.now()
                    )
                } else it
            }
            _statusBanner.value = "Updated '${item.title}' ($newStatus • ${newProgress.toInt()}%)."
            return
        }

        viewModelScope.launch {
            repository.updateCampusItem(
                item.copy(
                    status = newStatus,
                    primaryMetric = newProgress.coerceIn(0.0, 100.0),
                    metadataText = updatedMeta
                )
            ).onSuccess {
                _statusBanner.value = "Updated '${item.title}' ($newStatus • ${newProgress.toInt()}%)."
            }
        }
    }

    fun sendCollaborationRequestOrInvite(
        targetTitle: String,
        department: String,
        roleSkill: String,
        requestType: String,
        message: String,
        requesterName: String
    ) {
        val existingItems = (campusItemsState.value as? UiState.Success)?.data ?: emptyList()
        val duplicate = existingItems.any {
            it.moduleType == CampusModuleType.COLLAB_REQUEST.name &&
                it.title.equals(targetTitle.trim(), ignoreCase = true) &&
                it.ownerName.equals(requesterName.trim(), ignoreCase = true) &&
                (it.status == "PENDING" || it.status == "ACCEPTED")
        }
        if (duplicate) {
            _statusBanner.value = "Duplicate Request Prevented: Active request/invitation already exists for '$targetTitle'."
            return
        }

        addCampusModuleItem(
            moduleType = CampusModuleType.COLLAB_REQUEST,
            code = "CRQ-2026-${(100..999).random()}",
            title = targetTitle.trim(),
            subtitle = "Applicant/Target: $requesterName • Role: ${roleSkill.trim()}",
            department = department.trim(),
            semester = 6L,
            status = "PENDING",
            category = requestType.trim(),
            primaryMetric = 1.0,
            secondaryMetric = 1L,
            metadataText = message.trim(),
            ownerName = requesterName.trim()
        )
    }

    fun upsertStudentCollaborationProfile(
        studentId: String,
        fullName: String,
        department: String,
        semester: Long,
        preferredDomain: String,
        technicalInterests: String,
        projectsCompleted: Double,
        bioAndExperience: String
    ) {
        val existingItems = (campusItemsState.value as? UiState.Success)?.data ?: emptyList()
        val existingProfile = existingItems.firstOrNull {
            it.moduleType == CampusModuleType.COLLAB_PROFILE.name &&
                (it.code.equals(studentId.trim(), ignoreCase = true) ||
                    it.title.equals(fullName.trim(), ignoreCase = true))
        }

        if (existingProfile != null) {
            if (isDemoMode || repository == null) {
                demoCampusItemsFlow.value = demoCampusItemsFlow.value.map {
                    if (it.id == existingProfile.id) {
                        it.copy(
                            code = studentId.trim(),
                            title = fullName.trim(),
                            subtitle = technicalInterests.trim(),
                            department = department.trim(),
                            semester = semester,
                            category = preferredDomain.trim(),
                            primaryMetric = projectsCompleted,
                            metadataText = bioAndExperience.trim(),
                            ownerName = fullName.trim(),
                            updatedAt = Timestamp.now()
                        )
                    } else it
                }
                _statusBanner.value = "Updated Collaboration Profile for $fullName ($studentId)."
                return
            }

            viewModelScope.launch {
                repository.updateCampusItem(
                    existingProfile.copy(
                        code = studentId.trim(),
                        title = fullName.trim(),
                        subtitle = technicalInterests.trim(),
                        department = department.trim(),
                        semester = semester,
                        category = preferredDomain.trim(),
                        primaryMetric = projectsCompleted,
                        metadataText = bioAndExperience.trim(),
                        ownerName = fullName.trim()
                    )
                ).onSuccess {
                    _statusBanner.value = "Updated Collaboration Profile for $fullName ($studentId) in Firestore."
                }
            }
        } else {
            addCampusModuleItem(
                moduleType = CampusModuleType.COLLAB_PROFILE,
                code = studentId.trim(),
                title = fullName.trim(),
                subtitle = technicalInterests.trim(),
                department = department.trim(),
                semester = semester,
                status = "OPEN_TO_COLLAB",
                category = preferredDomain.trim(),
                primaryMetric = projectsCompleted,
                secondaryMetric = 1L,
                metadataText = bioAndExperience.trim(),
                ownerName = fullName.trim()
            )
        }
    }

    fun deleteCampusItem(itemId: String, title: String) {
        if (isDemoMode || repository == null) {
            demoCampusItemsFlow.value = demoCampusItemsFlow.value.filterNot { it.id == itemId }
            _statusBanner.value = "Deleted '$title'."
            return
        }

        viewModelScope.launch {
            repository.deleteCampusItem(itemId).onSuccess {
                _statusBanner.value = "Deleted '$title'."
            }
        }
    }

    // --- GLOBAL SEARCH & COMMAND PALETTE (CTRL+K / OMNIBOX) ---

    fun performGlobalSearch(query: String): List<GlobalSearchResult> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        val results = mutableListOf<GlobalSearchResult>()
        val students = (studentsState.value as? UiState.Success)?.data ?: emptyList()
        val faculty = (facultyState.value as? UiState.Success)?.data ?: emptyList()
        val grievances = (grievancesState.value as? UiState.Success)?.data ?: emptyList()
        val items = (campusItemsState.value as? UiState.Success)?.data ?: emptyList()

        students.filter {
            it.fullName.lowercase().contains(q) ||
                it.studentCode.lowercase().contains(q) ||
                it.department.lowercase().contains(q)
        }.forEach { s ->
            results.add(
                GlobalSearchResult(
                    category = "STUDENT",
                    code = s.studentCode,
                    title = s.fullName,
                    subtitle = "${s.department} • Sem ${s.semester} • CGPA ${s.cgpa}",
                    targetSection = OrbitSection.STUDENTS
                )
            )
        }

        faculty.filter {
            it.fullName.lowercase().contains(q) ||
                it.facultyCode.lowercase().contains(q) ||
                it.department.lowercase().contains(q) ||
                it.assignedSubjects.lowercase().contains(q)
        }.forEach { f ->
            results.add(
                GlobalSearchResult(
                    category = "FACULTY",
                    code = f.facultyCode,
                    title = f.fullName,
                    subtitle = "${f.designation} • ${f.department}",
                    targetSection = OrbitSection.FACULTY
                )
            )
        }

        grievances.filter {
            it.title.lowercase().contains(q) ||
                it.ticketNumber.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
        }.forEach { g ->
            results.add(
                GlobalSearchResult(
                    category = "GRIEVANCE",
                    code = g.ticketNumber,
                    title = g.title,
                    subtitle = "${g.status} • Priority: ${g.priority}",
                    targetSection = OrbitSection.FINANCE_GRIEVANCES
                )
            )
        }

        items.filter {
            it.title.lowercase().contains(q) ||
                it.code.lowercase().contains(q) ||
                it.subtitle.lowercase().contains(q) ||
                it.department.lowercase().contains(q) ||
                it.moduleType.lowercase().contains(q)
        }.forEach { item ->
            val target = when (item.moduleType) {
                CampusModuleType.DEPARTMENT.name,
                CampusModuleType.COURSE.name,
                CampusModuleType.TIMETABLE.name -> OrbitSection.ACADEMICS
                CampusModuleType.EXAM_RESULT.name,
                CampusModuleType.ASSIGNMENT.name -> OrbitSection.EXAMS_ASSIGNMENTS
                CampusModuleType.FEE_INVOICE.name -> OrbitSection.FINANCE_GRIEVANCES
                CampusModuleType.AUDIT_LOG.name -> OrbitSection.SUPER_ADMIN_GOVERNANCE
                CampusModuleType.NOTICE.name -> OrbitSection.OVERVIEW
                CampusModuleType.COLLAB_PROFILE.name,
                CampusModuleType.COLLAB_PROJECT.name,
                CampusModuleType.COLLAB_TEAM.name,
                CampusModuleType.COLLAB_REQUEST.name,
                CampusModuleType.COLLAB_TASK.name,
                CampusModuleType.COLLAB_DISCUSSION.name,
                CampusModuleType.COLLAB_SHOWCASE.name,
                CampusModuleType.COLLAB_MENTORSHIP.name -> OrbitSection.COLLABORATION_HUB
                else -> OrbitSection.CAMPUS_LIFE
            }
            results.add(
                GlobalSearchResult(
                    category = item.moduleType,
                    code = item.code,
                    title = item.title,
                    subtitle = item.subtitle,
                    targetSection = target
                )
            )
        }

        return results.take(25)
    }

    private companion object {
        const val TAG = "CampusOrbitVM"

        fun defaultSectionForRole(role: OrbitRole): OrbitSection = when (role) {
            OrbitRole.SUPER_ADMIN -> OrbitSection.SUPER_ADMIN_GOVERNANCE
            OrbitRole.COLLEGE_ADMIN -> OrbitSection.OVERVIEW
            OrbitRole.FACULTY -> OrbitSection.ATTENDANCE_QR
            OrbitRole.STUDENT -> OrbitSection.EXAMS_ASSIGNMENTS
            OrbitRole.PARENT -> OrbitSection.PARENT_PORTAL
        }
    }
}
