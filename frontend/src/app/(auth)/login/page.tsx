"use client";

import { ArrowRight, LoaderCircle, Mail, TriangleAlert } from "lucide-react";
import { useState, type FormEvent } from "react";

import { AuthShell } from "@/components/auth/auth-shell";
import { useDocumentTitle } from "@/components/shell/document-title";
import { ApiHttpError, NetworkError } from "@/features/auth/auth-api";
import { AuthField, PasswordField } from "@/features/auth/auth-fields";
import { useAuth } from "@/features/auth/auth-context";
import {
  AuthSwitchLink,
  SessionEndedNotice,
  SignedInNotice,
  useSignedInRedirect,
} from "@/features/auth/sign-in-return";

export default function LoginPage() {
  const { login } = useAuth();
  useDocumentTitle("Sign in");
  const signedIn = useSignedInRedirect();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError(null);

    try {
      await login({ email, password });
    } catch (cause) {
      setError(
        cause instanceof ApiHttpError || cause instanceof NetworkError
          ? cause.message
          : "Unable to sign in. Please try again.",
      );
    } finally {
      setPending(false);
    }
  }

  if (signedIn) return <SignedInNotice />;

  return (
    <AuthShell
      eyebrow="Welcome back"
      title="Sign in to TenderPilot"
      description="Access your organization's procurement intelligence workspace."
    >
      <form onSubmit={submit} className="space-y-5">
        <SessionEndedNotice />
        {error ? (
          <div
            role="alert"
            className="flex items-start gap-3 rounded-xl border border-danger-line bg-danger-soft px-4 py-3 text-sm text-danger"
          >
            <TriangleAlert className="mt-0.5 size-4 shrink-0" />
            <span>{error}</span>
          </div>
        ) : null}

        <AuthField
          label="Email address"
          icon={<Mail />}
          type="email"
          autoComplete="email"
          required
          autoFocus
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="you@company.com"
        />

        <PasswordField
          label="Password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Enter your password"
        />

        <button
          type="submit"
          disabled={pending}
          className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-navy px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-navy-hover disabled:cursor-not-allowed disabled:opacity-60"
        >
          {pending ? <LoaderCircle className="size-4 animate-spin" /> : <ArrowRight className="size-4" />}
          {pending ? "Signing in…" : "Sign in"}
        </button>

        <p className="text-center text-sm text-muted">
          New to TenderPilot?{" "}
          <AuthSwitchLink to="/register">Create your workspace</AuthSwitchLink>
        </p>
      </form>
    </AuthShell>
  );
}
