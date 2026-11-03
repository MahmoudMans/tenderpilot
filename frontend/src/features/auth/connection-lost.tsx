"use client";

import { RefreshCw, TriangleAlert } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";

import { BrandMark } from "@/components/shell/brand-mark";
import { Button, buttonClasses } from "@/components/ui/button";

/** How often the app quietly tries again while the server cannot be reached. */
const RETRY_EVERY_MS = 10_000;

/**
 * Shown instead of the workspace when the server cannot be reached while the session is restored.
 * The person is not signed out: this screen tries again every few seconds and as soon as the browser
 * reports that the connection is back, and the workspace opens by itself.
 */
export function ConnectionLost({ onRetry }: { onRetry: () => Promise<void> }) {
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const again = () => {
      void onRetry();
    };
    const timer = window.setInterval(again, RETRY_EVERY_MS);
    window.addEventListener("online", again);
    return () => {
      window.clearInterval(timer);
      window.removeEventListener("online", again);
    };
  }, [onRetry]);

  function retryNow() {
    setBusy(true);
    onRetry().finally(() => setBusy(false));
  }

  return (
    <div className="grid min-h-screen place-items-center px-6">
      <div role="alert" className="max-w-md text-center">
        <div className="relative mx-auto w-fit">
          <BrandMark className="size-14 rounded-2xl text-base shadow-lg" />
          <span className="absolute -bottom-2 -right-2 grid size-7 place-items-center rounded-full border border-warn-line bg-warn-soft text-warn shadow-sm">
            <TriangleAlert className="size-3.5" aria-hidden="true" />
          </span>
        </div>
        <h1 className="mt-6 text-lg font-semibold tracking-tight text-ink">TenderPilot cannot be reached</h1>
        <p className="mt-2 text-sm leading-6 text-muted">
          You have not been signed out. Check your internet connection, or that the TenderPilot server is running.
          This page tries again every few seconds and when your connection returns, and your workspace opens by itself.
        </p>
        <div className="mt-6 flex flex-wrap items-center justify-center gap-3">
          <Button variant="primary" onClick={retryNow} loading={busy} leading={<RefreshCw className="size-4" />}>
            Try again
          </Button>
          <Link href="/login" className={buttonClasses("secondary", "md")}>
            Go to sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
