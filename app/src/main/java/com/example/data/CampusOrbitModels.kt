package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class OrbitRole(
    val label: String,
    val badge: String,
    val tagline: String,
    val demoName: String,
    val demoEmail: String,
    val demoSubtitle: String,
    val demoLinkedStudentCode: String
) {
    SUPER_ADMIN(
        label = "Super Administrator",
        badge = "ROOT • GOVERNANCE",
        tagline = "Full institutional governance, role security, and system audit trails",
        demoName = "Dr. Julian Vance",
        demoEmail = "julian.vance@campusorbit.edu",
        demoSubtitle = "Chancellor & Root System Administrator",
        demoLinkedStudentCode = "STU-2026-001"
    ),
    COLLEGE_ADMIN(
        label = "College Administrator",
        badge = "ADMIN • OPERATIONS",
        tagline = "Departments, students, faculty, timetables, finance, and grievances",
        demoName = "Prof. Sarah Jenkins",
        demoEmail = "sarah.jenkins@campusorbit.edu",
        demoSubtitle = "Dean of Academic & Campus Operations",
        demoLinkedStudentCode = "STU-2026-001"
    ),
    FACULTY(
        label = "Faculty & Professor",
        badge = "ACADEMIC • FACULTY",
        tagline = "QR attendance sessions, internal grading, assignments, and classes",
        demoName = "Dr. Elena Rostova",
        demoEmail = "elena.rostova@campusorbit.edu",
        demoSubtitle = "Professor & HOD • Computer Science (FAC-CSE-101)",
        demoLinkedStudentCode = "STU-2026-001"
    ),
    STUDENT(
        label = "Student Portal",
        badge = "SCHOLAR • STUDENT",
        tagline = "Attendance QR scan, SGPA/CGPA results, assignments, fees, and career hub",
        demoName = "Aarav Mehta",
        demoEmail = "aarav.mehta@campusorbit.edu",
        demoSubtitle = "B.Tech CSE • Semester 6 • Roll ID: STU-2026-001",
        demoLinkedStudentCode = "STU-2026-001"
    ),
    PARENT(
        label = "Parent / Guardian",
        badge = "GUARDIAN • OVERSIGHT",
        tagline = "Verified ward attendance monitoring, exam schedules, fees, and mentor connect",
        demoName = "Rajesh Mehta",
        demoEmail = "rajesh.mehta@campusorbit.edu",
        demoSubtitle = "Verified Guardian of Aarav Mehta (STU-2026-001)",
        demoLinkedStudentCode = "STU-2026-001"
    );

    companion object {
        fun fromKey(key: String): OrbitRole = entries.firstOrNull { it.name == key } ?: COLLEGE_ADMIN
    }
}

enum class CampusModuleType(val title: String) {
    DEPARTMENT("Departments"),
    COURSE("Courses & Subjects"),
    EXAM_RESULT("Exams & Results"),
    TIMETABLE("Smart Timetable"),
    ASSIGNMENT("Assignments & Resources"),
    FEE_INVOICE("Fees & Finance"),
    NOTICE("Notices & Broadcasts"),
    FACILITY_BOOKING("Facility Booking"),
    CERTIFICATE("Certificates & Docs"),
    EVENT("Campus Events"),
    STUDY_GROUP("Study Groups"),
    PLACEMENT("Placements & Skills"),
    AUDIT_LOG("Security Audit Logs")
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val activeRole: String = OrbitRole.COLLEGE_ADMIN.name,
    val institutionName: String = "Orbit Institute of Technology",
    val department: String = "Computer Science & Engineering",
    val linkedStudentCode: String = "STU-2026-001",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentCode: String = "",
    val fullName: String = "",
    val email: String = "",
    val department: String = "",
    val semester: Long = 1L,
    val cgpa: Double = 0.0,
    val attendancePct: Long = 100L,
    val feeStatus: String = "PAID",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val mentorName: String = "",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FacultyRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val facultyCode: String = "",
    val fullName: String = "",
    val email: String = "",
    val department: String = "",
    val designation: String = "",
    val qualification: String = "",
    val assignedSubjects: String = "",
    val workloadHours: Long = 16L,
    val experienceYears: Long = 5L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AttendanceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentCode: String = "",
    val studentName: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val department: String = "",
    val semester: Long = 1L,
    val sessionDate: String = "",
    val status: String = "PRESENT",
    val method: String = "QR_SCAN",
    val sessionToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GrievanceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ticketNumber: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val priority: String = "MEDIUM",
    val status: String = "SUBMITTED",
    val submittedBy: String = "",
    val assignedTo: String = "",
    val resolutionNotes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CampusModuleItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val moduleType: String = CampusModuleType.NOTICE.name,
    val code: String = "",
    val title: String = "",
    val subtitle: String = "",
    val department: String = "",
    val semester: Long = 0L,
    val status: String = "ACTIVE",
    val category: String = "GENERAL",
    val primaryMetric: Double = 0.0,
    val secondaryMetric: Long = 0L,
    val metadataText: String = "",
    val ownerName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
