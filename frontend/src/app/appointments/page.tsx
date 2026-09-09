"use client";

import { AppShell, EmptyState, ErrorBanner, Skeleton } from "@/components/AppShell";
import { api } from "@/lib/api";
import { formatWhen } from "@/lib/privacy";
import { Appointment } from "@/lib/types";
import { useEffect, useState } from "react";

export default function AppointmentsPage() {
  const [items, setItems] = useState<Appointment[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  function load() {
    api<Appointment[]>("/appointments")
      .then(setItems)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function cancel(id: string) {
    setError("");
    try {
      await api(`/appointments/${id}/cancel`, { method: "POST" });
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Cancel failed");
    }
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Appointments</h1>
      {error ? (
        <div className="mt-4">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      {loading ? <Skeleton className="mt-6" /> : null}
      {!loading && items.length === 0 ? (
        <div className="mt-6">
          <EmptyState title="No visits yet" body="Book an open slot to start a coordination visit." />
        </div>
      ) : (
        <ul className="mt-6 space-y-3">
          {items.map((a) => (
            <li key={a.id} className="rounded-2xl bg-white px-5 py-4">
              <p className="text-xs uppercase tracking-[0.2em] text-ink/50">{a.status}</p>
              <p className="mt-1 font-medium">
                {a.patientName} · {a.clinicianName}
              </p>
              <p className="text-sm text-ink/65">
                {a.facilityName} · {formatWhen(a.startsAt)}
              </p>
              {a.status === "BOOKED" ? (
                <button className="mt-3 text-sm underline" onClick={() => cancel(a.id)}>
                  Cancel visit
                </button>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </AppShell>
  );
}
