"use client";

import { TriangleAlert } from "lucide-react";

import { Button, ButtonLink } from "@/components/ui/button";

export default function RootError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <main className="grid min-h-screen place-items-center px-6">
      <div className="max-w-md text-center">
        <span className="mx-auto grid size-14 place-items-center rounded-2xl bg-danger-soft text-danger">
          <TriangleAlert className="size-6" />
        </span>
        <h1 className="mt-6 text-2xl font-semibold tracking-tight text-ink">Something went wrong</h1>
        <p className="mt-3 text-sm leading-6 text-muted">
          TenderPilot hit an unexpected problem while rendering this page. Your data is safe. Try again, or return
          to the dashboard.
        </p>
        {error.digest ? <p className="mt-3 font-mono text-[11px] text-faint">Reference: {error.digest}</p> : null}
        <div className="mt-7 flex flex-wrap justify-center gap-3">
          <Button variant="primary" onClick={reset}>
            Try again
          </Button>
          <ButtonLink href="/dashboard">Go to dashboard</ButtonLink>
        </div>
      </div>
    </main>
  );
}
