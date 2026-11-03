"use client";

import { CalendarClock, Pencil } from "lucide-react";
import { useState, type DragEvent } from "react";
import { Badge, type BadgeTone } from "@/components/ui/badge";
import { formatDate, formatEnum } from "@/features/procurement/procurement-format";
import { cn } from "@/lib/cn";
import type { BidPlanningWorkspace, BidTaskPriority, BidTaskStatus } from "./bid-planning-types";

type BoardTask = BidPlanningWorkspace["tasks"][number];

const COLUMNS: { status: BidTaskStatus; title: string; dot: string }[] = [
  { status: "TODO", title: "To do", dot: "bg-muted" },
  { status: "IN_PROGRESS", title: "In progress", dot: "bg-info" },
  { status: "BLOCKED", title: "Blocked", dot: "bg-danger" },
  { status: "DONE", title: "Done", dot: "bg-success" },
  { status: "CANCELLED", title: "Cancelled", dot: "bg-faint" },
];

const PRIORITY_RANK: Record<BidTaskPriority, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
const PRIORITY_TONE: Record<BidTaskPriority, BadgeTone> = {
  CRITICAL: "danger",
  HIGH: "warn",
  MEDIUM: "info",
  LOW: "neutral",
};

/** Open work: overdue first, then the most urgent, then the earliest due date. */
function compareOpen(a: BoardTask, b: BoardTask): number {
  if (a.overdue !== b.overdue) return a.overdue ? -1 : 1;
  const byPriority = PRIORITY_RANK[a.priority] - PRIORITY_RANK[b.priority];
  if (byPriority !== 0) return byPriority;
  if (a.dueDate !== b.dueDate) {
    if (a.dueDate === null) return 1;
    if (b.dueDate === null) return -1;
    return a.dueDate < b.dueDate ? -1 : 1;
  }
  return a.createdAt < b.createdAt ? -1 : 1;
}

/** Finished work: the most recently touched first. */
function compareClosed(a: BoardTask, b: BoardTask): number {
  if (a.updatedAt === b.updatedAt) return 0;
  return a.updatedAt < b.updatedAt ? 1 : -1;
}

type TaskBoardProps = {
  tasks: BoardTask[];
  /** False while planning is locked, for example after a No-Go decision or a submission. */
  canEdit: boolean;
  busy: boolean;
  onMove: (taskId: string, status: BidTaskStatus) => void;
  /** Opens the edit dialog. Leave it out and cards have no Edit button. */
  onEdit?: (task: BoardTask) => void;
};

/**
 * Bid tasks as columns by status. Drag a card to another column to change its status.
 * Every card also has a "Move to" menu, which is what keyboard and touch users use,
 * because browsers do not drag cards on touch screens.
 */
export function TaskBoard({ tasks, canEdit, busy, onMove, onEdit }: TaskBoardProps) {
  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [overStatus, setOverStatus] = useState<BidTaskStatus | null>(null);
  const movable = canEdit && !busy;

  function endDrag() {
    setDraggingId(null);
    setOverStatus(null);
  }

  function handleDrop(event: DragEvent<HTMLElement>, status: BidTaskStatus) {
    event.preventDefault();
    const id = event.dataTransfer.getData("text/plain");
    endDrag();
    const task = tasks.find((item) => item.id === id);
    if (task !== undefined && task.status !== status) onMove(task.id, status);
  }

  return (
    <div className="mt-6" aria-busy={busy}>
      {/* "relative" keeps the screen-reader-only labels inside this scrolling area. Without it they are
          positioned against the page and make the whole page scroll sideways on phones. */}
      <div className="relative -mx-1 flex snap-x gap-3 overflow-x-auto px-1 pb-2 xl:grid xl:grid-cols-5 xl:overflow-visible">
        {COLUMNS.map((column) => {
          const items = tasks
            .filter((task) => task.status === column.status)
            .sort(column.status === "DONE" || column.status === "CANCELLED" ? compareClosed : compareOpen);
          const isOver = overStatus === column.status && draggingId !== null;
          return (
            <section
              key={column.status}
              aria-label={`${column.title}, ${items.length} ${items.length === 1 ? "task" : "tasks"}`}
              onDragOver={(event) => {
                if (draggingId === null || !movable) return;
                event.preventDefault();
                event.dataTransfer.dropEffect = "move";
                setOverStatus(column.status);
              }}
              onDragLeave={(event) => {
                if (!event.currentTarget.contains(event.relatedTarget as Node | null)) setOverStatus(null);
              }}
              onDrop={(event) => handleDrop(event, column.status)}
              className={cn(
                "flex w-[72vw] max-w-[320px] shrink-0 snap-start flex-col rounded-2xl border bg-surface-2 p-2 transition sm:w-[290px] xl:w-auto xl:max-w-none",
                isOver ? "border-brand ring-2 ring-brand-line" : "border-line",
              )}
            >
              <header className="flex items-center justify-between px-2 pb-2 pt-1">
                <h3 className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-ink-2">
                  <span aria-hidden="true" className={cn("size-2 rounded-full", column.dot)} />
                  {column.title}
                </h3>
                <span className="rounded-full bg-surface px-2 py-0.5 text-[11px] font-semibold text-muted ring-1 ring-line">
                  {items.length}
                </span>
              </header>
              <div className="flex min-h-24 flex-1 flex-col gap-2">
                {items.map((task) => (
                  <article
                    key={task.id}
                    draggable={movable}
                    onDragStart={(event) => {
                      event.dataTransfer.setData("text/plain", task.id);
                      event.dataTransfer.effectAllowed = "move";
                      setDraggingId(task.id);
                    }}
                    onDragEnd={endDrag}
                    className={cn(
                      "rounded-xl border border-line bg-surface p-3 shadow-sm",
                      movable && "cursor-grab active:cursor-grabbing",
                      draggingId === task.id && "opacity-50",
                      task.status === "CANCELLED" && "opacity-70",
                    )}
                  >
                    <div className="flex flex-wrap items-center gap-1.5">
                      <Badge tone={PRIORITY_TONE[task.priority]}>{formatEnum(task.priority)}</Badge>
                      {task.overdue ? <Badge tone="danger">Overdue</Badge> : null}
                    </div>
                    <p
                      className={cn(
                        "mt-2 text-sm font-semibold text-ink",
                        task.status === "CANCELLED" && "line-through decoration-faint",
                      )}
                    >
                      {task.title}
                    </p>
                    {task.description ? (
                      <p className="mt-1 line-clamp-2 text-xs leading-5 text-muted">{task.description}</p>
                    ) : null}
                    <dl className="mt-2 space-y-1 text-xs text-muted">
                      <div className="flex items-center gap-1.5">
                        <dt className="sr-only">Milestone</dt>
                        <dd>{formatEnum(task.milestone)}</dd>
                      </div>
                      {task.dueDate ? (
                        <div className={cn("flex items-center gap-1.5", task.overdue && "font-semibold text-danger")}>
                          <dt className="sr-only">Due</dt>
                          <dd className="inline-flex items-center gap-1.5">
                            <CalendarClock className="size-3.5" aria-hidden="true" />
                            {formatDate(task.dueDate)}
                          </dd>
                        </div>
                      ) : null}
                      {task.assignedTo ? (
                        <div>
                          <dt className="inline">Owner: </dt>
                          <dd className="inline">{task.assignedTo}</dd>
                        </div>
                      ) : null}
                      {task.requirementTitle ? (
                        <div className="truncate" title={task.requirementTitle}>
                          <dt className="inline">Requirement: </dt>
                          <dd className="inline">{task.requirementTitle}</dd>
                        </div>
                      ) : null}
                    </dl>
                    {canEdit ? (
                      <div className="mt-3 flex items-center gap-2">
                        <select
                          aria-label={`Move task ${task.title}`}
                          value={task.status}
                          disabled={busy}
                          onChange={(event) => onMove(task.id, event.target.value as BidTaskStatus)}
                          className="h-8 min-w-0 flex-1 rounded-lg border border-line bg-surface px-2 text-xs text-ink-2"
                        >
                          {COLUMNS.map((option) => (
                            <option key={option.status} value={option.status}>
                              {option.status === task.status ? option.title : `Move to ${option.title}`}
                            </option>
                          ))}
                        </select>
                        {onEdit ? (
                          <button
                            type="button"
                            aria-label={`Edit task ${task.title}`}
                            title="Edit task"
                            disabled={busy}
                            onClick={() => onEdit(task)}
                            className="grid size-8 shrink-0 place-items-center rounded-lg border border-line text-muted transition hover:border-line-strong hover:text-ink disabled:opacity-50"
                          >
                            <Pencil className="size-3.5" aria-hidden="true" />
                          </button>
                        ) : null}
                      </div>
                    ) : null}
                  </article>
                ))}
                {items.length === 0 ? (
                  <div className="grid flex-1 place-items-center rounded-xl border border-dashed border-line p-4 text-center text-xs text-faint">
                    {isOver ? "Drop to move here" : "No tasks"}
                  </div>
                ) : null}
              </div>
            </section>
          );
        })}
      </div>
      <p className="mt-2 text-xs text-muted">
        {canEdit
          ? "Drag a card to another column. On touch screens and with a keyboard, use the menu on the card."
          : "Tasks cannot be moved while planning is locked."}
      </p>
    </div>
  );
}
