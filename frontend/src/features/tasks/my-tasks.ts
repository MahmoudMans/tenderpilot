import type {
  BidPlanningWorkspace,
  BidTaskPriority,
  BidTaskStatus,
} from "@/features/bid-planning/bid-planning-types";
import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";
import type { CsvColumn } from "@/lib/csv";
import { OWNER_ANY, matchesOwner } from "@/lib/owner";
import { daysUntilDate } from "@/lib/relative-time";

type PortfolioBid = DashboardCommandCenter["portfolio"][number];
export type PlanTask = BidPlanningWorkspace["tasks"][number];

/** A bid and its planning workspace, or null when the workspace could not be read. */
export type BidPlan = { bid: PortfolioBid; workspace: BidPlanningWorkspace | null };

/** One task together with the bid it belongs to. */
export type WorkTask = {
  /** Unique across bids: the tender id and the task id. */
  id: string;
  tenderId: string;
  bidTitle: string;
  buyerName: string;
  /** Whether the bid's plan accepts changes, and why not when it does not. */
  writable: boolean;
  lockReason: string | null;
  task: PlanTask;
};

export const STATUS_LABELS: Record<BidTaskStatus, string> = {
  TODO: "To do",
  IN_PROGRESS: "In progress",
  BLOCKED: "Blocked",
  DONE: "Done",
  CANCELLED: "Cancelled",
};

export const STATUS_ORDER: readonly BidTaskStatus[] = ["TODO", "IN_PROGRESS", "BLOCKED", "DONE", "CANCELLED"];

const PRIORITY_RANK: Record<BidTaskPriority, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };

export const PRIORITY_LABELS: Record<BidTaskPriority, string> = {
  CRITICAL: "Critical",
  HIGH: "High",
  MEDIUM: "Medium",
  LOW: "Low",
};

export function isOpen(task: PlanTask): boolean {
  return task.status !== "DONE" && task.status !== "CANCELLED";
}

/** Every task of every plan that could be read. */
export function tasksFromPlans(plans: readonly BidPlan[]): WorkTask[] {
  const rows: WorkTask[] = [];
  for (const { bid, workspace } of plans) {
    if (workspace === null) continue;
    for (const task of workspace.tasks) {
      rows.push({
        id: `${bid.tenderId}:${task.id}`,
        tenderId: bid.tenderId,
        bidTitle: bid.title,
        buyerName: bid.buyerName,
        writable: workspace.writable,
        lockReason: workspace.writeLockReason,
        task,
      });
    }
  }
  return rows;
}

/** The status menu on top of the list: open tasks, every task, or exactly one status. */
export type StatusScope = "OPEN" | "ALL" | BidTaskStatus;

export type TaskFilters = {
  /** OWNER_ANY, OWNER_ME, OWNER_NONE or a name chosen from the menu (see lib/owner.ts). */
  owner: string;
  query: string;
  priority: "ALL" | BidTaskPriority;
  status: StatusScope;
};

export const NO_FILTERS: TaskFilters = { owner: OWNER_ANY, query: "", priority: "ALL", status: "OPEN" };

export function hasFilters(filters: TaskFilters): boolean {
  return (
    filters.owner !== OWNER_ANY || filters.query.trim() !== "" || filters.priority !== "ALL" || filters.status !== "OPEN"
  );
}

/**
 * Whether a task belongs to the chosen status scope. A task that was just changed here stays in the open
 * list for the rest of the visit, so a slip of the mouse can be undone.
 */
export function inScope(row: WorkTask, status: StatusScope, kept: ReadonlySet<string>): boolean {
  if (status === "ALL") return true;
  if (status === "OPEN") return isOpen(row.task) || kept.has(row.id);
  return row.task.status === status;
}

export function filterWorkTasks(
  rows: readonly WorkTask[],
  filters: TaskFilters,
  aliases: readonly string[],
  kept: ReadonlySet<string>,
): WorkTask[] {
  const needle = filters.query.trim().toLowerCase();
  return rows.filter((row) => {
    if (!inScope(row, filters.status, kept)) return false;
    if (filters.priority !== "ALL" && row.task.priority !== filters.priority) return false;
    if (!matchesOwner(row.task.assignedTo, filters.owner, aliases)) return false;
    if (needle === "") return true;
    return [row.task.title, row.task.description, row.task.assignedTo, row.task.requirementTitle, row.bidTitle, row.buyerName].some(
      (text) => (text ?? "").toLowerCase().includes(needle),
    );
  });
}

export type GroupId = "overdue" | "today" | "week" | "later" | "none" | "finished";

const GROUPS: ReadonlyArray<{ id: GroupId; label: string }> = [
  { id: "overdue", label: "Overdue" },
  { id: "today", label: "Due today" },
  { id: "week", label: "Next 7 days" },
  { id: "later", label: "Later" },
  { id: "none", label: "No due date" },
  { id: "finished", label: "Finished" },
];

/**
 * Where a task sits in the list, by its due date counted in local calendar days. A finished task is in the
 * Finished group unless `inPlace` is set, which keeps it where it was while it can still be undone.
 */
export function groupOf(task: PlanTask, now: number, inPlace = false): GroupId {
  if (!inPlace && !isOpen(task)) return "finished";
  if (task.dueDate === null) return "none";
  const days = daysUntilDate(task.dueDate, now);
  if (days < 0) return "overdue";
  if (days === 0) return "today";
  return days <= 7 ? "week" : "later";
}

/** Earliest due date first, no date last; then the more urgent task; then the title. */
function compareTasks(a: WorkTask, b: WorkTask): number {
  if (a.task.dueDate !== b.task.dueDate) {
    if (a.task.dueDate === null) return 1;
    if (b.task.dueDate === null) return -1;
    return a.task.dueDate < b.task.dueDate ? -1 : 1;
  }
  return (
    PRIORITY_RANK[a.task.priority] - PRIORITY_RANK[b.task.priority] ||
    a.task.title.localeCompare(b.task.title) ||
    a.id.localeCompare(b.id)
  );
}

export type TaskGroup = { id: GroupId; label: string; rows: WorkTask[] };

const NOTHING: ReadonlySet<string> = new Set();

/**
 * The tasks split into the groups that have any, most urgent group first. Tasks whose id is in `inPlace`
 * stay in the group of their due date even when finished.
 */
export function groupTasks(rows: readonly WorkTask[], now: number, inPlace: ReadonlySet<string> = NOTHING): TaskGroup[] {
  const buckets = new Map<GroupId, WorkTask[]>();
  for (const row of rows) {
    const id = groupOf(row.task, now, inPlace.has(row.id));
    const bucket = buckets.get(id);
    if (bucket) bucket.push(row);
    else buckets.set(id, [row]);
  }
  return GROUPS.flatMap(({ id, label }) => {
    const bucket = buckets.get(id);
    return bucket ? [{ id, label, rows: [...bucket].sort(compareTasks) }] : [];
  });
}

/** "Oct 22", or "Oct 22, 2027" when the date is not in the current year. */
export function formatDay(date: string, now: number): string {
  const [year, month, day] = date.split("-").map(Number);
  if (!year || !month || !day) return date;
  const options: Intl.DateTimeFormatOptions =
    year === new Date(now).getFullYear() ? { month: "short", day: "numeric" } : { month: "short", day: "numeric", year: "numeric" };
  return new Intl.DateTimeFormat("en", options).format(new Date(year, month - 1, day));
}

export const taskCsvColumns: CsvColumn<WorkTask>[] = [
  { header: "Bid", value: (row) => row.bidTitle },
  { header: "Buyer", value: (row) => row.buyerName },
  { header: "Task", value: (row) => row.task.title },
  { header: "Status", value: (row) => STATUS_LABELS[row.task.status] },
  { header: "Priority", value: (row) => PRIORITY_LABELS[row.task.priority] },
  { header: "Owner", value: (row) => row.task.assignedTo },
  { header: "Due date", value: (row) => row.task.dueDate },
  { header: "Overdue", value: (row) => row.task.overdue },
  { header: "Requirement", value: (row) => row.task.requirementTitle },
  { header: "Description", value: (row) => row.task.description },
];
