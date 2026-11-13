package com.example.ui.modules

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.GrievanceRecord
import com.example.data.OrbitRole
import com.example.data.StudentRecord
import com.example.ui.theme.OrbitAmber
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitPurple
import com.example.ui.theme.OrbitRose
import com.example.ui.theme.OrbitRoyalBlueBright
import java.util.Locale

@Composable
fun FinanceAndGrievancesSection(
    grievances: List<GrievanceRecord>,
    campusItems: List<CampusModuleItem>,
    onFileGrievance: (title: String, category: String, description: String, priority: String, submittedBy: String) -> Unit,
    onAdvanceGrievance: (GrievanceRecord, String) -> Unit,
    onCreateFeeInvoice: (
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
    onUpdateInvoiceStatus: (CampusModuleItem, String, String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Fee & Finance, 1 = Grievance Redressal
    var showCreateInvoiceModal by remember { mutableStateOf(false) }
    var showFileGrievanceModal by remember { mutableStateOf(false) }

    val invoices = campusItems.filter { it.moduleType == CampusModuleType.FEE_INVOICE.name }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                label = { Text("Fee & Finance (${invoices.size})") },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                label = { Text("Grievance Cell (${grievances.size})") },
                leadingIcon = { Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp)) }
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
                        Text("Fee Invoices & Digital Receipts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Semester fee allocation, outstanding balances, and verified receipts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showCreateInvoiceModal = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Invoice")
                    }
                }
            }

            invoices.forEach { inv ->
                ModuleItemCard(
                    item = inv,
                    badgeColor = if (inv.status == "PAID") OrbitEmerald else OrbitAmber,
                    actionLabel = if (inv.status == "PAID") null else "Record Verified Payment",
                    onActionClick = {
                        onUpdateInvoiceStatus(
                            inv,
                            "PAID",
                            "${inv.metadataText} | Full balance cleared • Digital Receipt #RCP-${(10000..99999).random()}"
                        )
                    }
                )
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
                        Text("Institutional Grievance Redressal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Confidential ticket lifecycle: Submitted -> Under Review -> In Progress -> Resolved", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { showFileGrievanceModal = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("file_grievance_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Ticket")
                    }
                }
            }

            grievances.forEach { grv ->
                val pColor = when (grv.priority) {
                    "CRITICAL", "HIGH" -> OrbitRose
                    "MEDIUM" -> OrbitAmber
                    else -> OrbitCyanAccent
                }
                val sColor = if (grv.status == "RESOLVED") OrbitEmerald else OrbitRoyalBlueBright

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BadgeChip(text = "${grv.ticketNumber} • ${grv.category}", color = pColor)
                            BadgeChip(text = grv.status, color = sColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(grv.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(grv.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Resolution Notes: ${grv.resolutionNotes}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "By: ${grv.submittedBy}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (grv.status != "RESOLVED") {
                                OutlinedButton(
                                    onClick = {
                                        onAdvanceGrievance(
                                            grv,
                                            "Reviewed and actioned by Institutional Redressal Committee."
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Advance Status")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateInvoiceModal) {
        var studentInfo by remember { mutableStateOf("Rohan Deshmukh (STU-2026-005)") }
        var amount by remember { mutableStateOf("4250") }
        var semester by remember { mutableStateOf("4") }
        var dept by remember { mutableStateOf("Mechanical & Robotics Engineering") }

        AlertDialog(
            onDismissRequest = { showCreateInvoiceModal = false },
            title = { Text("Generate Student Fee Invoice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = studentInfo, onValueChange = { studentInfo = it }, label = { Text("Student Name & ID") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, singleLine = true)
                    OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") }, singleLine = true)
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Invoice Amount (USD)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 4250.0
                        onCreateFeeInvoice(
                            CampusModuleType.FEE_INVOICE,
                            "INV-2026-${(1000..9999).random()}",
                            "Semester $semester Tuition & Lab Fee • $studentInfo",
                            "Amount: $$amt • Due within 14 days",
                            dept,
                            semester.toLongOrNull() ?: 4L,
                            "PENDING",
                            "TUITION FEE",
                            amt,
                            amt.toLong(),
                            "Generated by Finance Office. Payable via institutional portal.",
                            "Office of Finance & Accounts"
                        )
                        showCreateInvoiceModal = false
                    }
                ) {
                    Text("Issue Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateInvoiceModal = false }) { Text("Cancel") }
            }
        )
    }

    if (showFileGrievanceModal) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Academic & Examination") }
        var priority by remember { mutableStateOf("HIGH") }
        var description by remember { mutableStateOf("") }
        var submittedBy by remember { mutableStateOf("Aarav Mehta (STU-2026-001)") }

        AlertDialog(
            onDismissRequest = { showFileGrievanceModal = false },
            title = { Text("Submit Confidential Grievance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Issue Summary") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
                    OutlinedTextField(value = priority, onValueChange = { priority = it.uppercase() }, label = { Text("Priority (LOW / MEDIUM / HIGH / CRITICAL)") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Detailed Description") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onFileGrievance(title, category, description, priority, submittedBy)
                            showFileGrievanceModal = false
                        }
                    }
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFileGrievanceModal = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampusLifeAndCareerSection(
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
    var selectedCategory by remember { mutableStateOf(CampusModuleType.FACILITY_BOOKING) }
    var showCreateModal by remember { mutableStateOf(false) }

    val supportedModules = listOf(
        CampusModuleType.FACILITY_BOOKING,
        CampusModuleType.CERTIFICATE,
        CampusModuleType.EVENT,
        CampusModuleType.STUDY_GROUP,
        CampusModuleType.PLACEMENT
    )

    val filteredItems = campusItems.filter { it.moduleType == selectedCategory.name }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            supportedModules.forEach { mod ->
                val icon = when (mod) {
                    CampusModuleType.FACILITY_BOOKING -> Icons.Default.MeetingRoom
                    CampusModuleType.CERTIFICATE -> Icons.Default.CardMembership
                    CampusModuleType.EVENT -> Icons.Default.Event
                    CampusModuleType.STUDY_GROUP -> Icons.Default.Groups
                    else -> Icons.Default.Work
                }
                FilterChip(
                    selected = selectedCategory == mod,
                    onClick = { selectedCategory = mod },
                    label = { Text(mod.title) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

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
                    Text(selectedCategory.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = when (selectedCategory) {
                            CampusModuleType.FACILITY_BOOKING -> "Reserve auditoriums, labs, seminar halls with conflict checks"
                            CampusModuleType.CERTIFICATE -> "Bonafide, Study, and Transfer Certificate workflow & verification"
                            CampusModuleType.EVENT -> "Hackathons, workshops, seminars, and participant registrations"
                            CampusModuleType.STUDY_GROUP -> "Peer learning circles, subject collaboration, and shared notes"
                            else -> "Placement drives, internships, skill portfolios, and CTC offers"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { showCreateModal = true }, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create")
                }
            }
        }

        filteredItems.forEach { item ->
            val actionText = when (selectedCategory) {
                CampusModuleType.FACILITY_BOOKING -> if (item.status != "APPROVED") "Approve Booking" else null
                CampusModuleType.CERTIFICATE -> if (item.status != "APPROVED") "Approve & Issue PDF" else null
                CampusModuleType.EVENT -> "Register (+1)"
                CampusModuleType.STUDY_GROUP -> "Join Study Group"
                CampusModuleType.PLACEMENT -> "Apply with Portfolio"
                else -> null
            }

            ModuleItemCard(
                item = item,
                badgeColor = OrbitCyanAccent,
                actionLabel = actionText,
                onActionClick = {
                    val nextStatus = when (selectedCategory) {
                        CampusModuleType.FACILITY_BOOKING, CampusModuleType.CERTIFICATE -> "APPROVED"
                        CampusModuleType.PLACEMENT -> "APPLICATION SUBMITTED"
                        else -> item.status
                    }
                    val updatedMeta = "${item.metadataText} | Updated via Campus Orbit Portal."
                    onUpdateItemStatus(item, nextStatus, updatedMeta)
                },
                onDelete = { onDeleteItem(item.id, item.title) }
            )
        }
    }

    if (showCreateModal) {
        var code by remember { mutableStateOf("${selectedCategory.name.take(3)}-2026-${(10..99).random()}") }
        var title by remember { mutableStateOf("") }
        var subtitle by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Computer Science & Engineering") }
        var details by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateModal = false },
            title = { Text("New ${selectedCategory.title} Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Reference Code") }, singleLine = true)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title / Resource / Role") }, singleLine = true)
                    OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Slot / Company / Purpose") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, singleLine = true)
                    OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Description / Notes") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateItem(
                                selectedCategory,
                                code,
                                title,
                                subtitle.ifBlank { "Campus Orbit Verified Entry" },
                                dept,
                                6L,
                                "ACTIVE",
                                selectedCategory.name,
                                100.0,
                                25L,
                                details.ifBlank { "Created in real-time Firestore workspace." },
                                "Campus Orbit Coordinator"
                            )
                            showCreateModal = false
                        }
                    }
                ) {
                    Text("Save Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ParentGuardianPortalSection(
    linkedStudentCode: String,
    students: List<StudentRecord>,
    campusItems: List<CampusModuleItem>,
    onUpdateLinkedStudent: (String) -> Unit
) {
    val ward = students.firstOrNull { it.studentCode.equals(linkedStudentCode, ignoreCase = true) }
        ?: students.firstOrNull()
    var newLinkedCode by remember(linkedStudentCode) { mutableStateOf(linkedStudentCode) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = OrbitCyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Parent & Guardian Oversight Portal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Verified guardian link provides real-time visibility into your ward's attendance, exam performance, fee status, and faculty mentor contact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newLinkedCode,
                        onValueChange = { newLinkedCode = it },
                        label = { Text("Verified Ward Roll ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { onUpdateLinkedStudent(newLinkedCode) }) {
                        Text("Switch Ward")
                    }
                }
            }
        }

        if (ward != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BadgeChip(text = "VERIFIED WARD: ${ward.studentCode}", color = OrbitCyanAccent)
                        BadgeChip(
                            text = if (ward.attendancePct < 75L) "ATTENDANCE WARNING (<75%)" else "GOOD STANDING",
                            color = if (ward.attendancePct < 75L) OrbitRose else OrbitEmerald
                        )
                    }
                    Text(ward.fullName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${ward.department} • Semester ${ward.semester}")
                    Text("Overall Attendance: ${ward.attendancePct}% • Cumulative GPA: ${String.format(Locale.US, "%.2f", ward.cgpa)}")
                    Text("Fee Payment Status: ${ward.feeStatus}")
                    Text("Guardian on Record: ${ward.guardianName} (${ward.guardianPhone})")
                    Text("Assigned Faculty Mentor: ${ward.mentorName}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SuperAdminGovernanceSection(
    activeRole: OrbitRole,
    institutionName: String,
    linkedStudentCode: String,
    campusItems: List<CampusModuleItem>,
    onSaveConfig: (institutionName: String, linkedStudentCode: String, role: OrbitRole) -> Unit,
    onCreateAuditLog: (title: String, detail: String) -> Unit
) {
    var instName by remember(institutionName) { mutableStateOf(institutionName) }
    var wardCode by remember(linkedStudentCode) { mutableStateOf(linkedStudentCode) }
    val auditLogs = campusItems.filter { it.moduleType == CampusModuleType.AUDIT_LOG.name }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = OrbitPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Super Admin Institutional Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = instName,
                    onValueChange = { instName = it },
                    label = { Text("Institution Official Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = wardCode,
                    onValueChange = { wardCode = it },
                    label = { Text("Default Linked Student Roll ID (for Student/Parent Portal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onSaveConfig(instName, wardCode, activeRole)
                        onCreateAuditLog(
                            "Updated Institutional Settings",
                            "Institution set to '$instName' with linked roll '$wardCode'."
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Institutional Configuration")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("System Activity & Security Audit Logs (${auditLogs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                auditLogs.forEach { log ->
                    ModuleItemCard(item = log, badgeColor = OrbitPurple)
                }
            }
        }
    }
}
