"use client";

import { useEffect, useMemo, useState } from "react";

import { useAuth } from "@/features/auth/auth-context";
import type { BidPlanningWorkspace } from "@/features/bid-planning/bid-planning-types";
import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";

import { sortTaskEvents, taskEventsFromPlan, type TaskDueEvent } from "./task-events";

type PortfolioBid = DashboardCommandCenter["portfolio"][number];

type Loaded = { key: string; tasks: TaskDueEvent[]; failed: number };

export type TaskDueDates = {
  tasks: TaskDueEvent[];
  /** How many bid plans could not be read. */
  failed: number;
  loading: boolean;
};

const OFF: TaskDueDates = { tasks: [], failed: 0, loading: false };
const LOADING: TaskDueDates = { tasks: [], failed: 0, loading: true };

/**
 * Reads the planning workspace of each bid (one request per bid, all at once) and returns the open
 * tasks that have a due date. Nothing is requested until `enabled` is true. Pass a stable `bids` list.
 */
export function useTaskDueDates(bids: PortfolioBid[], enabled: boolean): TaskDueDates {
  const { api } = useAuth();
  const key = useMemo(() => bids.map((bid) => bid.tenderId).join(","), [bids]);
  const [loaded, setLoaded] = useState<Loaded | null>(null);
  const active = enabled && bids.length > 0;

  useEffect(() => {
    if (!active) return;
    let cancelled = false;
    Promise.allSettled(bids.map((bid) => api<BidPlanningWorkspace>(`/api/v1/bid-planning/${bid.tenderId}`))).then(
      (results) => {
        if (cancelled) return;
        const tasks: TaskDueEvent[] = [];
        let failed = 0;
        results.forEach((result, index) => {
          if (result.status === "rejected") {
            failed += 1;
            return;
          }
          tasks.push(...taskEventsFromPlan(bids[index], result.value.tasks));
        });
        setLoaded({ key, tasks: sortTaskEvents(tasks), failed });
      },
    );
    return () => {
      cancelled = true;
    };
  }, [active, api, bids, key]);

  if (!active) return OFF;
  if (loaded === null || loaded.key !== key) return LOADING;
  return { tasks: loaded.tasks, failed: loaded.failed, loading: false };
}
