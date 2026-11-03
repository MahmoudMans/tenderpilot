"use client";

import { ArrowRight } from "lucide-react";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { buttonClasses } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-context";
import type { BidPlanningWorkspace, BidTaskPriority } from "@/features/bid-planning/bid-planning-types";
import { cn } from "@/lib/cn";
import { daysUntilDate, deadlineLabel } from "@/lib/relative-time";
import { useNow } from "@/lib/use-clock";

import type { DashboardCommandCenter } from "./dashboard-types";

type PortfolioBid = DashboardCommandCenter["portfolio"][number];
type PlanTask = BidPlanningWorkspace["tasks"][number];

/** Plans are read for at most this many bids, and this many tasks are listed. */
const MAX_BIDS = 6;
const MAX_TASKS = 6;

type AttentionTask = {
  tenderId: string;
  bidTitle: string;
  buyerName: string;
  task: PlanTask;
};

type LoadState = { key: string; tasks: AttentionTask[]; failed: number };

const PRIORITY_RANK: Record<BidTaskPriority, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };

/** Bids still in play that the dashboard counts as having overdue, blocked or critical work. */
function bidsNeedingAttention(portfolio: PortfolioBid[]): PortfolioBid[] {
  return portfolio
    .filter(
      (bid) =>
        !bid.submitted &&
        bid.decisionOutcome !== "NO_GO" &&
        (bid.overdueTasks > 0 || bid.blockedTasks > 0 || bid.criticalOpenTasks > 0),
    )
    .sort(
      (a, b) =>
        Number(b.overdueTasks > 0) - Number(a.overdueTasks > 0) ||
        Number(b.blockedTasks > 0) - Number(a.blockedTasks > 0) ||
        b.criticalOpenTasks - a.criticalOpenTasks ||
        (a.daysToDeadline ?? Number.MAX_SAFE_INTEGER) - (b.daysToDeadline ?? Number.MAX_SAFE_INTEGER),
    );
}

function needsAttention(task: PlanTask): boolean {
  if (task.status === "DONE" || task.status === "CANCELLED") return false;
  return task.overdue || task.status === "BLOCKED" || task.priority === "CRITICAL";
}

/** Overdue work first, then blocked, then critical; ties go to the more urgent, then the earlier due date. */
function severity(task: PlanTask): number {
  return (task.overdue ? 4 : 0) + (task.status === "BLOCKED" ? 2 : 0) + (task.priority === "CRITICAL" ? 1 : 0);
}

function compareAttention(a: AttentionTask, b: AttentionTask): number {
  const bySeverity = severity(b.task) - severity(a.task);
  if (bySeverity !== 0) return bySeverity;
  const byPriority = PRIORITY_RANK[a.task.priority] - PRIORITY_RANK[b.task.priority];
  if (byPriority !== 0) return byPriority;
  if (a.task.dueDate === b.task.dueDate) return 0;
  if (a.task.dueDate === null) return 1;
  if (b.task.dueDate === null) return -1;
  return a.task.dueDate < b.task.dueDate ? -1 : 1;
}

function TaskRow({ item, now }: { item: AttentionTask; now: number }) {
  const { task } = item;
  const days = task.dueDate === null ? null : daysUntilDate(task.dueDate, now);
  return (
    <li>
      <Link
        href={`/bid-workspaces/${item.tenderId}/planning`}
        className="group flex flex-col gap-2 py-3 transition hover:opacity-80 sm:flex-row sm:items-start sm:gap-3"
      >
        <span className="min-w-0 flex-1">
          <span className="block text-sm font-semibold text-ink">{task.title}</span>
          <span className="mt-0.5 block truncate text-xs text-muted">
            {`${item.bidTitle} \u00b7 ${item.buyerName}`}
          </span>
          <span className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-0.5 text-xs text-muted">
            <span>{task.assignedTo ? `Owner: ${task.assignedTo}` : "No owner"}</span>
            <span className={cn(days !== null && days < 0 && "font-semibold text-danger")}>
              {days === null ? "No due date" : deadlineLabel(days)}
            </span>
          </span>
        </span>
        <span className="flex flex-wrap items-center gap-1.5 sm:shrink-0 sm:justify-end">
          {task.overdue ? <Badge tone="danger">Overdue</Badge> : null}
          {task.status === "BLOCKED" ? <Badge tone="warn">Blocked</Badge> : null}
          {task.priority === "CRITICAL" ? <Badge tone="danger">Critical</Badge> : null}
          <ArrowRight className="ml-auto size-4 text-faint transition group-hover:translate-x-0.5 sm:ml-0" aria-hidden="true" />
        </span>
      </Link>
    </li>
  );
}

function AttentionTasksCard({ bids, flagged }: { bids: PortfolioBid[]; flagged: number }) {
  const { api } = useAuth();
  const now = useNow();
  const key = useMemo(() => bids.map((bid) => bid.tenderId).join(","), [bids]);
  const [state, setState] = useState<LoadState | null>(null);
  const idle = bids.length === 0;

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled(bids.map((bid) => api<BidPlanningWorkspace>(`/api/v1/bid-planning/${bid.tenderId}`))).then(
      (results) => {
        if (cancelled) return;
        const tasks: AttentionTask[] = [];
        let failed = 0;
        results.forEach((result, index) => {
          if (result.status === "rejected") {
            failed += 1;
            return;
          }
          const bid = bids[index];
          for (const task of result.value.tasks) {
            if (needsAttention(task)) {
              tasks.push({ tenderId: bid.tenderId, bidTitle: bid.title, buyerName: bid.buyerName, task });
            }
          }
        });
        tasks.sort(compareAttention);
        setState({ key, tasks, failed });
      },
    );
    return () => {
      cancelled = true;
    };
  }, [api, bids, key]);

  const loading = !idle && (state === null || state.key !== key);
  const tasks = state !== null && state.key === key ? state.tasks : [];
  const shown = tasks.slice(0, MAX_TASKS);
  const failed = state !== null && state.key === key ? state.failed : 0;

  return (
    <Card aria-busy={loading}>
      <CardHeader
        eyebrow="Bid execution"
        title="Tasks that need attention"
        description="Overdue, blocked and critical open tasks across your active bids. Select one to open its plan."
        actions={
          <Link href="/tasks" className={buttonClasses("secondary", "sm")}>
            Open task list
          </Link>
        }
      />
      {loading ? (
        <div className="mt-4 space-y-3" role="status" aria-label="Loading tasks">
          {[0, 1, 2].map((item) => (
            <Skeleton key={item} className="h-14" />
          ))}
        </div>
      ) : shown.length === 0 ? (
        <p className="mt-5 rounded-2xl border border-dashed border-line-strong bg-surface-2 px-4 py-8 text-center text-sm text-muted">
          {failed > 0
            ? "The bid plans could not be loaded. Reload the page to try again."
            : "No overdue, blocked or critical tasks across your active bids."}
        </p>
      ) : (
        <ul className="mt-4 divide-y divide-line">
          {shown.map((item) => (
            <TaskRow key={`${item.tenderId}:${item.task.id}`} item={item} now={now} />
          ))}
        </ul>
      )}
      {!loading && (tasks.length > shown.length || flagged > bids.length || (failed > 0 && shown.length > 0)) ? (
        <p className="mt-3 text-xs text-muted">
          {tasks.length > shown.length ? `${tasks.length - shown.length} more on these bids. ` : ""}
          {flagged > bids.length ? `Showing the ${bids.length} most urgent of ${flagged} bids. ` : ""}
          {failed > 0 && shown.length > 0 ? `${failed} bid plan${failed === 1 ? "" : "s"} could not be loaded.` : ""}
        </p>
      ) : null}
    </Card>
  );
}

/**
 * The dashboard already counts overdue, blocked and critical tasks. This lists the tasks themselves,
 * read from the planning workspaces of the most urgent bids (at most six requests), so the team can see
 * what to chase without opening each bid. It stays hidden until some bid has tasks.
 */
export function AttentionTasks({ dashboard }: { dashboard: DashboardCommandCenter }) {
  const hasPlans = dashboard.portfolio.some((bid) => bid.openTasks + bid.completedTasks > 0);
  const flagged = useMemo(() => bidsNeedingAttention(dashboard.portfolio), [dashboard.portfolio]);
  const bids = useMemo(() => flagged.slice(0, MAX_BIDS), [flagged]);
  if (!hasPlans) return null;
  return <AttentionTasksCard bids={bids} flagged={flagged.length} />;
}
