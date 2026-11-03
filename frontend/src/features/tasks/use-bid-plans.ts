"use client";

import { useCallback, useEffect, useState } from "react";

import { useAuth } from "@/features/auth/auth-context";
import type { BidPlanningWorkspace } from "@/features/bid-planning/bid-planning-types";
import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";

import type { BidPlan, PlanTask } from "./my-tasks";

type PortfolioBid = DashboardCommandCenter["portfolio"][number];

type Loaded = { source: PortfolioBid[]; plans: BidPlan[] };

export type BidPlans = {
  plans: BidPlan[];
  /** True until the plans have been read for the first time. */
  loading: boolean;
  /** True while they are read again; the earlier plans stay available meanwhile. */
  refreshing: boolean;
  /** Puts one task, as the server returned it after a change, into the plan it belongs to. */
  replaceTask: (tenderId: string, task: PlanTask) => void;
};

/**
 * Reads the planning workspace of each bid (one request per bid, all at once) and again whenever the `bids`
 * list is a new one. Pass a stable list, for example one memoized on the dashboard that it came from.
 */
export function useBidPlans(bids: PortfolioBid[]): BidPlans {
  const { api } = useAuth();
  const [loaded, setLoaded] = useState<Loaded | null>(null);
  const active = bids.length > 0;

  useEffect(() => {
    if (!active) return;
    let cancelled = false;
    Promise.allSettled(bids.map((bid) => api<BidPlanningWorkspace>(`/api/v1/bid-planning/${bid.tenderId}`))).then(
      (results) => {
        if (cancelled) return;
        setLoaded({
          source: bids,
          plans: results.map((result, index) => ({
            bid: bids[index],
            workspace: result.status === "fulfilled" ? result.value : null,
          })),
        });
      },
    );
    return () => {
      cancelled = true;
    };
  }, [active, api, bids]);

  const replaceTask = useCallback((tenderId: string, task: PlanTask) => {
    setLoaded((current) => {
      if (current === null) return current;
      return {
        ...current,
        plans: current.plans.map((plan) =>
          plan.bid.tenderId === tenderId && plan.workspace !== null
            ? {
                ...plan,
                workspace: {
                  ...plan.workspace,
                  tasks: plan.workspace.tasks.map((existing) => (existing.id === task.id ? task : existing)),
                },
              }
            : plan,
        ),
      };
    });
  }, []);

  if (!active) return { plans: [], loading: false, refreshing: false, replaceTask };
  return {
    plans: loaded?.plans ?? [],
    loading: loaded === null,
    refreshing: loaded !== null && loaded.source !== bids,
    replaceTask,
  };
}
