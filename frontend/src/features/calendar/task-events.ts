import type {
  BidPlanningWorkspace,
  BidTaskPriority,
  BidTaskStatus,
} from "@/features/bid-planning/bid-planning-types";
import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";

type PortfolioBid = DashboardCommandCenter["portfolio"][number];

/** Bid plans are read for at most this many bids (one request each). */
export const MAX_TASK_BIDS = 30;

/** An open bid task with a due date, ready to be placed on the calendar. */
export type TaskDueEvent = {
  /** Unique across bids: the tender id and the task id. */
  id: string;
  taskId: string;
  tenderId: string;
  bidTitle: string;
  buyerName: string;
  /** The bid's decision, so the calendar's decision filter covers its tasks too. */
  decision: string | null;
  title: string;
  status: BidTaskStatus;
  priority: BidTaskPriority;
  assignedTo: string | null;
  /** The calendar day the task is due, YYYY-MM-DD. */
  day: string;
};

const DAY_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

const PRIORITY_RANK: Record<BidTaskPriority, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };

/**
 * The bids whose plans are read: still in play (not submitted, not No-Go) and with open tasks,
 * the closest deadline first. `skipped` counts the bids left out by the limit.
 */
export function bidsWithOpenTasks(portfolio: PortfolioBid[]): { bids: PortfolioBid[]; skipped: number } {
  const candidates = portfolio
    .filter((bid) => !bid.submitted && bid.decisionOutcome !== "NO_GO" && bid.openTasks > 0)
    .sort(
      (a, b) =>
        (a.daysToDeadline ?? Number.MAX_SAFE_INTEGER) - (b.daysToDeadline ?? Number.MAX_SAFE_INTEGER),
    );
  return {
    bids: candidates.slice(0, MAX_TASK_BIDS),
    skipped: Math.max(0, candidates.length - MAX_TASK_BIDS),
  };
}

/** The open tasks of one bid that have a due date. Finished and cancelled tasks have nothing left to do. */
export function taskEventsFromPlan(bid: PortfolioBid, tasks: BidPlanningWorkspace["tasks"]): TaskDueEvent[] {
  const events: TaskDueEvent[] = [];
  for (const task of tasks) {
    if (task.status === "DONE" || task.status === "CANCELLED") continue;
    if (task.dueDate === null || !DAY_PATTERN.test(task.dueDate)) continue;
    events.push({
      id: `${bid.tenderId}:${task.id}`,
      taskId: task.id,
      tenderId: bid.tenderId,
      bidTitle: bid.title,
      buyerName: bid.buyerName,
      decision: bid.decisionOutcome,
      title: task.title,
      status: task.status,
      priority: task.priority,
      assignedTo: task.assignedTo,
      day: task.dueDate,
    });
  }
  return events;
}

/** Earliest day first; on the same day the more urgent task, then the title. */
export function sortTaskEvents(events: TaskDueEvent[]): TaskDueEvent[] {
  return [...events].sort(
    (a, b) =>
      a.day.localeCompare(b.day) ||
      PRIORITY_RANK[a.priority] - PRIORITY_RANK[b.priority] ||
      a.title.localeCompare(b.title),
  );
}
