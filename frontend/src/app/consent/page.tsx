"use client";

import { AppShell, EmptyState, ErrorBanner } from "@/components/AppShell";
import { api } from "@/lib/api";
import { formatWhen } from "@/lib/privacy";
import { Clinician, Consent } from "@/lib/types";
import { FormEvent, useEffect, useState } from "react";

export default function ConsentPage() {
  const [consents, setConsents] = useState<Consent[]>([]);
  const [clinicians, setClinicians] = useState<Clinician[]>([]);
  const [clinicianId, setClinicianId] = useState("");
  const [error, setError] = useState("");

  function load() {
    api<Consent[]>("/consents").then(setConsents).catch((e: Error) => setError(e.message));
    api<Clinician[]>("/clinicians").then((rows) => {
      setClinicians(rows);
      setClinicianId((id) => id || rows[0]?.userId || "");
    });
  }

  useEffect(() => {
    load();
  }, []);

  async function grant(e: FormEvent) {
    e.preventDefault();
    setError("");
    try {
      await api("/consents", { method: "POST", body: JSON.stringify({ clinicianId }) });
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Grant failed");
    }
  }

  async function revoke(id: string) {
    setError("");
    try {
      await api(`/consents/${id}/revoke`, { method: "POST" });
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Revoke failed");
    }
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Consent</h1>
      <p className="mt-2 max-w-2xl text-sm text-ink/70">
        A clinician cannot read care notes until you grant consent. Revoking consent immediately closes that
        access. This is coordination software, not a clinical record of treatment.
      </p>
      {error ? (
        <div className="mt-4">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      <form onSubmit={grant} className="mt-6 flex flex-wrap gap-2">
        <select
          className="min-w-56 rounded-xl border bg-white px-3 py-2"
          value={clinicianId}
          onChange={(e) => setClinicianId(e.target.value)}
        >
          {clinicians.map((c) => (
            <option key={c.userId} value={c.userId}>
              {c.firstName} {c.lastName} · {c.specialty}
            </option>
          ))}
        </select>
        <button className="rounded-full bg-teal px-5 py-2 text-white">Grant consent</button>
      </form>
      {consents.length === 0 ? (
        <div className="mt-6">
          <EmptyState title="No consent records" body="Grant access to a clinician before they can open notes." />
        </div>
      ) : (
        <ul className="mt-6 space-y-3">
          {consents.map((c) => (
            <li key={c.id} className="rounded-2xl bg-white px-5 py-4">
              <p className="font-medium">
                {c.clinicianName} · {c.status}
              </p>
              <p className="text-sm text-ink/65">Granted {formatWhen(c.grantedAt)}</p>
              {c.status === "GRANTED" ? (
                <button className="mt-2 text-sm underline" onClick={() => revoke(c.id)}>
                  Revoke
                </button>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </AppShell>
  );
}
