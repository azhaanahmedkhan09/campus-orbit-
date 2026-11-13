import React, { useMemo, useState } from 'react';
import {
  AlertTriangle,
  Award,
  Bell,
  BookOpen,
  Building2,
  CheckCircle2,
  CreditCard,
  Download,
  FileSpreadsheet,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  Menu,
  QrCode,
  Search,
  ShieldCheck,
  Sparkles,
  UserCheck,
  Users,
  X,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { AcademicsAndFacultyView } from './components/AcademicsAndFacultyView';
import { CampusLifeAndGovernanceView } from './components/CampusLifeAndGovernanceView';
import { CollaborationHubView } from './components/CollaborationHubView';
import { ExamsAndFinanceView } from './components/ExamsAndFinanceView';
import {
  INITIAL_ATTENDANCE,
  INITIAL_CAMPUS_ITEMS,
  INITIAL_FACULTY,
  INITIAL_GRIEVANCES,
  INITIAL_STUDENTS,
  ROLES,
} from './seedData';
import {
  AttendanceRecord,
  CampusModuleItem,
  FacultyRecord,
  GrievanceRecord,
  OrbitRoleKey,
  OrbitSectionKey,
  StudentRecord,
} from './types';

export const App: React.FC = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [selectedRoleKey, setSelectedRoleKey] = useState<OrbitRoleKey>('STUDENT');
  const [selectedSection, setSelectedSection] = useState<OrbitSectionKey>('OVERVIEW');
  const [emailInput, setEmailInput] = useState('student.demo@campusorbit.edu');
  const [passwordInput, setPasswordInput] = useState('••••••••••••');
  const [institutionName, setInstitutionName] = useState('Orbit Institute of Technology');
  const [linkedStudentCode, setLinkedStudentCode] = useState('STU-2026-001');
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [searchModalOpen, setSearchModalOpen] = useState(false);
  const [globalQuery, setGlobalQuery] = useState('');
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  // Institutional state
  const [students, setStudents] = useState<StudentRecord[]>(INITIAL_STUDENTS);
  const [faculty, setFaculty] = useState<FacultyRecord[]>(INITIAL_FACULTY);
  const [attendance, setAttendance] = useState<AttendanceRecord[]>(INITIAL_ATTENDANCE);
  const [grievances, setGrievances] = useState<GrievanceRecord[]>(INITIAL_GRIEVANCES);
  const [campusItems, setCampusItems] = useState<CampusModuleItem[]>(INITIAL_CAMPUS_ITEMS);
  const [qrToken, setQrToken] = useState('ORB-CS601-20260513-LIVE');

  // Form states
  const [newStuName, setNewStuName] = useState('');
  const [newStuCode, setNewStuCode] = useState('STU-2026-005');
  const [newStuDept, setNewStuDept] = useState('Computer Science & Engineering');
  const [csvInput, setCsvInput] = useState('');

  const [noticeTitle, setNoticeTitle] = useState('');
  const [noticeBody, setNoticeBody] = useState('');

  const activeRole = useMemo(
    () => ROLES.find((r) => r.key === selectedRoleKey) || ROLES[3],
    [selectedRoleKey]
  );

  const notify = (msg: string) => {
    setToastMessage(msg);
  };

  const handleDemoLogin = (roleKey: OrbitRoleKey) => {
    const role = ROLES.find((r) => r.key === roleKey) || ROLES[0];
    setSelectedRoleKey(roleKey);
    setEmailInput(role.demoEmail);
    setIsLoggedIn(true);
    notify(`Signed in with Demo Account: ${role.demoName} (${role.label})`);
  };

  const handleCreateCampusItem = (item: Omit<CampusModuleItem, 'id' | 'userId'>) => {
    const created: CampusModuleItem = {
      ...item,
      id: `itm_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
      userId: 'demo',
    };
    setCampusItems((prev) => [created, ...prev]);
    notify(`Published "${item.title}" (${item.code})`);
  };

  const handleUpdateCampusItem = (id: string, patch: Partial<CampusModuleItem>) => {
    setCampusItems((prev) => prev.map((it) => (it.id === id ? { ...it, ...patch } : it)));
    notify('Record updated in real-time workspace.');
  };

  const handleDeleteCampusItem = (id: string) => {
    setCampusItems((prev) => prev.filter((it) => it.id !== id));
    notify('Item removed.');
  };

  const lowAttendanceStudents = useMemo(
    () => students.filter((s) => s.attendancePct < 75),
    [students]
  );

  const deptAnalytics = useMemo(() => {
    const groups: Record<string, { totalAtt: number; totalCgpa: number; count: number }> = {};
    students.forEach((s) => {
      const k = s.department.slice(0, 18);
      if (!groups[k]) groups[k] = { totalAtt: 0, totalCgpa: 0, count: 0 };
      groups[k].totalAtt += s.attendancePct;
      groups[k].totalCgpa += s.cgpa;
      groups[k].count += 1;
    });
    return Object.entries(groups).map(([dept, v]) => ({
      dept,
      attendance: Math.round(v.totalAtt / v.count),
      cgpa: Number((v.totalCgpa / v.count).toFixed(2)),
    }));
  }, [students]);

  const sections: { key: OrbitSectionKey; title: string; icon: React.ReactNode }[] = [
    { key: 'OVERVIEW', title: 'Command Center', icon: <LayoutDashboard className="w-4 h-4" /> },
    { key: 'COLLABORATION_HUB', title: 'Campus Collaboration Hub', icon: <Sparkles className="w-4 h-4" /> },
    { key: 'STUDENTS', title: 'Student Management', icon: <GraduationCap className="w-4 h-4" /> },
    { key: 'FACULTY', title: 'Faculty Portal', icon: <Users className="w-4 h-4" /> },
    { key: 'ACADEMICS', title: 'Depts, Courses & Timetable', icon: <Building2 className="w-4 h-4" /> },
    { key: 'ATTENDANCE_QR', title: 'Smart QR Attendance', icon: <QrCode className="w-4 h-4" /> },
    { key: 'EXAMS_ASSIGNMENTS', title: 'Exams, Results & LMS', icon: <BookOpen className="w-4 h-4" /> },
    { key: 'FINANCE_GRIEVANCES', title: 'Finance & Grievances', icon: <CreditCard className="w-4 h-4" /> },
    { key: 'CAMPUS_LIFE', title: 'Facilities, Certs & Career', icon: <Award className="w-4 h-4" /> },
    { key: 'PARENT_PORTAL', title: 'Parent & Guardian Hub', icon: <UserCheck className="w-4 h-4" /> },
    { key: 'SUPER_ADMIN_GOVERNANCE', title: 'Governance & Audit', icon: <ShieldCheck className="w-4 h-4" /> },
  ];

  // LOGIN SCREEN WITH 5-ROLE DEMO ACCOUNTS
  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center p-6">
        <div className="max-w-5xl w-full grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <div className="lg:col-span-6 space-y-6">
            <span className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold bg-cyan-500/15 text-cyan-300 border border-cyan-500/30">
              <Sparkles className="w-3.5 h-3.5" /> CAMPUS ORBIT • SMART CAMPUS & COLLABORATION HUB
            </span>
            <h1 className="text-4xl font-bold tracking-tight text-white font-display">
              Unified Multi-Role Academic Operations & Smart Campus Ecosystem
            </h1>
            <p className="text-slate-300 text-sm leading-relaxed">
              Connect all institutional operations—from Smart QR Attendance, SGPA/CGPA Grading, and Conflict-Free Timetables to the interdisciplinary <strong>Campus Collaboration Hub</strong>—in one real-time platform.
            </p>
            <div className="rounded-2xl bg-slate-900/90 border border-slate-800 p-5 space-y-3">
              <h3 className="text-sm font-bold text-white">Sign In with Institutional Credentials</h3>
              <input
                value={emailInput}
                onChange={(e) => setEmailInput(e.target.value)}
                placeholder="Email address"
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                type="password"
                value={passwordInput}
                onChange={(e) => setPasswordInput(e.target.value)}
                placeholder="Password"
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <button
                onClick={() => handleDemoLogin(selectedRoleKey)}
                className="w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm transition"
              >
                Sign In to {activeRole.label} Portal
              </button>
            </div>
          </div>

          {/* 5-Role Instant Demo Account Cards */}
          <div className="lg:col-span-6 rounded-2xl bg-slate-900 border border-slate-800 p-6 space-y-4">
            <div>
              <h2 className="text-lg font-bold text-white font-display">
                1-Click Demo Accounts for Every Role
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">
                Select any of the 5 institutional roles below to launch with pre-populated academic & Campus Collaboration Hub records:
              </p>
            </div>
            <div className="space-y-2.5">
              {ROLES.map((r) => (
                <div
                  key={r.key}
                  className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 hover:border-cyan-500/50 flex items-center justify-between gap-3 transition"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-white">{r.label}</span>
                      <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-blue-500/20 text-blue-300">
                        {r.badge}
                      </span>
                    </div>
                    <p className="text-xs text-cyan-400 mt-0.5">
                      {r.demoName} • {r.demoEmail}
                    </p>
                    <p className="text-xs text-slate-400 mt-0.5">{r.tagline}</p>
                  </div>
                  <button
                    onClick={() => handleDemoLogin(r.key)}
                    className="shrink-0 px-3.5 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs transition"
                  >
                    Quick Demo Login
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex">
      {/* Sidebar */}
      <aside
        className={`fixed lg:static inset-y-0 left-0 z-40 w-72 bg-slate-900 border-r border-slate-800 flex flex-col justify-between p-4 transition-transform ${
          mobileSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <div className="space-y-4 overflow-y-auto">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold text-white font-display tracking-tight">
                CAMPUS ORBIT
              </h1>
              <p className="text-xs text-slate-400">{institutionName}</p>
            </div>
            <button
              onClick={() => setMobileSidebarOpen(false)}
              className="lg:hidden p-1.5 rounded-lg bg-slate-800"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="p-3 rounded-xl bg-slate-950 border border-slate-800">
            <p className="text-[11px] font-mono text-cyan-400">{activeRole.badge}</p>
            <p className="text-sm font-bold text-white mt-0.5">{activeRole.demoName}</p>
            <p className="text-xs text-slate-400">{activeRole.demoEmail}</p>
          </div>

          <nav className="space-y-1">
            {sections.map((sec) => (
              <button
                key={sec.key}
                onClick={() => {
                  setSelectedSection(sec.key);
                  setMobileSidebarOpen(false);
                }}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition ${
                  selectedSection === sec.key
                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/25'
                    : 'text-slate-300 hover:bg-slate-800/80'
                }`}
              >
                {sec.icon}
                <span>{sec.title}</span>
              </button>
            ))}
          </nav>
        </div>

        <button
          onClick={() => setIsLoggedIn(false)}
          className="mt-4 w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-rose-500/15 hover:bg-rose-500/25 text-rose-300 text-xs font-semibold transition"
        >
          <LogOut className="w-4 h-4" /> Sign Out
        </button>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Header */}
        <header className="sticky top-0 z-30 bg-slate-900/90 backdrop-blur border-b border-slate-800 px-6 py-3.5 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setMobileSidebarOpen(true)}
              className="lg:hidden p-2 rounded-xl bg-slate-800"
            >
              <Menu className="w-4 h-4" />
            </button>
            <div>
              <h2 className="text-lg font-bold text-white font-display">
                {sections.find((s) => s.key === selectedSection)?.title}
              </h2>
              <p className="text-xs text-cyan-400">
                Active Role: {activeRole.label} ({activeRole.demoName})
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <button
              onClick={() => setSearchModalOpen(true)}
              className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 hover:border-cyan-500/50"
            >
              <Search className="w-3.5 h-3.5 text-cyan-400" />
              <span>Global Search (Ctrl+K)</span>
            </button>

            <select
              value={selectedRoleKey}
              onChange={(e) => handleDemoLogin(e.target.value as OrbitRoleKey)}
              className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs font-semibold text-cyan-300"
            >
              {ROLES.map((r) => (
                <option key={r.key} value={r.key}>
                  Switch Role: {r.label} ({r.demoName})
                </option>
              ))}
            </select>
          </div>
        </header>

        {/* Status Toast */}
        {toastMessage && (
          <div className="mx-6 mt-4 px-4 py-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-between text-xs text-emerald-300 font-semibold">
            <span>{toastMessage}</span>
            <button onClick={() => setToastMessage(null)}>
              <X className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Section Body */}
        <main className="p-6 space-y-6 max-w-7xl w-full mx-auto">
          {selectedSection === 'OVERVIEW' && (
            <div className="space-y-6">
              {lowAttendanceStudents.length > 0 && (
                <div className="rounded-2xl bg-amber-500/10 border border-amber-500/30 p-4 flex items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />
                    <div>
                      <p className="text-sm font-bold text-amber-300">
                        Automated Low Attendance Warning (&lt;75% Threshold)
                      </p>
                      <p className="text-xs text-slate-300">
                        {lowAttendanceStudents.map((s) => `${s.fullName} (${s.attendancePct}%)`).join(', ')}
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedSection('ATTENDANCE_QR')}
                    className="px-3 py-1.5 rounded-xl bg-amber-500 text-slate-950 text-xs font-bold"
                  >
                    Review Attendance
                  </button>
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div
                  onClick={() => setSelectedSection('STUDENTS')}
                  className="cursor-pointer rounded-2xl bg-slate-900 border border-slate-800 p-5 hover:border-blue-500/40"
                >
                  <p className="text-xs text-slate-400">Total Enrolled Scholars</p>
                  <p className="text-3xl font-bold text-white mt-1">{students.length}</p>
                  <p className="text-xs text-cyan-400 mt-1">Active across 3 departments</p>
                </div>
                <div
                  onClick={() => setSelectedSection('FACULTY')}
                  className="cursor-pointer rounded-2xl bg-slate-900 border border-slate-800 p-5 hover:border-blue-500/40"
                >
                  <p className="text-xs text-slate-400">Faculty & HODs</p>
                  <p className="text-3xl font-bold text-white mt-1">{faculty.length}</p>
                  <p className="text-xs text-emerald-400 mt-1">100% workload allocated</p>
                </div>
                <div
                  onClick={() => setSelectedSection('COLLABORATION_HUB')}
                  className="cursor-pointer rounded-2xl bg-slate-900 border border-slate-800 p-5 hover:border-blue-500/40"
                >
                  <p className="text-xs text-slate-400">Campus Collaboration Hub</p>
                  <p className="text-3xl font-bold text-cyan-400 mt-1">
                    {campusItems.filter((i) => i.moduleType.startsWith('COLLAB_')).length}
                  </p>
                  <p className="text-xs text-purple-300 mt-1">
                    Projects, Teams, Showcase & Mentors
                  </p>
                </div>
                <div
                  onClick={() => setSelectedSection('FINANCE_GRIEVANCES')}
                  className="cursor-pointer rounded-2xl bg-slate-900 border border-slate-800 p-5 hover:border-blue-500/40"
                >
                  <p className="text-xs text-slate-400">Open Grievances</p>
                  <p className="text-3xl font-bold text-amber-400 mt-1">
                    {grievances.filter((g) => g.status !== 'RESOLVED').length}
                  </p>
                  <p className="text-xs text-slate-400 mt-1">SLA monitored by Redressal Cell</p>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5">
                  <h3 className="text-base font-bold text-white mb-4">
                    Department Attendance % & Average CGPA Analytics
                  </h3>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={deptAnalytics}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                        <XAxis dataKey="dept" stroke="#94a3b8" fontSize={12} />
                        <YAxis stroke="#94a3b8" fontSize={12} />
                        <Tooltip
                          contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }}
                        />
                        <Bar
                          dataKey="attendance"
                          name="Avg Attendance %"
                          fill="#3B82F6"
                          radius={[6, 6, 0, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
                  <h3 className="text-base font-bold text-white flex items-center gap-2">
                    <Bell className="w-4 h-4 text-cyan-400" /> Broadcast Institutional Notice
                  </h3>
                  <input
                    value={noticeTitle}
                    onChange={(e) => setNoticeTitle(e.target.value)}
                    placeholder="Notice Headline"
                    className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
                  />
                  <textarea
                    value={noticeBody}
                    onChange={(e) => setNoticeBody(e.target.value)}
                    rows={2}
                    placeholder="Broadcast message..."
                    className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
                  />
                  <button
                    onClick={() => {
                      if (!noticeTitle.trim()) return;
                      handleCreateCampusItem({
                        moduleType: 'NOTICE',
                        code: `NOT-2026-${Math.floor(100 + Math.random() * 900)}`,
                        title: noticeTitle.trim(),
                        subtitle: 'All Departments • Official Broadcast',
                        department: 'All Departments',
                        semester: 0,
                        status: 'PUBLISHED',
                        category: 'INSTITUTIONAL',
                        primaryMetric: 1,
                        secondaryMetric: students.length,
                        metadataText: noticeBody || 'Published via Command Center.',
                        ownerName: activeRole.demoName,
                      });
                      setNoticeTitle('');
                      setNoticeBody('');
                    }}
                    className="px-4 py-2 rounded-xl bg-cyan-500 text-slate-950 text-xs font-bold"
                  >
                    Broadcast Notice Live
                  </button>
                  <div className="space-y-2 pt-2">
                    {campusItems
                      .filter((i) => i.moduleType === 'NOTICE')
                      .map((n) => (
                        <div
                          key={n.id}
                          className="p-3 rounded-xl bg-slate-950 border border-slate-800 space-y-1"
                        >
                          <p className="text-xs font-mono text-cyan-400">{n.code}</p>
                          <p className="text-sm font-bold text-white">{n.title}</p>
                          <p className="text-xs text-slate-300">{n.metadataText}</p>
                        </div>
                      ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {selectedSection === 'COLLABORATION_HUB' && (
            <CollaborationHubView
              activeRole={activeRole}
              currentUserName={activeRole.demoName}
              currentStudentCode={linkedStudentCode}
              campusItems={campusItems}
              facultyList={faculty}
              onCreateItem={handleCreateCampusItem}
              onUpdateItem={handleUpdateCampusItem}
              onDeleteItem={handleDeleteCampusItem}
              onNotify={notify}
            />
          )}

          {selectedSection === 'STUDENTS' && (
            <div className="space-y-6">
              <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
                <h3 className="text-base font-bold text-white">
                  Enroll New Student / Bulk CSV Import & Export
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  <input
                    value={newStuCode}
                    onChange={(e) => setNewStuCode(e.target.value)}
                    placeholder="Student Code (STU-2026-005)"
                    className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
                  />
                  <input
                    value={newStuName}
                    onChange={(e) => setNewStuName(e.target.value)}
                    placeholder="Full Name"
                    className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
                  />
                  <input
                    value={newStuDept}
                    onChange={(e) => setNewStuDept(e.target.value)}
                    placeholder="Department"
                    className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  <button
                    onClick={() => {
                      if (!newStuName.trim()) return;
                      setStudents((prev) => [
                        ...prev,
                        {
                          id: `stu_${Date.now()}`,
                          userId: 'demo',
                          studentCode: newStuCode,
                          fullName: newStuName,
                          email: `${newStuCode.toLowerCase()}@campusorbit.edu`,
                          department: newStuDept,
                          semester: 6,
                          cgpa: 8.9,
                          attendancePct: 90,
                          feeStatus: 'PAID',
                          guardianName: 'Verified Guardian',
                          guardianPhone: '+1-555-0199',
                          mentorName: 'Dr. Elena Rostova',
                          status: 'ACTIVE',
                        },
                      ]);
                      setNewStuName('');
                      notify(`Enrolled student ${newStuName} (${newStuCode})`);
                    }}
                    className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
                  >
                    Enroll Student
                  </button>
                  <button
                    onClick={() => {
                      const csv = [
                        'studentCode,fullName,email,department,semester,cgpa,attendancePct,feeStatus',
                        ...students.map(
                          (s) =>
                            `${s.studentCode},${s.fullName},${s.email},${s.department},${s.semester},${s.cgpa},${s.attendancePct},${s.feeStatus}`
                        ),
                      ].join('\n');
                      navigator.clipboard?.writeText(csv);
                      notify('Copied full Student Directory CSV to clipboard!');
                    }}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-800 text-slate-200 text-xs font-semibold"
                  >
                    <Download className="w-3.5 h-3.5" /> Export CSV
                  </button>
                </div>

                <div className="pt-2 border-t border-slate-800 space-y-2">
                  <p className="text-xs text-slate-400">
                    Bulk CSV Import (Format: <code>Code,Name,Email,Department,Sem,CGPA,Attendance</code>)
                  </p>
                  <div className="flex gap-2">
                    <input
                      value={csvInput}
                      onChange={(e) => setCsvInput(e.target.value)}
                      placeholder="STU-2026-010,Kabir Verma,kabir@campusorbit.edu,CSE,6,9.1,93"
                      className="flex-1 px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs font-mono"
                    />
                    <button
                      onClick={() => {
                        const cols = csvInput.split(',').map((c) => c.trim());
                        if (cols.length >= 4) {
                          setStudents((prev) => [
                            ...prev,
                            {
                              id: `stu_${Date.now()}`,
                              userId: 'demo',
                              studentCode: cols[0],
                              fullName: cols[1],
                              email: cols[2],
                              department: cols[3],
                              semester: Number(cols[4] || 6),
                              cgpa: Number(cols[5] || 8.8),
                              attendancePct: Number(cols[6] || 90),
                              feeStatus: 'PAID',
                              guardianName: 'Verified Guardian',
                              guardianPhone: '+1-555-0199',
                              mentorName: 'Dr. Elena Rostova',
                              status: 'ACTIVE',
                            },
                          ]);
                          setCsvInput('');
                          notify(`Imported ${cols[1]} (${cols[0]}) from CSV.`);
                        }
                      }}
                      className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 text-xs font-bold"
                    >
                      <FileSpreadsheet className="w-3.5 h-3.5" /> Import CSV Row
                    </button>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {students.map((s) => (
                  <div
                    key={s.id}
                    className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
                  >
                    <div className="flex justify-between">
                      <span className="text-xs font-mono text-cyan-400">{s.studentCode}</span>
                      <span
                        className={`text-xs font-bold px-2 py-0.5 rounded ${
                          s.attendancePct < 75
                            ? 'bg-rose-500/20 text-rose-300'
                            : 'bg-emerald-500/20 text-emerald-300'
                        }`}
                      >
                        Attendance: {s.attendancePct}%
                      </span>
                    </div>
                    <h4 className="text-base font-bold text-white">{s.fullName}</h4>
                    <p className="text-xs text-slate-400">
                      {s.department} • Sem {s.semester} • CGPA: {s.cgpa} • Fee: {s.feeStatus}
                    </p>
                    <p className="text-xs text-slate-500">
                      Guardian: {s.guardianName} ({s.guardianPhone}) • Mentor: {s.mentorName}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {selectedSection === 'ATTENDANCE_QR' && (
            <div className="space-y-6">
              <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 flex flex-wrap items-center justify-between gap-6">
                <div className="space-y-2">
                  <span className="px-2.5 py-1 rounded-full text-xs font-mono bg-cyan-500/20 text-cyan-300">
                    LIVE SESSION TOKEN: {qrToken}
                  </span>
                  <h3 className="text-xl font-bold text-white">
                    CS601 • Distributed Cloud Systems QR Attendance
                  </h3>
                  <p className="text-xs text-slate-400">
                    Duplicate-prevented QR session verification for enrolled students.
                  </p>
                  <div className="flex gap-2 pt-2">
                    <button
                      onClick={() => {
                        const next = `ORB-CS601-${Math.floor(1000 + Math.random() * 9000)}-LIVE`;
                        setQrToken(next);
                        notify(`Generated new QR Attendance token: ${next}`);
                      }}
                      className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
                    >
                      Generate New Live QR Token
                    </button>
                    <button
                      onClick={() => {
                        const dup = attendance.some(
                          (a) => a.studentCode === 'STU-2026-001' && a.sessionToken === qrToken
                        );
                        if (dup) {
                          notify('Duplicate prevented: Attendance already marked for this QR session!');
                          return;
                        }
                        setAttendance((prev) => [
                          {
                            id: `att_${Date.now()}`,
                            userId: 'demo',
                            studentCode: 'STU-2026-001',
                            studentName: 'Aarav Mehta',
                            subjectCode: 'CS601',
                            subjectName: 'Distributed Cloud Systems',
                            department: 'Computer Science & Engineering',
                            semester: 6,
                            sessionDate: new Date().toISOString().slice(0, 10),
                            status: 'PRESENT',
                            method: 'QR_SCAN',
                            sessionToken: qrToken,
                          },
                          ...prev,
                        ]);
                        notify('QR Attendance verified and logged for Aarav Mehta!');
                      }}
                      className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 text-xs font-bold"
                    >
                      Simulate Student QR Scan (Aarav Mehta)
                    </button>
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                {attendance.map((a) => (
                  <div
                    key={a.id}
                    className="rounded-2xl bg-slate-900 border border-slate-800 p-4 flex items-center justify-between"
                  >
                    <div>
                      <p className="text-sm font-bold text-white">
                        {a.studentName} ({a.studentCode})
                      </p>
                      <p className="text-xs text-slate-400">
                        {a.subjectCode} • {a.subjectName} • {a.sessionDate} • Token: {a.sessionToken}
                      </p>
                    </div>
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300">
                      {a.status} ({a.method})
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {(selectedSection === 'FACULTY' || selectedSection === 'ACADEMICS') && (
            <AcademicsAndFacultyView
              mode={selectedSection}
              faculty={faculty}
              campusItems={campusItems}
              onAddFaculty={(f) => {
                setFaculty((prev) => [
                  ...prev,
                  { ...f, id: `fac_${Date.now()}`, userId: 'demo' },
                ]);
                notify(`Registered faculty profile: ${f.fullName}`);
              }}
              onDeleteFaculty={(id) => {
                setFaculty((prev) => prev.filter((f) => f.id !== id));
                notify('Faculty record removed.');
              }}
              onCreateItem={handleCreateCampusItem}
              onDeleteItem={handleDeleteCampusItem}
              onNotify={notify}
            />
          )}

          {(selectedSection === 'EXAMS_ASSIGNMENTS' ||
            selectedSection === 'FINANCE_GRIEVANCES') && (
            <ExamsAndFinanceView
              mode={selectedSection}
              students={students}
              campusItems={campusItems}
              grievances={grievances}
              onCreateItem={handleCreateCampusItem}
              onUpdateItem={handleUpdateCampusItem}
              onFileGrievance={(g) => {
                const ticket = `GRV-2026-${Math.floor(100 + Math.random() * 900)}`;
                setGrievances((prev) => [
                  {
                    ...g,
                    id: `grv_${Date.now()}`,
                    userId: 'demo',
                    ticketNumber: ticket,
                    status: 'SUBMITTED',
                    assignedTo: 'Dean of Student Welfare',
                    resolutionNotes: 'Awaiting initial review by grievance cell.',
                  },
                  ...prev,
                ]);
                notify(`Submitted grievance ticket ${ticket}`);
              }}
              onAdvanceGrievance={(id, notes) => {
                setGrievances((prev) =>
                  prev.map((g) =>
                    g.id === id
                      ? { ...g, status: 'RESOLVED', resolutionNotes: notes }
                      : g
                  )
                );
                notify('Grievance ticket resolved.');
              }}
            />
          )}

          {(selectedSection === 'CAMPUS_LIFE' ||
            selectedSection === 'PARENT_PORTAL' ||
            selectedSection === 'SUPER_ADMIN_GOVERNANCE') && (
            <CampusLifeAndGovernanceView
              mode={selectedSection}
              institutionName={institutionName}
              linkedStudentCode={linkedStudentCode}
              students={students}
              campusItems={campusItems}
              onUpdateInstitution={(name, code) => {
                setInstitutionName(name);
                setLinkedStudentCode(code);
                notify('Institutional settings updated.');
              }}
              onCreateItem={handleCreateCampusItem}
              onUpdateItem={handleUpdateCampusItem}
              onNotify={notify}
            />
          )}
        </main>
      </div>

      {/* Global Command Palette Modal */}
      {searchModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-start justify-center pt-20 p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-5 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-white">Global Command Palette Search</h3>
              <button onClick={() => setSearchModalOpen(false)}>
                <X className="w-4 h-4 text-slate-400" />
              </button>
            </div>
            <input
              autoFocus
              value={globalQuery}
              onChange={(e) => setGlobalQuery(e.target.value)}
              placeholder="Search students, faculty, courses, collaboration projects, teams..."
              className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <div className="max-h-72 overflow-y-auto space-y-2">
              {campusItems
                .filter(
                  (i) =>
                    !globalQuery ||
                    i.title.toLowerCase().includes(globalQuery.toLowerCase()) ||
                    i.code.toLowerCase().includes(globalQuery.toLowerCase()) ||
                    i.subtitle.toLowerCase().includes(globalQuery.toLowerCase())
                )
                .slice(0, 10)
                .map((hit) => (
                  <div
                    key={hit.id}
                    onClick={() => {
                      if (hit.moduleType.startsWith('COLLAB_')) {
                        setSelectedSection('COLLABORATION_HUB');
                      } else if (
                        ['DEPARTMENT', 'COURSE', 'TIMETABLE'].includes(hit.moduleType)
                      ) {
                        setSelectedSection('ACADEMICS');
                      } else if (
                        ['EXAM_RESULT', 'ASSIGNMENT'].includes(hit.moduleType)
                      ) {
                        setSelectedSection('EXAMS_ASSIGNMENTS');
                      } else if (hit.moduleType === 'FEE_INVOICE') {
                        setSelectedSection('FINANCE_GRIEVANCES');
                      } else if (hit.moduleType === 'AUDIT_LOG') {
                        setSelectedSection('SUPER_ADMIN_GOVERNANCE');
                      } else {
                        setSelectedSection('CAMPUS_LIFE');
                      }
                      setSearchModalOpen(false);
                    }}
                    className="p-3 rounded-xl bg-slate-950 hover:bg-slate-800 cursor-pointer"
                  >
                    <p className="text-[11px] font-mono text-cyan-400">
                      {hit.moduleType} • {hit.code}
                    </p>
                    <p className="text-sm font-bold text-white">{hit.title}</p>
                    <p className="text-xs text-slate-400">{hit.subtitle}</p>
                  </div>
                ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
