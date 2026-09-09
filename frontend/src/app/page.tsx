import { DISCLAIMER } from "@/lib/privacy";
import Link from "next/link";

export default function HomePage() {
  return (
    <main className="mx-auto flex min-h-screen max-w-5xl flex-col justify-center px-6 py-16">
      <p className="text-xs uppercase tracking-[0.35em] text-teal">Hyguette Labs · MediBridge</p>
      <h1 className="mt-4 max-w-3xl font-serif text-5xl leading-tight md:text-7xl">
        Book a visit. See who opened your record.
      </h1>
      <p className="mt-6 max-w-xl text-lg text-ink/75">
        Privacy-first appointment coordination. Every PHI-adjacent read and write is audit-logged. Patients
        keep an access timeline — and clinicians need consent before they can see care notes.
      </p>
      <div className="mt-8 rounded-2xl border border-teal/20 bg-white/80 px-5 py-4 text-sm text-ink/80">
        {DISCLAIMER}
      </div>
      <div className="mt-10 flex flex-wrap gap-4">
        <Link href="/login" className="rounded-full bg-teal px-6 py-3 text-white">
          Sign in
        </Link>
        <Link href="/register" className="rounded-full border border-ink/15 bg-white px-6 py-3">
          Create a patient account
        </Link>
      </div>
      <p className="mt-16 text-sm text-ink/60">
        Demo: patient@medibridge.local · password from APP_SEED_PASSWORD
      </p>
    </main>
  );
}
