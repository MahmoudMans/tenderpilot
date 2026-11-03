"use client";

import { LoaderCircle, ShieldCheck } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useRef, type ReactNode } from "react";

import { AppShell } from "@/components/shell/app-shell";
import { BrandMark } from "@/components/shell/brand-mark";
import { QueryBoundary } from "@/features/navigation/query-boundary";

import { useAuth } from "./auth-context";
import { ConnectionLost } from "./connection-lost";
import { signInHref, wasSignedOutOnPurpose } from "./return-to";

export function ProtectedWorkspace({ children }: { children: ReactNode }) {
  const { status, retry } = useAuth();
  const router = useRouter();

  const wasSignedIn = useRef(false);

  useEffect(() => {
    if (status === "authenticated") {
      wasSignedIn.current = true;
    } else if (status === "unauthenticated") {
      // Signing out on purpose leads to a plain sign-in page. Losing the session, or opening a link while
      // signed out, leads to a sign-in page that brings the person back to this page afterwards.
      const here = window.location.pathname + window.location.search;
      router.replace(wasSignedOutOnPurpose() ? "/login" : signInHref(here, wasSignedIn.current ? "expired" : undefined));
    }
  }, [status, router]);

  if (status === "unreachable") {
    return <ConnectionLost onRetry={retry} />;
  }

  if (status !== "authenticated") {
    return (
      <div className="grid min-h-screen place-items-center px-6">
        <div role="status" aria-live="polite" className="text-center">
          <div className="relative mx-auto w-fit">
            <BrandMark className="size-14 rounded-2xl text-base shadow-lg" />
            <span className="absolute -bottom-2 -right-2 grid size-7 place-items-center rounded-full border border-line bg-surface text-brand shadow-sm">
              {status === "loading" ? (
                <LoaderCircle className="size-3.5 animate-spin" />
              ) : (
                <ShieldCheck className="size-3.5" />
              )}
            </span>
          </div>
          <p className="mt-6 text-sm font-semibold text-ink">
            {status === "loading" ? "Restoring your TenderPilot session…" : "Redirecting to sign in…"}
          </p>
          <p className="mt-1 text-xs text-muted">Procurement intelligence for smarter bid decisions</p>
        </div>
      </div>
    );
  }

  return (
    <AppShell>
      <QueryBoundary>{children}</QueryBoundary>
    </AppShell>
  );
}
