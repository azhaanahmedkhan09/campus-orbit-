package com.example.ui.modules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.FacultyRecord
import com.example.data.OrbitRole
import com.example.ui.theme.OrbitAmber
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitPurple
import com.example.ui.theme.OrbitRose
import com.example.ui.theme.OrbitRoyalBlueBright
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamWorkspaceTabContent(
    teams: List<CampusModuleItem>,
    tasks: List<CampusModuleItem>,
    currentUserName: String,
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
    onUpdateItemProgressAndStatus: (item: CampusModuleItem, newStatus: String, newProgress: Double, newMeta: String) -> Unit,
    onDeleteItem: (id: String, title: String) -> Unit
) {
    var showCreateTeamModal by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf<CampusModuleItem?>(null) }

    val statuses = listOf("PLANNING", "IN_PROGRESS", "UNDER_REVIEW", "COMPLETED", "ON_HOLD")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interdisciplinary Team Workspaces (${teams.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage team roles, objectives, announcements, resources & progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateTeamModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_create_team_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Form Team")
                }
            }

            teams.forEach { team ->
                val teamTasks = tasks.filter {
                    it.subtitle.contains(team.title.take(12), ignoreCase = true) ||
                        it.code.contains(team.code.takeLast(3), ignoreCase = true)
                }
                val completedTasks = teamTasks.count { it.status == "COMPLETED" }
                val progressPct = team.primaryMetric.coerceIn(0.0, 100.0)
                val statusColor = when (team.status) {
                    "COMPLETED" -> OrbitEmerald
                    "IN_PROGRESS" -> OrbitRoyalBlueBright
                    "UNDER_REVIEW" -> OrbitPurple
                    "ON_HOLD" -> OrbitRose
                    else -> OrbitAmber
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                BadgeChip(text = team.code, color = OrbitCyanAccent)
                                BadgeChip(text = team.status, color = statusColor)
                                BadgeChip(text = team.category, color = OrbitPurple)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { editingTeam = team }) {
                                    Text("Manage Workspace")
                                }
                                IconButton(onClick = { onDeleteItem(team.id, team.title) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Team", tint = OrbitRose)
                                }
                            }
                        }

                        Text(
                            text = team.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = team.subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Departments: ${team.department} • Target Capacity: ${team.secondaryMetric} Members • Lead: ${team.ownerName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = team.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Workspace Completion Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format(Locale.US, "%.0f%% (%d/%d tasks done)", progressPct, completedTasks, teamTasks.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                            ) {
                                drawRoundRect(
                                    color = statusColor.copy(alpha = 0.2f),
                                    size = size,
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
                                drawRoundRect(
                                    color = statusColor,
                                    size = Size(width = size.width * (progressPct / 100.0).toFloat().coerceIn(0.04f, 1f), height = size.height),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
                            }
                        }

                        // Quick Status & Progress Controls
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            statuses.forEach { st ->
                                FilterChip(
                                    selected = team.status == st,
                                    onClick = {
                                        val nextProgress = if (st == "COMPLETED") 100.0 else team.primaryMetric
                                        onUpdateItemProgressAndStatus(team, st, nextProgress, team.metadataText)
                                    },
                                    label = { Text(st.replace('_', ' ')) }
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val bumped = (team.primaryMetric + 10.0).coerceAtMost(100.0)
                                    val nextSt = if (bumped >= 100.0) "COMPLETED" else team.status
                                    onUpdateItemProgressAndStatus(team, nextSt, bumped, team.metadataText)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+10% Progress")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateTeamModal) {
        var teamName by remember { mutableStateOf("") }
        var projectTitle by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Artificial Intelligence and Machine Learning") }
        var departments by remember { mutableStateOf("CSE + ECE + Mechanical") }
        var technologies by remember { mutableStateOf("Kotlin, Python, TensorFlow, Cloud Firestore") }
        var maxMembers by remember { mutableStateOf("5") }
        var objectivesAndRoles by remember {
            mutableStateOf("Objectives: Build working prototype | Roles: Team Lead, ML Engineer, UI/UX & Cloud Architect | Resources: GitHub Repo & Figma Board")
        }

        AlertDialog(
            onDismissRequest = { showCreateTeamModal = false },
            title = { Text("Create Interdisciplinary Project Team") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name & Project Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Team Leader & Core Members") },
                        placeholder = { Text("Lead: $currentUserName • Members: ...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Project Domain Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = departments,
                        onValueChange = { departments = it },
                        label = { Text("Participating Departments") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = technologies,
                        onValueChange = { technologies = it },
                        label = { Text("Selected Technologies") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxMembers,
                        onValueChange = { maxMembers = it },
                        label = { Text("Required Number of Members") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = objectivesAndRoles,
                        onValueChange = { objectivesAndRoles = it },
                        label = { Text("Objectives, Role Assignments & Workspace Links") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank()) {
                            onCreateItem(
                                CampusModuleType.COLLAB_TEAM,
                                "CTM-2026-${(100..999).random()}",
                                teamName,
                                projectTitle.ifBlank { "Lead: $currentUserName • Tech: $technologies" },
                                departments,
                                6L,
                                "IN_PROGRESS",
                                category,
                                25.0,
                                maxMembers.toLongOrNull() ?: 5L,
                                "$objectivesAndRoles | Tech Stack: $technologies",
                                currentUserName
                            )
                            showCreateTeamModal = false
                        }
                    }
                ) {
                    Text("Create Team")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTeamModal = false }) { Text("Cancel") }
            }
        )
    }

    editingTeam?.let { team ->
        var updatedMeta by remember(team) { mutableStateOf(team.metadataText) }
        var updatedProgress by remember(team) { mutableStateOf(team.primaryMetric.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { editingTeam = null },
            title = { Text("Workspace Settings • ${team.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = updatedProgress,
                        onValueChange = { updatedProgress = it },
                        label = { Text("Completion Percentage (0 - 100)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = updatedMeta,
                        onValueChange = { updatedMeta = it },
                        label = { Text("Team Members, Roles, Announcements & Resource Links") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pct = (updatedProgress.toDoubleOrNull() ?: team.primaryMetric).coerceIn(0.0, 100.0)
                        val st = if (pct >= 100.0) "COMPLETED" else team.status
                        onUpdateItemProgressAndStatus(team, st, pct, updatedMeta)
                        editingTeam = null
                    }
                ) {
                    Text("Save Workspace")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTeam = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksAndMilestonesTabContent(
    tasks: List<CampusModuleItem>,
    currentUserName: String,
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
    onUpdateItemStatus: (item: CampusModuleItem, newStatus: String, updatedMeta: String) -> Unit,
    onDeleteItem: (id: String, title: String) -> Unit
) {
    var showCreateTaskModal by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val completedCount = tasks.count { it.status == "COMPLETED" }
    val completionPct = if (tasks.isNotEmpty()) (completedCount * 100.0 / tasks.size) else 0.0
    val filteredTasks = tasks.filter { selectedStatusFilter == "ALL" || it.status == selectedStatusFilter }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Project Tasks & Milestones (${tasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "Overall Milestone Completion: %.0f%% (%d/%d Completed)", completionPct, completedCount, tasks.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = OrbitEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = { showCreateTaskModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_add_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Task")
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "TO_DO", "IN_PROGRESS", "COMPLETED").forEach { st ->
                    FilterChip(
                        selected = selectedStatusFilter == st,
                        onClick = { selectedStatusFilter = st },
                        label = { Text(st.replace('_', ' ')) }
                    )
                }
            }

            filteredTasks.forEach { task ->
                val isOverdue = task.status != "COMPLETED" && task.metadataText.contains("OVERDUE", ignoreCase = true)
                val priorityColor = when (task.category.uppercase()) {
                    "CRITICAL" -> OrbitRose
                    "HIGH" -> OrbitAmber
                    "MEDIUM" -> OrbitRoyalBlueBright
                    else -> OrbitCyanAccent
                }
                val statusColor = when (task.status) {
                    "COMPLETED" -> OrbitEmerald
                    "IN_PROGRESS" -> OrbitRoyalBlueBright
                    else -> OrbitAmber
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                BadgeChip(text = task.code, color = OrbitCyanAccent)
                                BadgeChip(text = "Priority: ${task.category}", color = priorityColor)
                                BadgeChip(text = task.status.replace('_', ' '), color = statusColor)
                                if (isOverdue) {
                                    BadgeChip(text = "OVERDUE", color = OrbitRose)
                                }
                            }
                            IconButton(onClick = { onDeleteItem(task.id, task.title) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Task", tint = OrbitRose)
                            }
                        }

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${task.subtitle} • Assigned to: ${task.ownerName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = task.metadataText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("TO_DO", "IN_PROGRESS", "COMPLETED").forEach { targetStatus ->
                                if (task.status != targetStatus) {
                                    OutlinedButton(
                                        onClick = { onUpdateItemStatus(task, targetStatus, task.metadataText) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Mark ${targetStatus.replace('_', ' ')}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateTaskModal) {
        var title by remember { mutableStateOf("") }
        var teamProject by remember { mutableStateOf("Team AuraGrid • Milestone 2") }
        var assignee by remember { mutableStateOf(currentUserName) }
        var priority by remember { mutableStateOf("HIGH") }
        var deadline by remember { mutableStateOf("2026-05-30") }
        var deliverables by remember { mutableStateOf("Deliverables: Complete module implementation & unit validation.") }

        AlertDialog(
            onDismissRequest = { showCreateTaskModal = false },
            title = { Text("Assign New Team Task / Milestone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task / Milestone Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teamProject,
                        onValueChange = { teamProject = it },
                        label = { Text("Project / Team & Milestone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assignee,
                        onValueChange = { assignee = it },
                        label = { Text("Assigned Team Member") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority (LOW / MEDIUM / HIGH / CRITICAL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deliverables,
                        onValueChange = { deliverables = it },
                        label = { Text("Deliverables & Acceptance Criteria") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateItem(
                                CampusModuleType.COLLAB_TASK,
                                "TSK-2026-${(100..999).random()}",
                                title,
                                teamProject,
                                "Interdisciplinary Team",
                                6L,
                                "TO_DO",
                                priority.uppercase(),
                                0.0,
                                1L,
                                "Deadline: $deadline | $deliverables",
                                assignee
                            )
                            showCreateTaskModal = false
                        }
                    }
                ) {
                    Text("Assign Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DiscussionsTabContent(
    discussions: List<CampusModuleItem>,
    currentUserName: String,
    activeRole: OrbitRole,
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
    onUpdateItemStatus: (item: CampusModuleItem, newStatus: String, updatedMeta: String) -> Unit
) {
    var topicTitle by remember { mutableStateOf("") }
    var teamChannel by remember { mutableStateOf("Team AuraGrid • Architecture Channel") }
    var messageBody by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<CampusModuleItem?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Team Discussion & Project Updates Board (${discussions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Quick Post Composer
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Discussion Topic / Update Headline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teamChannel,
                        onValueChange = { teamChannel = it },
                        label = { Text("Team Workspace Channel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        label = { Text("Share project update, milestone note, or technical question...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (topicTitle.isNotBlank() && messageBody.isNotBlank()) {
                                onCreateItem(
                                    CampusModuleType.COLLAB_DISCUSSION,
                                    "DSC-2026-${(100..999).random()}",
                                    topicTitle,
                                    teamChannel,
                                    activeRole.label,
                                    6L,
                                    "ACTIVE",
                                    "PROJECT UPDATE",
                                    1.0,
                                    1L,
                                    "Posted just now by $currentUserName: $messageBody",
                                    currentUserName
                                )
                                topicTitle = ""
                                messageBody = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("collab_post_discussion_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post to Team Board")
                    }
                }
            }

            discussions.forEach { disc ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgeChip(text = disc.category, color = OrbitCyanAccent)
                                BadgeChip(text = disc.subtitle, color = OrbitRoyalBlueBright)
                            }
                            TextButton(onClick = { replyingTo = disc }) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reply")
                            }
                        }
                        Text(
                            text = disc.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = disc.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Author: ${disc.ownerName} • ${disc.department}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    replyingTo?.let { thread ->
        var replyText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { replyingTo = null },
            title = { Text("Reply to '${thread.title}'") },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Your Reply") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            val appended = "${thread.metadataText}\n• Reply ($currentUserName): ${replyText.trim()}"
                            onUpdateItemStatus(thread, "ACTIVE", appended)
                            replyingTo = null
                        }
                    }
                ) {
                    Text("Post Reply")
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingTo = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectShowcaseTabContent(
    showcases: List<CampusModuleItem>,
    currentUserName: String,
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
    ) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showPublishShowcaseModal by remember { mutableStateOf(false) }

    val categories = listOf(
        "ALL",
        "Artificial Intelligence and Machine Learning",
        "Internet of Things",
        "Web Development",
        "Mobile Application Development",
        "Robotics",
        "Cloud Computing",
        "Cybersecurity",
        "Electronics and Embedded Systems",
        "Social Innovation"
    )

    val filtered = showcases.filter { item ->
        val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.subtitle.contains(searchQuery, ignoreCase = true) ||
            item.department.contains(searchQuery, ignoreCase = true) ||
            item.metadataText.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Innovation & Project Showcase (${filtered.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed interdisciplinary student projects, GitHub repos, demos & outcomes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showPublishShowcaseModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_publish_showcase_button")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Showcase")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Showcase by Title, Tech Stack, Department or Outcome...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(if (cat == "ALL") "All Categories" else cat) }
                    )
                }
            }

            filtered.forEach { item ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgeChip(text = item.code, color = OrbitEmerald)
                                BadgeChip(text = item.category, color = OrbitPurple)
                            }
                            BadgeChip(text = "Rating ${item.primaryMetric}/10", color = OrbitAmber)
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Technologies: ${item.subtitle}",
                            style = MaterialTheme.typography.labelLarge,
                            color = OrbitCyanAccent
                        )
                        Text(
                            text = "Department: ${item.department} • Team Size: ${item.secondaryMetric}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Team & Contributors: ${item.ownerName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showPublishShowcaseModal) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Artificial Intelligence and Machine Learning") }
        var department by remember { mutableStateOf("Computer Science & Engineering") }
        var technologies by remember { mutableStateOf("Kotlin, Jetpack Compose, Python, TensorFlow") }
        var problemAndSolution by remember {
            mutableStateOf("Problem: Campus resource bottlenecks. | Solution: Real-time edge analytics platform.")
        }
        var githubAndDemo by remember {
            mutableStateOf("GitHub: github.com/orbit-edu/project-showcase | Demo Video: demo.campusorbit.edu/live | Outcome: Winner Smart India Hackathon")
        }
        var teamMembers by remember { mutableStateOf(currentUserName) }

        AlertDialog(
            onDismissRequest = { showPublishShowcaseModal = false },
            title = { Text("Publish Completed Project to Showcase") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Project Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Project Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department(s)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = technologies,
                        onValueChange = { technologies = it },
                        label = { Text("Technologies Used") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = problemAndSolution,
                        onValueChange = { problemAndSolution = it },
                        label = { Text("Problem Statement & Solution Overview") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = githubAndDemo,
                        onValueChange = { githubAndDemo = it },
                        label = { Text("GitHub Repo Link, Demo Video Link & Outcomes") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teamMembers,
                        onValueChange = { teamMembers = it },
                        label = { Text("Team Members") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateItem(
                                CampusModuleType.COLLAB_SHOWCASE,
                                "SHW-2026-${(100..999).random()}",
                                title,
                                technologies,
                                department,
                                8L,
                                "COMPLETED",
                                category,
                                9.8,
                                4L,
                                "$problemAndSolution | $githubAndDemo",
                                teamMembers
                            )
                            showPublishShowcaseModal = false
                        }
                    }
                ) {
                    Text("Publish to Showcase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishShowcaseModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FacultyMentorshipTabContent(
    mentorships: List<CampusModuleItem>,
    facultyList: List<FacultyRecord>,
    activeRole: OrbitRole,
    currentUserName: String,
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
    onUpdateItemStatus: (item: CampusModuleItem, newStatus: String, updatedMeta: String) -> Unit
) {
    var showRequestMentorModal by remember { mutableStateOf(false) }
    var reviewingItem by remember { mutableStateOf<CampusModuleItem?>(null) }

    val canApproveMentorship = activeRole == OrbitRole.FACULTY ||
        activeRole == OrbitRole.COLLEGE_ADMIN ||
        activeRole == OrbitRole.SUPER_ADMIN

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Faculty Mentorship & Milestone Review (${mentorships.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect student teams with faculty advisors for technical guidance & milestone sign-off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showRequestMentorModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_request_mentorship_button")
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Mentor")
                }
            }

            mentorships.forEach { item ->
                val statusColor = when (item.status) {
                    "ACCEPTED" -> OrbitEmerald
                    "REJECTED" -> OrbitRose
                    else -> OrbitAmber
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgeChip(text = item.code, color = OrbitCyanAccent)
                                BadgeChip(text = item.status, color = statusColor)
                                BadgeChip(text = item.category, color = OrbitPurple)
                            }
                            Text(
                                text = item.department,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (canApproveMentorship && item.status == "PENDING") {
                                Button(
                                    onClick = {
                                        onUpdateItemStatus(
                                            item,
                                            "ACCEPTED",
                                            "${item.metadataText} | Approved by ${currentUserName}: Bi-weekly milestone review scheduled."
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Accept Mentorship")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onUpdateItemStatus(
                                            item,
                                            "REJECTED",
                                            "${item.metadataText} | Declined due to current research lab capacity."
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reject")
                                }
                            }
                            OutlinedButton(
                                onClick = { reviewingItem = item },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Provide Feedback / Review Milestone")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRequestMentorModal) {
        val defaultMentor = facultyList.firstOrNull()?.fullName ?: "Dr. Elena Rostova"
        var mentorName by remember { mutableStateOf(defaultMentor) }
        var teamAndProject by remember { mutableStateOf("Team AuraGrid • Smart Microgrid Controller") }
        var department by remember { mutableStateOf("Computer Science & Engineering") }
        var domain by remember { mutableStateOf("Artificial Intelligence and Machine Learning") }
        var requestNotes by remember {
            mutableStateOf("Requesting faculty mentorship for architecture validation, research paper guidance, and mid-term milestone review.")
        }

        AlertDialog(
            onDismissRequest = { showRequestMentorModal = false },
            title = { Text("Request Faculty Project Mentorship") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mentorName,
                        onValueChange = { mentorName = it },
                        label = { Text("Faculty Mentor Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teamAndProject,
                        onValueChange = { teamAndProject = it },
                        label = { Text("Project Title & Team Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text("Research / Technical Domain") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = requestNotes,
                        onValueChange = { requestNotes = it },
                        label = { Text("Mentorship Objectives & Milestone Plan") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mentorName.isNotBlank() && teamAndProject.isNotBlank()) {
                            onCreateItem(
                                CampusModuleType.COLLAB_MENTORSHIP,
                                "MNT-2026-${(100..999).random()}",
                                "Mentorship: $teamAndProject",
                                "Mentor: $mentorName • Lead: $currentUserName",
                                department,
                                6L,
                                "PENDING",
                                domain,
                                0.0,
                                4L,
                                requestNotes,
                                mentorName
                            )
                            showRequestMentorModal = false
                        }
                    }
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestMentorModal = false }) { Text("Cancel") }
            }
        )
    }

    reviewingItem?.let { item ->
        var feedbackText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reviewingItem = null },
            title = { Text("Faculty Feedback & Milestone Review") },
            text = {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Enter milestone evaluation & technical recommendations") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            val updated = "${item.metadataText} | Faculty Review ($currentUserName): ${feedbackText.trim()}"
                            onUpdateItemStatus(item, "ACCEPTED", updated)
                            reviewingItem = null
                        }
                    }
                ) {
                    Text("Save Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewingItem = null }) { Text("Cancel") }
            }
        )
    }
}
