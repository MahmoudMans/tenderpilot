"use client";

import { ArrowRight, Building2, LoaderCircle, Mail, TriangleAlert, UserRound } from "lucide-react";
import { useState, type FormEvent } from "react";

import { AuthShell } from "@/components/auth/auth-shell";
import { useDocumentTitle } from "@/components/shell/document-title";
import { ApiHttpError, NetworkError } from "@/features/auth/auth-api";
import { AuthField, PasswordField, PasswordStrengthMeter } from "@/features/auth/auth-fields";
import { useAuth } from "@/features/auth/auth-context";
import { AuthSwitchLink, SignedInNotice, useSignedInRedirect } from "@/features/auth/sign-in-return";

export default function RegisterPage() {
  const { register } = useAuth();
  useDocumentTitle("Create account");
  const signedIn = useSignedInRedirect();
  const [fullName, setFullName] = useState("");
  const [organizationName, setOrganizationName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError(null);

    try {
      await register({ fullName, organizationName, email, password });
    } catch (cause) {
      setError(
        cause instanceof ApiHttpError || cause instanceof NetworkError
          ? cause.message
          : "Unable to create your TenderPilot workspace.",
      );
    } finally {
      setPending(false);
    }
  }

  if (signedIn) return <SignedInNotice />;

  return (
    <AuthShell
      eyebrow="Create workspace"
      title="Start your TenderPilot organization"
      description="Your first account becomes the organization owner. You can build the procurement profile in the next development phase."
    >
      <form onSubmit={submit} className="space-y-4">
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
          label="Full name"
          icon={<UserRound />}
          autoComplete="name"
          required
          autoFocus
          maxLength={160}
          value={fullName}
          onChange={(event) => setFullName(event.target.value)}
          placeholder="Julie Martin"
        />

        <AuthField
          label="Organization name"
          icon={<Building2 />}
          autoComplete="organization"
          required
          maxLength={180}
          value={organizationName}
          onChange={(event) => setOrganizationName(event.target.value)}
          placeholder="Aurelia Rénovation"
        />

        <AuthField
          label="Work email"
          icon={<Mail />}
          type="email"
          autoComplete="email"
          required
          maxLength={320}
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="julie@aurelia.fr"
        />

        <div>
          <PasswordField
            label="Password"
            autoComplete="new-password"
            required
            minLength={10}
            maxLength={100}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="At least 10 characters"
            hint="Use at least 10 characters. Passwords are stored only as adaptive one-way hashes."
          />
          <PasswordStrengthMeter password={password} />
        </div>

        <button
          type="submit"
          disabled={pending}
          className="mt-2 flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-navy px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-navy-hover disabled:cursor-not-allowed disabled:opacity-60"
        >
          {pending ? <LoaderCircle className="size-4 animate-spin" /> : <ArrowRight className="size-4" />}
          {pending ? "Creating workspace…" : "Create workspace"}
        </button>

        <p className="text-center text-sm text-muted">
          Already have an account?{" "}
          <AuthSwitchLink to="/login">Sign in</AuthSwitchLink>
        </p>
      </form>
    </AuthShell>
  );
}
