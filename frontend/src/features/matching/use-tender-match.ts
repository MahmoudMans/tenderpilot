"use client";

import { useEffect, useState } from "react";

import { useAuth } from "@/features/auth/auth-context";

import type { TenderMatch } from "./matching-types";

export type TenderMatchState =
  | { status: "loading" }
  | { status: "error" }
  | { status: "ready"; match: TenderMatch | null };

/**
 * Finds the company match for one tender. The Go/No-Go workspace exists only for
 * tenders that are in the organisation's match list, so the opportunity page uses
 * this to decide whether to link to it.
 */
export function useTenderMatch(tenderId: string): TenderMatchState {
  const { api } = useAuth();
  const [state, setState] = useState<{ tenderId: string; result: TenderMatchState } | null>(null);

  useEffect(() => {
    let cancelled = false;

    api<TenderMatch[]>("/api/v1/matches?includeBlocked=true")
      .then((matches) => {
        if (cancelled) return;
        const match = matches.find((item) => item.opportunity.id === tenderId) ?? null;
        setState({ tenderId, result: { status: "ready", match } });
      })
      .catch(() => {
        if (!cancelled) setState({ tenderId, result: { status: "error" } });
      });

    return () => {
      cancelled = true;
    };
  }, [api, tenderId]);

  return state !== null && state.tenderId === tenderId ? state.result : { status: "loading" };
}
