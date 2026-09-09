"use client";

import { homeFor, useAuth } from "@/context/AuthContext";
import { DISCLAIMER } from "@/lib/privacy";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState({ firstName: "", lastName: "", email: "", password: "", phone: "" });
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setPending(true);
    try {
      const user = await register(form);
      router.push(homeFor(user.role));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-6 py-12">
      <h1 className="font-serif text-4xl">Open a patient account</h1>
      <p className="mt-2 text-sm text-ink/70">
        Self-registration creates a PATIENT role. Clinicians and admins are provisioned separately.
      </p>
      <p className="mt-3 text-xs text-ink/60">{DISCLAIMER}</p>
      <form onSubmit={onSubmit} className="mt-8 space-y-3">
        {(["firstName", "lastName", "email", "phone", "password"] as const).map((key) => (
          <label key={key} className="block text-sm capitalize">
            {key === "firstName" ? "First name" : key === "lastName" ? "Last name" : key}
            <input
              className="mt-1 w-full rounded-xl border border-ink/15 bg-white px-3 py-2"
              type={key === "password" ? "password" : key === "email" ? "email" : "text"}
              required={key !== "phone"}
              minLength={key === "password" ? 8 : undefined}
              value={form[key]}
              onChange={(e) => setForm({ ...form, [key]: e.target.value })}
            />
          </label>
        ))}
        {error ? <p className="text-sm text-coral">{error}</p> : null}
        <button disabled={pending} className="w-full rounded-full bg-teal py-3 text-white">
          {pending ? "Creating…" : "Create account"}
        </button>
      </form>
      <Link className="mt-4 text-sm underline" href="/login">
        Already registered
      </Link>
    </main>
  );
}
