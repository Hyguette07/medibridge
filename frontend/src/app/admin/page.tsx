"use client";

import { AppShell, ErrorBanner, Skeleton } from "@/components/AppShell";
import { api } from "@/lib/api";
import { AdminDashboard, Facility } from "@/lib/types";
import { FormEvent, useEffect, useState } from "react";

type UserRow = { id: string; email: string; role: string; firstName: string; lastName: string; enabled: boolean };

export default function AdminPage() {
  const [dash, setDash] = useState<AdminDashboard | null>(null);
  const [users, setUsers] = useState<UserRow[]>([]);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [form, setForm] = useState({ name: "", type: "CLINIC", address: "", city: "Kigali", phone: "" });
  const [error, setError] = useState("");

  function load() {
    api<AdminDashboard>("/dashboard").then(setDash).catch((e: Error) => setError(e.message));
    api<UserRow[]>("/admin/users").then(setUsers).catch((e: Error) => setError(e.message));
    api<Facility[]>("/facilities").then(setFacilities).catch((e: Error) => setError(e.message));
  }

  useEffect(() => {
    load();
  }, []);

  async function addFacility(e: FormEvent) {
    e.preventDefault();
    await api("/facilities", {
      method: "POST",
      body: JSON.stringify({ ...form, active: true }),
    });
    setForm({ name: "", type: "CLINIC", address: "", city: "Kigali", phone: "" });
    load();
  }

  async function toggle(user: UserRow) {
    await api(`/admin/users/${user.id}`, { method: "PATCH", body: JSON.stringify({ enabled: !user.enabled }) });
    load();
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Administration</h1>
      {error ? (
        <div className="mt-4">
          <ErrorBanner message={error} />
        </div>
      ) : null}
      {!dash ? (
        <Skeleton className="mt-6" />
      ) : (
        <div className="mt-6 grid gap-4 md:grid-cols-4">
          <Tile label="Users" value={dash.userCount} />
          <Tile label="Facilities" value={dash.facilityCount} />
          <Tile label="Booked visits" value={dash.appointmentCount} />
          <Tile label="Active consents" value={dash.consentCount} />
        </div>
      )}
      <section className="mt-8">
        <h2 className="font-serif text-2xl">Users</h2>
        <ul className="mt-3 space-y-2">
          {users.map((u) => (
            <li key={u.id} className="flex items-center justify-between rounded-2xl bg-white px-4 py-3 text-sm">
              <span>
                {u.firstName} {u.lastName} · {u.email} · {u.role} · {u.enabled ? "active" : "disabled"}
              </span>
              <button className="underline" onClick={() => toggle(u)}>
                {u.enabled ? "Disable" : "Enable"}
              </button>
            </li>
          ))}
        </ul>
      </section>
      <section className="mt-8">
        <h2 className="font-serif text-2xl">Facilities</h2>
        <ul className="mt-3 space-y-2 text-sm">
          {facilities.map((f) => (
            <li key={f.id} className="rounded-2xl bg-white px-4 py-3">
              {f.name} · {f.type} · {f.city}
            </li>
          ))}
        </ul>
        <form onSubmit={addFacility} className="mt-4 grid gap-2 rounded-3xl bg-white p-5 md:grid-cols-2">
          <input
            required
            className="rounded-xl border px-3 py-2"
            placeholder="Facility name"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <select
            className="rounded-xl border px-3 py-2"
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            <option value="CLINIC">Clinic</option>
            <option value="HOSPITAL">Hospital</option>
            <option value="COMMUNITY">Community</option>
          </select>
          <input
            className="rounded-xl border px-3 py-2"
            placeholder="Address"
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
          />
          <input
            className="rounded-xl border px-3 py-2"
            placeholder="City"
            value={form.city}
            onChange={(e) => setForm({ ...form, city: e.target.value })}
          />
          <button className="rounded-full bg-teal px-5 py-2 text-white md:col-span-2">Add facility</button>
        </form>
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
