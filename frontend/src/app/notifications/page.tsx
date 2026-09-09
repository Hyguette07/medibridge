"use client";

import { AppShell, EmptyState } from "@/components/AppShell";
import { api } from "@/lib/api";
import { NotificationItem } from "@/lib/types";
import { FormEvent, useEffect, useState } from "react";

export default function NotificationsPage() {
  const [items, setItems] = useState<NotificationItem[]>([]);

  function load() {
    api<NotificationItem[]>("/notifications").then(setItems);
  }

  useEffect(() => {
    load();
  }, []);

  async function mark(id: string, e: FormEvent) {
    e.preventDefault();
    await api(`/notifications/${id}/read`, { method: "PATCH" });
    load();
  }

  return (
    <AppShell>
      <h1 className="font-serif text-4xl">Inbox</h1>
      {items.length === 0 ? (
        <div className="mt-6">
          <EmptyState title="Inbox is quiet" body="Bookings, consent changes, and record-access alerts land here." />
        </div>
      ) : (
        <ul className="mt-6 space-y-3">
          {items.map((n) => (
            <li key={n.id} className={`rounded-2xl p-4 ${n.read ? "bg-white/60" : "bg-white"}`}>
              <p className="text-xs uppercase tracking-[0.2em]">{n.type}</p>
              <p className="font-medium">{n.title}</p>
              <p className="text-sm text-ink/70">{n.body}</p>
              {!n.read ? (
                <button className="mt-2 text-sm underline" onClick={(e) => mark(n.id, e)}>
                  Mark read
                </button>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </AppShell>
  );
}
