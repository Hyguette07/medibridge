"use client";

import { AppShell, EmptyState, ErrorBanner, Skeleton } from "@/components/AppShell";
import { homeFor, useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { formatWhen } from "@/lib/privacy";
import { PatientDashboard } from "@/lib/types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function DashboardPage() {
  const { user, ready } = useAuth();
  const router = useRouter();
  const [data, setData] = useState<PatientDashboard | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (ready && user && user.role !== "PATIENT") {
      router.replace(homeFor(user.role));
    }
  }, [ready, user, router]);

  useEffect(() => {
    if (!user || user.role !== "PATIENT") return;
    api<PatientDashboard>("/dashboard")
      .then(setData)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [user]);

  return (
    <AppShell>
      <p className="text-xs uppercase tracking-[0.3em] text-teal">Patient desk</p>
      <h1 className="mt-1 font-serif text-4xl">Upcoming visits</h1>
      {loading ? <Skeleton className="mt-6 h-40" /> : null}
      {error ? (
        <div className="mt-6">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      {data ? (
        <>
          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            <Stat title="Access events" value={String(data.recentAccessCount)} note="Who touched your record" />
            <Stat title="Active consents" value={String(data.activeConsents)} note="Clinicians you have allowed" />
            <Stat title="Unread inbox" value={String(data.unreadNotifications)} note="Bookings and access alerts" />
          </div>
          <section className="mt-10">
            <div className="flex items-end justify-between">
              <h2 className="font-serif text-2xl">Next visits</h2>
              <Link className="text-sm underline" href="/book">
                Book a slot
              </Link>
            </div>
            {data.upcoming.length === 0 ? (
              <div className="mt-4">
                <EmptyState title="No upcoming visits" body="Open Book to choose an open clinician slot." />
              </div>
            ) : (
              <ul className="mt-4 space-y-3">
                {data.upcoming.map((a) => (
                  <li key={a.id} className="rounded-2xl bg-white px-5 py-4 shadow-sm">
                    <p className="font-medium">
                      {a.clinicianName} · {a.facilityName}
                    </p>
                    <p className="text-sm text-ink/65">{formatWhen(a.startsAt)}</p>
                    {a.reason ? <p className="mt-1 text-xs text-ink/50">{a.reason}</p> : null}
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      ) : null}
    </AppShell>
  );
}

function Stat({ title, value, note }: { title: string; value: string; note: string }) {
  return (
    <div className="rounded-3xl bg-white p-5 shadow-sm">
      <p className="text-xs uppercase tracking-[0.2em] text-ink/50">{title}</p>
      <p className="mt-2 font-serif text-3xl">{value}</p>
      <p className="mt-2 text-xs text-ink/60">{note}</p>
    </div>
  );
}
