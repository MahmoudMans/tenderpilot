"use client";

import { CircleCheck, ClipboardList, Download, RefreshCw, TriangleAlert } from "lucide-react";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { Badge, type BadgeTone } from "@/components/ui/badge";
import { Button, buttonClasses } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { SearchInput, SelectField } from "@/components/ui/field";
import { FilterSummary } from "@/components/ui/filter-summary";
import { PageHeader } from "@/components/ui/page-header";
import { PageSkeleton, Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { useAuth } from "@/features/auth/auth-context";
import type { BidPlanningWorkspace, BidTaskPriority, BidTaskStatus } from "@/features/bid-planning/bid-planning-types";
import { MAX_TASK_BIDS, bidsWithOpenTasks } from "@/features/calendar/task-events";
import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";
import {
  NO_FILTERS,
  PRIORITY_LABELS,
  STATUS_LABELS,
  STATUS_ORDER,
  filterWorkTasks,
  formatDay,
  groupTasks,
  hasFilters,
  inScope,
  isOpen,
  taskCsvColumns,
  tasksFromPlans,
  type StatusScope,
  type TaskFilters,
  type WorkTask,
} from "@/features/tasks/my-tasks";
import { useBidPlans } from "@/features/tasks/use-bid-plans";
import { cn } from "@/lib/cn";
import { exportCsv } from "@/lib/csv";
import { OWNER_ANY, OWNER_ME, matchesOwner, ownerAliases, ownerOptions } from "@/lib/owner";
import { daysUntilDate, deadlineLabel } from "@/lib/relative-time";
import { useNow } from "@/lib/use-clock";

const PRIORITY_TONE: Partial<Record<BidTaskPriority, BadgeTone>> = { CRITICAL: "danger", HIGH: "warn" };

type TaskRowProps = {
  item: WorkTask;
  now: number;
  busy: boolean;
  onStatus: (item: WorkTask, status: BidTaskStatus) => void;
};

function TaskRow({ item, now, busy, onStatus }: TaskRowProps) {
  const { task } = item;
  const open = isOpen(task);
  const days = task.dueDate === null ? null : daysUntilDate(task.dueDate, now);
  const locked = !item.writable;
  const lockText = item.lockReason ?? "This bid plan is locked, so its tasks cannot be changed.";
  const priorityTone = PRIORITY_TONE[task.priority];
  return (
    <li className={cn("grid grid-cols-[auto_1fr] items-start gap-x-3 gap-y-2 py-3 sm:flex", !open && "opacity-70")}>
      <button
        type="button"
        disabled={busy || locked}
        title={locked ? lockText : undefined}
        aria-label={open ? `Mark task ${task.title} done` : `Reopen task ${task.title}`}
        onClick={() => onStatus(item, open ? "DONE" : "TODO")}
        className="group mt-0.5 grid size-6 shrink-0 place-items-center rounded-full transition hover:bg-success-soft disabled:cursor-not-allowed disabled:opacity-50"
      >
        {open ? (
          <span className="size-5 rounded-full border-2 border-line-strong transition group-hover:border-success" />
        ) : (
          <CircleCheck className={cn("size-5", task.status === "DONE" ? "text-success" : "text-faint")} />
        )}
      </button>
      <div className="min-w-0 sm:flex-1">
        <Link
          href={`/bid-workspaces/${item.tenderId}/planning`}
          className={cn("text-sm font-semibold text-ink hover:underline", !open && "line-through")}
        >
          {task.title}
        </Link>
        <p className="mt-0.5 truncate text-xs text-muted">{`${item.bidTitle} \u00b7 ${item.buyerName}`}</p>
        <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-0.5 text-xs text-muted">
          <span>{task.assignedTo ? `Owner: ${task.assignedTo}` : "No owner"}</span>
          <span className={cn(open && days !== null && days < 0 && "font-semibold text-danger")}>
            {task.dueDate === null
              ? "No due date"
              : open && days !== null
                ? `${formatDay(task.dueDate, now)} \u00b7 ${deadlineLabel(days)}`
                : `Due ${formatDay(task.dueDate, now)}`}
          </span>
        </p>
      </div>
      <div className="col-start-2 flex flex-wrap items-center gap-2 sm:shrink-0 sm:justify-end">
        <span className="flex flex-wrap justify-end gap-1.5">
          {open && priorityTone ? <Badge tone={priorityTone}>{PRIORITY_LABELS[task.priority]}</Badge> : null}
          {open && task.status === "BLOCKED" ? <Badge tone="warn">Blocked</Badge> : null}
        </span>
        <div className="w-36">
          <SelectField
            label={`Status of task ${task.title}`}
            value={task.status}
            disabled={busy || locked}
            title={locked ? lockText : undefined}
            onChange={(event) => onStatus(item, event.target.value as BidTaskStatus)}
          >
            {STATUS_ORDER.map((status) => (
              <option key={status} value={status}>
                {STATUS_LABELS[status]}
              </option>
            ))}
          </SelectField>
        </div>
      </div>
    </li>
  );
}

export default function TasksPage() {
  const { api, user } = useAuth();
  const { toast } = useToast();
  const now = useNow();

  const [dashboard, setDashboard] = useState<DashboardCommandCenter | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [reload, setReload] = useState(0);
  const [reloading, setReloading] = useState(false);

  const [ownerChoice, setOwnerChoice] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const [priority, setPriority] = useState<TaskFilters["priority"]>(NO_FILTERS.priority);
  const [status, setStatus] = useState<StatusScope>(NO_FILTERS.status);
  const [busy, setBusy] = useState<ReadonlySet<string>>(() => new Set());
  const [kept, setKept] = useState<ReadonlySet<string>>(() => new Set());

  useEffect(() => {
    let cancelled = false;
    api<DashboardCommandCenter>("/api/v1/dashboard/command-center")
      .then((result) => {
        if (cancelled) return;
        setDashboard(result);
        setError(null);
        setReloading(false);
      })
      .catch((cause) => {
        if (cancelled) return;
        setError(cause instanceof Error ? cause.message : "Unable to load your bids.");
        setReloading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [api, reload]);

  const taskBids = useMemo(() => bidsWithOpenTasks(dashboard?.portfolio ?? []), [dashboard]);
  const bidPlans = useBidPlans(taskBids.bids);
  const myNames = useMemo(() => ownerAliases(user), [user]);

  const all = useMemo(() => tasksFromPlans(bidPlans.plans), [bidPlans.plans]);
  const unreadPlans = useMemo(() => bidPlans.plans.filter((plan) => plan.workspace === null).length, [bidPlans.plans]);

  // Until a choice is made the list shows the person's own tasks, or everyone's when none name them.
  // A task just finished here still counts, so finishing the last one does not switch the list to everyone.
  const hasMine = useMemo(
    () =>
      all.some(
        (row) => (isOpen(row.task) || kept.has(row.id)) && matchesOwner(row.task.assignedTo, OWNER_ME, myNames),
      ),
    [all, kept, myNames],
  );
  const owner = ownerChoice ?? (hasMine ? OWNER_ME : OWNER_ANY);
  const filters: TaskFilters = { owner, query, priority, status };

  const inStatus = useMemo(() => all.filter((row) => inScope(row, status, kept)), [all, status, kept]);
  const visible = useMemo(
    () => filterWorkTasks(all, { owner, query, priority, status }, myNames, kept),
    [all, owner, query, priority, status, myNames, kept],
  );
  // A task changed here stays in place, in its due-date group, until the page is loaded again.
  const groups = useMemo(
    () => groupTasks(visible, now, status === "OPEN" ? kept : new Set<string>()),
    [visible, now, status, kept],
  );
  const owners = useMemo(
    () => ownerOptions(all.map((row) => row.task.assignedTo), myNames, owner),
    [all, myNames, owner],
  );

  function clearFilters() {
    setOwnerChoice(OWNER_ANY);
    setQuery("");
    setPriority(NO_FILTERS.priority);
    setStatus(NO_FILTERS.status);
  }

  function refresh() {
    setReloading(true);
    setReload((count) => count + 1);
  }

  async function changeStatus(item: WorkTask, next: BidTaskStatus) {
    if (next === item.task.status || busy.has(item.id)) return;
    setBusy((current) => new Set(current).add(item.id));
    try {
      const updated = await api<BidPlanningWorkspace>(`/api/v1/bid-planning/${item.tenderId}/tasks/${item.task.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: next }),
      });
      const saved = updated.tasks.find((task) => task.id === item.task.id);
      if (saved) bidPlans.replaceTask(item.tenderId, saved);
      setKept((current) => new Set(current).add(item.id));
      toast({
        tone: "success",
        title: next === "DONE" ? "Task marked done" : `Task set to ${STATUS_LABELS[next].toLowerCase()}`,
        description: item.task.title,
      });
    } catch (cause) {
      toast({
        tone: "error",
        title: "The task was not changed",
        description: cause instanceof Error ? cause.message : "Try again in a moment.",
      });
    } finally {
      setBusy((current) => {
        const rest = new Set(current);
        rest.delete(item.id);
        return rest;
      });
    }
  }

  function exportTasks() {
    exportCsv("tenderpilot-my-tasks", visible, taskCsvColumns);
    toast({
      tone: "success",
      title: "CSV exported",
      description: `${visible.length} ${visible.length === 1 ? "task" : "tasks"}.`,
    });
  }

  if (error && !dashboard) {
    return (
      <div className="mx-auto max-w-[1100px]">
        <PageHeader eyebrow="Pursuit" title="My tasks" />
        <EmptyState
          icon={TriangleAlert}
          title="Your tasks could not be loaded"
          description={error}
          action={
            <Button variant="primary" onClick={refresh} leading={<RefreshCw className="size-4" />}>
              Try again
            </Button>
          }
        />
      </div>
    );
  }

  if (!dashboard) return <PageSkeleton />;

  const active = hasFilters(filters);
  const loadingPlans = bidPlans.loading;
  const noTasksAtAll = !loadingPlans && all.length === 0;
  const onlyMineEmpty = visible.length === 0 && owner === OWNER_ME && query.trim() === "" && priority === "ALL";

  return (
    <div className="mx-auto max-w-[1100px]">
      <PageHeader
        eyebrow="Pursuit"
        title="My tasks"
        description="The open tasks of your active bids in one list, most urgent first. Change a status here, or open a task to see its bid plan."
        actions={
          <>
            <Button
              onClick={refresh}
              loading={reloading || bidPlans.refreshing}
              leading={<RefreshCw className="size-4" />}
            >
              Refresh
            </Button>
            <Button
              variant="primary"
              disabled={visible.length === 0}
              onClick={exportTasks}
              leading={<Download className="size-4" />}
            >
              Export CSV
            </Button>
          </>
        }
      />

      {error ? (
        <p
          role="alert"
          className="mb-4 flex items-start gap-2 rounded-xl border border-warn-line bg-warn-soft px-4 py-3 text-sm text-warn"
        >
          <TriangleAlert className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
          <span>{`The list could not be refreshed: ${error}`}</span>
        </p>
      ) : null}

      <div className="mb-5 space-y-2">
        <div className="flex flex-wrap items-center gap-2">
          <SearchInput
            className="w-full sm:w-72"
            value={query}
            onValueChange={setQuery}
            placeholder="Search tasks, bids or buyers"
            aria-label="Search tasks"
          />
          <div className="min-w-[10rem] flex-1 sm:flex-none">
            <SelectField label="Filter by owner" value={owner} onChange={(event) => setOwnerChoice(event.target.value)}>
              {owners.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </SelectField>
          </div>
          <div className="min-w-[10rem] flex-1 sm:flex-none">
            <SelectField
              label="Filter by priority"
              value={priority}
              onChange={(event) => setPriority(event.target.value as TaskFilters["priority"])}
            >
              <option value="ALL">Any priority</option>
              {(["CRITICAL", "HIGH", "MEDIUM", "LOW"] as const).map((level) => (
                <option key={level} value={level}>
                  {PRIORITY_LABELS[level]}
                </option>
              ))}
            </SelectField>
          </div>
          <div className="min-w-[10rem] flex-1 sm:flex-none">
            <SelectField
              label="Filter by status"
              value={status}
              onChange={(event) => setStatus(event.target.value as StatusScope)}
            >
              <option value="OPEN">Open tasks</option>
              <option value="ALL">Any status</option>
              {STATUS_ORDER.map((value) => (
                <option key={value} value={value}>
                  {STATUS_LABELS[value]}
                </option>
              ))}
            </SelectField>
          </div>
        </div>
        {!loadingPlans && !noTasksAtAll ? (
          <FilterSummary
            shown={visible.length}
            total={inStatus.length}
            noun="task"
            active={active}
            onClear={clearFilters}
          />
        ) : null}
      </div>

      {unreadPlans > 0 && !loadingPlans ? (
        <p
          role="status"
          className="mb-4 flex flex-wrap items-center gap-x-2 gap-y-1 rounded-xl border border-warn-line bg-warn-soft px-4 py-3 text-sm text-warn"
        >
          <TriangleAlert className="size-4 shrink-0" aria-hidden="true" />
          <span>
            {`${unreadPlans} bid ${unreadPlans === 1 ? "plan" : "plans"} could not be read, so ${unreadPlans === 1 ? "its" : "their"} tasks are missing.`}
          </span>
          <button type="button" onClick={refresh} className="font-semibold underline">
            Try again
          </button>
        </p>
      ) : null}

      {loadingPlans ? (
        <Card aria-busy="true">
          <div role="status" aria-label="Loading tasks" className="space-y-3">
            {[0, 1, 2, 3].map((item) => (
              <Skeleton key={item} className="h-14" />
            ))}
          </div>
        </Card>
      ) : noTasksAtAll ? (
        <EmptyState
          icon={ClipboardList}
          title="No open tasks"
          description="Tasks that you add to a bid plan appear here, together with the tasks of your other active bids."
          action={
            <Link href="/bid-decisions" className={buttonClasses("primary", "md")}>
              Open bid decisions
            </Link>
          }
        />
      ) : visible.length === 0 ? (
        <EmptyState
          icon={ClipboardList}
          title={onlyMineEmpty ? "Nothing is assigned to you" : "No tasks match these filters"}
          description={
            onlyMineEmpty
              ? "No task in the chosen status names you as its owner. Owners are matched by your full name or your email address."
              : "Try a different search, or clear the filters."
          }
          action={
            onlyMineEmpty ? (
              <Button variant="primary" onClick={() => setOwnerChoice(OWNER_ANY)}>
                Show all owners
              </Button>
            ) : (
              <Button variant="primary" onClick={clearFilters}>
                Clear filters
              </Button>
            )
          }
        />
      ) : (
        <div aria-busy={bidPlans.refreshing} className={cn(bidPlans.refreshing && "opacity-70 transition-opacity")}>
          {groups.map((group) => (
            <section key={group.id} aria-labelledby={`tasks-${group.id}`} className="mb-6">
              <h2
                id={`tasks-${group.id}`}
                className={cn(
                  "mb-2 flex items-center gap-2 text-sm font-semibold",
                  group.id === "overdue" ? "text-danger" : "text-ink",
                )}
              >
                {group.label}
                <Badge tone={group.id === "overdue" ? "danger" : "neutral"}>{group.rows.length}</Badge>
              </h2>
              <Card className="px-5 py-1">
                <ul className="divide-y divide-line">
                  {group.rows.map((row) => (
                    <TaskRow
                      key={row.id}
                      item={row}
                      now={now}
                      busy={busy.has(row.id)}
                      onStatus={(item, next) => void changeStatus(item, next)}
                    />
                  ))}
                </ul>
              </Card>
            </section>
          ))}
        </div>
      )}

      {!loadingPlans && taskBids.skipped > 0 ? (
        <p className="mt-2 text-xs text-muted">
          {`Tasks are read for the ${MAX_TASK_BIDS} bids closest to their deadline. ${taskBids.skipped} more ${taskBids.skipped === 1 ? "bid is" : "bids are"} not included.`}
        </p>
      ) : null}
    </div>
  );
}
