"use client";

import { AppShell, EmptyState, ErrorBanner, Skeleton } from "@/components/AppShell";
import { api } from "@/lib/api";
import { formatWhen } from "@/lib/privacy";
import { Slot } from "@/lib/types";
import { FormEvent, useEffect, useState } from "react";

export default function BookPage() {
  const [slots, setSlots] = useState<Slot[]>([]);
  const [reason, setReason] = useState("Coordination check-in");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  function load() {
    setLoading(true);
    api<Slot[]>("/slots?openOnly=true")
      .then(setSlots)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function book(id: string, e: FormEvent) {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      await api(`/slots/${id}/book`, { method: "POST", body: JSON.stringify({ reason }) });
      setMessage("Visit booked. You will see it on your dashboard.");
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Booking failed");
    }
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Book a visit</h1>
      <p className="mt-2 text-sm text-ink/70">Open slots published by clinicians. Reasons stay short — not a diagnosis.</p>
      <label className="mt-6 block max-w-md text-sm">
        Visit reason
        <input
          className="mt-1 w-full rounded-xl border border-ink/15 bg-white px-3 py-2"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
      </label>
      {error ? (
        <div className="mt-4">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      {message ? <p className="mt-4 text-sm text-teal">{message}</p> : null}
      {loading ? <Skeleton className="mt-6" /> : null}
      {!loading && slots.length === 0 ? (
        <div className="mt-6">
          <EmptyState title="No open slots" body="Ask a clinician to publish availability, or check back later." />
        </div>
      ) : (
        <ul className="mt-6 space-y-3">
          {slots.map((s) => (
            <li key={s.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-white px-5 py-4">
              <div>
                <p className="font-medium">
                  {s.clinicianName} · {s.facilityName}
                </p>
                <p className="text-sm text-ink/65">
                  {formatWhen(s.startsAt)} – {formatWhen(s.endsAt)}
                </p>
              </div>
              <button className="rounded-full bg-teal px-4 py-2 text-sm text-white" onClick={(e) => book(s.id, e)}>
                Book
              </button>
            </li>
          ))}
        </ul>
      )}
    </AppShell>
  );
}
