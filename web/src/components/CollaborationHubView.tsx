import React, { useMemo, useState } from 'react';
import {
  Award,
  CheckCircle2,
  Compass,
  FolderGit2,
  GitPullRequest,
  GraduationCap,
  LayoutDashboard,
  ListTodo,
  MessageSquare,
  Plus,
  Search,
  Send,
  Sparkles,
  Trash2,
  UserPlus,
  Users,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  CampusModuleItem,
  CampusModuleTypeKey,
  FacultyRecord,
  RoleMetadata,
} from '../types';

export type CollabSubTabKey =
  | 'OVERVIEW'
  | 'DISCOVER_PROJECTS'
  | 'MY_COLLABORATIONS'
  | 'FIND_COLLABORATORS'
  | 'TEAM_WORKSPACE'
  | 'TASKS_MILESTONES'
  | 'DISCUSSIONS'
  | 'PROJECT_SHOWCASE'
  | 'FACULTY_MENTORSHIP';

const PROJECT_CATEGORIES = [
  'Artificial Intelligence and Machine Learning',
  'Internet of Things',
  'Web Development',
  'Mobile Application Development',
  'Robotics',
  'Cloud Computing',
  'Cybersecurity',
  'Data Science',
  'Electronics and Embedded Systems',
  'Social Innovation',
];

const CHART_COLORS = ['#3B82F6', '#06B6D4', '#10B981', '#8B5CF6', '#F59E0B', '#F43F5E'];

interface CollaborationHubViewProps {
  activeRole: RoleMetadata;
  currentUserName: string;
  currentStudentCode: string;
  campusItems: CampusModuleItem[];
  facultyList: FacultyRecord[];
  onCreateItem: (item: Omit<CampusModuleItem, 'id' | 'userId'>) => void;
  onUpdateItem: (id: string, patch: Partial<CampusModuleItem>) => void;
  onDeleteItem: (id: string) => void;
  onNotify: (msg: string) => void;
}

export const CollaborationHubView: React.FC<CollaborationHubViewProps> = ({
  activeRole,
  currentUserName,
  currentStudentCode,
  campusItems,
  facultyList,
  onCreateItem,
  onUpdateItem,
  onDeleteItem,
  onNotify,
}) => {
  const [subTab, setSubTab] = useState<CollabSubTabKey>('OVERVIEW');

  // Filter states
  const [projectSearch, setProjectSearch] = useState('');
  const [projectCatFilter, setProjectCatFilter] = useState('ALL');
  const [projectDeptFilter, setProjectDeptFilter] = useState('ALL');
  const [projectTechFilter, setProjectTechFilter] = useState('');

  const [collabSearch, setCollabSearch] = useState('');
  const [collabDeptFilter, setCollabDeptFilter] = useState('ALL');

  const [requestStatusFilter, setRequestStatusFilter] = useState('ALL');
  const [taskStatusFilter, setTaskStatusFilter] = useState('ALL');
  const [showcaseSearch, setShowcaseSearch] = useState('');
  const [showcaseCatFilter, setShowcaseCatFilter] = useState('ALL');

  // Quick composer states
  const [newProjTitle, setNewProjTitle] = useState('');
  const [newProjCat, setNewProjCat] = useState(PROJECT_CATEGORIES[0]);
  const [newProjDept, setNewProjDept] = useState('Computer Science & Engineering');
  const [newProjTech, setNewProjTech] = useState('Kotlin, Python, TensorFlow, Cloud Firestore');
  const [newProjMembers, setNewProjMembers] = useState(5);
  const [newProjDesc, setNewProjDesc] = useState('');

  const [profileName, setProfileName] = useState(currentUserName);
  const [profileCode, setProfileCode] = useState(currentStudentCode);
  const [profileDept, setProfileDept] = useState('Computer Science & Engineering');
  const [profileSem, setProfileSem] = useState(6);
  const [profileDomain, setProfileDomain] = useState(PROJECT_CATEGORIES[0]);
  const [profileSkills, setProfileSkills] = useState('React, TypeScript, Kotlin, Python, TensorFlow');
  const [profileBio, setProfileBio] = useState(
    'Areas of Interest: Distributed Cloud & Edge AI | Experience: Lead Builder on Campus Orbit | Open to interdisciplinary hackathons & capstones.'
  );

  const [teamName, setTeamName] = useState('');
  const [teamDepts, setTeamDepts] = useState('Interdisciplinary (CSE + ECE + Mech)');
  const [teamCat, setTeamCat] = useState(PROJECT_CATEGORIES[0]);
  const [teamTech, setTeamTech] = useState('React, Kotlin, ESP32, TensorFlow');
  const [teamObjectives, setTeamObjectives] = useState('');

  const [taskTitle, setTaskTitle] = useState('');
  const [taskTeam, setTaskTeam] = useState('Team AuraGrid • Milestone 2');
  const [taskAssignee, setTaskAssignee] = useState(currentUserName);
  const [taskPriority, setTaskPriority] = useState('HIGH');
  const [taskDeadline, setTaskDeadline] = useState('2026-05-30');

  const [discTitle, setDiscTitle] = useState('');
  const [discChannel, setDiscChannel] = useState('Team AuraGrid • Architecture Channel');
  const [discBody, setDiscBody] = useState('');

  const [showcaseTitle, setShowcaseTitle] = useState('');
  const [showcaseCat, setShowcaseCat] = useState(PROJECT_CATEGORIES[0]);
  const [showcaseTech, setShowcaseTech] = useState('React, Kotlin, Python, Cloud Firestore');
  const [showcaseMeta, setShowcaseMeta] = useState(
    'Problem: Campus resource bottlenecks | Solution: AI-driven scheduling & IoT telemetry | GitHub: github.com/orbit-edu/project | Demo: demo.campusorbit.edu'
  );

  const [mentorName, setMentorName] = useState(facultyList[0]?.fullName || 'Dr. Elena Rostova');
  const [mentorProject, setMentorProject] = useState('Team AuraGrid • Smart Energy Microgrid');
  const [mentorNotes, setMentorNotes] = useState(
    'Requesting bi-weekly faculty mentorship for system architecture review and milestone evaluation.'
  );

  const profiles = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_PROFILE'),
    [campusItems]
  );
  const projects = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_PROJECT'),
    [campusItems]
  );
  const teams = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_TEAM'),
    [campusItems]
  );
  const requests = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_REQUEST'),
    [campusItems]
  );
  const tasks = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_TASK'),
    [campusItems]
  );
  const discussions = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_DISCUSSION'),
    [campusItems]
  );
  const showcases = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_SHOWCASE'),
    [campusItems]
  );
  const mentorships = useMemo(
    () => campusItems.filter((i) => i.moduleType === 'COLLAB_MENTORSHIP'),
    [campusItems]
  );

  const deptChartData = useMemo(() => {
    const counts: Record<string, number> = {};
    [...projects, ...profiles, ...showcases].forEach((item) => {
      const key = item.department.slice(0, 22);
      counts[key] = (counts[key] || 0) + 1;
    });
    return Object.entries(counts).map(([name, count]) => ({ name, count }));
  }, [projects, profiles, showcases]);

  const categoryChartData = useMemo(() => {
    const counts: Record<string, number> = {};
    [...projects, ...teams, ...showcases].forEach((item) => {
      counts[item.category] = (counts[item.category] || 0) + 1;
    });
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
  }, [projects, teams, showcases]);

  const handleSendJoinRequest = (targetTitle: string, department: string, roleSkill: string, reqType: string, pitch: string) => {
    const duplicate = requests.some(
      (r) =>
        r.title.toLowerCase() === targetTitle.trim().toLowerCase() &&
        r.ownerName.toLowerCase() === currentUserName.trim().toLowerCase() &&
        (r.status === 'PENDING' || r.status === 'ACCEPTED')
    );
    if (duplicate) {
      onNotify(`Duplicate prevented: Active request already exists for "${targetTitle}"`);
      return;
    }
    onCreateItem({
      moduleType: 'COLLAB_REQUEST',
      code: `CRQ-2026-${Math.floor(100 + Math.random() * 900)}`,
      title: targetTitle,
      subtitle: `Applicant/Target: ${currentUserName} • Role: ${roleSkill}`,
      department,
      semester: 6,
      status: 'PENDING',
      category: reqType,
      primaryMetric: 1,
      secondaryMetric: 1,
      metadataText: pitch,
      ownerName: currentUserName,
    });
  };

  const navTabs: { key: CollabSubTabKey; label: string; icon: React.ReactNode }[] = [
    { key: 'OVERVIEW', label: '1. Overview', icon: <LayoutDashboard className="w-4 h-4" /> },
    { key: 'DISCOVER_PROJECTS', label: '2. Discover Projects', icon: <Compass className="w-4 h-4" /> },
    { key: 'MY_COLLABORATIONS', label: '3. My Collaborations', icon: <GitPullRequest className="w-4 h-4" /> },
    { key: 'FIND_COLLABORATORS', label: '4. Find Collaborators', icon: <Users className="w-4 h-4" /> },
    { key: 'TEAM_WORKSPACE', label: '5. Team Workspace', icon: <FolderGit2 className="w-4 h-4" /> },
    { key: 'TASKS_MILESTONES', label: '6. Tasks & Milestones', icon: <ListTodo className="w-4 h-4" /> },
    { key: 'DISCUSSIONS', label: '7. Discussions', icon: <MessageSquare className="w-4 h-4" /> },
    { key: 'PROJECT_SHOWCASE', label: '8. Project Showcase', icon: <Award className="w-4 h-4" /> },
    { key: 'FACULTY_MENTORSHIP', label: '9. Faculty Mentorship', icon: <GraduationCap className="w-4 h-4" /> },
  ];

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div className="rounded-2xl bg-gradient-to-r from-slate-900 via-blue-950 to-indigo-950 border border-blue-500/30 p-6 shadow-xl">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-cyan-500/20 text-cyan-300 border border-cyan-400/30">
              <Sparkles className="w-3.5 h-3.5" /> CAMPUS COLLABORATION HUB • INTERDISCIPLINARY R&D
            </span>
            <h2 className="mt-2 text-2xl font-bold text-white font-display">
              Welcome, {currentUserName} — Connect, Innovate & Build Together
            </h2>
            <p className="text-sm text-slate-300 mt-1 max-w-3xl">
              Discover student projects across 10 technical domains, build cross-department teams, manage tasks & milestones, showcase outcomes, and collaborate with faculty mentors.
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setSubTab('DISCOVER_PROJECTS')}
              className="px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold text-sm transition"
            >
              Explore Projects
            </button>
            <button
              onClick={() => setSubTab('TEAM_WORKSPACE')}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-100 border border-slate-700 font-semibold text-sm transition"
            >
              Team Workspaces
            </button>
          </div>
        </div>
      </div>

      {/* 9 Sub-Navigation Tabs */}
      <div className="flex flex-wrap gap-2 bg-slate-900/90 border border-slate-800 p-2.5 rounded-2xl">
        {navTabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setSubTab(t.key)}
            className={`inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold transition ${
              subTab === t.key
                ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
                : 'bg-slate-800/70 text-slate-300 hover:bg-slate-800'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {/* 1. OVERVIEW & COLLABORATION ANALYTICS */}
      {subTab === 'OVERVIEW' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-4">
              <p className="text-xs text-slate-400">Active Projects</p>
              <p className="text-2xl font-bold text-blue-400 mt-1">{projects.length}</p>
              <p className="text-xs text-slate-500 mt-1">Across 10 tech categories</p>
            </div>
            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-4">
              <p className="text-xs text-slate-400">Student Collaborators</p>
              <p className="text-2xl font-bold text-cyan-400 mt-1">{profiles.length}</p>
              <p className="text-xs text-slate-500 mt-1">Verified skill profiles</p>
            </div>
            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-4">
              <p className="text-xs text-slate-400">Active Teams</p>
              <p className="text-2xl font-bold text-emerald-400 mt-1">{teams.length}</p>
              <p className="text-xs text-slate-500 mt-1">Interdisciplinary squads</p>
            </div>
            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-4">
              <p className="text-xs text-slate-400">Completed Projects</p>
              <p className="text-2xl font-bold text-purple-400 mt-1">{showcases.length}</p>
              <p className="text-xs text-slate-500 mt-1">Published in Showcase</p>
            </div>
            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-4">
              <p className="text-xs text-slate-400">Pending Requests</p>
              <p className="text-2xl font-bold text-amber-400 mt-1">
                {requests.filter((r) => r.status === 'PENDING').length}
              </p>
              <p className="text-xs text-slate-500 mt-1">Awaiting team decision</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5">
              <h3 className="text-base font-bold text-white mb-4">
                Department-Wise Collaboration Activity
              </h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={deptChartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                    <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} />
                    <YAxis stroke="#94a3b8" fontSize={11} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }}
                    />
                    <Bar dataKey="count" fill="#06B6D4" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5">
              <h3 className="text-base font-bold text-white mb-4">
                Popular Project Categories
              </h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={categoryChartData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={85}
                      label
                    >
                      {categoryChartData.map((_, idx) => (
                        <Cell key={idx} fill={CHART_COLORS[idx % CHART_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 2. DISCOVER PROJECTS */}
      {subTab === 'DISCOVER_PROJECTS' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
            <h3 className="text-base font-bold text-white">Publish New Project Idea</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <input
                value={newProjTitle}
                onChange={(e) => setNewProjTitle(e.target.value)}
                placeholder="Project Title"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <select
                value={newProjCat}
                onChange={(e) => setNewProjCat(e.target.value)}
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              >
                {PROJECT_CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
              <input
                value={newProjDept}
                onChange={(e) => setNewProjDept(e.target.value)}
                placeholder="Department"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={newProjTech}
                onChange={(e) => setNewProjTech(e.target.value)}
                placeholder="Technologies (e.g., ROS2, Python, React)"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                type="number"
                value={newProjMembers}
                onChange={(e) => setNewProjMembers(Number(e.target.value))}
                placeholder="Required Team Size"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={newProjDesc}
                onChange={(e) => setNewProjDesc(e.target.value)}
                placeholder="Project Description & Required Team Roles"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                if (!newProjTitle.trim()) return;
                onCreateItem({
                  moduleType: 'COLLAB_PROJECT',
                  code: `PRJ-2026-${Math.floor(100 + Math.random() * 900)}`,
                  title: newProjTitle.trim(),
                  subtitle: `Tech: ${newProjTech} • Team: 1/${newProjMembers}`,
                  department: newProjDept,
                  semester: 6,
                  status: 'PLANNING',
                  category: newProjCat,
                  primaryMetric: 15,
                  secondaryMetric: newProjMembers,
                  metadataText:
                    newProjDesc ||
                    'Seeking interdisciplinary collaborators across hardware, AI, and full-stack engineering.',
                  ownerName: currentUserName,
                });
                setNewProjTitle('');
                setNewProjDesc('');
              }}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold"
            >
              <Plus className="w-4 h-4" /> Publish Project Idea
            </button>
          </div>

          {/* Search & Filters */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
              <input
                value={projectSearch}
                onChange={(e) => setProjectSearch(e.target.value)}
                placeholder="Search project title..."
                className="w-full pl-9 pr-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
              />
            </div>
            <select
              value={projectCatFilter}
              onChange={(e) => setProjectCatFilter(e.target.value)}
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            >
              <option value="ALL">All Categories</option>
              {PROJECT_CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
            <select
              value={projectDeptFilter}
              onChange={(e) => setProjectDeptFilter(e.target.value)}
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            >
              <option value="ALL">All Departments</option>
              <option value="Computer Science & Engineering">Computer Science & Engineering</option>
              <option value="Electronics & Communication">Electronics & Communication</option>
              <option value="Mechanical & Robotics Engineering">Mechanical & Robotics Engineering</option>
            </select>
            <input
              value={projectTechFilter}
              onChange={(e) => setProjectTechFilter(e.target.value)}
              placeholder="Filter by technology (e.g. ROS2, Kotlin)"
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {projects
              .filter((p) => {
                const matchTitle =
                  !projectSearch ||
                  p.title.toLowerCase().includes(projectSearch.toLowerCase()) ||
                  p.metadataText.toLowerCase().includes(projectSearch.toLowerCase());
                const matchCat = projectCatFilter === 'ALL' || p.category === projectCatFilter;
                const matchDept = projectDeptFilter === 'ALL' || p.department === projectDeptFilter;
                const matchTech =
                  !projectTechFilter ||
                  p.subtitle.toLowerCase().includes(projectTechFilter.toLowerCase());
                return matchTitle && matchCat && matchDept && matchTech;
              })
              .map((proj) => (
                <div
                  key={proj.id}
                  className="rounded-2xl bg-slate-900 border border-slate-800 p-5 flex flex-col justify-between gap-4"
                >
                  <div className="space-y-2">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="px-2.5 py-0.5 rounded-full text-xs font-mono bg-cyan-500/15 text-cyan-300">
                        {proj.code} • {proj.status}
                      </span>
                      <span className="px-2.5 py-0.5 rounded-full text-xs bg-purple-500/15 text-purple-300">
                        {proj.category}
                      </span>
                    </div>
                    <h4 className="text-lg font-bold text-white">{proj.title}</h4>
                    <p className="text-xs font-semibold text-blue-400">{proj.subtitle}</p>
                    <p className="text-xs text-slate-400">
                      Department: {proj.department} • Required Team Size: {proj.secondaryMetric} • Lead:{' '}
                      {proj.ownerName}
                    </p>
                    <p className="text-sm text-slate-300">{proj.metadataText}</p>
                  </div>
                  <div className="flex justify-end">
                    <button
                      onClick={() =>
                        handleSendJoinRequest(
                          `Join Request: ${proj.title}`,
                          proj.department,
                          'Interdisciplinary Collaborator',
                          'JOIN REQUEST',
                          `Requesting to join "${proj.title}" (${proj.code}). Ready to contribute to ${proj.subtitle}.`
                        )
                      }
                      className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold text-xs"
                    >
                      <UserPlus className="w-3.5 h-3.5" /> Request to Join Project
                    </button>
                  </div>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* 3. MY COLLABORATIONS (COLLABORATION REQUESTS) */}
      {subTab === 'MY_COLLABORATIONS' && (
        <div className="space-y-4">
          <div className="flex flex-wrap gap-2">
            {['ALL', 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'].map((st) => (
              <button
                key={st}
                onClick={() => setRequestStatusFilter(st)}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold ${
                  requestStatusFilter === st
                    ? 'bg-cyan-500 text-slate-950'
                    : 'bg-slate-900 text-slate-300 border border-slate-800'
                }`}
              >
                {st}
              </button>
            ))}
          </div>

          {requests
            .filter((r) => requestStatusFilter === 'ALL' || r.status === requestStatusFilter)
            .map((req) => (
              <div
                key={req.id}
                className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3"
              >
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className="px-2.5 py-0.5 rounded-full text-xs font-mono bg-blue-500/20 text-blue-300">
                      {req.code}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-xs bg-amber-500/20 text-amber-300">
                      {req.status}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-xs bg-purple-500/20 text-purple-300">
                      {req.category}
                    </span>
                  </div>
                  <span className="text-xs text-slate-400">{req.department}</span>
                </div>
                <h4 className="text-base font-bold text-white">{req.title}</h4>
                <p className="text-xs text-cyan-400">{req.subtitle}</p>
                <p className="text-sm text-slate-300">{req.metadataText}</p>
                {req.status === 'PENDING' && (
                  <div className="flex flex-wrap gap-2 pt-1">
                    <button
                      onClick={() =>
                        onUpdateItem(req.id, {
                          status: 'ACCEPTED',
                          metadataText: `${req.metadataText} | Accepted by ${currentUserName}`,
                        })
                      }
                      className="px-3 py-1.5 rounded-lg bg-emerald-500 text-slate-950 text-xs font-semibold"
                    >
                      Accept
                    </button>
                    <button
                      onClick={() =>
                        onUpdateItem(req.id, {
                          status: 'REJECTED',
                          metadataText: `${req.metadataText} | Rejected by ${currentUserName}`,
                        })
                      }
                      className="px-3 py-1.5 rounded-lg bg-rose-500/20 text-rose-300 border border-rose-500/30 text-xs font-semibold"
                    >
                      Reject
                    </button>
                    <button
                      onClick={() =>
                        onUpdateItem(req.id, {
                          status: 'WITHDRAWN',
                          metadataText: `${req.metadataText} | Withdrawn by requester`,
                        })
                      }
                      className="px-3 py-1.5 rounded-lg bg-slate-800 text-slate-300 text-xs font-semibold"
                    >
                      Withdraw Request
                    </button>
                  </div>
                )}
              </div>
            ))}
        </div>
      )}

      {/* 4. FIND COLLABORATORS (STUDENT COLLABORATION PROFILES) */}
      {subTab === 'FIND_COLLABORATORS' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
            <h3 className="text-base font-bold text-white">
              Create / Update My Student Collaboration Profile
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <input
                value={profileName}
                onChange={(e) => setProfileName(e.target.value)}
                placeholder="Full Name"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={profileCode}
                onChange={(e) => setProfileCode(e.target.value)}
                placeholder="Student ID"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={profileDept}
                onChange={(e) => setProfileDept(e.target.value)}
                placeholder="Department"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <select
                value={profileDomain}
                onChange={(e) => setProfileDomain(e.target.value)}
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              >
                {PROJECT_CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
              <input
                value={profileSkills}
                onChange={(e) => setProfileSkills(e.target.value)}
                placeholder="Technical Interests & Skills"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={profileBio}
                onChange={(e) => setProfileBio(e.target.value)}
                placeholder="Experience, Collaboration Preferences & Short Intro"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                const existing = profiles.find(
                  (p) => p.code.toLowerCase() === profileCode.trim().toLowerCase()
                );
                if (existing) {
                  onUpdateItem(existing.id, {
                    title: profileName,
                    subtitle: profileSkills,
                    department: profileDept,
                    semester: profileSem,
                    category: profileDomain,
                    metadataText: profileBio,
                  });
                } else {
                  onCreateItem({
                    moduleType: 'COLLAB_PROFILE',
                    code: profileCode,
                    title: profileName,
                    subtitle: profileSkills,
                    department: profileDept,
                    semester: profileSem,
                    status: 'OPEN_TO_COLLAB',
                    category: profileDomain,
                    primaryMetric: 4,
                    secondaryMetric: profileSem,
                    metadataText: profileBio,
                    ownerName: profileName,
                  });
                }
              }}
              className="px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold text-sm"
            >
              Save Collaboration Profile
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <input
              value={collabSearch}
              onChange={(e) => setCollabSearch(e.target.value)}
              placeholder="Search collaborators by name, ID, skills, domain..."
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            />
            <select
              value={collabDeptFilter}
              onChange={(e) => setCollabDeptFilter(e.target.value)}
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            >
              <option value="ALL">All Departments</option>
              <option value="Computer Science & Engineering">Computer Science & Engineering</option>
              <option value="Electronics & Communication">Electronics & Communication</option>
              <option value="Mechanical & Robotics Engineering">Mechanical & Robotics Engineering</option>
            </select>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {profiles
              .filter((prof) => {
                const matchDept =
                  collabDeptFilter === 'ALL' || prof.department === collabDeptFilter;
                const matchQ =
                  !collabSearch ||
                  prof.title.toLowerCase().includes(collabSearch.toLowerCase()) ||
                  prof.subtitle.toLowerCase().includes(collabSearch.toLowerCase()) ||
                  prof.metadataText.toLowerCase().includes(collabSearch.toLowerCase());
                return matchDept && matchQ;
              })
              .map((prof) => (
                <div
                  key={prof.id}
                  className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-base font-bold text-white">{prof.title}</h4>
                      <p className="text-xs text-slate-400">
                        {prof.code} • {prof.department} • Sem {prof.semester}
                      </p>
                    </div>
                    <span className="px-2.5 py-1 rounded-full text-xs bg-emerald-500/20 text-emerald-300">
                      {prof.category}
                    </span>
                  </div>
                  <p className="text-xs font-semibold text-cyan-400">
                    Technical Interests: {prof.subtitle}
                  </p>
                  <p className="text-sm text-slate-300">{prof.metadataText}</p>
                  <div className="flex justify-end">
                    <button
                      onClick={() =>
                        handleSendJoinRequest(
                          `Team Invite: ${prof.title} -> Team AuraGrid`,
                          prof.department,
                          prof.category,
                          'TEAM INVITATION',
                          `Invited ${prof.title} (${prof.code}) to collaborate on Team AuraGrid.`
                        )
                      }
                      className="px-3.5 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold"
                    >
                      Invite to Team
                    </button>
                  </div>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* 5. TEAM WORKSPACE */}
      {subTab === 'TEAM_WORKSPACE' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
            <h3 className="text-base font-bold text-white">Create Interdisciplinary Project Team</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <input
                value={teamName}
                onChange={(e) => setTeamName(e.target.value)}
                placeholder="Team Name & Project Title"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={teamDepts}
                onChange={(e) => setTeamDepts(e.target.value)}
                placeholder="Participating Departments"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={teamTech}
                onChange={(e) => setTeamTech(e.target.value)}
                placeholder="Selected Technologies"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={teamObjectives}
                onChange={(e) => setTeamObjectives(e.target.value)}
                placeholder="Objectives, Role Assignments & Workspace Links"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                if (!teamName.trim()) return;
                onCreateItem({
                  moduleType: 'COLLAB_TEAM',
                  code: `CTM-2026-${Math.floor(100 + Math.random() * 900)}`,
                  title: teamName.trim(),
                  subtitle: `Lead: ${currentUserName} • Tech: ${teamTech}`,
                  department: teamDepts,
                  semester: 6,
                  status: 'IN_PROGRESS',
                  category: teamCat,
                  primaryMetric: 25,
                  secondaryMetric: 5,
                  metadataText:
                    teamObjectives ||
                    'Interdisciplinary workspace with shared GitHub repo, milestone tracker, and architecture docs.',
                  ownerName: currentUserName,
                });
                setTeamName('');
                setTeamObjectives('');
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold"
            >
              Create Team Workspace
            </button>
          </div>

          {teams.map((team) => (
            <div
              key={team.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4"
            >
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-mono bg-cyan-500/20 text-cyan-300">
                    {team.code}
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full text-xs bg-blue-500/20 text-blue-300">
                    {team.status}
                  </span>
                </div>
                <button
                  onClick={() => onDeleteItem(team.id)}
                  className="text-rose-400 hover:text-rose-300 text-xs inline-flex items-center gap-1"
                >
                  <Trash2 className="w-3.5 h-3.5" /> Remove
                </button>
              </div>
              <h4 className="text-lg font-bold text-white">{team.title}</h4>
              <p className="text-xs text-cyan-400">{team.subtitle}</p>
              <p className="text-sm text-slate-300">{team.metadataText}</p>
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-slate-400">Workspace Progress</span>
                  <span className="text-emerald-400 font-bold">{team.primaryMetric}%</span>
                </div>
                <div className="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-blue-500 to-emerald-400"
                    style={{ width: `${Math.min(100, team.primaryMetric)}%` }}
                  />
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                {['PLANNING', 'IN_PROGRESS', 'UNDER_REVIEW', 'COMPLETED', 'ON_HOLD'].map((st) => (
                  <button
                    key={st}
                    onClick={() =>
                      onUpdateItem(team.id, {
                        status: st,
                        primaryMetric: st === 'COMPLETED' ? 100 : team.primaryMetric,
                      })
                    }
                    className={`px-3 py-1 rounded-lg text-xs font-semibold ${
                      team.status === st
                        ? 'bg-blue-600 text-white'
                        : 'bg-slate-800 text-slate-300'
                    }`}
                  >
                    {st.replace('_', ' ')}
                  </button>
                ))}
                <button
                  onClick={() =>
                    onUpdateItem(team.id, {
                      primaryMetric: Math.min(100, team.primaryMetric + 10),
                    })
                  }
                  className="px-3 py-1 rounded-lg text-xs font-semibold bg-emerald-500/20 text-emerald-300"
                >
                  +10% Progress
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 6. TASKS & MILESTONES */}
      {subTab === 'TASKS_MILESTONES' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
            <h3 className="text-base font-bold text-white">Assign Team Task / Milestone</h3>
            <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
              <input
                value={taskTitle}
                onChange={(e) => setTaskTitle(e.target.value)}
                placeholder="Task Title"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={taskTeam}
                onChange={(e) => setTaskTeam(e.target.value)}
                placeholder="Team & Milestone"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={taskAssignee}
                onChange={(e) => setTaskAssignee(e.target.value)}
                placeholder="Assigned Member"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <select
                value={taskPriority}
                onChange={(e) => setTaskPriority(e.target.value)}
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              >
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>
              <input
                type="date"
                value={taskDeadline}
                onChange={(e) => setTaskDeadline(e.target.value)}
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                if (!taskTitle.trim()) return;
                onCreateItem({
                  moduleType: 'COLLAB_TASK',
                  code: `TSK-2026-${Math.floor(100 + Math.random() * 900)}`,
                  title: taskTitle.trim(),
                  subtitle: taskTeam,
                  department: 'Interdisciplinary Team',
                  semester: 6,
                  status: 'TO_DO',
                  category: taskPriority,
                  primaryMetric: 0,
                  secondaryMetric: 1,
                  metadataText: `Assigned to: ${taskAssignee} | Deadline: ${taskDeadline}`,
                  ownerName: taskAssignee,
                });
                setTaskTitle('');
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold"
            >
              Add Task
            </button>
          </div>

          <div className="flex gap-2">
            {['ALL', 'TO_DO', 'IN_PROGRESS', 'COMPLETED'].map((st) => (
              <button
                key={st}
                onClick={() => setTaskStatusFilter(st)}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold ${
                  taskStatusFilter === st
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-900 text-slate-300 border border-slate-800'
                }`}
              >
                {st.replace('_', ' ')}
              </button>
            ))}
          </div>

          {tasks
            .filter((t) => taskStatusFilter === 'ALL' || t.status === taskStatusFilter)
            .map((task) => (
              <div
                key={task.id}
                className="rounded-2xl bg-slate-900 border border-slate-800 p-5 flex flex-wrap items-center justify-between gap-4"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded text-xs font-mono bg-cyan-500/20 text-cyan-300">
                      {task.code}
                    </span>
                    <span className="px-2 py-0.5 rounded text-xs bg-amber-500/20 text-amber-300">
                      Priority: {task.category}
                    </span>
                    <span className="px-2 py-0.5 rounded text-xs bg-emerald-500/20 text-emerald-300">
                      {task.status}
                    </span>
                  </div>
                  <h4 className="text-base font-bold text-white">{task.title}</h4>
                  <p className="text-xs text-blue-400">{task.subtitle}</p>
                  <p className="text-xs text-slate-400">{task.metadataText}</p>
                </div>
                <div className="flex gap-2">
                  {['TO_DO', 'IN_PROGRESS', 'COMPLETED'].map((st) => (
                    <button
                      key={st}
                      onClick={() => onUpdateItem(task.id, { status: st })}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold ${
                        task.status === st
                          ? 'bg-emerald-500 text-slate-950'
                          : 'bg-slate-800 text-slate-300'
                      }`}
                    >
                      {st.replace('_', ' ')}
                    </button>
                  ))}
                </div>
              </div>
            ))}
        </div>
      )}

      {/* 7. DISCUSSIONS */}
      {subTab === 'DISCUSSIONS' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white">Post Team Discussion / Update</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <input
                value={discTitle}
                onChange={(e) => setDiscTitle(e.target.value)}
                placeholder="Topic Headline"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={discChannel}
                onChange={(e) => setDiscChannel(e.target.value)}
                placeholder="Team Channel"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <textarea
              value={discBody}
              onChange={(e) => setDiscBody(e.target.value)}
              placeholder="Write your project update, technical question, or announcement..."
              rows={2}
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <button
              onClick={() => {
                if (!discTitle.trim() || !discBody.trim()) return;
                onCreateItem({
                  moduleType: 'COLLAB_DISCUSSION',
                  code: `DSC-2026-${Math.floor(100 + Math.random() * 900)}`,
                  title: discTitle.trim(),
                  subtitle: discChannel,
                  department: activeRole.label,
                  semester: 6,
                  status: 'ACTIVE',
                  category: 'PROJECT UPDATE',
                  primaryMetric: 1,
                  secondaryMetric: 1,
                  metadataText: `Posted by ${currentUserName}: ${discBody.trim()}`,
                  ownerName: currentUserName,
                });
                setDiscTitle('');
                setDiscBody('');
              }}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-cyan-500 text-slate-950 font-semibold text-sm"
            >
              <Send className="w-4 h-4" /> Post Message
            </button>
          </div>

          {discussions.map((d) => (
            <div
              key={d.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
            >
              <div className="flex justify-between text-xs text-cyan-400">
                <span>{d.subtitle}</span>
                <span>Author: {d.ownerName}</span>
              </div>
              <h4 className="text-base font-bold text-white">{d.title}</h4>
              <p className="text-sm text-slate-300 whitespace-pre-line">{d.metadataText}</p>
            </div>
          ))}
        </div>
      )}

      {/* 8. PROJECT SHOWCASE */}
      {subTab === 'PROJECT_SHOWCASE' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white">Publish Completed Project to Showcase</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <input
                value={showcaseTitle}
                onChange={(e) => setShowcaseTitle(e.target.value)}
                placeholder="Completed Project Title"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <select
                value={showcaseCat}
                onChange={(e) => setShowcaseCat(e.target.value)}
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              >
                {PROJECT_CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
              <input
                value={showcaseTech}
                onChange={(e) => setShowcaseTech(e.target.value)}
                placeholder="Technologies Used"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <textarea
              value={showcaseMeta}
              onChange={(e) => setShowcaseMeta(e.target.value)}
              rows={2}
              placeholder="Problem Statement, Solution Overview, GitHub Link, Demo Video Link & Outcomes"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <button
              onClick={() => {
                if (!showcaseTitle.trim()) return;
                onCreateItem({
                  moduleType: 'COLLAB_SHOWCASE',
                  code: `SHW-2026-${Math.floor(100 + Math.random() * 900)}`,
                  title: showcaseTitle.trim(),
                  subtitle: `Tech: ${showcaseTech}`,
                  department: 'Interdisciplinary Innovation',
                  semester: 8,
                  status: 'COMPLETED',
                  category: showcaseCat,
                  primaryMetric: 9.8,
                  secondaryMetric: 4,
                  metadataText: showcaseMeta,
                  ownerName: currentUserName,
                });
                setShowcaseTitle('');
              }}
              className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-semibold text-sm"
            >
              Publish to Showcase
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <input
              value={showcaseSearch}
              onChange={(e) => setShowcaseSearch(e.target.value)}
              placeholder="Search showcase by title, technology, outcome..."
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            />
            <select
              value={showcaseCatFilter}
              onChange={(e) => setShowcaseCatFilter(e.target.value)}
              className="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-sm"
            >
              <option value="ALL">All Categories</option>
              {PROJECT_CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {showcases
              .filter((s) => {
                const matchCat = showcaseCatFilter === 'ALL' || s.category === showcaseCatFilter;
                const matchQ =
                  !showcaseSearch ||
                  s.title.toLowerCase().includes(showcaseSearch.toLowerCase()) ||
                  s.subtitle.toLowerCase().includes(showcaseSearch.toLowerCase()) ||
                  s.metadataText.toLowerCase().includes(showcaseSearch.toLowerCase());
                return matchCat && matchQ;
              })
              .map((shw) => (
                <div
                  key={shw.id}
                  className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3"
                >
                  <div className="flex justify-between items-center">
                    <span className="px-2.5 py-0.5 rounded-full text-xs bg-emerald-500/20 text-emerald-300">
                      {shw.code} • {shw.category}
                    </span>
                    <span className="text-xs font-bold text-amber-400">
                      Impact Score: {shw.primaryMetric}/10
                    </span>
                  </div>
                  <h4 className="text-lg font-bold text-white">{shw.title}</h4>
                  <p className="text-xs text-cyan-400">{shw.subtitle}</p>
                  <p className="text-sm text-slate-300">{shw.metadataText}</p>
                  <p className="text-xs text-slate-400">Contributors: {shw.ownerName}</p>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* 9. FACULTY MENTORSHIP */}
      {subTab === 'FACULTY_MENTORSHIP' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white">Request Faculty Mentorship</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <input
                value={mentorName}
                onChange={(e) => setMentorName(e.target.value)}
                placeholder="Faculty Mentor Name"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={mentorProject}
                onChange={(e) => setMentorProject(e.target.value)}
                placeholder="Project & Team Name"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <textarea
              value={mentorNotes}
              onChange={(e) => setMentorNotes(e.target.value)}
              rows={2}
              placeholder="Mentorship goals & milestone review request"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <button
              onClick={() => {
                if (!mentorName.trim() || !mentorProject.trim()) return;
                onCreateItem({
                  moduleType: 'COLLAB_MENTORSHIP',
                  code: `MNT-2026-${Math.floor(100 + Math.random() * 900)}`,
                  title: `Mentorship: ${mentorProject}`,
                  subtitle: `Mentor: ${mentorName} • Requested by ${currentUserName}`,
                  department: 'Computer Science & Engineering',
                  semester: 6,
                  status: 'PENDING',
                  category: 'FACULTY MENTORSHIP',
                  primaryMetric: 90,
                  secondaryMetric: 4,
                  metadataText: mentorNotes,
                  ownerName: mentorName,
                });
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold"
            >
              Submit Mentorship Request
            </button>
          </div>

          {mentorships.map((mnt) => (
            <div
              key={mnt.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3"
            >
              <div className="flex items-center justify-between">
                <span className="px-2.5 py-0.5 rounded-full text-xs bg-cyan-500/20 text-cyan-300">
                  {mnt.code} • {mnt.status}
                </span>
                <span className="text-xs text-slate-400">{mnt.department}</span>
              </div>
              <h4 className="text-base font-bold text-white">{mnt.title}</h4>
              <p className="text-xs text-blue-400">{mnt.subtitle}</p>
              <p className="text-sm text-slate-300">{mnt.metadataText}</p>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() =>
                    onUpdateItem(mnt.id, {
                      status: 'ACCEPTED',
                      metadataText: `${mnt.metadataText} | Approved & Reviewed by ${currentUserName}: Milestone sign-off granted.`,
                    })
                  }
                  className="px-3 py-1.5 rounded-lg bg-emerald-500 text-slate-950 text-xs font-semibold"
                >
                  Approve & Sign-Off Milestone
                </button>
                <button
                  onClick={() =>
                    onUpdateItem(mnt.id, {
                      status: 'REJECTED',
                      metadataText: `${mnt.metadataText} | Declined due to schedule conflict.`,
                    })
                  }
                  className="px-3 py-1.5 rounded-lg bg-rose-500/20 text-rose-300 text-xs font-semibold"
                >
                  Decline
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
