import React, { useState } from 'react';
import {
  Award,
  BookOpen,
  CheckCircle2,
  CreditCard,
  FileText,
  MessageSquareWarning,
  Plus,
  QrCode,
} from 'lucide-react';
import { CampusModuleItem, GrievanceRecord, StudentRecord } from '../types';

interface ExamsAndFinanceViewProps {
  mode: 'EXAMS_ASSIGNMENTS' | 'FINANCE_GRIEVANCES';
  students: StudentRecord[];
  campusItems: CampusModuleItem[];
  grievances: GrievanceRecord[];
  onCreateItem: (item: Omit<CampusModuleItem, 'id' | 'userId'>) => void;
  onUpdateItem: (id: string, patch: Partial<CampusModuleItem>) => void;
  onFileGrievance: (g: Omit<GrievanceRecord, 'id' | 'userId' | 'ticketNumber' | 'status' | 'assignedTo' | 'resolutionNotes'>) => void;
  onAdvanceGrievance: (id: string, notes: string) => void;
}

export const ExamsAndFinanceView: React.FC<ExamsAndFinanceViewProps> = ({
  mode,
  students,
  campusItems,
  grievances,
  onCreateItem,
  onUpdateItem,
  onFileGrievance,
  onAdvanceGrievance,
}) => {
  // Exam & Assignment state
  const [examStudent, setExamStudent] = useState(students[0]?.fullName || 'Aarav Mehta (STU-2026-001)');
  const [examSgpa, setExamSgpa] = useState(9.4);
  const [examSummary, setExamSummary] = useState('CS601: 96/100 (O) | CS604: 91/100 (A+) | Hall Ticket Verified');

  const [asgTitle, setAsgTitle] = useState('');
  const [asgCourse, setAsgCourse] = useState('CS601 Distributed Cloud Systems');
  const [asgMaxMarks, setAsgMaxMarks] = useState(50);

  // Finance & Grievance state
  const [invStudent, setInvStudent] = useState('Aarav Mehta (STU-2026-001)');
  const [invCategory, setInvCategory] = useState('TUITION & LAB FEE');
  const [invAmount, setInvAmount] = useState(2450);

  const [grvTitle, setGrvTitle] = useState('');
  const [grvCategory, setGrvCategory] = useState('Infrastructure & IT');
  const [grvPriority, setGrvPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'>('HIGH');
  const [grvDesc, setGrvDesc] = useState('');

  if (mode === 'EXAMS_ASSIGNMENTS') {
    const examResults = campusItems.filter((i) => i.moduleType === 'EXAM_RESULT');
    const assignments = campusItems.filter((i) => i.moduleType === 'ASSIGNMENT');

    return (
      <div className="space-y-6">
        {/* Digital Hall Ticket Card */}
        <div className="rounded-2xl bg-gradient-to-r from-slate-900 via-blue-950 to-slate-900 border border-cyan-500/30 p-5 flex flex-wrap items-center justify-between gap-4">
          <div className="space-y-1">
            <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-mono bg-emerald-500/20 text-emerald-300">
              <CheckCircle2 className="w-3.5 h-3.5" /> DIGITAL HALL TICKET VERIFIED (ATTENDANCE &ge; 75%)
            </span>
            <h3 className="text-lg font-bold text-white">
              End-Semester Examinations May–June 2026 • Hall Ticket #HT-ORBIT-2026-001
            </h3>
            <p className="text-xs text-slate-300">
              Scholar: Aarav Mehta (STU-2026-001) • Center: Block A Smart Examination Hall • Seat: A-14
            </p>
          </div>
          <div className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-xs font-mono text-cyan-300">
            <QrCode className="w-5 h-5 text-cyan-400" /> QR-HT-STU2026001-VALID
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Publish Exam Result */}
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Award className="w-4 h-4 text-cyan-400" /> Publish SGPA / Exam Result
            </h3>
            <input
              value={examStudent}
              onChange={(e) => setExamStudent(e.target.value)}
              placeholder="Student Name & ID"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              type="number"
              step="0.01"
              value={examSgpa}
              onChange={(e) => setExamSgpa(Number(e.target.value))}
              placeholder="SGPA (0 - 10)"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={examSummary}
              onChange={(e) => setExamSummary(e.target.value)}
              placeholder="Subject Marks Breakdown"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <button
              onClick={() => {
                const grade =
                  examSgpa >= 9.0 ? 'O (Outstanding)' : examSgpa >= 8.0 ? 'A+ (Excellent)' : 'A (Very Good)';
                onCreateItem({
                  moduleType: 'EXAM_RESULT',
                  code: `RES-${Math.floor(100 + Math.random() * 900)}`,
                  title: `${examStudent} • Semester Grade Report`,
                  subtitle: `SGPA: ${examSgpa.toFixed(2)} • Grade: ${grade}`,
                  department: 'Computer Science & Engineering',
                  semester: 6,
                  status: 'PUBLISHED',
                  category: 'END-SEMESTER',
                  primaryMetric: examSgpa,
                  secondaryMetric: 24,
                  metadataText: examSummary,
                  ownerName: 'Controller of Examinations',
                });
              }}
              className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
            >
              Publish Scorecard
            </button>
          </div>

          {/* Publish Assignment / LMS */}
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <BookOpen className="w-4 h-4 text-emerald-400" /> Publish Course Assignment / LMS Resource
            </h3>
            <input
              value={asgTitle}
              onChange={(e) => setAsgTitle(e.target.value)}
              placeholder="Assignment / Lab Project Title"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={asgCourse}
              onChange={(e) => setAsgCourse(e.target.value)}
              placeholder="Course Code & Name"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              type="number"
              value={asgMaxMarks}
              onChange={(e) => setAsgMaxMarks(Number(e.target.value))}
              placeholder="Maximum Marks"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <button
              onClick={() => {
                if (!asgTitle.trim()) return;
                onCreateItem({
                  moduleType: 'ASSIGNMENT',
                  code: `ASG-${Math.floor(100 + Math.random() * 900)}`,
                  title: asgTitle.trim(),
                  subtitle: `${asgCourse} • Max Marks: ${asgMaxMarks}`,
                  department: 'Computer Science & Engineering',
                  semester: 6,
                  status: 'OPEN',
                  category: 'LAB & LMS',
                  primaryMetric: asgMaxMarks,
                  secondaryMetric: asgMaxMarks,
                  metadataText: 'Rubric & starter repository attached in LMS portal.',
                  ownerName: 'Dr. Elena Rostova',
                });
                setAsgTitle('');
              }}
              className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 text-xs font-bold"
            >
              Publish Assignment
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {examResults.map((res) => (
            <div
              key={res.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
            >
              <span className="text-xs font-mono text-cyan-400">
                EXAM RESULT • {res.code}
              </span>
              <h4 className="text-base font-bold text-white">{res.title}</h4>
              <p className="text-xs font-bold text-emerald-400">{res.subtitle}</p>
              <p className="text-xs text-slate-300">{res.metadataText}</p>
            </div>
          ))}
          {assignments.map((asg) => (
            <div
              key={asg.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
            >
              <div className="flex justify-between">
                <span className="text-xs font-mono text-purple-400">
                  ASSIGNMENT • {asg.code}
                </span>
                <span className="text-xs text-amber-300">{asg.status}</span>
              </div>
              <h4 className="text-base font-bold text-white">{asg.title}</h4>
              <p className="text-xs text-blue-400">{asg.subtitle}</p>
              <p className="text-xs text-slate-300">{asg.metadataText}</p>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const invoices = campusItems.filter((i) => i.moduleType === 'FEE_INVOICE');

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Issue Fee Invoice */}
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <CreditCard className="w-4 h-4 text-cyan-400" /> Issue Student Fee Invoice
          </h3>
          <input
            value={invStudent}
            onChange={(e) => setInvStudent(e.target.value)}
            placeholder="Student Name & ID"
            className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <input
            value={invCategory}
            onChange={(e) => setInvCategory(e.target.value)}
            placeholder="Fee Head (Tuition / Hostel / Exam)"
            className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <input
            type="number"
            value={invAmount}
            onChange={(e) => setInvAmount(Number(e.target.value))}
            placeholder="Amount ($)"
            className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <button
            onClick={() => {
              onCreateItem({
                moduleType: 'FEE_INVOICE',
                code: `INV-2026-${Math.floor(100 + Math.random() * 900)}`,
                title: `${invStudent} • ${invCategory}`,
                subtitle: `Amount: $${invAmount.toLocaleString()} • Digital Receipt Ready`,
                department: 'Finance & Accounts',
                semester: 6,
                status: 'PENDING',
                category: invCategory,
                primaryMetric: invAmount,
                secondaryMetric: 0,
                metadataText: `Invoice generated for $${invAmount.toLocaleString()}.`,
                ownerName: 'Finance Office',
              });
            }}
            className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
          >
            Issue Fee Invoice
          </button>
        </div>

        {/* File Grievance */}
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <MessageSquareWarning className="w-4 h-4 text-amber-400" /> File Institutional Grievance Ticket
          </h3>
          <input
            value={grvTitle}
            onChange={(e) => setGrvTitle(e.target.value)}
            placeholder="Grievance Headline"
            className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <div className="grid grid-cols-2 gap-3">
            <input
              value={grvCategory}
              onChange={(e) => setGrvCategory(e.target.value)}
              placeholder="Category"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <select
              value={grvPriority}
              onChange={(e) => setGrvPriority(e.target.value as typeof grvPriority)}
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="CRITICAL">CRITICAL</option>
            </select>
          </div>
          <input
            value={grvDesc}
            onChange={(e) => setGrvDesc(e.target.value)}
            placeholder="Detailed description"
            className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <button
            onClick={() => {
              if (!grvTitle.trim()) return;
              onFileGrievance({
                title: grvTitle.trim(),
                category: grvCategory,
                description: grvDesc || 'Submitted via Campus Orbit Redressal Portal.',
                priority: grvPriority,
                submittedBy: 'Aarav Mehta (STU-2026-001)',
              });
              setGrvTitle('');
              setGrvDesc('');
            }}
            className="px-4 py-2 rounded-xl bg-amber-500 text-slate-950 text-xs font-bold"
          >
            Submit Grievance Ticket
          </button>
        </div>
      </div>

      {/* Invoices List */}
      <div className="space-y-3">
        <h3 className="text-base font-bold text-white">Tuition & Fee Invoices ({invoices.length})</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {invoices.map((inv) => (
            <div
              key={inv.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
            >
              <div className="flex justify-between items-center">
                <span className="text-xs font-mono text-cyan-400">{inv.code}</span>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                    inv.status === 'PAID'
                      ? 'bg-emerald-500/20 text-emerald-300'
                      : 'bg-amber-500/20 text-amber-300'
                  }`}
                >
                  {inv.status}
                </span>
              </div>
              <h4 className="text-base font-bold text-white">{inv.title}</h4>
              <p className="text-xs text-blue-400">{inv.subtitle}</p>
              <p className="text-xs text-slate-300">{inv.metadataText}</p>
              {inv.status !== 'PAID' && (
                <button
                  onClick={() =>
                    onUpdateItem(inv.id, {
                      status: 'PAID',
                      metadataText: `Paid in full ($${inv.primaryMetric}). Digital Receipt #ORBIT-RCP-${Math.floor(1000 + Math.random() * 9000)} issued.`,
                    })
                  }
                  className="px-3.5 py-1.5 rounded-xl bg-emerald-500 text-slate-950 text-xs font-bold"
                >
                  Mark as Paid & Generate Receipt
                </button>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Grievances List */}
      <div className="space-y-3">
        <h3 className="text-base font-bold text-white">
          Institutional Grievance Redressal Cell ({grievances.length})
        </h3>
        {grievances.map((g) => (
          <div
            key={g.id}
            className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
          >
            <div className="flex justify-between items-center">
              <span className="text-xs font-mono text-amber-400">
                {g.ticketNumber} • Priority: {g.priority}
              </span>
              <span className="px-2.5 py-0.5 rounded-full text-xs bg-blue-500/20 text-blue-300">
                {g.status}
              </span>
            </div>
            <h4 className="text-base font-bold text-white">{g.title}</h4>
            <p className="text-xs text-slate-300">{g.description}</p>
            <p className="text-xs text-emerald-400">Resolution Notes: {g.resolutionNotes}</p>
            {g.status !== 'RESOLVED' && (
              <button
                onClick={() =>
                  onAdvanceGrievance(
                    g.id,
                    'Action verified and resolved by Institutional Grievance Redressal Cell.'
                  )
                }
                className="px-3.5 py-1.5 rounded-xl bg-blue-600 text-white text-xs font-semibold"
              >
                Advance Status / Resolve Ticket
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
