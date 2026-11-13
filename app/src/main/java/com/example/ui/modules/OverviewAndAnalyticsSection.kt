package com.example.ui.modules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AttendanceRecord
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.FacultyRecord
import com.example.data.GrievanceRecord
import com.example.data.OrbitRole
import com.example.data.StudentRecord
import com.example.ui.OrbitSection
import com.example.ui.theme.OrbitAmber
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitCyanSoft
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitNavyCard
import com.example.ui.theme.OrbitNavyDeep
import com.example.ui.theme.OrbitPurple
import com.example.ui.theme.OrbitRose
import com.example.ui.theme.OrbitRoyalBlue
import com.example.ui.theme.OrbitRoyalBlueBright
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewAndAnalyticsSection(
    activeRole: OrbitRole,
    institutionName: String,
    students: List<StudentRecord>,
    faculty: List<FacultyRecord>,
    attendance: List<AttendanceRecord>,
    grievances: List<GrievanceRecord>,
    campusItems: List<CampusModuleItem>,
    onNavigateSection: (OrbitSection) -> Unit,
    onPublishNotice: (code: String, title: String, dept: String, category: String, body: String) -> Unit
) {
    var showNoticeModal by remember { mutableStateOf(false) }

    val departments = campusItems.filter { it.moduleType == CampusModuleType.DEPARTMENT.name }
    val courses = campusItems.filter { it.moduleType == CampusModuleType.COURSE.name }
    val notices = campusItems.filter { it.moduleType == CampusModuleType.NOTICE.name }
    val invoices = campusItems.filter { it.moduleType == CampusModuleType.FEE_INVOICE.name }
    val events = campusItems.filter { it.moduleType == CampusModuleType.EVENT.name }

    val avgAttendance = if (students.isNotEmpty()) {
        students.map { it.attendancePct }.average()
    } else 91.0
    val avgCgpa = if (students.isNotEmpty()) {
        students.map { it.cgpa }.average()
    } else 8.85
    val lowAttendanceStudents = students.filter { it.attendancePct < 75L }
    val openGrievances = grievances.count { it.status != "RESOLVED" && it.status != "REJECTED" }
    val pendingFeeStudents = students.count { it.feeStatus != "PAID" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Role-Tailored Hero Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_campus_hero_1794576874005),
                    contentDescription = "Campus Orbit Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    OrbitNavyDeep.copy(alpha = 0.94f),
                                    OrbitNavyDeep.copy(alpha = 0.80f),
                                    OrbitRoyalBlue.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = OrbitCyanAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(1.dp, OrbitCyanSoft.copy(alpha = 0.5f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "${activeRole.badge} • LIVE FIRESTORE SYNC",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrbitCyanSoft,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Button(
                            onClick = { showNoticeModal = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrbitCyanAccent,
                                contentColor = OrbitNavyDeep
                            ),
                            modifier = Modifier.testTag("publish_notice_button")
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Broadcast Notice", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Column {
                        Text(
                            text = institutionName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeRole.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Low Attendance (<75%) Automatic Warning Banner
        if (lowAttendanceStudents.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitAmber.copy(alpha = 0.14f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrbitAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Low Attendance Alert",
                        tint = OrbitAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automated Attendance Threshold Alert (<75%)",
                            style = MaterialTheme.typography.titleSmall,
                            color = OrbitAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lowAttendanceStudents.size} student(s) below 75% mandatory threshold: ${
                                lowAttendanceStudents.joinToString { "${it.fullName} (${it.attendancePct}%)" }
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = { onNavigateSection(OrbitSection.ATTENDANCE_QR) }) {
                        Text("Review", color = OrbitAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Institutional KPI Metrics Grid (Derived directly from Firestore records)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Total Students",
                value = "${students.size}",
                subtitle = "${students.count { it.status == "ACTIVE" }} Active Scholars",
                icon = Icons.Default.School,
                accentColor = OrbitRoyalBlueBright,
                onClick = { onNavigateSection(OrbitSection.STUDENTS) }
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Faculty & HODs",
                value = "${faculty.size}",
                subtitle = "${departments.size} Departments • ${courses.size} Courses",
                icon = Icons.Default.SupervisorAccount,
                accentColor = OrbitCyanAccent,
                onClick = { onNavigateSection(OrbitSection.FACULTY) }
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Avg Attendance",
                value = String.format(Locale.US, "%.1f%%", avgAttendance),
                subtitle = "${attendance.size} QR/Manual Logs",
                icon = Icons.Default.QrCode2,
                accentColor = OrbitEmerald,
                onClick = { onNavigateSection(OrbitSection.ATTENDANCE_QR) }
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Academic CGPA",
                value = String.format(Locale.US, "%.2f", avgCgpa),
                subtitle = "$pendingFeeStudents Pending Fees • $openGrievances Grievances",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = OrbitPurple,
                onClick = { onNavigateSection(OrbitSection.EXAMS_ASSIGNMENTS) }
            )
        }

        // Interactive Data Visualizations (Department Distribution + Attendance/Fee Ring)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-Time Institutional Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Firestore Live Telemetry",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Department-wise CGPA & Attendance Bar Visualization
                val deptGroups = students.groupBy { it.department.take(22) }
                if (deptGroups.isNotEmpty()) {
                    deptGroups.entries.forEachIndexed { index, (deptName, deptStudents) ->
                        val deptAtt = deptStudents.map { it.attendancePct }.average().toFloat()
                        val deptCgpa = deptStudents.map { it.cgpa }.average()
                        val barColor = when (index % 4) {
                            0 -> OrbitRoyalBlueBright
                            1 -> OrbitCyanAccent
                            2 -> OrbitEmerald
                            else -> OrbitPurple
                        }
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$deptName (${deptStudents.size} students)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = String.format(Locale.US, "Att: %.0f%% • CGPA: %.2f", deptAtt, deptCgpa),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                            ) {
                                drawRoundRect(
                                    color = barColor.copy(alpha = 0.18f),
                                    size = size,
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                                val fillFraction = (deptAtt / 100f).coerceIn(0.05f, 1f)
                                drawRoundRect(
                                    color = barColor,
                                    size = Size(width = size.width * fillFraction, height = size.height),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Gauges Row: Overall Attendance, Fee Clearance, Grievance Resolution
                val paidRatio = if (students.isNotEmpty()) {
                    students.count { it.feeStatus == "PAID" }.toFloat() / students.size.toFloat()
                } else 0.8f
                val resolvedRatio = if (grievances.isNotEmpty()) {
                    grievances.count { it.status == "RESOLVED" }.toFloat() / grievances.size.toFloat()
                } else 0.7f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RadialMetricGauge(
                        label = "Attendance",
                        percentage = (avgAttendance / 100.0).toFloat().coerceIn(0f, 1f),
                        color = OrbitEmerald
                    )
                    RadialMetricGauge(
                        label = "Fee Cleared",
                        percentage = paidRatio.coerceIn(0f, 1f),
                        color = OrbitCyanAccent
                    )
                    RadialMetricGauge(
                        label = "Grievances",
                        percentage = resolvedRatio.coerceIn(0f, 1f),
                        color = OrbitRoyalBlueBright
                    )
                }
            }
        }

        // Quick Module Launchpad
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Unified Campus Operating Modules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrbitSection.entries.filter { it != OrbitSection.OVERVIEW }.forEach { section ->
                        OutlinedButton(
                            onClick = { onNavigateSection(section) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("quick_nav_${section.name.lowercase()}")
                        ) {
                            Text(section.title, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Real-Time Institutional Notices & Broadcasts (Module 11)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = OrbitCyanAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-Time Institutional Notices (${notices.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { showNoticeModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Notice")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (notices.isEmpty()) {
                    Text(
                        text = "No institutional notices published yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    notices.forEach { notice ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = OrbitRoyalBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${notice.code} • ${notice.category}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OrbitCyanSoft,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = notice.department,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notice.metadataText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Published by ${notice.ownerName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoticeModal) {
        var code by remember { mutableStateOf("NOT-2026-${(200..999).random()}") }
        var title by remember { mutableStateOf("") }
        var department by remember { mutableStateOf("All Departments") }
        var category by remember { mutableStateOf("EXAMINATION") }
        var body by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNoticeModal = false },
            title = { Text("Publish Institutional Notice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Notice Reference Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notice Headline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Target Department / Audience") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (EXAMINATION / EVENT / URGENT)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Detailed Broadcast Message") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            onPublishNotice(code, title, department, category, body)
                            showNoticeModal = false
                        }
                    }
                ) {
                    Text("Publish Live")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoticeModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun KpiStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RadialMetricGauge(
    label: String,
    percentage: Float,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(78.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                drawArc(
                    color = color.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * percentage,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
