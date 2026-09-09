"use client";

import { AppShell, EmptyState, ErrorBanner, Skeleton } from "@/components/AppShell";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { clinicianMayReadNotes, formatWhen } from "@/lib/privacy";
import { CareNote, ClinicianDashboard, Facility, PatientSummary, Slot } from "@/lib/types";
import { FormEvent, useEffect, useState } from "react";

export default function ClinicianPage() {
  const { user } = useAuth();
  const [dash, setDash] = useState<ClinicianDashboard | null>(null);
  const [patients, setPatients] = useState<PatientSummary[]>([]);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [selected, setSelected] = useState<string>("");
  const [notes, setNotes] = useState<CareNote[]>([]);
  const [noteBody, setNoteBody] = useState("Educational coordination note. Not a diagnosis.");
  const [slotForm, setSlotForm] = useState({ facilityId: "", startsAt: "", endsAt: "" });
  const [error, setError] = useState("");
  const [noteError, setNoteError] = useState("");

  function load() {
    api<ClinicianDashboard>("/dashboard").then(setDash).catch((e: Error) => setError(e.message));
    api<PatientSummary[]>("/patients").then(setPatients).catch((e: Error) => setError(e.message));
    api<Facility[]>("/facilities").then((rows) => {
      setFacilities(rows);
      setSlotForm((f) => ({ ...f, facilityId: f.facilityId || rows[0]?.id || "" }));
    });
    if (user?.userId) {
      api<Slot[]>(`/slots?openOnly=false&clinicianId=${user.userId}`).then(setSlots).catch(() => undefined);
    }
  }

  useEffect(() => {
    if (user) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadNotes(patientId: string) {
    setSelected(patientId);
    setNoteError("");
    const patient = patients.find((p) => p.userId === patientId);
    if (patient && !clinicianMayReadNotes(patient.consentActive)) {
      setNotes([]);
      setNoteError("Patient consent is required before viewing care notes.");
      return;
    }
    try {
      const rows = await api<CareNote[]>(`/notes?patientId=${patientId}`);
      setNotes(rows);
    } catch (err) {
      setNotes([]);
      setNoteError(err instanceof Error ? err.message : "Cannot load notes");
    }
  }

  async function writeNote(e: FormEvent) {
    e.preventDefault();
    if (!selected) return;
    setNoteError("");
    try {
      await api("/notes", { method: "POST", body: JSON.stringify({ patientId: selected, body: noteBody }) });
      await loadNotes(selected);
    } catch (err) {
      setNoteError(err instanceof Error ? err.message : "Cannot write note");
    }
  }

  async function publishSlot(e: FormEvent) {
    e.preventDefault();
    await api("/slots", {
      method: "POST",
      body: JSON.stringify({
        facilityId: slotForm.facilityId,
        startsAt: new Date(slotForm.startsAt).toISOString(),
        endsAt: new Date(slotForm.endsAt).toISOString(),
      }),
    });
    setSlotForm((f) => ({ ...f, startsAt: "", endsAt: "" }));
    load();
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Clinician desk</h1>
      <p className="mt-2 text-sm text-ink/70">Consent gates notes. Every patient-record open is written to the audit timeline.</p>
      {error ? (
        <div className="mt-4">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      {!dash ? (
        <Skeleton className="mt-6" />
      ) : (
        <div className="mt-6 grid gap-4 md:grid-cols-3">
          <Tile label="Today" value={dash.todayAppointments.length} />
          <Tile label="Open slots" value={dash.openSlotCount} />
          <Tile label="Consented patients" value={dash.consentedPatientCount} />
        </div>
      )}

      <section className="mt-10 grid gap-8 lg:grid-cols-2">
        <div>
          <h2 className="font-serif text-2xl">Patients with consent</h2>
          {patients.length === 0 ? (
            <div className="mt-3">
              <EmptyState title="No consented patients" body="A patient must grant consent before notes are visible." />
            </div>
          ) : (
            <ul className="mt-3 space-y-2">
              {patients.map((p) => (
                <li key={p.userId}>
                  <button
                    className={`w-full rounded-2xl px-4 py-3 text-left text-sm ${
                      selected === p.userId ? "bg-teal text-white" : "bg-white"
                    }`}
                    onClick={() => loadNotes(p.userId)}
                  >
                    {p.firstName} {p.lastName} · {p.city || "city unset"}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        <div>
          <h2 className="font-serif text-2xl">Care notes</h2>
          <p className="text-xs text-ink/50">Clinician-only. Never shown on the patient dashboard.</p>
          {noteError ? (
            <div className="mt-3">
              <ErrorBanner message={noteError} />
            </div>
          ) : null}
          {notes.length === 0 && !noteError ? (
            <p className="mt-3 text-sm text-ink/60">Select a consented patient to read notes.</p>
          ) : (
            <ul className="mt-3 space-y-2 text-sm">
              {notes.map((n) => (
                <li key={n.id} className="rounded-2xl bg-white px-4 py-3">
                  <p className="text-xs text-ink/50">{formatWhen(n.createdAt)}</p>
                  <p className="mt-1">{n.body}</p>
                </li>
              ))}
            </ul>
          )}
          {selected ? (
            <form onSubmit={writeNote} className="mt-4 space-y-2">
              <textarea
                className="h-24 w-full rounded-xl border px-3 py-2 text-sm"
                value={noteBody}
                onChange={(e) => setNoteBody(e.target.value)}
              />
              <button className="rounded-full bg-teal px-4 py-2 text-sm text-white">Save note</button>
            </form>
          ) : null}
        </div>
      </section>

      <form onSubmit={publishSlot} className="mt-10 space-y-3 rounded-3xl bg-white p-5">
        <h2 className="font-serif text-2xl">Publish availability</h2>
        <select
          className="w-full rounded-xl border px-3 py-2"
          value={slotForm.facilityId}
          onChange={(e) => setSlotForm({ ...slotForm, facilityId: e.target.value })}
        >
          {facilities.map((f) => (
            <option key={f.id} value={f.id}>
              {f.name}
            </option>
          ))}
        </select>
        <input
          required
          type="datetime-local"
          className="w-full rounded-xl border px-3 py-2"
          value={slotForm.startsAt}
          onChange={(e) => setSlotForm({ ...slotForm, startsAt: e.target.value })}
        />
        <input
          required
          type="datetime-local"
          className="w-full rounded-xl border px-3 py-2"
          value={slotForm.endsAt}
          onChange={(e) => setSlotForm({ ...slotForm, endsAt: e.target.value })}
        />
        <button className="rounded-full bg-teal px-5 py-2 text-white">Add slot</button>
      </form>

      <section className="mt-8">
        <h2 className="font-serif text-2xl">Your slots</h2>
        <ul className="mt-3 space-y-2 text-sm">
          {slots.map((s) => (
            <li key={s.id} className="rounded-2xl bg-white px-4 py-3">
              {s.facilityName} · {formatWhen(s.startsAt)} · {s.booked ? "booked" : "open"}
            </li>
          ))}
        </ul>
      </section>
    </AppShell>
  );
}

function Tile({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-3xl bg-white p-5">
      <p className="text-xs uppercase tracking-[0.2em] text-ink/50">{label}</p>
      <p className="mt-2 font-serif text-3xl">{value}</p>
    </div>
  );
}
