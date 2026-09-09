"use client";

import { AppShell, EmptyState, ErrorBanner, Skeleton } from "@/components/AppShell";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { auditActionLabel, formatWhen } from "@/lib/privacy";
import { AuditEntry, PatientSummary } from "@/lib/types";
import { useEffect, useState } from "react";

export default function AuditPage() {
  const { user } = useAuth();
  const [items, setItems] = useState<AuditEntry[]>([]);
  const [patients, setPatients] = useState<PatientSummary[]>([]);
  const [selected, setSelected] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    if (user.role === "PATIENT") {
      api<AuditEntry[]>("/me/audit")
        .then(setItems)
        .catch((e: Error) => setError(e.message))
        .finally(() => setLoading(false));
    } else if (user.role === "ADMIN") {
      api<AuditEntry[]>("/audit")
        .then(setItems)
        .catch((e: Error) => setError(e.message))
        .finally(() => setLoading(false));
    } else {
      api<PatientSummary[]>("/patients")
        .then(setPatients)
        .catch((e: Error) => setError(e.message))
        .finally(() => setLoading(false));
    }
  }, [user]);

  async function loadPatient(id: string) {
    setSelected(id);
    setError("");
    try {
      const rows = await api<AuditEntry[]>(`/patients/${id}/audit`);
      setItems(rows);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Cannot load audit");
    }
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Access timeline</h1>
      <p className="mt-2 text-sm text-ink/70">
        Every PHI-adjacent read or write is recorded. Care-note bodies are never stored in the log.
      </p>
      {error ? (
        <div className="mt-4">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      {user?.role === "CLINICIAN" ? (
        <div className="mt-4 flex flex-wrap gap-2">
          {patients.map((p) => (
            <button
              key={p.userId}
              className={`rounded-full px-4 py-2 text-sm ${selected === p.userId ? "bg-teal text-white" : "bg-white"}`}
              onClick={() => loadPatient(p.userId)}
            >
              {p.firstName} {p.lastName}
            </button>
          ))}
        </div>
      ) : null}
      {loading ? <Skeleton className="mt-6" /> : null}
      {!loading && items.length === 0 ? (
        <div className="mt-6">
          <EmptyState title="No access events yet" body="Bookings, consents, and record views appear here." />
        </div>
      ) : (
        <ol className="mt-6 space-y-3">
          {items.map((e) => (
            <li key={e.id} className="rounded-2xl border border-teal/10 bg-white px-5 py-4">
              <p className="font-medium">{auditActionLabel(e.action)}</p>
              <p className="text-sm text-ink/65">
                {e.actorEmail} {e.actorRole ? `· ${e.actorRole}` : ""} · {formatWhen(e.createdAt)}
              </p>
              {e.detail ? <p className="mt-1 text-xs text-ink/50">{e.detail}</p> : null}
            </li>
          ))}
        </ol>
      )}
    </AppShell>
  );
}
