export type OrbitRoleKey =
  | 'SUPER_ADMIN'
  | 'COLLEGE_ADMIN'
  | 'FACULTY'
  | 'STUDENT'
  | 'PARENT';

export interface RoleMetadata {
  key: OrbitRoleKey;
  label: string;
  badge: string;
  tagline: string;
  demoName: string;
  demoEmail: string;
  demoDepartment: string;
  demoLinkedStudentCode: string;
}

export type OrbitSectionKey =
  | 'OVERVIEW'
  | 'STUDENTS'
  | 'FACULTY'
  | 'ACADEMICS'
  | 'ATTENDANCE_QR'
  | 'EXAMS_ASSIGNMENTS'
  | 'FINANCE_GRIEVANCES'
  | 'COLLABORATION_HUB'
  | 'CAMPUS_LIFE'
  | 'PARENT_PORTAL'
  | 'SUPER_ADMIN_GOVERNANCE';

export type CampusModuleTypeKey =
  | 'DEPARTMENT'
  | 'COURSE'
  | 'TIMETABLE'
  | 'EXAM_RESULT'
  | 'ASSIGNMENT'
  | 'FEE_INVOICE'
  | 'NOTICE'
  | 'FACILITY_BOOKING'
  | 'CERTIFICATE_REQUEST'
  | 'EVENT'
  | 'STUDY_GROUP'
  | 'PLACEMENT_DRIVE'
  | 'AUDIT_LOG'
  | 'COLLAB_PROFILE'
  | 'COLLAB_PROJECT'
  | 'COLLAB_TEAM'
  | 'COLLAB_REQUEST'
  | 'COLLAB_TASK'
  | 'COLLAB_DISCUSSION'
  | 'COLLAB_SHOWCASE'
  | 'COLLAB_MENTORSHIP';

export interface StudentRecord {
  id: string;
  userId: string;
  studentCode: string;
  fullName: string;
  email: string;
  department: string;
  semester: number;
  cgpa: number;
  attendancePct: number;
  feeStatus: 'PAID' | 'PENDING' | 'PARTIAL' | 'OVERDUE';
  guardianName: string;
  guardianPhone: string;
  mentorName: string;
  status: string;
}

export interface FacultyRecord {
  id: string;
  userId: string;
  facultyCode: string;
  fullName: string;
  email: string;
  department: string;
  designation: string;
  qualification: string;
  assignedSubjects: string;
  workloadHours: number;
  experienceYears: number;
}

export interface AttendanceRecord {
  id: string;
  userId: string;
  studentCode: string;
  studentName: string;
  subjectCode: string;
  subjectName: string;
  department: string;
  semester: number;
  sessionDate: string;
  status: 'PRESENT' | 'ABSENT' | 'LATE';
  method: 'QR_SCAN' | 'FACULTY_MANUAL';
  sessionToken: string;
}

export interface GrievanceRecord {
  id: string;
  userId: string;
  ticketNumber: string;
  title: string;
  category: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED';
  submittedBy: string;
  assignedTo: string;
  resolutionNotes: string;
}

export interface CampusModuleItem {
  id: string;
  userId: string;
  moduleType: CampusModuleTypeKey;
  code: string;
  title: string;
  subtitle: string;
  department: string;
  semester: number;
  status: string;
  category: string;
  primaryMetric: number;
  secondaryMetric: number;
  metadataText: string;
  ownerName: string;
}
