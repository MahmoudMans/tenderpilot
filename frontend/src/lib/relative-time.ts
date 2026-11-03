const MINUTE = 60_000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

/** "5m ago", "3h ago", "2d ago", or a short date once older than two weeks. `now` is epoch ms. */
export function timeAgo(iso: string, now: number): string {
  const then = new Date(iso).getTime();
  if (Number.isNaN(then)) return "";
  const diff = Math.max(0, now - then);
  if (diff < MINUTE) return "just now";
  if (diff < HOUR) return `${Math.floor(diff / MINUTE)}m ago`;
  if (diff < DAY) return `${Math.floor(diff / HOUR)}h ago`;
  if (diff < 14 * DAY) return `${Math.floor(diff / DAY)}d ago`;
  return new Intl.DateTimeFormat("en", { month: "short", day: "numeric" }).format(then);
}

/** Whole days from `now` until `iso` (negative when past). Uses local calendar days. */
export function daysUntil(iso: string, now: number): number {
  const target = new Date(iso);
  const current = new Date(now);
  const startTarget = new Date(target.getFullYear(), target.getMonth(), target.getDate()).getTime();
  const startCurrent = new Date(current.getFullYear(), current.getMonth(), current.getDate()).getTime();
  return Math.round((startTarget - startCurrent) / DAY);
}

/** Whole days from `now` until a calendar date such as 2026-10-15, counted in local days (negative when past). */
export function daysUntilDate(date: string, now: number): number {
  const [year, month, day] = date.split("-").map(Number);
  if (!year || !month || !day) return daysUntil(date, now);
  const current = new Date(now);
  const startCurrent = new Date(current.getFullYear(), current.getMonth(), current.getDate()).getTime();
  return Math.round((new Date(year, month - 1, day).getTime() - startCurrent) / DAY);
}

export function deadlineLabel(days: number): string {
  if (days < 0) return `${Math.abs(days)}d overdue`;
  if (days === 0) return "Due today";
  if (days === 1) return "Due tomorrow";
  return `${days} days left`;
}
