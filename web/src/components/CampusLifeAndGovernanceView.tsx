import React, { useState } from 'react';
import {
  Award,
  Briefcase,
  Building,
  Calendar,
  FileCheck,
  ShieldCheck,
  UserCheck,
  Users,
} from 'lucide-react';
import { ROLES } from '../seedData';
import { CampusModuleItem, CampusModuleTypeKey, StudentRecord } from '../types';

interface CampusLifeAndGovernanceViewProps {
  mode: 'CAMPUS_LIFE' | 'PARENT_PORTAL' | 'SUPER_ADMIN_GOVERNANCE';
  institutionName: string;
  linkedStudentCode: string;
  students: StudentRecord[];
  campusItems: CampusModuleItem[];
  onUpdateInstitution: (name: string, linkedCode: string) => void;
  onCreateItem: (item: Omit<CampusModuleItem, 'id' | 'userId'>) => void;
  onUpdateItem: (id: string, patch: Partial<CampusModuleItem>) => void;
  onNotify: (msg: string) => void;
}

export const CampusLifeAndGovernanceView: React.FC<CampusLifeAndGovernanceViewProps> = ({
  mode,
  institutionName,
  linkedStudentCode,
  students,
  campusItems,
  onUpdateInstitution,
  onCreateItem,
  onUpdateItem,
  onNotify,
}) => {
  const [lifeType, setLifeType] = useState<CampusModuleTypeKey>('PLACEMENT_DRIVE');
  const [lifeTitle, setLifeTitle] = useState('');
  const [lifeSubtitle, setLifeSubtitle] = useState('');
  const [lifeMeta, setLifeMeta] = useState('');

  const [instInput, setInstInput] = useState(institutionName);
  const [wardInput, setWardInput] = useState(linkedStudentCode);
  const [auditTitle, setAuditTitle] = useState('');
  const [auditDetail, setAuditDetail] = useState('');

  if (mode === 'CAMPUS_LIFE') {
    const filtered = campusItems.filter((i) =>
      [
        'FACILITY_BOOKING',
        'CERTIFICATE_REQUEST',
        'EVENT',
        'STUDY_GROUP',
        'PLACEMENT_DRIVE',
      ].includes(i.moduleType)
    );

    return (
      <div className="space-y-6">
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
          <h3 className="text-base font-bold text-white">
            Publish Facility Booking, Certificate Request, Event, Study Group or Placement Drive
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <select
              value={lifeType}
              onChange={(e) => setLifeType(e.target.value as CampusModuleTypeKey)}
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            >
              <option value="PLACEMENT_DRIVE">Placement & Internship Drive</option>
              <option value="FACILITY_BOOKING">Facility & Resource Booking</option>
              <option value="CERTIFICATE_REQUEST">Certificate Request (Bonafide/NOC)</option>
              <option value="EVENT">Campus Event & Hackathon</option>
              <option value="STUDY_GROUP">Peer Study Group</option>
            </select>
            <input
              value={lifeTitle}
              onChange={(e) => setLifeTitle(e.target.value)}
              placeholder="Title (e.g., Main Auditorium / Microsoft Drive)"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={lifeSubtitle}
              onChange={(e) => setLifeSubtitle(e.target.value)}
              placeholder="Date/Slot or Package/Role"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
            <input
              value={lifeMeta}
              onChange={(e) => setLifeMeta(e.target.value)}
              placeholder="Eligibility, Capacity or Notes"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
            />
          </div>
          <button
            onClick={() => {
              if (!lifeTitle.trim()) return;
              if (lifeType === 'FACILITY_BOOKING') {
                const conflict = campusItems.some(
                  (i) =>
                    i.moduleType === 'FACILITY_BOOKING' &&
                    i.title.toLowerCase() === lifeTitle.trim().toLowerCase() &&
                    i.subtitle.toLowerCase() === lifeSubtitle.trim().toLowerCase()
                );
                if (conflict) {
                  onNotify(`Booking Conflict: ${lifeTitle} is already reserved for ${lifeSubtitle}!`);
                  return;
                }
              }
              onCreateItem({
                moduleType: lifeType,
                code: `${lifeType.slice(0, 3)}-2026-${Math.floor(100 + Math.random() * 900)}`,
                title: lifeTitle.trim(),
                subtitle: lifeSubtitle || 'Verified Campus Activity',
                department: 'All Departments',
                semester: 6,
                status: 'OPEN',
                category: lifeType,
                primaryMetric: 8.0,
                secondaryMetric: 50,
                metadataText: lifeMeta || 'Published via Campus Life & Career Cell.',
                ownerName: 'Campus Administration',
              });
              setLifeTitle('');
              setLifeSubtitle('');
              setLifeMeta('');
            }}
            className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
          >
            Publish Record
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filtered.map((item) => (
            <div
              key={item.id}
              className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-2"
            >
              <div className="flex justify-between">
                <span className="text-xs font-mono text-cyan-400">
                  {item.moduleType} • {item.code}
                </span>
                <span className="text-xs font-bold text-emerald-400">{item.status}</span>
              </div>
              <h4 className="text-base font-bold text-white">{item.title}</h4>
              <p className="text-xs text-blue-400">{item.subtitle}</p>
              <p className="text-xs text-slate-300">{item.metadataText}</p>
              <div className="flex gap-2 pt-1">
                <button
                  onClick={() => onUpdateItem(item.id, { status: 'APPROVED' })}
                  className="px-3 py-1 rounded-lg bg-emerald-500/20 text-emerald-300 text-xs font-semibold"
                >
                  Approve / Register
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (mode === 'PARENT_PORTAL') {
    const ward =
      students.find((s) => s.studentCode.toLowerCase() === wardInput.trim().toLowerCase()) ||
      students[0];

    return (
      <div className="space-y-6">
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <UserCheck className="w-4 h-4 text-cyan-400" /> Verified Parent & Guardian Oversight Portal
            </h3>
            <p className="text-xs text-slate-400">
              Real-time academic telemetry, attendance alerts, fee status & faculty mentor contact for your linked ward.
            </p>
          </div>
          <div className="flex items-center gap-2">
            <input
              value={wardInput}
              onChange={(e) => setWardInput(e.target.value)}
              placeholder="Ward Student ID (STU-2026-001)"
              className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs"
            />
            <button
              onClick={() => onUpdateInstitution(institutionName, wardInput)}
              className="px-3.5 py-2 rounded-xl bg-cyan-500 text-slate-950 text-xs font-bold"
            >
              Link Ward ID
            </button>
          </div>
        </div>

        {ward && (
          <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 space-y-4">
            <div className="flex flex-wrap justify-between items-center gap-4">
              <div>
                <span className="text-xs font-mono text-cyan-400">{ward.studentCode}</span>
                <h4 className="text-2xl font-bold text-white">{ward.fullName}</h4>
                <p className="text-sm text-slate-300">
                  {ward.department} • Semester {ward.semester}
                </p>
              </div>
              <div className="flex gap-3">
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-center">
                  <p className="text-xs text-slate-400">Attendance</p>
                  <p className="text-xl font-bold text-emerald-400">{ward.attendancePct}%</p>
                </div>
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-center">
                  <p className="text-xs text-slate-400">Cumulative CGPA</p>
                  <p className="text-xl font-bold text-blue-400">{ward.cgpa}</p>
                </div>
                <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-center">
                  <p className="text-xs text-slate-400">Fee Status</p>
                  <p className="text-xl font-bold text-cyan-400">{ward.feeStatus}</p>
                </div>
              </div>
            </div>
            <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 space-y-1">
              <p>
                <strong>Assigned Faculty Mentor:</strong> {ward.mentorName}
              </p>
              <p>
                <strong>Registered Guardian:</strong> {ward.guardianName} ({ward.guardianPhone})
              </p>
            </div>
          </div>
        )}
      </div>
    );
  }

  // SUPER_ADMIN_GOVERNANCE
  const auditLogs = campusItems.filter((i) => i.moduleType === 'AUDIT_LOG');

  return (
    <div className="space-y-6">
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4">
        <h3 className="text-base font-bold text-white flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-cyan-400" /> Super Administrator Institutional Configuration
        </h3>
        <div className="flex flex-wrap gap-3">
          <input
            value={instInput}
            onChange={(e) => setInstInput(e.target.value)}
            placeholder="Institution Name"
            className="flex-1 px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <button
            onClick={() => onUpdateInstitution(instInput, linkedStudentCode)}
            className="px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold"
          >
            Update Institution Identity
          </button>
        </div>
      </div>

      {/* RBAC Matrix */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
        <h3 className="text-base font-bold text-white">5-Role Access Control (RBAC) Matrix</h3>
        <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
          {ROLES.map((r) => (
            <div
              key={r.key}
              className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 space-y-1"
            >
              <span className="text-[10px] font-mono text-cyan-400">{r.badge}</span>
              <p className="text-sm font-bold text-white">{r.label}</p>
              <p className="text-xs text-slate-400">{r.tagline}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Security Audit Log */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-3">
        <h3 className="text-base font-bold text-white">Record Institutional Security Audit Event</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <input
            value={auditTitle}
            onChange={(e) => setAuditTitle(e.target.value)}
            placeholder="Audit Action Headline"
            className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
          <input
            value={auditDetail}
            onChange={(e) => setAuditDetail(e.target.value)}
            placeholder="Detailed Security / Policy Change Log"
            className="px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-sm"
          />
        </div>
        <button
          onClick={() => {
            if (!auditTitle.trim()) return;
            onCreateItem({
              moduleType: 'AUDIT_LOG',
              code: `AUD-2026-${Math.floor(100 + Math.random() * 900)}`,
              title: auditTitle.trim(),
              subtitle: 'Zero-Trust RBAC & Firestore Security Verification',
              department: 'Institutional Governance',
              semester: 0,
              status: 'LOGGED',
              category: 'SECURITY AUDIT',
              primaryMetric: 100,
              secondaryMetric: 1,
              metadataText: auditDetail || 'Logged by Super Administrator.',
              ownerName: 'Dr. Julian Vance',
            });
            setAuditTitle('');
            setAuditDetail('');
          }}
          className="px-4 py-2 rounded-xl bg-cyan-500 text-slate-950 text-xs font-bold"
        >
          Append Audit Log
        </button>

        <div className="space-y-2 pt-3">
          {auditLogs.map((log) => (
            <div
              key={log.id}
              className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 flex justify-between items-center"
            >
              <div>
                <p className="text-xs font-mono text-cyan-400">{log.code}</p>
                <p className="text-sm font-bold text-white">{log.title}</p>
                <p className="text-xs text-slate-400">{log.metadataText}</p>
              </div>
              <span className="text-xs text-emerald-400 font-mono">{log.status}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
