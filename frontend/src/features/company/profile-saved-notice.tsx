"use client";

import { CheckCircle2, X } from "lucide-react";
import { ButtonLink } from "@/components/ui/button";

/** Shown after the company profile is saved: matches use the profile, but only change when recalculated. */
export function ProfileSavedNotice({ onDismiss }: { onDismiss: () => void }) {
  return (
    <div
      role="status"
      className="mb-6 flex flex-col gap-3 rounded-2xl border border-success-line bg-success-soft p-4 sm:flex-row sm:items-center sm:justify-between"
    >
      <div className="flex items-start gap-3">
        <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-success" />
        <div>
          <p className="text-sm font-semibold text-ink">Profile saved</p>
          <p className="mt-0.5 text-sm text-ink-2">
            Match scores are built from this profile. Open Matches and recalculate to apply your changes.
          </p>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <ButtonLink href="/matches" variant="primary" size="sm">
          Open matches
        </ButtonLink>
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss"
          className="grid size-8 place-items-center rounded-lg text-muted hover:bg-surface-3 hover:text-ink"
        >
          <X className="size-4" />
        </button>
      </div>
    </div>
  );
}
