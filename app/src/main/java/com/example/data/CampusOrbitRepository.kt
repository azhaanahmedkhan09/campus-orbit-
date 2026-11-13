package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CampusOrbitRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. USER PROFILE ---

    fun observeUserProfile(userId: String = requireUserId()): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureOrUpdateUserProfile(
        fullName: String,
        email: String,
        activeRole: OrbitRole,
        institutionName: String = "Orbit Institute of Technology",
        department: String = "Computer Science & Engineering",
        linkedStudentCode: String = "STU-2026-001"
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        val safeName = fullName.trim().ifBlank { "Campus Orbit Member" }.take(120)
        val safeEmail = email.trim().ifBlank { "member@campusorbit.edu" }.take(160)
        val safeInstitution = institutionName.trim().ifBlank { "Orbit Institute of Technology" }.take(160)
        val safeDept = department.trim().ifBlank { "Computer Science & Engineering" }.take(120)
        val safeLinked = linkedStudentCode.trim().ifBlank { "STU-2026-001" }.take(64)

        if (!existingSnap.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "fullName" to safeName,
                "email" to safeEmail,
                "activeRole" to activeRole.name,
                "institutionName" to safeInstitution,
                "department" to safeDept,
                "linkedStudentCode" to safeLinked,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        } else {
            val existingModel = existingSnap.toObject(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            requireNotNull(existingModel?.createdAt) { "Profile createdAt must not be null for update" }
            val updatedModel = existingModel.copy(
                fullName = safeName,
                email = safeEmail,
                activeRole = activeRole.name,
                institutionName = safeInstitution,
                department = safeDept,
                linkedStudentCode = safeLinked,
                updatedAt = null
            )
            try {
                docRef.set(updatedModel).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
        val refreshed = docRef.get().await()
        refreshed.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: UserProfile(id = uid, userId = uid, fullName = safeName, email = safeEmail, activeRole = activeRole.name)
    }

    suspend fun switchActiveRole(role: OrbitRole, institutionName: String? = null, linkedStudentCode: String? = null): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val existingSnap = docRef.get().await()
        if (existingSnap.exists()) {
            val existing = existingSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)!!
            requireNotNull(existing.createdAt) { "createdAt required" }
            val updated = existing.copy(
                activeRole = role.name,
                institutionName = institutionName?.trim()?.ifBlank { existing.institutionName } ?: existing.institutionName,
                linkedStudentCode = linkedStudentCode?.trim()?.ifBlank { existing.linkedStudentCode } ?: existing.linkedStudentCode,
                updatedAt = null
            )
            try {
                docRef.set(updated).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            val user = auth.currentUser
            ensureOrUpdateUserProfile(
                fullName = user?.displayName ?: "Campus Administrator",
                email = user?.email ?: "admin@campusorbit.edu",
                activeRole = role,
                institutionName = institutionName ?: "Orbit Institute of Technology",
                linkedStudentCode = linkedStudentCode ?: "STU-2026-001"
            ).getOrThrow()
        }
    }

    // --- 2. STUDENTS ---

    fun observeStudents(userId: String = requireUserId()): Flow<List<StudentRecord>> = flow {
        val path = "students"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(StudentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.studentCode }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getStudentById(studentId: String): Result<StudentRecord> = runCatching {
        val docRef = db.collection("students").document(studentId)
        try {
            val snap = docRef.get().await()
            snap.toObject(StudentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw NoSuchElementException("Student not found")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createStudent(
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
        mentorName: String,
        status: String = "ACTIVE"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("students").document()
        val payload = mapOf(
            "userId" to uid,
            "studentCode" to studentCode.trim().take(64),
            "fullName" to fullName.trim().take(120),
            "email" to email.trim().take(160),
            "department" to department.trim().take(120),
            "semester" to semester.coerceIn(1L, 12L),
            "cgpa" to cgpa.coerceIn(0.0, 10.0),
            "attendancePct" to attendancePct.coerceIn(0L, 100L),
            "feeStatus" to if (feeStatus in listOf("PAID", "PENDING", "OVERDUE")) feeStatus else "PENDING",
            "guardianName" to guardianName.trim().ifBlank { "Guardian" }.take(120),
            "guardianPhone" to guardianPhone.trim().ifBlank { "+1-555-0100" }.take(40),
            "mentorName" to mentorName.trim().ifBlank { "Faculty Mentor" }.take(120),
            "status" to if (status in listOf("ACTIVE", "INACTIVE")) status else "ACTIVE",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStudent(student: StudentRecord): Result<Unit> = runCatching {
        requireUserId()
        require(student.id.isNotBlank()) { "Student ID must not be blank" }
        requireNotNull(student.createdAt) { "Student createdAt must not be null" }
        val docRef = db.collection("students").document(student.id)
        try {
            docRef.set(student.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteStudent(studentId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("students").document(studentId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 3. FACULTY ---

    fun observeFaculty(userId: String = requireUserId()): Flow<List<FacultyRecord>> = flow {
        val path = "faculty"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FacultyRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.facultyCode }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createFaculty(
        facultyCode: String,
        fullName: String,
        email: String,
        department: String,
        designation: String,
        qualification: String,
        assignedSubjects: String,
        workloadHours: Long,
        experienceYears: Long
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("faculty").document()
        val payload = mapOf(
            "userId" to uid,
            "facultyCode" to facultyCode.trim().take(64),
            "fullName" to fullName.trim().take(120),
            "email" to email.trim().take(160),
            "department" to department.trim().take(120),
            "designation" to designation.trim().take(120),
            "qualification" to qualification.trim().take(160),
            "assignedSubjects" to assignedSubjects.trim().take(300),
            "workloadHours" to workloadHours.coerceIn(0L, 60L),
            "experienceYears" to experienceYears.coerceIn(0L, 60L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteFaculty(facultyId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("faculty").document(facultyId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 4. ATTENDANCE & QR SESSIONS ---

    fun observeAttendance(userId: String = requireUserId()): Flow<List<AttendanceRecord>> = flow {
        val path = "attendance"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AttendanceRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun markAttendance(
        studentCode: String,
        studentName: String,
        subjectCode: String,
        subjectName: String,
        department: String,
        semester: Long,
        sessionDate: String,
        status: String,
        method: String,
        sessionToken: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("attendance").document()
        val payload = mapOf(
            "userId" to uid,
            "studentCode" to studentCode.trim().take(64),
            "studentName" to studentName.trim().take(120),
            "subjectCode" to subjectCode.trim().take(64),
            "subjectName" to subjectName.trim().take(160),
            "department" to department.trim().take(120),
            "semester" to semester.coerceIn(1L, 12L),
            "sessionDate" to sessionDate.trim().take(40),
            "status" to if (status in listOf("PRESENT", "ABSENT", "LATE")) status else "PRESENT",
            "method" to if (method in listOf("QR_SCAN", "MANUAL")) method else "MANUAL",
            "sessionToken" to sessionToken.trim().ifBlank { "ORB-SESSION-DEFAULT" }.take(128),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateAttendanceStatus(record: AttendanceRecord, newStatus: String): Result<Unit> = runCatching {
        requireUserId()
        require(record.id.isNotBlank()) { "Attendance ID must not be blank" }
        requireNotNull(record.createdAt) { "Attendance createdAt must not be null" }
        val docRef = db.collection("attendance").document(record.id)
        try {
            docRef.set(record.copy(status = newStatus, updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 5. GRIEVANCES ---

    fun observeGrievances(userId: String = requireUserId()): Flow<List<GrievanceRecord>> = flow {
        val path = "grievances"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GrievanceRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitGrievance(
        ticketNumber: String,
        title: String,
        category: String,
        description: String,
        priority: String,
        submittedBy: String,
        assignedTo: String = "Dean of Student Welfare",
        resolutionNotes: String = "Awaiting initial review by grievance cell."
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("grievances").document()
        val payload = mapOf(
            "userId" to uid,
            "ticketNumber" to ticketNumber.trim().take(64),
            "title" to title.trim().take(160),
            "category" to category.trim().take(80),
            "description" to description.trim().take(1000),
            "priority" to if (priority in listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")) priority else "MEDIUM",
            "status" to "SUBMITTED",
            "submittedBy" to submittedBy.trim().take(120),
            "assignedTo" to assignedTo.trim().take(120),
            "resolutionNotes" to resolutionNotes.trim().take(1000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateGrievanceStatus(
        grievance: GrievanceRecord,
        newStatus: String,
        resolutionNotes: String,
        assignedTo: String = grievance.assignedTo
    ): Result<Unit> = runCatching {
        requireUserId()
        require(grievance.id.isNotBlank()) { "Grievance ID must not be blank" }
        requireNotNull(grievance.createdAt) { "Grievance createdAt must not be null" }
        val docRef = db.collection("grievances").document(grievance.id)
        try {
            docRef.set(
                grievance.copy(
                    status = newStatus,
                    resolutionNotes = resolutionNotes.trim().ifBlank { grievance.resolutionNotes }.take(1000),
                    assignedTo = assignedTo.trim().ifBlank { grievance.assignedTo }.take(120),
                    updatedAt = null
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 6. UNIFIED CAMPUS MODULE ITEMS ---

    fun observeCampusItems(userId: String = requireUserId()): Flow<List<CampusModuleItem>> = flow {
        val path = "campus_items"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CampusModuleItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createCampusItem(
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
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("campus_items").document()
        val payload = mapOf(
            "userId" to uid,
            "moduleType" to moduleType.name,
            "code" to code.trim().ifBlank { "ORB-001" }.take(64),
            "title" to title.trim().ifBlank { "Untitled Record" }.take(180),
            "subtitle" to subtitle.trim().ifBlank { "Campus Orbit Record" }.take(240),
            "department" to department.trim().ifBlank { "Computer Science & Engineering" }.take(120),
            "semester" to semester.coerceIn(0L, 12L),
            "status" to status.trim().ifBlank { "ACTIVE" }.take(64),
            "category" to category.trim().ifBlank { "GENERAL" }.take(80),
            "primaryMetric" to primaryMetric.coerceIn(0.0, 10000000.0),
            "secondaryMetric" to secondaryMetric.coerceIn(0L, 10000000L),
            "metadataText" to metadataText.trim().ifBlank { "Official institutional record." }.take(1500),
            "ownerName" to ownerName.trim().ifBlank { "Campus Administration" }.take(120),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateCampusItem(item: CampusModuleItem): Result<Unit> = runCatching {
        requireUserId()
        require(item.id.isNotBlank()) { "Campus item ID must not be blank" }
        requireNotNull(item.createdAt) { "Campus item createdAt must not be null" }
        val docRef = db.collection("campus_items").document(item.id)
        try {
            docRef.set(item.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCampusItem(itemId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("campus_items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 7. INITIAL INSTITUTIONAL DATA SEEDING ---

    suspend fun seedInstitutionalDataIfEmpty(): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val existingStudents = try {
            db.collection("students")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "students")
            throw e
        }

        if (!existingStudents.isEmpty) {
            return@runCatching false
        }

        // Seed Students (Module 2 & Module 15 Parent Portal)
        createStudent(
            studentCode = "STU-2026-001",
            fullName = "Aarav Mehta",
            email = "aarav.mehta@campusorbit.edu",
            department = "Computer Science & Engineering",
            semester = 6L,
            cgpa = 9.24,
            attendancePct = 92L,
            feeStatus = "PAID",
            guardianName = "Rajesh Mehta",
            guardianPhone = "+1-555-234-8901",
            mentorName = "Dr. Elena Rostova",
            status = "ACTIVE"
        ).getOrThrow()

        createStudent(
            studentCode = "STU-2026-002",
            fullName = "Maya Lin",
            email = "maya.lin@campusorbit.edu",
            department = "Electronics & Communication",
            semester = 6L,
            cgpa = 8.95,
            attendancePct = 86L,
            feeStatus = "PAID",
            guardianName = "Chen Lin",
            guardianPhone = "+1-555-391-4412",
            mentorName = "Prof. Vikram Sethi",
            status = "ACTIVE"
        ).getOrThrow()

        createStudent(
            studentCode = "STU-2026-003",
            fullName = "Liam O'Connor",
            email = "liam.oconnor@campusorbit.edu",
            department = "Computer Science & Engineering",
            semester = 4L,
            cgpa = 7.82,
            attendancePct = 71L,
            feeStatus = "PENDING",
            guardianName = "Sean O'Connor",
            guardianPhone = "+1-555-892-1134",
            mentorName = "Dr. Elena Rostova",
            status = "ACTIVE"
        ).getOrThrow()

        createStudent(
            studentCode = "STU-2026-004",
            fullName = "Zara Al-Mansoor",
            email = "zara.almansoor@campusorbit.edu",
            department = "Artificial Intelligence & Data Science",
            semester = 8L,
            cgpa = 9.62,
            attendancePct = 96L,
            feeStatus = "PAID",
            guardianName = "Tariq Al-Mansoor",
            guardianPhone = "+1-555-774-2090",
            mentorName = "Dr. Siddharth Rao",
            status = "ACTIVE"
        ).getOrThrow()

        createStudent(
            studentCode = "STU-2026-005",
            fullName = "Rohan Deshmukh",
            email = "rohan.deshmukh@campusorbit.edu",
            department = "Mechanical & Robotics Engineering",
            semester = 4L,
            cgpa = 7.45,
            attendancePct = 68L,
            feeStatus = "OVERDUE",
            guardianName = "Anil Deshmukh",
            guardianPhone = "+1-555-602-8819",
            mentorName = "Prof. Claire Vance",
            status = "ACTIVE"
        ).getOrThrow()

        // Seed Faculty (Module 3)
        createFaculty(
            facultyCode = "FAC-CSE-101",
            fullName = "Dr. Elena Rostova",
            email = "elena.rostova@campusorbit.edu",
            department = "Computer Science & Engineering",
            designation = "Professor & HOD",
            qualification = "Ph.D. in Distributed Systems (MIT)",
            assignedSubjects = "CS601 Distributed Cloud Systems, CS402 Operating Systems",
            workloadHours = 18L,
            experienceYears = 14L
        ).getOrThrow()

        createFaculty(
            facultyCode = "FAC-AI-104",
            fullName = "Dr. Siddharth Rao",
            email = "siddharth.rao@campusorbit.edu",
            department = "Artificial Intelligence & Data Science",
            designation = "Associate Professor",
            qualification = "Ph.D. in Machine Learning (Stanford)",
            assignedSubjects = "AI801 Deep Learning Architectures, AI603 Natural Language Processing",
            workloadHours = 16L,
            experienceYears = 9L
        ).getOrThrow()

        createFaculty(
            facultyCode = "FAC-ECE-108",
            fullName = "Prof. Vikram Sethi",
            email = "vikram.sethi@campusorbit.edu",
            department = "Electronics & Communication",
            designation = "Professor & Dean Academics",
            qualification = "Ph.D. in VLSI & Embedded Systems",
            assignedSubjects = "EC601 Embedded IoT Systems, EC404 Digital Signal Processing",
            workloadHours = 14L,
            experienceYears = 17L
        ).getOrThrow()

        // Seed Attendance Logs (Module 5)
        markAttendance(
            studentCode = "STU-2026-001",
            studentName = "Aarav Mehta",
            subjectCode = "CS601",
            subjectName = "Distributed Cloud Systems",
            department = "Computer Science & Engineering",
            semester = 6L,
            sessionDate = "2026-11-13",
            status = "PRESENT",
            method = "QR_SCAN",
            sessionToken = "ORB-CS601-20261113-A9F"
        ).getOrThrow()

        markAttendance(
            studentCode = "STU-2026-003",
            studentName = "Liam O'Connor",
            subjectCode = "CS402",
            subjectName = "Operating Systems Kernel Lab",
            department = "Computer Science & Engineering",
            semester = 4L,
            sessionDate = "2026-11-13",
            status = "ABSENT",
            method = "MANUAL",
            sessionToken = "ORB-CS402-20261113-B2C"
        ).getOrThrow()

        markAttendance(
            studentCode = "STU-2026-004",
            studentName = "Zara Al-Mansoor",
            subjectCode = "AI801",
            subjectName = "Deep Learning Architectures",
            department = "Artificial Intelligence & Data Science",
            semester = 8L,
            sessionDate = "2026-11-13",
            status = "PRESENT",
            method = "QR_SCAN",
            sessionToken = "ORB-AI801-20261113-X7Q"
        ).getOrThrow()

        // Seed Grievances (Module 9)
        submitGrievance(
            ticketNumber = "GRV-2026-841",
            title = "High-Performance GPU Cluster Access Delay in AI Lab 3",
            category = "Infrastructure & Labs",
            description = "Final year capstone students require extended evening credentials for the NVIDIA compute nodes in Block B.",
            priority = "HIGH",
            submittedBy = "Zara Al-Mansoor (STU-2026-004)",
            assignedTo = "Dr. Siddharth Rao • IT Infrastructure Cell",
            resolutionNotes = "Cluster quota expanded and 24x7 badge access provisioned for Semester 8 scholars."
        ).getOrThrow()

        submitGrievance(
            ticketNumber = "GRV-2026-845",
            title = "Request for Medical Attendance Condonation Review",
            category = "Academic & Attendance",
            description = "Submitted hospitalization discharge summary for October 18-24 lectures in CS402.",
            priority = "MEDIUM",
            submittedBy = "Liam O'Connor (STU-2026-003)",
            assignedTo = "Dr. Elena Rostova • HOD CSE",
            resolutionNotes = "Medical certificates verified; awaiting Academic Council sign-off."
        ).getOrThrow()

        // Seed Unified Campus Module Items (Modules 4, 6, 7, 8, 10, 11, 12, 13, 14, 16, 17, 18)
        // Module 4: Departments & Courses
        createCampusItem(
            moduleType = CampusModuleType.DEPARTMENT,
            code = "DEPT-CSE",
            title = "Computer Science & Engineering",
            subtitle = "HOD: Dr. Elena Rostova • Block A (Levels 1-4)",
            department = "Computer Science & Engineering",
            semester = 0L,
            status = "NBA ACCREDITED",
            category = "B.Tech / M.Tech / Ph.D.",
            primaryMetric = 480.0,
            secondaryMetric = 32L,
            metadataText = "12 Research Labs, Cloud & Cybersecurity Center of Excellence, 96.4% Placement Rate.",
            ownerName = "Dr. Elena Rostova"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.DEPARTMENT,
            code = "DEPT-AIDS",
            title = "Artificial Intelligence & Data Science",
            subtitle = "HOD: Dr. Siddharth Rao • Innovation Tower",
            department = "Artificial Intelligence & Data Science",
            semester = 0L,
            status = "NBA ACCREDITED",
            category = "B.Tech / M.S. Research",
            primaryMetric = 360.0,
            secondaryMetric = 24L,
            metadataText = "Autonomous Robotics & Generative Compute Cluster, Industry Sponsored Capstone Studio.",
            ownerName = "Dr. Siddharth Rao"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COURSE,
            code = "CS601",
            title = "Distributed Cloud Systems & Microservices",
            subtitle = "4 Credits • Core Theory + Lab • Sem 6",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "ACTIVE",
            category = "CORE SUBJECT",
            primaryMetric = 4.0,
            secondaryMetric = 64L,
            metadataText = "Consensus protocols, Raft/Paxos, Kubernetes orchestration, and zero-trust service meshes.",
            ownerName = "Dr. Elena Rostova"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COURSE,
            code = "AI801",
            title = "Deep Learning & Multimodal Architectures",
            subtitle = "4 Credits • Honours Elective • Sem 8",
            department = "Artificial Intelligence & Data Science",
            semester = 8L,
            status = "ACTIVE",
            category = "ELECTIVE",
            primaryMetric = 4.0,
            secondaryMetric = 58L,
            metadataText = "Transformers, diffusion systems, hardware-aware quantization, and distributed training.",
            ownerName = "Dr. Siddharth Rao"
        ).getOrThrow()

        // Module 6: Examinations & Academic Results
        createCampusItem(
            moduleType = CampusModuleType.EXAM_RESULT,
            code = "EXM-2026-CS601",
            title = "Aarav Mehta (STU-2026-001) • CS601 Distributed Cloud Systems",
            subtitle = "Grade: O (Outstanding) • SGPA: 9.40 • CGPA: 9.24",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "PUBLISHED",
            category = "END-SEMESTER RESULT",
            primaryMetric = 94.0,
            secondaryMetric = 100L,
            metadataText = "Internal Assessment: 38/40 | End-Sem Exam: 56/60 | Hall Ticket Verified.",
            ownerName = "Dr. Elena Rostova"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.EXAM_RESULT,
            code = "EXM-2026-AI801",
            title = "Zara Al-Mansoor (STU-2026-004) • AI801 Deep Learning Architectures",
            subtitle = "Grade: O (Outstanding) • SGPA: 9.75 • CGPA: 9.62",
            department = "Artificial Intelligence & Data Science",
            semester = 8L,
            status = "PUBLISHED",
            category = "END-SEMESTER RESULT",
            primaryMetric = 97.0,
            secondaryMetric = 100L,
            metadataText = "Internal Assessment: 40/40 | End-Sem Exam: 57/60 | University Gold Medal Nominee.",
            ownerName = "Dr. Siddharth Rao"
        ).getOrThrow()

        // Module 7: Smart Timetable Management
        createCampusItem(
            moduleType = CampusModuleType.TIMETABLE,
            code = "TT-MON-0900",
            title = "09:00 AM - 10:30 AM • CS601 Distributed Cloud Systems",
            subtitle = "Monday • Smart Lecture Hall A-302 • Sem 6 CSE",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "CONFLICT-FREE",
            category = "MONDAY",
            primaryMetric = 9.0,
            secondaryMetric = 90L,
            metadataText = "Faculty: Dr. Elena Rostova | Classroom Capacity: 120 | Interactive Smartboard Enabled.",
            ownerName = "Dr. Elena Rostova"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.TIMETABLE,
            code = "TT-TUE-1100",
            title = "11:00 AM - 12:30 PM • AI801 Deep Learning Studio",
            subtitle = "Tuesday • NVIDIA AI Supercomputing Lab B-204 • Sem 8 AIDS",
            department = "Artificial Intelligence & Data Science",
            semester = 8L,
            status = "CONFLICT-FREE",
            category = "TUESDAY",
            primaryMetric = 11.0,
            secondaryMetric = 90L,
            metadataText = "Faculty: Dr. Siddharth Rao | Lab Workstations: 64 | Zero scheduling conflicts detected.",
            ownerName = "Dr. Siddharth Rao"
        ).getOrThrow()

        // Module 8: Assignments & Learning Resources
        createCampusItem(
            moduleType = CampusModuleType.ASSIGNMENT,
            code = "ASN-CS601-03",
            title = "Raft Consensus & Fault-Tolerant Key-Value Store Implementation",
            subtitle = "Due: Nov 25, 2026 • Max Marks: 50 • PDF + Git Submission",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "OPEN FOR SUBMISSION",
            category = "LAB ASSIGNMENT",
            primaryMetric = 50.0,
            secondaryMetric = 48L,
            metadataText = "Includes lecture deck PDF, gRPC starter repository, and automated testbench rubric.",
            ownerName = "Dr. Elena Rostova"
        ).getOrThrow()

        // Module 10: Fee & Finance Management
        createCampusItem(
            moduleType = CampusModuleType.FEE_INVOICE,
            code = "INV-2026-9901",
            title = "Semester 6 Tuition & Smart Lab Development Fee • Aarav Mehta",
            subtitle = "Student: STU-2026-001 • Receipt #RCP-88412",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "PAID",
            category = "TUITION & LAB FEE",
            primaryMetric = 4250.0,
            secondaryMetric = 0L,
            metadataText = "Tuition: $3,800 | Cloud Lab & Library: $450 | Verified Digital Receipt Issued.",
            ownerName = "Office of Finance & Accounts"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.FEE_INVOICE,
            code = "INV-2026-9903",
            title = "Semester 4 Tuition & Examination Fee • Liam O'Connor",
            subtitle = "Student: STU-2026-003 • Due Date: Nov 28, 2026",
            department = "Computer Science & Engineering",
            semester = 4L,
            status = "PENDING",
            category = "TUITION & EXAM FEE",
            primaryMetric = 4100.0,
            secondaryMetric = 1200L,
            metadataText = "Partial installment paid ($2,900). Balance due: $1,200 before hall ticket release.",
            ownerName = "Office of Finance & Accounts"
        ).getOrThrow()

        // Module 11: Notices & Announcements
        createCampusItem(
            moduleType = CampusModuleType.NOTICE,
            code = "NOT-2026-112",
            title = "Autumn 2026 End-Semester Examination Schedule & Digital Hall Tickets",
            subtitle = "Priority Broadcast • All Departments & Guardians",
            department = "All Departments",
            semester = 0L,
            status = "PUBLISHED",
            category = "HIGH PRIORITY",
            primaryMetric = 1.0,
            secondaryMetric = 1450L,
            metadataText = "Theory examinations commence Dec 4, 2026. Students with >=75% attendance and cleared dues can download QR-verified Hall Tickets.",
            ownerName = "Controller of Examinations"
        ).getOrThrow()

        // Module 12: Campus Facilities & Resource Booking
        createCampusItem(
            moduleType = CampusModuleType.FACILITY_BOOKING,
            code = "BKG-AUD-01",
            title = "Main Orbital Convention Auditorium (800 Seats)",
            subtitle = "Reserved: Nov 20, 2026 (02:00 PM - 06:00 PM)",
            department = "Computer Science & Engineering",
            semester = 0L,
            status = "APPROVED",
            category = "AUDITORIUM",
            primaryMetric = 800.0,
            secondaryMetric = 4L,
            metadataText = "Booked for IEEE Smart Campus Symposium & Keynote. 4K Laser Projection + Dolby Atmos Audio ready.",
            ownerName = "Dr. Elena Rostova"
        ).getOrThrow()

        // Module 13: Certificate & Document Management
        createCampusItem(
            moduleType = CampusModuleType.CERTIFICATE,
            code = "CERT-BON-2026-41",
            title = "Bonafide & Character Certificate • Aarav Mehta (STU-2026-001)",
            subtitle = "Purpose: International Research Internship Visa Application",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "APPROVED",
            category = "BONAFIDE CERTIFICATE",
            primaryMetric = 1.0,
            secondaryMetric = 6L,
            metadataText = "Digitally signed by Registrar with QR verification hash #SHA256-9F82C1.",
            ownerName = "Office of the Registrar"
        ).getOrThrow()

        // Module 14: Campus Events & Activities
        createCampusItem(
            moduleType = CampusModuleType.EVENT,
            code = "EVT-ORBIT-HACK",
            title = "OrbitHack 2026 — 36-Hour National Smart Campus Hackathon",
            subtitle = "Nov 28-29, 2026 • Innovation Arena • Prize Pool: $15,000",
            department = "All Departments",
            semester = 0L,
            status = "REGISTRATION OPEN",
            category = "HACKATHON",
            primaryMetric = 15000.0,
            secondaryMetric = 342L,
            metadataText = "Tracks: FinTech, Sustainable Smart Cities, Edge AI, and EdTech. 342 participants registered.",
            ownerName = "Campus Innovation Council"
        ).getOrThrow()

        // Module 16: Student Collaboration & Study Groups
        createCampusItem(
            moduleType = CampusModuleType.STUDY_GROUP,
            code = "GRP-SYS-601",
            title = "Distributed Systems & Cloud Native Architects Circle",
            subtitle = "Subject: CS601 • Sem 6 • Active Peer Study Group",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "ACTIVE",
            category = "PEER LEARNING",
            primaryMetric = 9.2,
            secondaryMetric = 28L,
            metadataText = "Weekly paper reading on Raft, Spanner, and Kafka internals + mock system design rounds.",
            ownerName = "Aarav Mehta (STU-2026-001)"
        ).getOrThrow()

        // Module 17: Student Skill & Placement Development
        createCampusItem(
            moduleType = CampusModuleType.PLACEMENT,
            code = "PLC-2026-CLOUD",
            title = "Cloud Kernel & Distributed Systems Engineer — Apex Cloud Corp",
            subtitle = "CTC: 28.5 LPA • Eligibility: CGPA >= 8.0 (CSE / AIDS Sem 7-8)",
            department = "Computer Science & Engineering",
            semester = 8L,
            status = "ACCEPTING APPLICATIONS",
            category = "FULL-TIME + INTERNSHIP",
            primaryMetric = 28.5,
            secondaryMetric = 64L,
            metadataText = "Skills Required: Kotlin, Go, Kubernetes, Distributed Storage. Online Assessment on Nov 22, 2026.",
            ownerName = "Training & Placement Cell"
        ).getOrThrow()

        // Module 18 & Super Admin: System Audit Logs
        createCampusItem(
            moduleType = CampusModuleType.AUDIT_LOG,
            code = "AUD-SEC-001",
            title = "RBAC Security Policy & Firestore Zero-Trust Rules Verified",
            subtitle = "Action: SYSTEM_INITIALIZATION • Actor: Super Admin",
            department = "Institutional Governance",
            semester = 0L,
            status = "VERIFIED",
            category = "SECURITY AUDIT",
            primaryMetric = 100.0,
            secondaryMetric = 19L,
            metadataText = "All academic, administrative, and Campus Collaboration Hub modules synchronized with real-time Firestore listeners.",
            ownerName = "System Security Engine"
        ).getOrThrow()

        // --- CAMPUS COLLABORATION HUB INITIAL RECORDS ---
        createCampusItem(
            moduleType = CampusModuleType.COLLAB_PROFILE,
            code = "STU-2026-001",
            title = "Aarav Mehta • Full-Stack & Distributed Cloud Architect",
            subtitle = "Kotlin, Jetpack Compose, Go, Kubernetes, PyTorch • Open for Hackathons & Capstones",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "AVAILABLE",
            category = "Cloud Computing",
            primaryMetric = 9.24,
            secondaryMetric = 6L,
            metadataText = "Interests: Distributed Systems, FinTech, Edge AI | Experience: Winner OrbitHack 2025 | Prefers: Interdisciplinary 4-member teams.",
            ownerName = "Aarav Mehta"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_PROFILE,
            code = "STU-2026-002",
            title = "Maya Lin • Embedded IoT & Autonomous Hardware Lead",
            subtitle = "RTOS, C++, ESP32, LoRaWAN, PCB Design, ROS2 • Seeking AI/Mobile Teammates",
            department = "Electronics & Communication",
            semester = 6L,
            status = "AVAILABLE",
            category = "Internet of Things",
            primaryMetric = 8.95,
            secondaryMetric = 5L,
            metadataText = "Interests: Smart Agriculture, Wearable Telemetry, Swarm Robotics | Experience: IEEE Hardware Fellow.",
            ownerName = "Maya Lin"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_PROJECT,
            code = "PRJ-ORB-101",
            title = "AeroSense — Autonomous Drone Swarm for Campus Micro-Climate & Air Quality Mapping",
            subtitle = "Tech: ROS2, ESP32, Python, PyTorch, Jetpack Compose • Team: 3/5 Members",
            department = "Electronics & Communication",
            semester = 6L,
            status = "IN_PROGRESS",
            category = "Internet of Things",
            primaryMetric = 3.0,
            secondaryMetric = 5L,
            metadataText = "Building a low-power LoRaWAN sensor mesh + autonomous quadcopter telemetry dashboard. Looking for 1 Mobile App developer and 1 ML engineer.",
            ownerName = "Maya Lin (STU-2026-002)"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_PROJECT,
            code = "PRJ-ORB-103",
            title = "ZeroTrust Grid — Quantum-Resistant Campus Microgrid & Smart EV Charging Controller",
            subtitle = "Tech: Go, Rust, Kubernetes, Modbus TCP • Team: 2/4 Members",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "PLANNING",
            category = "Cybersecurity",
            primaryMetric = 2.0,
            secondaryMetric = 4L,
            metadataText = "Securing campus solar microgrid controllers with post-quantum mTLS authentication and predictive load balancing.",
            ownerName = "Aarav Mehta (STU-2026-001)"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_TEAM,
            code = "TEAM-NEBULA-01",
            title = "Team Nebula Dynamics • Project: AeroSense Drone Swarm (PRJ-ORB-101)",
            subtitle = "Leader: Maya Lin (ECE) • Members: Aarav Mehta (CSE), Rohan Deshmukh (Mech) • 3/5 Filled",
            department = "Interdisciplinary (ECE + CSE + Mech)",
            semester = 6L,
            status = "IN_PROGRESS",
            category = "Internet of Things",
            primaryMetric = 68.0,
            secondaryMetric = 5L,
            metadataText = "Objectives: Complete LoRa telemetry PCB v2 & Android ground-control station | Resources: github.com/campusorbit/aerosense-swarm",
            ownerName = "Maya Lin (STU-2026-002)"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_REQUEST,
            code = "REQ-2026-301",
            title = "Join Request: Liam O'Connor (STU-2026-003) -> AeroSense Drone Swarm (PRJ-ORB-101)",
            subtitle = "Role Requested: Embedded Kernel & Telemetry Engineer • Dept: CSE Sem 4",
            department = "Computer Science & Engineering",
            semester = 4L,
            status = "PENDING",
            category = "JOIN_REQUEST",
            primaryMetric = 1.0,
            secondaryMetric = 4L,
            metadataText = "Experienced with FreeRTOS and MAVLink telemetry parsing; can dedicate 12 hrs/week.",
            ownerName = "Liam O'Connor (STU-2026-003)"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_TASK,
            code = "TSK-102",
            title = "Milestone 2: Build Jetpack Compose Live 3D Waypoint & Air-Quality Heatmap UI",
            subtitle = "Team Nebula Dynamics • Assignee: Aarav Mehta • Due: Nov 28, 2026",
            department = "Computer Science & Engineering",
            semester = 6L,
            status = "IN_PROGRESS",
            category = "CRITICAL",
            primaryMetric = 65.0,
            secondaryMetric = 2L,
            metadataText = "WebSocket telemetry ingestion complete; polishing real-time AQI gradient overlay.",
            ownerName = "Aarav Mehta"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_DISCUSSION,
            code = "DSC-NEBULA-01",
            title = "[Team Nebula Dynamics] Flight Controller Firmware v2.1 Bench Test Results",
            subtitle = "Posted by Maya Lin (Team Leader) • Active Thread",
            department = "Electronics & Communication",
            semester = 6L,
            status = "PINNED",
            category = "TEAM UPDATE",
            primaryMetric = 3.0,
            secondaryMetric = 6L,
            metadataText = "PID loop tuning on the quadcopter test rig is stable! Ready to stream live JSON telemetry packets to the Android app at 20Hz.",
            ownerName = "Maya Lin (STU-2026-002)"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_SHOWCASE,
            code = "SHW-2026-01",
            title = "BrailleWave — Haptic & Vision-Language Smart Reader for Visually Impaired Scholars",
            subtitle = "Category: Social Innovation & AI • Winner: National Smart India Innovation Cup",
            department = "Artificial Intelligence & Data Science",
            semester = 8L,
            status = "SHOWCASED",
            category = "Social Innovation",
            primaryMetric = 98.5,
            secondaryMetric = 4L,
            metadataText = "Problem: Textbook diagrams are inaccessible | Solution: Wearable camera + piezo braille matrix | Tech: Kotlin, PyTorch, STM32 | GitHub: github.com/campusorbit/braillewave",
            ownerName = "Zara Al-Mansoor & Team"
        ).getOrThrow()

        createCampusItem(
            moduleType = CampusModuleType.COLLAB_MENTORSHIP,
            code = "MNT-2026-01",
            title = "Mentorship: AeroSense Drone Swarm (Team Nebula) <-> Prof. Vikram Sethi",
            subtitle = "Mentor: Prof. Vikram Sethi (ECE & Dean Academics) • Status: ACCEPTED",
            department = "Electronics & Communication",
            semester = 6L,
            status = "ACCEPTED",
            category = "Internet of Things",
            primaryMetric = 92.0,
            secondaryMetric = 6L,
            metadataText = "Faculty Feedback: Excellent PCB layout and link-budget analysis. Recommend adding redundant barometer failsafe before outdoor flight demo.",
            ownerName = "Prof. Vikram Sethi"
        ).getOrThrow()

        true
    }
}
