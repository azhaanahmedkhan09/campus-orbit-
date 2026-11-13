package com.example.ui.modules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Workspaces
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CampusModuleItem
import com.example.data.CampusModuleType
import com.example.data.FacultyRecord
import com.example.data.OrbitRole
import com.example.ui.theme.OrbitAmber
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitCyanSoft
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitNavyDeep
import com.example.ui.theme.OrbitPurple
import com.example.ui.theme.OrbitRose
import com.example.ui.theme.OrbitRoyalBlue
import com.example.ui.theme.OrbitRoyalBlueBright
import java.util.Locale

enum class CollabSubTab(val label: String, val icon: ImageVector) {
    OVERVIEW("1. Overview", Icons.Default.Dashboard),
    DISCOVER_PROJECTS("2. Discover Projects", Icons.Default.Explore),
    MY_COLLABORATIONS("3. My Collaborations", Icons.Default.Handshake),
    FIND_COLLABORATORS("4. Find Collaborators", Icons.Default.PersonSearch),
    TEAM_WORKSPACE("5. Team Workspace", Icons.Default.Groups),
    TASKS_MILESTONES("6. Tasks & Milestones", Icons.AutoMirrored.Filled.Assignment),
    DISCUSSIONS("7. Discussions", Icons.AutoMirrored.Filled.Chat),
    PROJECT_SHOWCASE("8. Project Showcase", Icons.Default.EmojiEvents),
    FACULTY_MENTORSHIP("9. Faculty Mentorship", Icons.Default.School)
}

val COLLAB_PROJECT_CATEGORIES = listOf(
    "Artificial Intelligence and Machine Learning",
    "Internet of Things",
    "Web Development",
    "Mobile Application Development",
    "Robotics",
    "Cloud Computing",
    "Cybersecurity",
    "Data Science",
    "Electronics and Embedded Systems",
    "Social Innovation"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollaborationHubSection(
    activeRole: OrbitRole,
    currentUserName: String,
    currentStudentCode: String,
    campusItems: List<CampusModuleItem>,
    facultyList: List<FacultyRecord>,
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
    onUpdateItemProgressAndStatus: (item: CampusModuleItem, newStatus: String, newProgress: Double, newMeta: String) -> Unit,
    onRequestJoinOrInvite: (targetTitle: String, department: String, roleSkill: String, requestType: String, message: String) -> Unit,
    onUpsertCollabProfile: (
        studentId: String,
        fullName: String,
        department: String,
        semester: Long,
        preferredDomain: String,
        technicalInterests: String,
        projectsCompleted: Double,
        bioAndExperience: String
    ) -> Unit,
    onDeleteItem: (id: String, title: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(CollabSubTab.OVERVIEW) }

    val profiles = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_PROFILE.name } }
    val projects = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_PROJECT.name } }
    val teams = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_TEAM.name } }
    val requests = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_REQUEST.name } }
    val tasks = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_TASK.name } }
    val discussions = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_DISCUSSION.name } }
    val showcases = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_SHOWCASE.name } }
    val mentorships = remember(campusItems) { campusItems.filter { it.moduleType == CampusModuleType.COLLAB_MENTORSHIP.name } }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Campus Collaboration Hub Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(OrbitNavyDeep, OrbitRoyalBlue, OrbitPurple)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgeChip(text = "CAMPUS COLLABORATION HUB • INTERDISCIPLINARY R&D", color = OrbitCyanSoft)
                        BadgeChip(text = activeRole.label, color = OrbitEmerald)
                    }
                    Text(
                        text = "Welcome, $currentUserName — Connect, Innovate & Build Together",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Discover interdisciplinary projects across 10 technical domains, form cross-department teams, track milestones, showcase outcomes & connect with faculty mentors.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // 9 Sub-Navigation Tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CollabSubTab.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(tab.label, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("collab_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        when (selectedTab) {
            CollabSubTab.OVERVIEW -> CollabOverviewAndAnalyticsContent(
                projects = projects,
                profiles = profiles,
                teams = teams,
                requests = requests,
                tasks = tasks,
                showcases = showcases,
                onNavigateSubTab = { selectedTab = it }
            )

            CollabSubTab.DISCOVER_PROJECTS -> DiscoverProjectsTabContent(
                projects = projects,
                currentUserName = currentUserName,
                onCreateItem = onCreateItem,
                onRequestJoinProject = { project, roleSkill, pitch ->
                    onRequestJoinOrInvite(
                        "Join Request: ${project.title}",
                        project.department,
                        roleSkill,
                        "JOIN REQUEST",
                        pitch
                    )
                }
            )

            CollabSubTab.MY_COLLABORATIONS -> MyCollaborationsTabContent(
                requests = requests,
                projects = projects,
                currentUserName = currentUserName,
                onSendNewRequestOrInvite = onRequestJoinOrInvite,
                onUpdateRequestStatus = onUpdateItemStatus
            )

            CollabSubTab.FIND_COLLABORATORS -> FindCollaboratorsTabContent(
                profiles = profiles,
                currentUserName = currentUserName,
                currentStudentCode = currentStudentCode,
                onUpsertCollabProfile = onUpsertCollabProfile,
                onInviteCollaborator = { profile, teamName, roleOffered ->
                    onRequestJoinOrInvite(
                        "Team Invite: ${profile.title} -> $teamName",
                        profile.department,
                        roleOffered,
                        "TEAM INVITATION",
                        "Invited ${profile.title} (${profile.code}) to join $teamName as $roleOffered."
                    )
                }
            )

            CollabSubTab.TEAM_WORKSPACE -> TeamWorkspaceTabContent(
                teams = teams,
                tasks = tasks,
                currentUserName = currentUserName,
                onCreateItem = onCreateItem,
                onUpdateItemProgressAndStatus = onUpdateItemProgressAndStatus,
                onDeleteItem = onDeleteItem
            )

            CollabSubTab.TASKS_MILESTONES -> TasksAndMilestonesTabContent(
                tasks = tasks,
                currentUserName = currentUserName,
                onCreateItem = onCreateItem,
                onUpdateItemStatus = onUpdateItemStatus,
                onDeleteItem = onDeleteItem
            )

            CollabSubTab.DISCUSSIONS -> DiscussionsTabContent(
                discussions = discussions,
                currentUserName = currentUserName,
                activeRole = activeRole,
                onCreateItem = onCreateItem,
                onUpdateItemStatus = onUpdateItemStatus
            )

            CollabSubTab.PROJECT_SHOWCASE -> ProjectShowcaseTabContent(
                showcases = showcases,
                currentUserName = currentUserName,
                onCreateItem = onCreateItem
            )

            CollabSubTab.FACULTY_MENTORSHIP -> FacultyMentorshipTabContent(
                mentorships = mentorships,
                facultyList = facultyList,
                activeRole = activeRole,
                currentUserName = currentUserName,
                onCreateItem = onCreateItem,
                onUpdateItemStatus = onUpdateItemStatus
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollabOverviewAndAnalyticsContent(
    projects: List<CampusModuleItem>,
    profiles: List<CampusModuleItem>,
    teams: List<CampusModuleItem>,
    requests: List<CampusModuleItem>,
    tasks: List<CampusModuleItem>,
    showcases: List<CampusModuleItem>,
    onNavigateSubTab: (CollabSubTab) -> Unit
) {
    val activeProjectsCount = projects.count { it.status == "IN_PROGRESS" || it.status == "PLANNING" || it.status == "UNDER_REVIEW" }
    val completedProjectsCount = showcases.size + projects.count { it.status == "COMPLETED" }
    val pendingRequestsCount = requests.count { it.status == "PENDING" }
    val upcomingTasks = tasks.filter { it.status != "COMPLETED" }

    // KPI Cards Grid
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2
    ) {
        CollabKpiCard(
            modifier = Modifier.weight(1f),
            title = "Active Projects",
            value = "$activeProjectsCount",
            subtitle = "${projects.size} Total Published Ideas",
            color = OrbitRoyalBlueBright,
            onClick = { onNavigateSubTab(CollabSubTab.DISCOVER_PROJECTS) }
        )
        CollabKpiCard(
            modifier = Modifier.weight(1f),
            title = "Collaborators",
            value = "${profiles.size}",
            subtitle = "Interdisciplinary Directory",
            color = OrbitCyanAccent,
            onClick = { onNavigateSubTab(CollabSubTab.FIND_COLLABORATORS) }
        )
        CollabKpiCard(
            modifier = Modifier.weight(1f),
            title = "Active Teams",
            value = "${teams.size}",
            subtitle = "$completedProjectsCount Completed & Showcased",
            color = OrbitEmerald,
            onClick = { onNavigateSubTab(CollabSubTab.TEAM_WORKSPACE) }
        )
        CollabKpiCard(
            modifier = Modifier.weight(1f),
            title = "Pending Requests",
            value = "$pendingRequestsCount",
            subtitle = "${ upcomingTasks.size } Open Tasks & Deadlines",
            color = OrbitAmber,
            onClick = { onNavigateSubTab(CollabSubTab.MY_COLLABORATIONS) }
        )
    }

    // Quick Action Launchpad
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Quick Collaboration Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateSubTab(CollabSubTab.DISCOVER_PROJECTS) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Explore & Publish Projects")
                }
                OutlinedButton(
                    onClick = { onNavigateSubTab(CollabSubTab.TEAM_WORKSPACE) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Team Workspaces")
                }
                OutlinedButton(
                    onClick = { onNavigateSubTab(CollabSubTab.FIND_COLLABORATORS) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Find Collaborators")
                }
                OutlinedButton(
                    onClick = { onNavigateSubTab(CollabSubTab.PROJECT_SHOWCASE) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Project Showcase")
                }
            }
        }
    }

    // Collaboration Analytics & Charts (Section 3 of Prompt)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = OrbitCyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Collaboration Analytics: Department & Category Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Department-wise Collaboration Bar Visualization
            val combinedItems = projects + profiles + showcases
            val deptCounts = combinedItems.groupBy { it.department.take(28) }.mapValues { it.value.size }
            val maxDeptCount = (deptCounts.values.maxOrNull() ?: 1).coerceAtLeast(1)

            Text(
                text = "Department-Wise Collaboration Activity",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            deptCounts.entries.forEachIndexed { idx, (dept, count) ->
                val barColor = when (idx % 4) {
                    0 -> OrbitRoyalBlueBright
                    1 -> OrbitCyanAccent
                    2 -> OrbitEmerald
                    else -> OrbitPurple
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = dept, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "$count active initiatives", style = MaterialTheme.typography.labelMedium, color = barColor)
                    }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    ) {
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.18f),
                            size = size,
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = barColor,
                            size = Size(width = size.width * (count.toFloat() / maxDeptCount.toFloat()).coerceIn(0.1f, 1f), height = size.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Popular Project Categories Breakdown
            Text(
                text = "Popular Innovation Categories",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            val categoryCounts = (projects + teams + showcases).groupBy { it.category }.mapValues { it.value.size }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryCounts.forEach { (cat, cnt) ->
                    BadgeChip(text = "$cat ($cnt)", color = OrbitPurple)
                }
            }
        }
    }

    // Upcoming Deadlines & Recommended Projects Preview
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Recommended Interdisciplinary Projects & Upcoming Deadlines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            projects.take(2).forEach { proj ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateSubTab(CollabSubTab.DISCOVER_PROJECTS) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BadgeChip(text = proj.category, color = OrbitCyanAccent)
                            BadgeChip(text = proj.status, color = OrbitEmerald)
                        }
                        Text(text = proj.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(text = proj.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(text = proj.metadataText, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            upcomingTasks.take(2).forEach { tsk ->
                Surface(
                    color = OrbitAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateSubTab(CollabSubTab.TASKS_MILESTONES) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Upcoming Milestone: ${tsk.title}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(text = tsk.metadataText, style = MaterialTheme.typography.bodySmall)
                        }
                        BadgeChip(text = tsk.category, color = OrbitAmber)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoverProjectsTabContent(
    projects: List<CampusModuleItem>,
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
    onRequestJoinProject: (project: CampusModuleItem, roleSkill: String, pitch: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedDepartment by remember { mutableStateOf("ALL") }
    var selectedTechnology by remember { mutableStateOf("") }
    var showPublishProjectModal by remember { mutableStateOf(false) }
    var joiningProject by remember { mutableStateOf<CampusModuleItem?>(null) }

    val departments = listOf(
        "ALL",
        "Computer Science & Engineering",
        "Electronics & Communication",
        "Mechanical & Robotics Engineering"
    )

    val filteredProjects = projects.filter { proj ->
        val matchesTitle = searchQuery.isBlank() ||
            proj.title.contains(searchQuery, ignoreCase = true) ||
            proj.metadataText.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "ALL" || proj.category.equals(selectedCategory, ignoreCase = true)
        val matchesDept = selectedDepartment == "ALL" || proj.department.equals(selectedDepartment, ignoreCase = true)
        val matchesTech = selectedTechnology.isBlank() || proj.subtitle.contains(selectedTechnology, ignoreCase = true)
        matchesTitle && matchesCategory && matchesDept && matchesTech
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
                        text = "Project Discovery Board (${filteredProjects.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explore open student projects, filter by category/tech/department, or publish a new idea",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showPublishProjectModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_publish_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Idea")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Project Title...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = selectedTechnology,
                    onValueChange = { selectedTechnology = it },
                    label = { Text("Filter by Tech (e.g. ROS2, Kotlin)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(text = "Filter by Department:", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                departments.forEach { dept ->
                    FilterChip(
                        selected = selectedDepartment == dept,
                        onClick = { selectedDepartment = dept },
                        label = { Text(if (dept == "ALL") "All Departments" else dept) }
                    )
                }
            }

            Text(text = "Filter by Project Category:", style = MaterialTheme.typography.labelMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == "ALL",
                    onClick = { selectedCategory = "ALL" },
                    label = { Text("All Categories") }
                )
                COLLAB_PROJECT_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            filteredProjects.forEach { proj ->
                val statusColor = when (proj.status) {
                    "COMPLETED" -> OrbitEmerald
                    "IN_PROGRESS" -> OrbitRoyalBlueBright
                    "UNDER_REVIEW" -> OrbitPurple
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
                                BadgeChip(text = proj.code, color = OrbitCyanAccent)
                                BadgeChip(text = proj.status.replace('_', ' '), color = statusColor)
                            }
                            BadgeChip(text = proj.category, color = OrbitPurple)
                        }

                        Text(
                            text = proj.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = proj.subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Host Dept: ${proj.department} • Target Semester: Sem ${proj.semester} • Team Size: ${proj.secondaryMetric} Members • Lead: ${proj.ownerName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = proj.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { joiningProject = proj },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("request_join_${proj.code}")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Request to Join Project")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPublishProjectModal) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(COLLAB_PROJECT_CATEGORIES.first()) }
        var department by remember { mutableStateOf("Computer Science & Engineering") }
        var technologies by remember { mutableStateOf("Kotlin, Python, TensorFlow, Cloud Firestore") }
        var requiredMembers by remember { mutableStateOf("5") }
        var requiredRolesAndDesc by remember {
            mutableStateOf("Required Members: 1 ML Engineer, 1 IoT Firmware Dev, 1 Android UI Architect | Objective: Build real-time campus sustainability and safety platform.")
        }

        AlertDialog(
            onDismissRequest = { showPublishProjectModal = false },
            title = { Text("Publish New Interdisciplinary Project Idea") },
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
                        label = { Text("Project Category (AI/ML, IoT, Robotics, Web, Mobile...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Primary Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = technologies,
                        onValueChange = { technologies = it },
                        label = { Text("Technologies & Stack") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = requiredMembers,
                        onValueChange = { requiredMembers = it },
                        label = { Text("Required Team Size") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = requiredRolesAndDesc,
                        onValueChange = { requiredRolesAndDesc = it },
                        label = { Text("Project Description & Required Team Roles") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val size = requiredMembers.toLongOrNull() ?: 5L
                            onCreateItem(
                                CampusModuleType.COLLAB_PROJECT,
                                "PRJ-2026-${(100..999).random()}",
                                title,
                                "Tech: $technologies • Team: 1/$size",
                                department,
                                6L,
                                "PLANNING",
                                category,
                                15.0,
                                size,
                                requiredRolesAndDesc,
                                currentUserName
                            )
                            showPublishProjectModal = false
                        }
                    }
                ) {
                    Text("Publish Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishProjectModal = false }) { Text("Cancel") }
            }
        )
    }

    joiningProject?.let { proj ->
        var roleOffered by remember { mutableStateOf("Full-Stack / ML Collaborator") }
        var pitchNote by remember {
            mutableStateOf("I would love to contribute to '${proj.title}'. I have hands-on experience with ${proj.subtitle.removePrefix("Tech: ")}.")
        }

        AlertDialog(
            onDismissRequest = { joiningProject = null },
            title = { Text("Request to Join • ${proj.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roleOffered,
                        onValueChange = { roleOffered = it },
                        label = { Text("Your Proposed Team Role / Technical Specialty") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pitchNote,
                        onValueChange = { pitchNote = it },
                        label = { Text("Collaboration Pitch & Experience Note") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestJoinProject(proj, roleOffered, pitchNote)
                        joiningProject = null
                    }
                ) {
                    Text("Send Join Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { joiningProject = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MyCollaborationsTabContent(
    requests: List<CampusModuleItem>,
    projects: List<CampusModuleItem>,
    currentUserName: String,
    onSendNewRequestOrInvite: (targetTitle: String, department: String, roleSkill: String, requestType: String, message: String) -> Unit,
    onUpdateRequestStatus: (item: CampusModuleItem, newStatus: String, updatedMeta: String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") }
    var showNewRequestModal by remember { mutableStateOf(false) }

    val statuses = listOf("ALL", "PENDING", "ACCEPTED", "REJECTED", "WITHDRAWN")
    val filteredRequests = requests.filter { statusFilter == "ALL" || it.status == statusFilter }

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
                        text = "Collaboration Requests & Invitations (${filteredRequests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage incoming join requests, team invitations, withdrawals & request history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showNewRequestModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_new_request_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Request / Invite")
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { statusFilter = st },
                        label = { Text(st) }
                    )
                }
            }

            filteredRequests.forEach { req ->
                val statusColor = when (req.status) {
                    "ACCEPTED" -> OrbitEmerald
                    "REJECTED" -> OrbitRose
                    "WITHDRAWN" -> OrbitPurple
                    else -> OrbitAmber
                }

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
                                BadgeChip(text = req.code, color = OrbitCyanAccent)
                                BadgeChip(text = req.category, color = OrbitRoyalBlueBright)
                                BadgeChip(text = req.status, color = statusColor)
                            }
                            Text(
                                text = req.department,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = req.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = req.subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = req.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (req.status == "PENDING") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onUpdateRequestStatus(
                                            req,
                                            "ACCEPTED",
                                            "${req.metadataText} | Status: ACCEPTED by $currentUserName"
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Accept")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onUpdateRequestStatus(
                                            req,
                                            "REJECTED",
                                            "${req.metadataText} | Status: REJECTED by $currentUserName"
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reject")
                                }
                                TextButton(
                                    onClick = {
                                        onUpdateRequestStatus(
                                            req,
                                            "WITHDRAWN",
                                            "${req.metadataText} | Status: WITHDRAWN by requester"
                                        )
                                    }
                                ) {
                                    Text("Withdraw Request")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewRequestModal) {
        val defaultTarget = projects.firstOrNull()?.title ?: "Smart Campus Energy Microgrid"
        var targetTitle by remember { mutableStateOf("Join Request: $defaultTarget") }
        var department by remember { mutableStateOf("Computer Science & Engineering") }
        var roleSkill by remember { mutableStateOf("Cloud & Edge Systems Engineer") }
        var requestType by remember { mutableStateOf("JOIN REQUEST") }
        var pitch by remember { mutableStateOf("Requesting to collaborate on milestone deliverables and system integration.") }

        AlertDialog(
            onDismissRequest = { showNewRequestModal = false },
            title = { Text("Send Collaboration Request or Team Invitation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetTitle,
                        onValueChange = { targetTitle = it },
                        label = { Text("Project Title or Student Invite Target") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = requestType,
                        onValueChange = { requestType = it },
                        label = { Text("Type (JOIN REQUEST / TEAM INVITATION)") },
                        singleLine = true,
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
                        value = roleSkill,
                        onValueChange = { roleSkill = it },
                        label = { Text("Proposed Role & Technical Specialty") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pitch,
                        onValueChange = { pitch = it },
                        label = { Text("Message / Collaboration Pitch") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetTitle.isNotBlank()) {
                            onSendNewRequestOrInvite(targetTitle, department, roleSkill, requestType, pitch)
                            showNewRequestModal = false
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewRequestModal = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FindCollaboratorsTabContent(
    profiles: List<CampusModuleItem>,
    currentUserName: String,
    currentStudentCode: String,
    onUpsertCollabProfile: (
        studentId: String,
        fullName: String,
        department: String,
        semester: Long,
        preferredDomain: String,
        technicalInterests: String,
        projectsCompleted: Double,
        bioAndExperience: String
    ) -> Unit,
    onInviteCollaborator: (profile: CampusModuleItem, teamName: String, roleOffered: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("ALL") }
    var showEditProfileModal by remember { mutableStateOf(false) }
    var invitingProfile by remember { mutableStateOf<CampusModuleItem?>(null) }

    val filteredProfiles = profiles.filter { prof ->
        val matchesDept = selectedDept == "ALL" || prof.department.equals(selectedDept, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
            prof.title.contains(searchQuery, ignoreCase = true) ||
            prof.code.contains(searchQuery, ignoreCase = true) ||
            prof.subtitle.contains(searchQuery, ignoreCase = true) ||
            prof.category.contains(searchQuery, ignoreCase = true) ||
            prof.metadataText.contains(searchQuery, ignoreCase = true)
        matchesDept && matchesQuery
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
                        text = "Student Collaboration Profiles (${filteredProfiles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Search students by skills, domain interests, department & collaboration preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showEditProfileModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("collab_edit_profile_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("My Collab Profile")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name, Student ID, Technical Interests, Domain or Experience...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL",
                    "Computer Science & Engineering",
                    "Electronics & Communication",
                    "Mechanical & Robotics Engineering"
                ).forEach { dept ->
                    FilterChip(
                        selected = selectedDept == dept,
                        onClick = { selectedDept = dept },
                        label = { Text(if (dept == "ALL") "All Departments" else dept) }
                    )
                }
            }

            filteredProfiles.forEach { prof ->
                val initials = prof.title.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(OrbitRoyalBlueBright.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials.ifBlank { "ST" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OrbitCyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        text = prof.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${prof.code} • ${prof.department} • Sem ${prof.semester}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            BadgeChip(text = prof.status.replace('_', ' '), color = OrbitEmerald)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BadgeChip(text = "Preferred Domain: ${prof.category}", color = OrbitPurple)
                            BadgeChip(text = "${prof.primaryMetric.toInt()} Projects Completed", color = OrbitCyanAccent)
                        }

                        Text(
                            text = "Technical Interests: ${prof.subtitle}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = prof.metadataText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { invitingProfile = prof },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Invite to Project Team")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileModal) {
        var studentId by remember { mutableStateOf(currentStudentCode) }
        var fullName by remember { mutableStateOf(currentUserName) }
        var department by remember { mutableStateOf("Computer Science & Engineering") }
        var semester by remember { mutableStateOf("6") }
        var preferredDomain by remember { mutableStateOf("Artificial Intelligence and Machine Learning") }
        var technicalInterests by remember { mutableStateOf("Kotlin, Jetpack Compose, Python, TensorFlow, Cloud Firestore") }
        var areasOfInterest by remember { mutableStateOf("Smart Campus Automation, Distributed Edge AI, Sustainable FinTech") }
        var previousExperience by remember { mutableStateOf("Lead Developer on Campus Orbit & Smart Attendance QR System") }
        var collabPrefs by remember { mutableStateOf("Open to interdisciplinary hackathons, capstones & faculty-mentored research") }
        var shortIntro by remember { mutableStateOf("Passionate builder looking to team up with ECE and Mechanical innovators.") }

        AlertDialog(
            onDismissRequest = { showEditProfileModal = false },
            title = { Text("Create / Update Student Collaboration Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("Student ID") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = semester,
                            onValueChange = { semester = it },
                            label = { Text("Semester") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = preferredDomain,
                        onValueChange = { preferredDomain = it },
                        label = { Text("Preferred Project Domain") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = technicalInterests,
                        onValueChange = { technicalInterests = it },
                        label = { Text("Technical Interests & Stack") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = areasOfInterest,
                        onValueChange = { areasOfInterest = it },
                        label = { Text("Areas of Interest") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = previousExperience,
                        onValueChange = { previousExperience = it },
                        label = { Text("Previous Project Experience") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = collabPrefs,
                        onValueChange = { collabPrefs = it },
                        label = { Text("Collaboration Preferences") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shortIntro,
                        onValueChange = { shortIntro = it },
                        label = { Text("Short Personal Introduction") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isNotBlank() && studentId.isNotBlank()) {
                            val combinedMeta = "Areas of Interest: $areasOfInterest | Experience: $previousExperience | Preferences: $collabPrefs | Bio: $shortIntro"
                            onUpsertCollabProfile(
                                studentId,
                                fullName,
                                department,
                                semester.toLongOrNull() ?: 6L,
                                preferredDomain,
                                technicalInterests,
                                4.0,
                                combinedMeta
                            )
                            showEditProfileModal = false
                        }
                    }
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileModal = false }) { Text("Cancel") }
            }
        )
    }

    invitingProfile?.let { prof ->
        var teamName by remember { mutableStateOf("Team AuraGrid") }
        var roleOffered by remember { mutableStateOf("Embedded / AI Specialist") }

        AlertDialog(
            onDismissRequest = { invitingProfile = null },
            title = { Text("Invite ${prof.title} to Team") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Select / Enter Project Team Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roleOffered,
                        onValueChange = { roleOffered = it },
                        label = { Text("Team Role Offered") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onInviteCollaborator(prof, teamName, roleOffered)
                        invitingProfile = null
                    }
                ) {
                    Text("Send Invitation")
                }
            },
            dismissButton = {
                TextButton(onClick = { invitingProfile = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CollabKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
