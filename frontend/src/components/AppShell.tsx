"use client";

import { useAuth } from "@/context/AuthContext";
import { DISCLAIMER } from "@/lib/privacy";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";

const patientLinks = [
  { href: "/dashboard", label: "Home" },
  { href: "/book", label: "Book" },
  { href: "/appointments", label: "Visits" },
  { href: "/consent", label: "Consent" },
  { href: "/audit", label: "Access log" },
  { href: "/notifications", label: "Inbox" },
];

const clinicianLinks = [
  { href: "/clinician", label: "Desk" },
  { href: "/appointments", label: "Visits" },
  { href: "/audit", label: "Access log" },
  { href: "/notifications", label: "Inbox" },
];

const adminLinks = [
  { href: "/admin", label: "Admin" },
  { href: "/audit", label: "Access log" },
  { href: "/notifications", label: "Inbox" },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const { user, ready, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (ready && !user) router.replace("/login");
  }, [ready, user, router]);

  if (!ready) {
    return <div className="p-10 text-sm text-ink/60">Checking session…</div>;
  }
  if (!user) return null;

  const links =
    user.role === "ADMIN" ? adminLinks : user.role === "CLINICIAN" ? clinicianLinks : patientLinks;

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[250px_1fr]">
      <aside className="border-b border-teal/15 bg-teal text-white lg:border-b-0">
        <div className="px-5 py-6">
          <p className="font-serif text-2xl tracking-tight">MediBridge</p>
          <p className="mt-1 text-xs uppercase tracking-[0.22em] text-white/70">Private coordination</p>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-3 pb-4 lg:flex-col">
          {links.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={`whitespace-nowrap rounded-full px-4 py-2 text-sm ${
                pathname === link.href ? "bg-white text-teal" : "text-white/85 hover:bg-white/10"
              }`}
            >
              {link.label}
            </Link>
          ))}
        </nav>
        <div className="hidden px-5 pb-6 text-xs text-white/75 lg:block">
          <p>
            {user.firstName} {user.lastName}
          </p>
          <p className="mt-1">{user.role}</p>
          <button className="mt-4 underline" onClick={() => logout().then(() => router.push("/login"))}>
            Sign out
          </button>
        </div>
      </aside>
      <div>
        <header className="flex items-center justify-between border-b border-teal/10 bg-white/80 px-4 py-3 lg:hidden">
          <p className="text-sm">
            {user.firstName} · {user.role}
          </p>
          <button className="text-sm underline" onClick={() => logout().then(() => router.push("/login"))}>
            Sign out
          </button>
        </header>
        <div className="border-b border-teal/10 bg-mist px-4 py-2 text-xs text-ink/70">{DISCLAIMER}</div>
        <main className="mx-auto max-w-6xl px-4 py-8">{children}</main>
      </div>
    </div>
  );
}

export function EmptyState({ title, body }: { title: string; body: string }) {
  return (
    <div className="rounded-2xl border border-dashed border-teal/25 bg-white p-8 text-center">
      <p className="font-serif text-xl">{title}</p>
      <p className="mt-2 text-sm text-ink/70">{body}</p>
    </div>
  );
}

export function ErrorBanner({ message }: { message: string }) {
  return (
    <div className="rounded-xl border border-coral/40 bg-coral/10 px-4 py-3 text-sm text-coral" role="alert">
      {message}
    </div>
  );
}

export function Skeleton({ className = "h-24" }: { className?: string }) {
  return <div className={`animate-pulse rounded-2xl bg-teal/10 ${className}`} />;
}

export function DisclaimerNote() {
  return <p className="mt-6 text-xs text-ink/50">{DISCLAIMER}</p>;
}
