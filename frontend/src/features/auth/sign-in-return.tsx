"use client";

import { Info, LoaderCircle } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, type ReactNode } from "react";

import { AuthShell } from "@/components/auth/auth-shell";

import { useAuth } from "./auth-context";
import { returnPathFromSearch, withReturnPath } from "./return-to";

/**
 * While a person is signed in the sign-in and sign-up pages have nothing to offer, so this sends them on to the
 * page they asked for (the "next" address parameter) or the dashboard. It also runs right after signing in or
 * creating an account, so one mechanism covers both. It returns true while that is happening, so the page can
 * show SignedInNotice instead of the form.
 */
export function useSignedInRedirect(): boolean {
  const router = useRouter();
  const { status } = useAuth();
  const signedIn = status === "authenticated";

  useEffect(() => {
    if (signedIn) router.replace(returnPathFromSearch(window.location.search));
  }, [signedIn, router]);

  return signedIn;
}

export function SignedInNotice() {
  return (
    <AuthShell eyebrow="Signed in" title="Opening your workspace" description="You are signed in to TenderPilot.">
      <div role="status" className="flex items-center gap-3 text-sm text-muted">
        <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
        Taking you to the page you asked for&hellip;
      </div>
    </AuthShell>
  );
}

function ExpiredNotice() {
  const params = useSearchParams();
  if (params.get("reason") !== "expired") return null;
  return (
    <div
      role="status"
      className="flex items-start gap-3 rounded-xl border border-info-line bg-info-soft px-4 py-3 text-sm text-info"
    >
      <Info className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
      <span>Your session has ended. Sign in again to pick up where you left off.</span>
    </div>
  );
}

/** Explains why the sign-in page appeared when the session ended by itself. */
export function SessionEndedNotice() {
  return (
    <Suspense fallback={null}>
      <ExpiredNotice />
    </Suspense>
  );
}

const switchLinkClass = "font-semibold text-brand hover:underline";

function CarryLink({ to, children }: { to: "/login" | "/register"; children: ReactNode }) {
  const params = useSearchParams();
  return (
    <Link href={withReturnPath(to, params.toString())} className={switchLinkClass}>
      {children}
    </Link>
  );
}

/** The link between the sign-in and sign-up pages, keeping the page to come back to. */
export function AuthSwitchLink({ to, children }: { to: "/login" | "/register"; children: ReactNode }) {
  return (
    <Suspense
      fallback={
        <Link href={to} className={switchLinkClass}>
          {children}
        </Link>
      }
    >
      <CarryLink to={to}>{children}</CarryLink>
    </Suspense>
  );
}
