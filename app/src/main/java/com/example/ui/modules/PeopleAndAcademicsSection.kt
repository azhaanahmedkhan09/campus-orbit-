package com.example.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.FacultyRecord
import com.example.data.StudentRecord
import com.example.ui.theme.OrbitAmber
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitRose
import com.example.ui.theme.OrbitRoyalBlueBright
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentManagementSection(
    students: List<StudentRecord>,
    onAddStudent: (
        code: String,
        name: String,
        email: String,
        dept: String,
        sem: Long,
        cgpa: Double,
        att: Long,
        feeStatus: String,
        guardianName: String,
        guardianPhone: String,
        mentorName: String
    ) -> Unit,
    onImportCsv: (String) -> Unit,
    onUpdateFeeOrAttendance: (StudentRecord, String, Long) -> Unit,
    onDeleteStudent: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var showCsvExportDialog by remember { mutableStateOf(false) }
    var selectedStudentProfile by remember { mutableStateOf<StudentRecord?>(null) }

    val departments = remember(students) {
        listOf("ALL") + students.map { it.department }.distinct()
    }

    val filteredStudents = students.filter { s ->
        val matchesDept = selectedDeptFilter == "ALL" || s.department == selectedDeptFilter
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
            s.fullName.lowercase().contains(q) ||
            s.studentCode.lowercase().contains(q) ||
            s.email.lowercase().contains(q)
        matchesDept && matchesQuery
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student Information System (${filteredStudents.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage enrollment, academic CGPA, attendance, guardians, and CSV bulk sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_student_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enroll")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Student Name, Roll ID, or Email") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departments.forEach { dept ->
                        FilterChip(
                            selected = selectedDeptFilter == dept,
                            onClick = { selectedDeptFilter = dept },
                            label = { Text(if (dept == "ALL") "All Departments" else dept.take(22)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showCsvImportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("csv_import_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bulk CSV Import")
                    }
                    OutlinedButton(
                        onClick = { showCsvExportDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV")
                    }
                }
            }
        }

        filteredStudents.forEach { student ->
            val feeColor = when (student.feeStatus) {
                "PAID" -> OrbitEmerald
                "OVERDUE" -> OrbitRose
                else -> OrbitAmber
            }
            val attColor = if (student.attendancePct < 75L) OrbitRose else OrbitEmerald

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedStudentProfile = student },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(OrbitRoyalBlueBright.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.fullName.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OrbitRoyalBlueBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${student.studentCode} • ${student.department} (Sem ${student.semester})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteStudent(student.id, student.fullName) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Student",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgeChip(text = "CGPA: ${String.format(Locale.US, "%.2f", student.cgpa)}", color = OrbitCyanAccent)
                        BadgeChip(text = "Attendance: ${student.attendancePct}%", color = attColor)
                        BadgeChip(text = "Fee: ${student.feeStatus}", color = feeColor)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Guardian: ${student.guardianName} • Mentor: ${student.mentorName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                val nextFee = if (student.feeStatus == "PAID") "PENDING" else "PAID"
                                onUpdateFeeOrAttendance(student, nextFee, student.attendancePct)
                            }
                        ) {
                            Text(if (student.feeStatus == "PAID") "Mark Fee Pending" else "Mark Fee Paid")
                        }
                    }
                }
            }
        }
    }

    if (selectedStudentProfile != null) {
        val st = selectedStudentProfile!!
        AlertDialog(
            onDismissRequest = { selectedStudentProfile = null },
            title = { Text("${st.fullName} (${st.studentCode})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Department: ${st.department}")
                    Text("Semester: ${st.semester} • Status: ${st.status}")
                    Text("Email: ${st.email}")
                    Text("Cumulative GPA: ${st.cgpa} / 10.0")
                    Text("Attendance Percentage: ${st.attendancePct}%")
                    Text("Tuition Fee Status: ${st.feeStatus}")
                    Text("Verified Guardian: ${st.guardianName} (${st.guardianPhone})")
                    Text("Assigned Faculty Mentor: ${st.mentorName}")
                }
            },
            confirmButton = {
                Button(onClick = { selectedStudentProfile = null }) {
                    Text("Close Profile")
                }
            }
        )
    }

    if (showAddDialog) {
        var code by remember { mutableStateOf("STU-2026-${(100..999).random()}") }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Computer Science & Engineering") }
        var sem by remember { mutableStateOf("6") }
        var cgpa by remember { mutableStateOf("8.8") }
        var att by remember { mutableStateOf("90") }
        var guardian by remember { mutableStateOf("") }
        var guardianPhone by remember { mutableStateOf("+1-555-0144") }
        var mentor by remember { mutableStateOf("Dr. Elena Rostova") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Enroll New Student") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Student Roll ID") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Institutional Email") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sem, onValueChange = { sem = it }, label = { Text("Sem") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cgpa, onValueChange = { cgpa = it }, label = { Text("CGPA") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = att, onValueChange = { att = it }, label = { Text("Att %") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = guardian, onValueChange = { guardian = it }, label = { Text("Parent / Guardian Name") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddStudent(
                                code,
                                name,
                                email.ifBlank { "${code.lowercase()}@campusorbit.edu" },
                                dept,
                                sem.toLongOrNull() ?: 6L,
                                cgpa.toDoubleOrNull() ?: 8.5,
                                att.toLongOrNull() ?: 90L,
                                "PAID",
                                guardian.ifBlank { "Verified Guardian" },
                                guardianPhone,
                                mentor
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save to Firestore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCsvImportDialog) {
        var csvInput by remember {
            mutableStateOf(
                "STU-2026-108,Neha Verma,neha.verma@campusorbit.edu,Computer Science & Engineering,6,9.15,94\n" +
                    "STU-2026-109,David Kim,david.kim@campusorbit.edu,Artificial Intelligence & Data Science,4,8.74,89"
            )
        }
        AlertDialog(
            onDismissRequest = { showCsvImportDialog = false },
            title = { Text("Bulk CSV Student Import") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Format per row: RollCode,FullName,Email,Department,Semester,CGPA,Attendance%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    OutlinedTextField(
                        value = csvInput,
                        onValueChange = { csvInput = it },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportCsv(csvInput)
                        showCsvImportDialog = false
                    }
                ) {
                    Text("Import Rows")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCsvExportDialog) {
        val csvExportText = buildString {
            appendLine("studentCode,fullName,email,department,semester,cgpa,attendancePct,feeStatus")
            filteredStudents.forEach { s ->
                appendLine("${s.studentCode},${s.fullName},${s.email},${s.department},${s.semester},${s.cgpa},${s.attendancePct},${s.feeStatus}")
            }
        }
        AlertDialog(
            onDismissRequest = { showCsvExportDialog = false },
            title = { Text("Exported Student CSV Dataset") },
            text = {
                OutlinedTextField(
                    value = csvExportText,
                    onValueChange = {},
                    readOnly = true,
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { showCsvExportDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
fun FacultyManagementSection(
    facultyList: List<FacultyRecord>,
    onAddFaculty: (
        code: String,
        name: String,
        email: String,
        dept: String,
        designation: String,
        qualification: String,
        subjects: String,
        workload: Long,
        experience: Long
    ) -> Unit,
    onDeleteFaculty: (String, String) -> Unit
) {
    var showAddFacultyModal by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Faculty & Professor Directory (${facultyList.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Subject allocations, teaching workload hours, and research qualifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddFacultyModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_faculty_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Faculty")
                }
            }
        }

        facultyList.forEach { prof ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${prof.fullName} (${prof.facultyCode})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${prof.designation} • ${prof.department}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDeleteFaculty(prof.id, prof.fullName) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Faculty",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Qualification: ${prof.qualification}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Assigned Subjects: ${prof.assignedSubjects}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgeChip(text = "${prof.workloadHours} hrs/week", color = OrbitCyanAccent)
                        BadgeChip(text = "${prof.experienceYears} yrs exp", color = OrbitEmerald)
                        BadgeChip(text = prof.email, color = OrbitRoyalBlueBright)
                    }
                }
            }
        }
    }

    if (showAddFacultyModal) {
        var code by remember { mutableStateOf("FAC-CSE-${(200..999).random()}") }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Computer Science & Engineering") }
        var designation by remember { mutableStateOf("Associate Professor") }
        var qualification by remember { mutableStateOf("Ph.D. in Computer Science") }
        var subjects by remember { mutableStateOf("CS502 Compiler Design, CS604 Cloud Security") }
        var workload by remember { mutableStateOf("16") }
        var exp by remember { mutableStateOf("8") }

        AlertDialog(
            onDismissRequest = { showAddFacultyModal = false },
            title = { Text("Add Faculty Member") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Faculty ID") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, singleLine = true)
                    OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, singleLine = true)
                    OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("Qualification") }, singleLine = true)
                    OutlinedTextField(value = subjects, onValueChange = { subjects = it }, label = { Text("Assigned Subjects") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddFaculty(
                                code,
                                name,
                                email.ifBlank { "${code.lowercase()}@campusorbit.edu" },
                                dept,
                                designation,
                                qualification,
                                subjects,
                                workload.toLongOrNull() ?: 16L,
                                exp.toLongOrNull() ?: 8L
                            )
                            showAddFacultyModal = false
                        }
                    }
                ) {
                    Text("Save Faculty")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFacultyModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AcademicsAndTimetableSection(
    campusItems: List<CampusModuleItem>,
    onCreateItem: (
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
    ) -> Unit,
    onDeleteItem: (String, String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Depts & Courses, 1 = Smart Timetable
    var showAddCourseOrDeptModal by remember { mutableStateOf(false) }
    var showAddTimetableModal by remember { mutableStateOf(false) }

    val departments = campusItems.filter { it.moduleType == CampusModuleType.DEPARTMENT.name }
    val courses = campusItems.filter { it.moduleType == CampusModuleType.COURSE.name }
    val timetableSlots = campusItems.filter { it.moduleType == CampusModuleType.TIMETABLE.name }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                label = { Text("Departments & Courses (${departments.size + courses.size})") },
                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                label = { Text("Smart Timetable (${timetableSlots.size})") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (activeTab == 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Academic Departments & Credit Courses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Manage HODs, semester credits, and curriculum", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showAddCourseOrDeptModal = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Course")
                    }
                }
            }

            departments.forEach { dept ->
                ModuleItemCard(item = dept, badgeColor = OrbitRoyalBlueBright, onDelete = { onDeleteItem(dept.id, dept.title) })
            }
            courses.forEach { course ->
                ModuleItemCard(item = course, badgeColor = OrbitCyanAccent, onDelete = { onDeleteItem(course.id, course.title) })
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Weekly Timetable Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Automated faculty & classroom scheduling conflict detection", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showAddTimetableModal = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Slot")
                    }
                }
            }

            timetableSlots.forEach { slot ->
                ModuleItemCard(item = slot, badgeColor = OrbitEmerald, onDelete = { onDeleteItem(slot.id, slot.title) })
            }
        }
    }

    if (showAddCourseOrDeptModal) {
        var code by remember { mutableStateOf("CS${(500..899).random()}") }
        var title by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Computer Science & Engineering") }
        var sem by remember { mutableStateOf("6") }
        var credits by remember { mutableStateOf("4") }
        var facultyName by remember { mutableStateOf("Dr. Elena Rostova") }
        var syllabus by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCourseOrDeptModal = false },
            title = { Text("Add Academic Course / Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") }, singleLine = true)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Subject Title") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sem, onValueChange = { sem = it }, label = { Text("Semester") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = credits, onValueChange = { credits = it }, label = { Text("Credits") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = facultyName, onValueChange = { facultyName = it }, label = { Text("Assigned Professor") }, singleLine = true)
                    OutlinedTextField(value = syllabus, onValueChange = { syllabus = it }, label = { Text("Syllabus Summary") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateItem(
                                CampusModuleType.COURSE,
                                code,
                                title,
                                "$credits Credits • Sem $sem",
                                dept,
                                sem.toLongOrNull() ?: 6L,
                                "ACTIVE",
                                "CORE SUBJECT",
                                credits.toDoubleOrNull() ?: 4.0,
                                60L,
                                syllabus.ifBlank { "Core theoretical and laboratory curriculum." },
                                facultyName
                            )
                            showAddCourseOrDeptModal = false
                        }
                    }
                ) {
                    Text("Save Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseOrDeptModal = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddTimetableModal) {
        var day by remember { mutableStateOf("WEDNESDAY") }
        var timeSlot by remember { mutableStateOf("10:00 AM - 11:30 AM") }
        var subject by remember { mutableStateOf("CS601 Distributed Cloud Systems") }
        var room by remember { mutableStateOf("Smart Lecture Hall A-302") }
        var dept by remember { mutableStateOf("Computer Science & Engineering") }
        var sem by remember { mutableStateOf("6") }
        var facultyName by remember { mutableStateOf("Dr. Elena Rostova") }

        AlertDialog(
            onDismissRequest = { showAddTimetableModal = false },
            title = { Text("Schedule Conflict-Checked Timetable Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = day, onValueChange = { day = it.uppercase() }, label = { Text("Day (MONDAY..SATURDAY)") }, singleLine = true)
                    OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("Time Window") }, singleLine = true)
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Code & Name") }, singleLine = true)
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Classroom / Lab") }, singleLine = true)
                    OutlinedTextField(value = facultyName, onValueChange = { facultyName = it }, label = { Text("Assigned Faculty") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateItem(
                            CampusModuleType.TIMETABLE,
                            "TT-${day.take(3)}-${(100..999).random()}",
                            "$timeSlot • $subject",
                            "$day • $room • Sem $sem",
                            dept,
                            sem.toLongOrNull() ?: 6L,
                            "CONFLICT-FREE",
                            day,
                            10.0,
                            90L,
                            "Faculty: $facultyName | Room: $room | Verified conflict-free slot.",
                            facultyName
                        )
                        showAddTimetableModal = false
                    }
                ) {
                    Text("Validate & Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTimetableModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BadgeChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ModuleItemCard(
    item: CampusModuleItem,
    badgeColor: androidx.compose.ui.graphics.Color,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgeChip(text = item.code, color = badgeColor)
                    BadgeChip(text = item.status, color = OrbitEmerald)
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.metadataText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.department} • ${item.ownerName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (actionLabel != null && onActionClick != null) {
                    OutlinedButton(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
