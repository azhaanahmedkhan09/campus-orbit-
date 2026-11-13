package com.example.ui.modules

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AttendanceRecord
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.StudentRecord
import com.example.ui.theme.OrbitAmber
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitCyanSoft
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitNavyDeep
import com.example.ui.theme.OrbitPurple
import com.example.ui.theme.OrbitRose
import com.example.ui.theme.OrbitRoyalBlueBright
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartAttendanceSection(
    activeQrToken: String,
    activeQrSubject: String,
    students: List<StudentRecord>,
    attendanceList: List<AttendanceRecord>,
    onGenerateNewQr: (subjectCode: String, subjectName: String) -> Unit,
    onRecordAttendance: (
        studentCode: String,
        studentName: String,
        subjectCode: String,
        subjectName: String,
        department: String,
        semester: Long,
        status: String,
        method: String,
        sessionToken: String
    ) -> Unit,
    onToggleStatus: (AttendanceRecord) -> Unit
) {
    var selectedSubjectCode by remember { mutableStateOf("CS601") }
    var selectedSubjectName by remember { mutableStateOf("Distributed Cloud Systems") }
    var showStudentQrScanDialog by remember { mutableStateOf(false) }
    var showManualMarkDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Live Faculty QR Session Generator & Student QR Scanner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Smart QR Attendance Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeQrSubject,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(
                        onClick = { onGenerateNewQr(selectedSubjectCode, selectedSubjectName) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("generate_qr_session_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate QR")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Deterministic Visual QR Matrix rendered via Jetpack Compose Canvas
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .size(186.dp)
                        .border(2.dp, OrbitCyanAccent, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    QrMatrixCanvas(token = activeQrToken)
                }

                Spacer(modifier = Modifier.height(10.dp))

                BadgeChip(text = "LIVE TOKEN: $activeQrToken", color = OrbitCyanAccent)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showStudentQrScanDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrbitCyanAccent,
                            contentColor = OrbitNavyDeep
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_qr_attendance_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Student QR Check-In", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { showManualMarkDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_attendance_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manual Roll Call")
                    }
                }
            }
        }

        // Attendance Logs List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Verified Attendance Register (${attendanceList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any status badge to cycle PRESENT -> LATE -> ABSENT in real time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                attendanceList.forEach { rec ->
                    val statusColor = when (rec.status) {
                        "PRESENT" -> OrbitEmerald
                        "LATE" -> OrbitAmber
                        else -> OrbitRose
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { onToggleStatus(rec) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${rec.studentName} (${rec.studentCode})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${rec.subjectCode} ${rec.subjectName} • ${rec.sessionDate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Method: ${rec.method} • Token: ${rec.sessionToken}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            BadgeChip(text = rec.status, color = statusColor)
                        }
                    }
                }
            }
        }
    }

    if (showStudentQrScanDialog) {
        val defaultStudent = students.firstOrNull()
        var studentCode by remember { mutableStateOf(defaultStudent?.studentCode ?: "STU-2026-001") }
        var studentName by remember { mutableStateOf(defaultStudent?.fullName ?: "Aarav Mehta") }
        var scannedToken by remember { mutableStateOf(activeQrToken) }

        AlertDialog(
            onDismissRequest = { showStudentQrScanDialog = false },
            title = { Text("Student QR Attendance Check-In") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Student or verify QR Session Token:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        students.take(4).forEach { st ->
                            FilterChip(
                                selected = studentCode == st.studentCode,
                                onClick = {
                                    studentCode = st.studentCode
                                    studentName = st.fullName
                                },
                                label = { Text(st.fullName.substringBefore(" ")) }
                            )
                        }
                    }
                    OutlinedTextField(value = studentCode, onValueChange = { studentCode = it }, label = { Text("Student Roll ID") }, singleLine = true)
                    OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Student Name") }, singleLine = true)
                    OutlinedTextField(value = scannedToken, onValueChange = { scannedToken = it }, label = { Text("Scanned QR Session Token") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRecordAttendance(
                            studentCode,
                            studentName,
                            selectedSubjectCode,
                            selectedSubjectName,
                            "Computer Science & Engineering",
                            6L,
                            "PRESENT",
                            "QR_SCAN",
                            scannedToken
                        )
                        showStudentQrScanDialog = false
                    }
                ) {
                    Text("Verify & Check In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStudentQrScanDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showManualMarkDialog) {
        val defaultStudent = students.firstOrNull()
        var studentCode by remember { mutableStateOf(defaultStudent?.studentCode ?: "STU-2026-002") }
        var studentName by remember { mutableStateOf(defaultStudent?.fullName ?: "Maya Lin") }
        var subjectCode by remember { mutableStateOf(selectedSubjectCode) }
        var subjectName by remember { mutableStateOf(selectedSubjectName) }
        var status by remember { mutableStateOf("PRESENT") }

        AlertDialog(
            onDismissRequest = { showManualMarkDialog = false },
            title = { Text("Faculty Manual Attendance Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = studentCode, onValueChange = { studentCode = it }, label = { Text("Student Code") }, singleLine = true)
                    OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Student Name") }, singleLine = true)
                    OutlinedTextField(value = subjectCode, onValueChange = { subjectCode = it }, label = { Text("Subject Code") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PRESENT", "LATE", "ABSENT").forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRecordAttendance(
                            studentCode,
                            studentName,
                            subjectCode,
                            subjectName,
                            "Computer Science & Engineering",
                            6L,
                            status,
                            "MANUAL",
                            activeQrToken
                        )
                        showManualMarkDialog = false
                    }
                ) {
                    Text("Record Attendance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualMarkDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun QrMatrixCanvas(token: String) {
    Canvas(modifier = Modifier.fillMaxWidth().height(158.dp)) {
        val gridCount = 15
        val cellSize = size.width / gridCount
        val hash = abs(token.hashCode())

        for (r in 0 until gridCount) {
            for (c in 0 until gridCount) {
                val isFinderCorner =
                    (r < 4 && c < 4) || (r < 4 && c >= gridCount - 4) || (r >= gridCount - 4 && c < 4)
                val isFinderBorder = isFinderCorner && (
                    r == 0 || r == 3 || c == 0 || c == 3 ||
                        c == gridCount - 1 || c == gridCount - 4 ||
                        r == gridCount - 1 || r == gridCount - 4
                    )
                val isFinderCenter =
                    (r in 1..2 && c in 1..2) ||
                        (r in 1..2 && c in (gridCount - 3)..(gridCount - 2)) ||
                        (r in (gridCount - 3)..(gridCount - 2) && c in 1..2)

                val bit = ((hash shr ((r * gridCount + c) % 28)) and 1) == 1
                val shouldFill = if (isFinderCorner) (isFinderBorder || isFinderCenter) else bit

                if (shouldFill) {
                    drawRect(
                        color = OrbitNavyDeep,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize * 0.92f, cellSize * 0.92f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExamsAndAssignmentsSection(
    students: List<StudentRecord>,
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
    onUpdateItemStatus: (CampusModuleItem, String, String) -> Unit,
    onDeleteItem: (String, String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Exams & Results, 1 = Assignments & LMS, 2 = Hall Ticket
    var showAddExamModal by remember { mutableStateOf(false) }
    var showAddAssignmentModal by remember { mutableStateOf(false) }

    val examResults = campusItems.filter { it.moduleType == CampusModuleType.EXAM_RESULT.name }
    val assignments = campusItems.filter { it.moduleType == CampusModuleType.ASSIGNMENT.name }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                label = { Text("Results & SGPA (${examResults.size})") },
                leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                label = { Text("Assignments (${assignments.size})") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                label = { Text("Hall Ticket") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (activeTab) {
            0 -> {
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
                            Text("Examination Grading & SGPA/CGPA Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Enter internal + end-sem marks; auto-computes grade & SGPA", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { showAddExamModal = true }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Marks")
                        }
                    }
                }
                examResults.forEach { res ->
                    ModuleItemCard(
                        item = res,
                        badgeColor = OrbitPurple,
                        onDelete = { onDeleteItem(res.id, res.title) }
                    )
                }
            }

            1 -> {
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
                            Text("Assignments & Learning Resources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Publish coursework PDFs, deadlines, and grade student submissions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { showAddAssignmentModal = true }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Assignment")
                        }
                    }
                }
                assignments.forEach { asn ->
                    ModuleItemCard(
                        item = asn,
                        badgeColor = OrbitCyanAccent,
                        actionLabel = if (asn.status == "SUBMITTED & GRADED") null else "Submit / Grade",
                        onActionClick = {
                            onUpdateItemStatus(
                                asn,
                                "SUBMITTED & GRADED",
                                "${asn.metadataText} | Verified submission • Score: 48/50"
                            )
                        },
                        onDelete = { onDeleteItem(asn.id, asn.title) }
                    )
                }
            }

            else -> {
                val student = students.firstOrNull()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgeChip(text = "OFFICIAL DIGITAL HALL TICKET", color = OrbitCyanAccent)
                            BadgeChip(text = "VERIFIED ELIGIBLE", color = OrbitEmerald)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Orbit Institute of Technology — Autumn 2026 Examinations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Candidate Name: ${student?.fullName ?: "Aarav Mehta"}")
                        Text("Enrollment / Roll No: ${student?.studentCode ?: "STU-2026-001"}")
                        Text("Department: ${student?.department ?: "Computer Science & Engineering"} (Sem ${student?.semester ?: 6})")
                        Text("Attendance Clearance: ${student?.attendancePct ?: 92}% (Min 75% Required)")
                        Text("Fee Clearance Status: ${student?.feeStatus ?: "PAID"}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Examination Schedule:", fontWeight = FontWeight.Bold)
                                Text("• Dec 04, 2026 (09:30 AM) — CS601 Distributed Cloud Systems (Hall A-302)")
                                Text("• Dec 07, 2026 (09:30 AM) — CS602 Compiler Construction (Hall A-304)")
                                Text("• Dec 10, 2026 (02:00 PM) — CS603 Cryptography & Network Security (Hall B-101)")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExamModal) {
        var studentInfo by remember { mutableStateOf("Aarav Mehta (STU-2026-001)") }
        var subject by remember { mutableStateOf("CS602 Compiler Design") }
        var internalMarks by remember { mutableStateOf("37") }
        var externalMarks by remember { mutableStateOf("55") }
        var dept by remember { mutableStateOf("Computer Science & Engineering") }

        AlertDialog(
            onDismissRequest = { showAddExamModal = false },
            title = { Text("Enter Marks & Auto-Compute Grade/SGPA") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = studentInfo, onValueChange = { studentInfo = it }, label = { Text("Student Name & Roll ID") }, singleLine = true)
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Code & Title") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = internalMarks, onValueChange = { internalMarks = it }, label = { Text("Internal (/40)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = externalMarks, onValueChange = { externalMarks = it }, label = { Text("End-Sem (/60)") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intM = internalMarks.toIntOrNull() ?: 36
                        val extM = externalMarks.toIntOrNull() ?: 52
                        val total = (intM + extM).coerceIn(0, 100)
                        val grade = when {
                            total >= 90 -> "O (Outstanding)"
                            total >= 80 -> "A+ (Excellent)"
                            total >= 70 -> "A (Very Good)"
                            total >= 60 -> "B+ (Good)"
                            else -> "P (Pass)"
                        }
                        val sgpa = String.format(Locale.US, "%.2f", (total / 10.0).coerceIn(5.0, 10.0))
                        onCreateItem(
                            CampusModuleType.EXAM_RESULT,
                            "EXM-2026-${(100..999).random()}",
                            "$studentInfo • $subject",
                            "Grade: $grade • SGPA: $sgpa • Total: $total/100",
                            dept,
                            6L,
                            "PUBLISHED",
                            "END-SEMESTER RESULT",
                            total.toDouble(),
                            100L,
                            "Internal Assessment: $intM/40 | End-Sem Exam: $extM/60 | Verified by Controller of Examinations.",
                            "Dr. Elena Rostova"
                        )
                        showAddExamModal = false
                    }
                ) {
                    Text("Compute & Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExamModal = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddAssignmentModal) {
        var code by remember { mutableStateOf("ASN-CS-${(10..99).random()}") }
        var title by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("Dec 02, 2026") }
        var maxMarks by remember { mutableStateOf("50") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddAssignmentModal = false },
            title = { Text("Create Assignment & Learning Resource") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Assignment Code") }, singleLine = true)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Assignment Title") }, singleLine = true)
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Submission Deadline") }, singleLine = true)
                    OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Max Marks") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Resource URL / Instructions") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateItem(
                                CampusModuleType.ASSIGNMENT,
                                code,
                                title,
                                "Due: $dueDate • Max Marks: $maxMarks • PDF Resource Attached",
                                "Computer Science & Engineering",
                                6L,
                                "OPEN FOR SUBMISSION",
                                "COURSEWORK",
                                maxMarks.toDoubleOrNull() ?: 50.0,
                                60L,
                                description.ifBlank { "Includes reference lecture notes and lab evaluation rubric." },
                                "Dr. Elena Rostova"
                            )
                            showAddAssignmentModal = false
                        }
                    }
                ) {
                    Text("Publish Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssignmentModal = false }) { Text("Cancel") }
            }
        )
    }
}
