"use client";

import { homeFor, useAuth } from "@/context/AuthContext";
import { DISCLAIMER } from "@/lib/privacy";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("patient@medibridge.local");
  const [password, setPassword] = useState("ChangeMe123!");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setPending(true);
    try {
      const user = await login(email, password);
      router.push(homeFor(user.role));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-6">
      <p className="text-xs uppercase tracking-[0.3em] text-teal">Sign in</p>
      <h1 className="mt-2 font-serif text-4xl">Return to your desk</h1>
      <p className="mt-3 text-xs text-ink/60">{DISCLAIMER}</p>
      <form onSubmit={onSubmit} className="mt-8 space-y-4">
        <label className="block text-sm">
          Email
          <input
            className="mt-1 w-full rounded-xl border border-ink/15 bg-white px-3 py-2"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          Password
          <input
            className="mt-1 w-full rounded-xl border border-ink/15 bg-white px-3 py-2"
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        {error ? <p className="text-sm text-coral">{error}</p> : null}
        <button disabled={pending} className="w-full rounded-full bg-teal py-3 text-white disabled:opacity-60">
          {pending ? "Signing in…" : "Sign in"}
        </button>
      </form>
      <p className="mt-4 text-sm">
        New here?{" "}
        <Link className="underline" href="/register">
          Create a patient account
        </Link>
      </p>
    </main>
  );
}
