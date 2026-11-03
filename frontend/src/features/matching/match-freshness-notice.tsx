"use client";

import { Building2, RefreshCw, TriangleAlert } from "lucide-react";
import { useEffect, useState } from "react";
import { Button, ButtonLink } from "@/components/ui/button";
import { useAuth } from "@/features/auth/auth-context";
import type { CompanyProfile } from "@/features/company/company-types";

type MatchFreshnessNoticeProps = {
  /** When the matches were last calculated, or null if they never were. */
  evaluatedAt: string | null;
  /** True once the match overview has loaded. */
  loaded: boolean;
  /** True once the user recalculated during this visit, even if that produced no stored matches. */
  justRecalculated: boolean;
  canRecalculate: boolean;
  recalculating: boolean;
  onRecalculate: () => void;
};

/**
 * Matches are stored, not live: they only change when someone clicks Recalculate.
 * This tells the user when the company profile is missing, or was edited after the
 * last calculation, so the scores on the page are not silently out of date.
 */
export function MatchFreshnessNotice({
  evaluatedAt,
  loaded,
  justRecalculated,
  canRecalculate,
  recalculating,
  onRecalculate,
}: MatchFreshnessNoticeProps) {
  const { api } = useAuth();
  const [profile, setProfile] = useState<CompanyProfile | null>(null);

  useEffect(() => {
    let cancelled = false;
    api<CompanyProfile>("/api/v1/company/profile")
      .then((result) => {
        if (!cancelled) setProfile(result);
      })
      .catch(() => {
        // The notice is optional. If the profile cannot be read, the page works without it.
      });
    return () => {
      cancelled = true;
    };
  }, [api]);

  if (!loaded || profile === null) return null;

  if (!profile.exists) {
    return (
      <div
        role="status"
        className="mt-6 flex flex-col gap-3 rounded-2xl border border-info-line bg-info-soft p-4 sm:flex-row sm:items-center sm:justify-between"
      >
        <div className="flex items-start gap-3">
          <Building2 className="mt-0.5 size-5 shrink-0 text-info" />
          <div>
            <p className="text-sm font-semibold text-ink">Add your company profile first</p>
            <p className="mt-0.5 text-sm text-ink-2">
              Matches score each tender against your CPV codes, regions, contract sizes and capabilities. Without a
              profile there is nothing to compare.
            </p>
          </div>
        </div>
        <ButtonLink href="/company" variant="primary" size="sm">
          Build company profile
        </ButtonLink>
      </div>
    );
  }

  const profileChanged =
    profile.updatedAt !== null &&
    evaluatedAt !== null &&
    new Date(profile.updatedAt).getTime() > new Date(evaluatedAt).getTime();
  const neverCalculated = evaluatedAt === null && !justRecalculated;

  if (!profileChanged && !neverCalculated) return null;

  return (
    <div
      role="status"
      className="mt-6 flex flex-col gap-3 rounded-2xl border border-warn-line bg-warn-soft p-4 sm:flex-row sm:items-center sm:justify-between"
    >
      <div className="flex items-start gap-3">
        <TriangleAlert className="mt-0.5 size-5 shrink-0 text-warn" />
        <div>
          <p className="text-sm font-semibold text-ink">
            {neverCalculated ? "No matches have been calculated yet" : "These scores are out of date"}
          </p>
          <p className="mt-0.5 text-sm text-ink-2">
            {neverCalculated
              ? "Calculate matches to score the latest tenders against your company profile."
              : "Your company profile changed after the matches were last calculated. Recalculate to apply the change."}
          </p>
        </div>
      </div>
      {canRecalculate ? (
        <Button
          variant="primary"
          size="sm"
          loading={recalculating}
          leading={<RefreshCw className="size-3.5" />}
          onClick={onRecalculate}
        >
          Recalculate now
        </Button>
      ) : (
        <p className="text-xs font-medium text-muted">Ask an owner, bid manager or analyst to recalculate.</p>
      )}
    </div>
  );
}
