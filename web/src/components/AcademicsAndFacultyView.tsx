import React, { useState } from 'react';
import {
  AlertCircle,
  BookOpen,
  Building2,
  Calendar,
  Plus,
  Trash2,
  Users,
} from 'lucide-react';
import { CampusModuleItem, FacultyRecord } from '../types';

interface AcademicsAndFacultyViewProps {
  mode: 'FACULTY' | 'ACADEMICS';
  faculty: FacultyRecord[];
  campusItems: CampusModuleItem[];
  onAddFaculty: (f: Omit<FacultyRecord, 'id' | 'userId'>) => void;
  onDeleteFaculty: (id: string) => void;
  onCreateItem: (item: Omit<CampusModuleItem, 'id' | 'userId'>) => void;
  onDeleteItem: (id: string) => void;
  onNotify: (msg: string) => void;
}

export const AcademicsAndFacultyView: React.FC<AcademicsAndFacultyViewProps> = ({
  mode,
  faculty,
  campusItems,
  onAddFaculty,
  onDeleteFaculty,
  onCreateItem,
  onDeleteItem,
  onNotify,
}) => {
  // Faculty form state
  const [facCode, setFacCode] = useState('FAC-CSE-112');
  const [facName, setFacName] = useState('');
  const [facEmail, setFacEmail] = useState('');
  const [facDept, setFacDept] = useState('Computer Science & Engineering');
  const [facDesig, setFacDesig] = useState('Associate Professor');
  const [facQual, setFacQual] = useState('Ph.D. in Artificial Intelligence');
  const [facSubjects, setFacSubjects] = useState('CS604 Deep Learning, CS608 Cloud Native');
  const [facWorkload, setFacWorkload] = useState(16);
  const [facExp, setFacExp] = useState(9);

  // Academics sub-tab state
  const [acadSubTab, setAcadSubTab] = useState<'DEPARTMENTS' | 'COURSES' | 'TIMETABLE'>('TIMETABLE');
  const [deptCode, setDeptCode] = useState('DEPT-AI');
  const [deptName, setDeptName] = useState('');
  const [deptHod, setDeptHod] = useState('Dr. Elena Rostova');

  const [courseCode, setCourseCode] = useState('CS609');
  const [courseTitle, setCourseTitle] = useState('');
  const [courseDept, setCourseDept] = useState('Computer Science & Engineering');
  const [courseCredits, setCourseCredits] = useState(4);
  const [courseSem, setCourseSem] = useState(6);

  const [ttDay, setTtDay] = useState('MONDAY');
  const [ttTime, setTtTime] = useState('11:00 - 12:30 PM');
  const [ttSubject, setTtSubject] = useState('CS604 AI Architecture');
  const [ttRoom, setTtRoom] = useState('Smart Lecture Hall A-301');
  const [ttInstructor, setTtInstructor] = useState('Dr. Elena Rostova');
  const [ttDept, setTtDept] = useState('Computer Science & Engineering');
  const [ttSem, setTtSem] = useState(6);

  if (mode === 'FACULTY') {
    return (
      <div className="space-y-6">
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Users className="w-4 h-4 text-cyan-400" /> Add Faculty Member & Subject Allocation
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <input
              value={facCode}
              onChange={(e) => setFacCode(e.target.value)}
              placeholder="Faculty ID (FAC-CSE-112)"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={facName}
              onChange={(e) => setFacName(e.target.value)}
              placeholder="Full Name"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={facEmail}
              onChange={(e) => setFacEmail(e.target.value)}
              placeholder="Institutional Email"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={facDept}
              onChange={(e) => setFacDept(e.target.value)}
              placeholder="Department"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={facDesig}
              onChange={(e) => setFacDesig(e.target.value)}
              placeholder="Designation"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={facQual}
              onChange={(e) => setFacQual(e.target.value)}
              placeholder="Highest Qualification"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={facSubjects}
              onChange={(e) => setFacSubjects(e.target.value)}
              placeholder="Assigned Subjects"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm md:col-span-2"
            />
            <input
              type="number"
              value={facWorkload}
              onChange={(e) => setFacWorkload(Number(e.target.value))}
              placeholder="Weekly Workload Hours"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
          </div>
          <button
            onClick={() => {
              if (!facName.trim()) return;
              onAddFaculty({
                facultyCode: facCode.trim(),
                fullName: facName.trim(),
                email: facEmail.trim() || `${facCode.toLowerCase()}@campusorbit.edu`,
                department: facDept,
                designation: facDesig,
                qualification: facQual,
                assignedSubjects: facSubjects,
                workloadHours: facWorkload,
                experienceYears: facExp,
              });
              setFacName('');
              setFacEmail('');
            }}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold"
          >
            <Plus className="w-4 h-4" /> Register Faculty Profile
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {faculty.map((f) => (
            <div
              key={f.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3"
            >
              <div className="flex items-center justify-between">
                <span className="px-2.5 py-0.5 rounded-full text-xs font-mono bg-cyan-500/20 text-cyan-300">
                  {f.facultyCode}
                </span>
                <button
                  onClick={() => onDeleteFaculty(f.id)}
                  className="text-rose-400 hover:text-rose-300 text-xs inline-flex items-center gap-1"
                >
                  <Trash2 className="w-3.5 h-3.5" /> Remove
                </button>
              </div>
              <div>
                <h4 className="text-lg font-bold text-white">{f.fullName}</h4>
                <p className="text-xs text-blue-400 font-semibold">
                  {f.designation} • {f.department}
                </p>
              </div>
              <p className="text-xs text-slate-300">Qualification: {f.qualification}</p>
              <p className="text-xs text-emerald-400">Assigned Subjects: {f.assignedSubjects}</p>
              <div className="flex items-center justify-between text-xs text-slate-400 pt-1 border-t border-slate-800">
                <span>Workload: {f.workloadHours} hrs/week</span>
                <span>Experience: {f.experienceYears} years</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const departments = campusItems.filter((i) => i.moduleType === 'DEPARTMENT');
  const courses = campusItems.filter((i) => i.moduleType === 'COURSE');
  const timetables = campusItems.filter((i) => i.moduleType === 'TIMETABLE');

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap gap-2 bg-slate-900 border border-slate-800 p-2 rounded-2xl">
        {[
          { key: 'TIMETABLE', label: `Conflict-Free Timetable (${timetables.length})`, icon: <Calendar className="w-4 h-4" /> },
          { key: 'DEPARTMENTS', label: `Departments (${departments.length})`, icon: <Building2 className="w-4 h-4" /> },
          { key: 'COURSES', label: `Credit Courses (${courses.length})`, icon: <BookOpen className="w-4 h-4" /> },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setAcadSubTab(t.key as typeof acadSubTab)}
            className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition ${
              acadSubTab === t.key
                ? 'bg-blue-600 text-white'
                : 'bg-slate-800/70 text-slate-300 hover:bg-slate-800'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {acadSubTab === 'TIMETABLE' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white">
                Schedule Timetable Slot (Automatic Conflict Detection)
              </h3>
              <span className="text-xs text-cyan-400 flex items-center gap-1">
                <AlertCircle className="w-3.5 h-3.5" /> Prevents room & instructor double-booking
              </span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <select
                value={ttDay}
                onChange={(e) => setTtDay(e.target.value)}
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              >
                {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'].map((d) => (
                  <option key={d} value={d}>
                    {d}
                  </option>
                ))}
              </select>
              <input
                value={ttTime}
                onChange={(e) => setTtTime(e.target.value)}
                placeholder="Time Slot (e.g., 09:00 - 10:30 AM)"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={ttSubject}
                onChange={(e) => setTtSubject(e.target.value)}
                placeholder="Course Code & Subject"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={ttRoom}
                onChange={(e) => setTtRoom(e.target.value)}
                placeholder="Classroom / Lab"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={ttInstructor}
                onChange={(e) => setTtInstructor(e.target.value)}
                placeholder="Assigned Faculty"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={ttDept}
                onChange={(e) => setTtDept(e.target.value)}
                placeholder="Department"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                const fullTitle = `${ttTime} • ${ttSubject}`;
                const fullSub = `${ttRoom} • Sem ${ttSem}`;
                const conflict = timetables.some(
                  (t) =>
                    t.category.toUpperCase() === ttDay.toUpperCase() &&
                    t.title.slice(0, 10).toLowerCase() === fullTitle.slice(0, 10).toLowerCase() &&
                    (t.ownerName.toLowerCase() === ttInstructor.trim().toLowerCase() ||
                      t.subtitle.toLowerCase().includes(ttRoom.trim().toLowerCase()))
                );
                if (conflict) {
                  onNotify(
                    `Scheduling Conflict Detected: ${ttInstructor} or ${ttRoom} is already booked on ${ttDay} at ${ttTime}!`
                  );
                  return;
                }
                onCreateItem({
                  moduleType: 'TIMETABLE',
                  code: `TT-${ttDay.slice(0, 3)}-${Math.floor(10 + Math.random() * 89)}`,
                  title: fullTitle,
                  subtitle: fullSub,
                  department: ttDept,
                  semester: ttSem,
                  status: 'SCHEDULED',
                  category: ttDay,
                  primaryMetric: 1.5,
                  secondaryMetric: 60,
                  metadataText: `Conflict-Free Verified Slot • Instructor: ${ttInstructor}`,
                  ownerName: ttInstructor,
                });
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold"
            >
              Add Timetable Slot
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {timetables.map((tt) => (
              <div
                key={tt.id}
                className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
              >
                <div className="flex justify-between items-center">
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-mono bg-cyan-500/20 text-cyan-300">
                    {tt.category} • {tt.code}
                  </span>
                  <button
                    onClick={() => onDeleteItem(tt.id)}
                    className="text-rose-400 text-xs"
                  >
                    Remove
                  </button>
                </div>
                <h4 className="text-base font-bold text-white">{tt.title}</h4>
                <p className="text-xs text-blue-400">{tt.subtitle}</p>
                <p className="text-xs text-slate-300">{tt.metadataText}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {acadSubTab === 'DEPARTMENTS' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white">Create Academic Department</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <input
                value={deptCode}
                onChange={(e) => setDeptCode(e.target.value)}
                placeholder="Department Code"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={deptName}
                onChange={(e) => setDeptName(e.target.value)}
                placeholder="Department Title"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={deptHod}
                onChange={(e) => setDeptHod(e.target.value)}
                placeholder="Head of Department (HOD)"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                if (!deptName.trim()) return;
                onCreateItem({
                  moduleType: 'DEPARTMENT',
                  code: deptCode,
                  title: deptName,
                  subtitle: `HOD: ${deptHod}`,
                  department: deptName,
                  semester: 8,
                  status: 'ACTIVE',
                  category: 'Engineering & Research',
                  primaryMetric: 9.0,
                  secondaryMetric: 320,
                  metadataText: 'NBA & NAAC Accredited Department with Specialized Research Labs',
                  ownerName: deptHod,
                });
                setDeptName('');
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
            >
              Add Department
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {departments.map((d) => (
              <div
                key={d.id}
                className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
              >
                <span className="text-xs font-mono text-cyan-400">{d.code}</span>
                <h4 className="text-lg font-bold text-white">{d.title}</h4>
                <p className="text-xs text-blue-400">{d.subtitle}</p>
                <p className="text-sm text-slate-300">{d.metadataText}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {acadSubTab === 'COURSES' && (
        <div className="space-y-6">
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white">Add Credit Course</h3>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              <input
                value={courseCode}
                onChange={(e) => setCourseCode(e.target.value)}
                placeholder="Course Code"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={courseTitle}
                onChange={(e) => setCourseTitle(e.target.value)}
                placeholder="Course Title"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                value={courseDept}
                onChange={(e) => setCourseDept(e.target.value)}
                placeholder="Department"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
              <input
                type="number"
                value={courseCredits}
                onChange={(e) => setCourseCredits(Number(e.target.value))}
                placeholder="Credits"
                className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
              />
            </div>
            <button
              onClick={() => {
                if (!courseTitle.trim()) return;
                onCreateItem({
                  moduleType: 'COURSE',
                  code: courseCode,
                  title: courseTitle,
                  subtitle: `${courseCredits} Credits • Sem ${courseSem}`,
                  department: courseDept,
                  semester: courseSem,
                  status: 'ACTIVE',
                  category: 'CORE',
                  primaryMetric: courseCredits,
                  secondaryMetric: 60,
                  metadataText: 'Outcome-Based Education (OBE) curriculum with lab assessment.',
                  ownerName: 'Academic Council',
                });
                setCourseTitle('');
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
            >
              Publish Course
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {courses.map((c) => (
              <div
                key={c.id}
                className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
              >
                <span className="text-xs font-mono text-cyan-400">{c.code}</span>
                <h4 className="text-base font-bold text-white">{c.title}</h4>
                <p className="text-xs text-blue-400">{c.subtitle}</p>
                <p className="text-xs text-slate-300">{c.metadataText}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
