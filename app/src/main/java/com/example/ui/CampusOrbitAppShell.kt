package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CampusModuleType
import com.example.data.OrbitRole
import com.example.ui.auth.signOut
import com.example.ui.modules.AcademicsAndTimetableSection
import com.example.ui.modules.BadgeChip
import com.example.ui.modules.CampusLifeAndCareerSection
import com.example.ui.modules.CollaborationHubSection
import com.example.ui.modules.ExamsAndAssignmentsSection
import com.example.ui.modules.FacultyManagementSection
import com.example.ui.modules.FinanceAndGrievancesSection
import com.example.ui.modules.OverviewAndAnalyticsSection
import com.example.ui.modules.ParentGuardianPortalSection
import com.example.ui.modules.SmartAttendanceSection
import com.example.ui.modules.StudentManagementSection
import com.example.ui.modules.SuperAdminGovernanceSection
import com.example.ui.theme.OrbitCyanAccent
import com.example.ui.theme.OrbitEmerald
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CampusOrbitAppShell(
    viewModel: CampusOrbitViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val activeQrToken by viewModel.activeQrToken.collectAsStateWithLifecycle()
    val activeQrSubject by viewModel.activeQrSubject.collectAsStateWithLifecycle()

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val facultyState by viewModel.facultyState.collectAsStateWithLifecycle()
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val grievancesState by viewModel.grievancesState.collectAsStateWithLifecycle()
    val campusItemsState by viewModel.campusItemsState.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data
    val activeRole = OrbitRole.fromKey(profile?.activeRole ?: OrbitRole.COLLEGE_ADMIN.name)
    val institutionName = profile?.institutionName ?: "Orbit Institute of Technology"
    val linkedStudentCode = profile?.linkedStudentCode ?: "STU-2026-001"

    val students = (studentsState as? UiState.Success)?.data ?: emptyList()
    val faculty = (facultyState as? UiState.Success)?.data ?: emptyList()
    val attendance = (attendanceState as? UiState.Success)?.data ?: emptyList()
    val grievances = (grievancesState as? UiState.Success)?.data ?: emptyList()
    val campusItems = (campusItemsState as? UiState.Success)?.data ?: emptyList()

    var showGlobalSearchModal by remember { mutableStateOf(false) }
    var showRoleSwitcherModal by remember { mutableStateOf(false) }

    // Mandatory BackHandler for custom section navigation
    BackHandler(enabled = selectedSection != OrbitSection.OVERVIEW) {
        viewModel.selectSection(OrbitSection.OVERVIEW)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "CAMPUS ORBIT",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = institutionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BadgeChip(text = activeRole.label, color = OrbitCyanAccent)

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    OrbitSection.entries.forEach { section ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = sectionIcon(section),
                                    contentDescription = section.title
                                )
                            },
                            label = {
                                Column {
                                    Text(section.title, fontWeight = FontWeight.SemiBold)
                                }
                            },
                            selected = selectedSection == section,
                            onClick = {
                                viewModel.selectSection(section)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("drawer_nav_${section.name.lowercase()}")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out") },
                        label = { Text("Sign Out (${profile?.email ?: "Session"})") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            signOut(
                                credentialManager = credentialManager,
                                scope = scope,
                                onSignOutComplete = onSignedOut
                            )
                        },
                        modifier = Modifier.testTag("drawer_sign_out_button")
                    )
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Drawer")
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = selectedSection.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (viewModel.isDemoMode) {
                                    "DEMO: ${profile?.fullName ?: activeRole.demoName} • ${activeRole.label}"
                                } else {
                                    "${activeRole.label} • ${students.size} Scholars"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Global Command Palette Search Button (Ctrl+K equivalent)
                        IconButton(
                            onClick = { showGlobalSearchModal = true },
                            modifier = Modifier.testTag("global_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Global Command Palette Search")
                        }
                        // Role Switcher Button
                        IconButton(
                            onClick = { showRoleSwitcherModal = true },
                            modifier = Modifier.testTag("role_switcher_button")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Switch Active Role")
                        }
                        // Theme Toggle Button
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark or Light Mode"
                            )
                        }
                        // Direct Sign Out Button
                        IconButton(
                            onClick = {
                                signOut(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = onSignedOut
                                )
                            },
                            modifier = Modifier.testTag("top_sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                val primaryTabs = listOf(
                    OrbitSection.OVERVIEW,
                    OrbitSection.STUDENTS,
                    OrbitSection.COLLABORATION_HUB,
                    OrbitSection.ATTENDANCE_QR,
                    OrbitSection.EXAMS_ASSIGNMENTS
                )
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    primaryTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedSection == tab,
                            onClick = { viewModel.selectSection(tab) },
                            icon = {
                                Icon(
                                    imageVector = sectionIcon(tab),
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = when (tab) {
                                        OrbitSection.OVERVIEW -> "Orbit"
                                        OrbitSection.STUDENTS -> "Students"
                                        OrbitSection.COLLABORATION_HUB -> "Collab"
                                        OrbitSection.ATTENDANCE_QR -> "QR Att"
                                        else -> "Exams"
                                    },
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Real-Time Status Feedback Toast Banner
                    AnimatedVisibility(visible = statusBanner != null) {
                        Surface(
                            color = OrbitEmerald.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusBanner ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OrbitEmerald,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.clearBanner() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = OrbitEmerald)
                                }
                            }
                        }
                    }

                    // Active Section Content
                    when (selectedSection) {
                        OrbitSection.OVERVIEW -> OverviewAndAnalyticsSection(
                            activeRole = activeRole,
                            institutionName = institutionName,
                            students = students,
                            faculty = faculty,
                            attendance = attendance,
                            grievances = grievances,
                            campusItems = campusItems,
                            onNavigateSection = { viewModel.selectSection(it) },
                            onPublishNotice = { code, title, dept, category, body ->
                                viewModel.addCampusModuleItem(
                                    moduleType = CampusModuleType.NOTICE,
                                    code = code,
                                    title = title,
                                    subtitle = "$category • $dept",
                                    department = dept,
                                    semester = 0L,
                                    status = "PUBLISHED",
                                    category = category,
                                    primaryMetric = 1.0,
                                    secondaryMetric = students.size.toLong(),
                                    metadataText = body,
                                    ownerName = profile?.fullName ?: "Campus Administration"
                                )
                            }
                        )

                        OrbitSection.STUDENTS -> StudentManagementSection(
                            students = students,
                            onAddStudent = viewModel::addStudent,
                            onImportCsv = viewModel::importStudentsFromCsv,
                            onUpdateFeeOrAttendance = viewModel::updateStudentFeeOrAttendance,
                            onDeleteStudent = viewModel::removeStudent
                        )

                        OrbitSection.FACULTY -> FacultyManagementSection(
                            facultyList = faculty,
                            onAddFaculty = viewModel::addFaculty,
                            onDeleteFaculty = viewModel::removeFaculty
                        )

                        OrbitSection.ACADEMICS -> AcademicsAndTimetableSection(
                            campusItems = campusItems,
                            onCreateItem = viewModel::addCampusModuleItem,
                            onDeleteItem = viewModel::deleteCampusItem
                        )

                        OrbitSection.ATTENDANCE_QR -> SmartAttendanceSection(
                            activeQrToken = activeQrToken,
                            activeQrSubject = activeQrSubject,
                            students = students,
                            attendanceList = attendance,
                            onGenerateNewQr = viewModel::generateNewQrAttendanceSession,
                            onRecordAttendance = viewModel::recordStudentAttendance,
                            onToggleStatus = viewModel::toggleAttendanceStatus
                        )

                        OrbitSection.EXAMS_ASSIGNMENTS -> ExamsAndAssignmentsSection(
                            students = students,
                            campusItems = campusItems,
                            onCreateItem = viewModel::addCampusModuleItem,
                            onUpdateItemStatus = viewModel::updateCampusItemStatus,
                            onDeleteItem = viewModel::deleteCampusItem
                        )

                        OrbitSection.FINANCE_GRIEVANCES -> FinanceAndGrievancesSection(
                            grievances = grievances,
                            campusItems = campusItems,
                            onFileGrievance = viewModel::fileGrievance,
                            onAdvanceGrievance = viewModel::advanceGrievanceStatus,
                            onCreateFeeInvoice = viewModel::addCampusModuleItem,
                            onUpdateInvoiceStatus = viewModel::updateCampusItemStatus
                        )

                        OrbitSection.COLLABORATION_HUB -> CollaborationHubSection(
                            activeRole = activeRole,
                            currentUserName = profile?.fullName ?: activeRole.demoName,
                            currentStudentCode = linkedStudentCode,
                            campusItems = campusItems,
                            facultyList = faculty,
                            onCreateItem = viewModel::addCampusModuleItem,
                            onUpdateItemStatus = viewModel::updateCampusItemStatus,
                            onUpdateItemProgressAndStatus = viewModel::updateCampusItemProgressAndStatus,
                            onRequestJoinOrInvite = { targetTitle, dept, roleSkill, reqType, msg ->
                                viewModel.sendCollaborationRequestOrInvite(
                                    targetTitle = targetTitle,
                                    department = dept,
                                    roleSkill = roleSkill,
                                    requestType = reqType,
                                    message = msg,
                                    requesterName = profile?.fullName ?: activeRole.demoName
                                )
                            },
                            onUpsertCollabProfile = viewModel::upsertStudentCollaborationProfile,
                            onDeleteItem = viewModel::deleteCampusItem
                        )

                        OrbitSection.CAMPUS_LIFE -> CampusLifeAndCareerSection(
                            campusItems = campusItems,
                            onCreateItem = viewModel::addCampusModuleItem,
                            onUpdateItemStatus = viewModel::updateCampusItemStatus,
                            onDeleteItem = viewModel::deleteCampusItem
                        )

                        OrbitSection.PARENT_PORTAL -> ParentGuardianPortalSection(
                            linkedStudentCode = linkedStudentCode,
                            students = students,
                            campusItems = campusItems,
                            onUpdateLinkedStudent = { newCode ->
                                viewModel.updateInstitutionConfig(
                                    institutionName = institutionName,
                                    linkedStudentCode = newCode,
                                    activeRole = activeRole
                                )
                            }
                        )

                        OrbitSection.SUPER_ADMIN_GOVERNANCE -> SuperAdminGovernanceSection(
                            activeRole = activeRole,
                            institutionName = institutionName,
                            linkedStudentCode = linkedStudentCode,
                            campusItems = campusItems,
                            onSaveConfig = viewModel::updateInstitutionConfig,
                            onCreateAuditLog = { title, detail ->
                                viewModel.addCampusModuleItem(
                                    moduleType = CampusModuleType.AUDIT_LOG,
                                    code = "AUD-${(100..999).random()}",
                                    title = title,
                                    subtitle = "Actor: ${profile?.fullName ?: "Super Admin"}",
                                    department = "Institutional Governance",
                                    semester = 0L,
                                    status = "LOGGED",
                                    category = "SECURITY AUDIT",
                                    primaryMetric = 100.0,
                                    secondaryMetric = 1L,
                                    metadataText = detail,
                                    ownerName = profile?.fullName ?: "Super Admin"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Global Command Palette Search Modal (Module 18)
    if (showGlobalSearchModal) {
        var query by remember { mutableStateOf("") }
        val results = remember(query, students, faculty, grievances, campusItems) {
            viewModel.performGlobalSearch(query)
        }

        AlertDialog(
            onDismissRequest = { showGlobalSearchModal = false },
            title = { Text("Global Command Palette Search") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search Students, Faculty, Courses, Notices, Fees...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (query.isBlank()) {
                        Text(
                            text = "Try searching 'Aarav', 'CS601', 'Rostova', 'Hackathon', or 'GRV'...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (results.isEmpty()) {
                        Text(
                            text = "No matching records found for '$query'.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            results.forEach { hit ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectSection(hit.targetSection)
                                            showGlobalSearchModal = false
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${hit.category} • ${hit.code}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = hit.targetSection.title,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                        Text(
                                            text = hit.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = hit.subtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGlobalSearchModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Multi-Role Portal Switcher Modal
    if (showRoleSwitcherModal) {
        AlertDialog(
            onDismissRequest = { showRoleSwitcherModal = false },
            title = { Text("Switch Active Institutional Role") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select any of the 5 Campus Orbit roles to experience its dedicated dashboard and permissions:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OrbitRole.entries.forEach { role ->
                        FilterChip(
                            selected = activeRole == role,
                            onClick = {
                                viewModel.switchRole(role)
                                showRoleSwitcherModal = false
                            },
                            label = {
                                Text("${role.label} • ${role.demoName} (${role.demoEmail})")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleSwitcherModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun sectionIcon(section: OrbitSection): ImageVector = when (section) {
    OrbitSection.OVERVIEW -> Icons.Default.Dashboard
    OrbitSection.STUDENTS -> Icons.Default.School
    OrbitSection.FACULTY -> Icons.Default.SupervisorAccount
    OrbitSection.ACADEMICS -> Icons.Default.Apartment
    OrbitSection.ATTENDANCE_QR -> Icons.Default.QrCode2
    OrbitSection.EXAMS_ASSIGNMENTS -> Icons.Default.EmojiEvents
    OrbitSection.FINANCE_GRIEVANCES -> Icons.Default.Payments
    OrbitSection.COLLABORATION_HUB -> Icons.Default.Groups
    OrbitSection.CAMPUS_LIFE -> Icons.Default.Workspaces
    OrbitSection.PARENT_PORTAL -> Icons.Default.FamilyRestroom
    OrbitSection.SUPER_ADMIN_GOVERNANCE -> Icons.Default.AccountBalance
}
