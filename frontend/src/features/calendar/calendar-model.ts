import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";

export type DeadlineEvent = {
  id: string;
  tenderId: string;
  title: string;
  buyerName: string;
  /** ISO timestamp of the submission deadline. */
  iso: string;
  /** Local calendar day, YYYY-MM-DD. */
  day: string;
  decision: string | null;
  matchScore: number;
  submitted: boolean;
  ready: boolean;
  openTasks: number;
  overdueTasks: number;
};

export function localDayKey(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

export function eventsFromPortfolio(portfolio: DashboardCommandCenter["portfolio"]): DeadlineEvent[] {
  const events: DeadlineEvent[] = [];
  for (const bid of portfolio) {
    if (!bid.submissionDeadline) continue;
    const when = new Date(bid.submissionDeadline);
    if (Number.isNaN(when.getTime())) continue;
    events.push({
      id: bid.tenderId,
      tenderId: bid.tenderId,
      title: bid.title,
      buyerName: bid.buyerName,
      iso: bid.submissionDeadline,
      day: localDayKey(when),
      decision: bid.decisionOutcome,
      matchScore: bid.matchScore,
      submitted: bid.submitted,
      ready: bid.readyForSubmission,
      openTasks: bid.openTasks,
      overdueTasks: bid.overdueTasks,
    });
  }
  return events.sort((a, b) => a.iso.localeCompare(b.iso));
}

export type MonthCell = {
  key: string;
  date: Date;
  inMonth: boolean;
};

/** Six-week grid starting on Monday that contains the given month. */
export function monthGrid(year: number, month: number): MonthCell[] {
  const first = new Date(year, month, 1);
  const offset = (first.getDay() + 6) % 7;
  const cells: MonthCell[] = [];
  for (let index = 0; index < 42; index += 1) {
    const date = new Date(year, month, 1 - offset + index);
    cells.push({ key: localDayKey(date), date, inMonth: date.getMonth() === month });
  }
  return cells;
}

export type Urgency = "overdue" | "critical" | "soon" | "later" | "done";

export function urgencyOf(event: DeadlineEvent, daysLeft: number): Urgency {
  if (event.submitted) return "done";
  if (daysLeft < 0) return "overdue";
  if (daysLeft <= 3) return "critical";
  if (daysLeft <= 14) return "soon";
  return "later";
}
