import { Compass } from "lucide-react";

import { ButtonLink } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main className="grid min-h-screen place-items-center px-6">
      <div className="max-w-md text-center">
        <span className="mx-auto grid size-14 place-items-center rounded-2xl bg-brand-soft text-brand">
          <Compass className="size-6" />
        </span>
        <p className="mt-6 text-xs font-semibold uppercase tracking-[0.18em] text-brand">Error 404</p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight text-ink">This page is off the map</h1>
        <p className="mt-3 text-sm leading-6 text-muted">
          The address may be mistyped, or the page may have moved. Head back to your command center or browse the
          full product map.
        </p>
        <div className="mt-7 flex flex-wrap justify-center gap-3">
          <ButtonLink href="/dashboard" variant="primary">
            Go to dashboard
          </ButtonLink>
          <ButtonLink href="/workspace" variant="secondary">
            Open product map
          </ButtonLink>
        </div>
      </div>
    </main>
  );
}
