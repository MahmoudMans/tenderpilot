"use client";

import {
  CalendarDays,
  CalendarPlus,
  ChevronLeft,
  ChevronRight,
  ClipboardList,
  Download,
  List,
  TriangleAlert,
} from "lucide-react";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { Badge, type BadgeTone } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/ui/page-header";
import { SegmentedControl } from "@/components/ui/segmented-control";
import { PageSkeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { useAuth } from "@/features/auth/auth-context";
import {
  eventsFromPortfolio,
  localDayKey,
  monthGrid,
  urgencyOf,
  type DeadlineEvent,
  type Urgency,
} from "@/features/calendar/calendar-model";
import { MAX_TASK_BIDS, bidsWithOpenTasks, type TaskDueEvent } from "@/features/calendar/task-events";
import { useTaskDueDates } from "@/features/calendar/use-task-due-dates";
import type { DashboardCommandCenter } from "@/features/dashboard/dashboard-types";
import { formatEnum } from "@/features/procurement/procurement-format";
import { cn } from "@/lib/cn";
import { downloadTextFile } from "@/lib/csv";
import { buildIcs } from "@/lib/ics";
import { OWNER_ME, matchesOwner, ownerAliases } from "@/lib/owner";
import { daysUntil, daysUntilDate, deadlineLabel } from "@/lib/relative-time";
import { useStoredState } from "@/lib/storage";
import { useNow } from "@/lib/use-clock";

type ViewMode = "month" | "agenda";
type DecisionFilter = "ALL" | "GO" | "HOLD" | "PENDING" | "NO_GO";

const WEEKDAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

const urgencyChip: Record<Urgency, string> = {
  overdue: "border-danger-line bg-danger-soft text-danger",
  critical: "border-danger-line bg-danger-soft text-danger",
  soon: "border-warn-line bg-warn-soft text-warn",
  later: "border-brand-line bg-brand-soft text-brand",
  done: "border-success-line bg-success-soft text-success",
};

const urgencyTone: Record<Urgency, BadgeTone> = {
  overdue: "danger",
  critical: "danger",
  soon: "warn",
  later: "info",
  done: "success",
};

const decisionFilters: ReadonlyArray<{ value: DecisionFilter; label: string }> = [
  { value: "ALL", label: "All" },
  { value: "GO", label: "Go" },
  { value: "HOLD", label: "Hold" },
  { value: "PENDING", label: "Pending" },
  { value: "NO_GO", label: "No-go" },
];

function timeOf(iso: string): string {
  return new Intl.DateTimeFormat("en", { hour: "2-digit", minute: "2-digit" }).format(new Date(iso));
}

function EventRow({ event, now }: { event: DeadlineEvent; now: number }) {
  const days = daysUntil(event.iso, now);
  const urgency = urgencyOf(event, days);
  return (
    <li>
      <Link
        href={`/bid-decisions/${event.tenderId}`}
        className="block rounded-2xl border border-line bg-surface p-3.5 transition hover:border-line-strong hover:shadow-card"
      >
        <div className="flex flex-wrap items-center gap-2">
          <Badge tone={urgencyTone[urgency]} dot>
            {event.submitted ? "Submitted" : deadlineLabel(days)}
          </Badge>
          {event.decision ? <Badge>{formatEnum(event.decision)}</Badge> : null}
          <span className="ml-auto text-[11px] tabular-nums text-faint">{timeOf(event.iso)}</span>
        </div>
        <p className="mt-2 text-sm font-semibold leading-5 text-ink">{event.title}</p>
        <p className="mt-0.5 text-xs text-muted">{event.buyerName}</p>
        <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-[11px] text-muted">
          <span>Match {Math.round(event.matchScore)}</span>
          <span>{event.openTasks} open tasks</span>
          {event.overdueTasks > 0 ? <span className="font-semibold text-danger">{event.overdueTasks} overdue</span> : null}
          {event.ready && !event.submitted ? <span className="font-semibold text-success">Ready to submit</span> : null}
        </div>
      </Link>
    </li>
  );
}

function TaskDueRow({ task, now }: { task: TaskDueEvent; now: number }) {
  const days = daysUntilDate(task.day, now);
  return (
    <li>
      <Link
        href={`/bid-workspaces/${task.tenderId}/planning`}
        className="block rounded-2xl border border-dashed border-line-strong bg-surface p-3.5 transition hover:shadow-card"
      >
        <div className="flex flex-wrap items-center gap-2">
          <Badge tone={days < 0 ? "danger" : days <= 3 ? "warn" : "info"} dot>
            {deadlineLabel(days)}
          </Badge>
          {task.status === "BLOCKED" ? <Badge tone="warn">Blocked</Badge> : null}
          {task.priority === "CRITICAL" ? <Badge tone="danger">Critical</Badge> : null}
          <span className="ml-auto inline-flex items-center gap-1 text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
            <ClipboardList className="size-3" aria-hidden="true" />
            Task
          </span>
        </div>
        <p className="mt-2 text-sm font-semibold leading-5 text-ink">{task.title}</p>
        <p className="mt-0.5 text-xs text-muted">{`${task.bidTitle} \u00b7 ${task.buyerName}`}</p>
        <p className="mt-2 text-[11px] text-muted">{task.assignedTo ? `Owner: ${task.assignedTo}` : "No owner"}</p>
      </Link>
    </li>
  );
}

export default function CalendarPage() {
  const { api, user } = useAuth();
  const { toast } = useToast();
  const now = useNow();

  const [dashboard, setDashboard] = useState<DashboardCommandCenter | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useStoredState<ViewMode>("tp-calendar-view", "month");
  const [decision, setDecision] = useState<DecisionFilter>("ALL");
  const [hideSubmitted, setHideSubmitted] = useState(false);
  const [monthOffset, setMonthOffset] = useState(0);
  const [selectedDay, setSelectedDay] = useState<string | null>(null);
  const [showTasks, setShowTasks] = useStoredState<boolean>("tp-calendar-tasks", false);
  const [onlyMine, setOnlyMine] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api<DashboardCommandCenter>("/api/v1/dashboard/command-center")
      .then((result) => {
        if (!cancelled) setDashboard(result);
      })
      .catch((cause) => {
        if (!cancelled) {
          setError(cause instanceof Error ? cause.message : "Unable to load bid deadlines.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [api]);

  const allEvents = useMemo(() => (dashboard ? eventsFromPortfolio(dashboard.portfolio) : []), [dashboard]);

  const taskBids = useMemo(() => bidsWithOpenTasks(dashboard?.portfolio ?? []), [dashboard]);
  const taskDates = useTaskDueDates(taskBids.bids, showTasks);
  const myNames = useMemo(() => ownerAliases(user), [user]);

  const events = useMemo(
    () =>
      allEvents.filter((event) => {
        if (decision !== "ALL" && (event.decision ?? "PENDING") !== decision) return false;
        if (hideSubmitted && event.submitted) return false;
        return true;
      }),
    [allEvents, decision, hideSubmitted],
  );

  const byDay = useMemo(() => {
    const map = new Map<string, DeadlineEvent[]>();
    for (const event of events) {
      const list = map.get(event.day);
      if (list) list.push(event);
      else map.set(event.day, [event]);
    }
    return map;
  }, [events]);

  const taskEvents = useMemo(
    () =>
      taskDates.tasks.filter((task) => {
        if (decision !== "ALL" && (task.decision ?? "PENDING") !== decision) return false;
        if (onlyMine && !matchesOwner(task.assignedTo, OWNER_ME, myNames)) return false;
        return true;
      }),
    [taskDates.tasks, decision, onlyMine, myNames],
  );

  const tasksByDay = useMemo(() => {
    const map = new Map<string, TaskDueEvent[]>();
    for (const task of taskEvents) {
      const list = map.get(task.day);
      if (list) list.push(task);
      else map.set(task.day, [task]);
    }
    return map;
  }, [taskEvents]);

  const today = new Date(now);
  const todayKey = localDayKey(today);
  const shown = new Date(today.getFullYear(), today.getMonth() + monthOffset, 1);
  const grid = monthGrid(shown.getFullYear(), shown.getMonth());
  const monthLabel = new Intl.DateTimeFormat("en", { month: "long", year: "numeric" }).format(shown);

  const upcoming = events.filter((event) => !event.submitted && daysUntil(event.iso, now) >= 0);
  const overdue = events.filter((event) => !event.submitted && daysUntil(event.iso, now) < 0);
  const thisWeek = upcoming.filter((event) => daysUntil(event.iso, now) <= 7);

  const tasksDueBetween = (from: number, to: number) =>
    taskEvents.filter((task) => {
      const days = daysUntilDate(task.day, now);
      return days >= from && days <= to;
    });

  const agendaGroups: Array<{ title: string; items: DeadlineEvent[]; tasks: TaskDueEvent[] }> = [
    { title: "Overdue", items: overdue, tasks: tasksDueBetween(-Infinity, -1) },
    { title: "Next 7 days", items: thisWeek, tasks: tasksDueBetween(0, 7) },
    {
      title: "Next 30 days",
      items: upcoming.filter((event) => daysUntil(event.iso, now) > 7 && daysUntil(event.iso, now) <= 30),
      tasks: tasksDueBetween(8, 30),
    },
    {
      title: "Later",
      items: upcoming.filter((event) => daysUntil(event.iso, now) > 30),
      tasks: tasksDueBetween(31, Infinity),
    },
    { title: "Submitted", items: events.filter((event) => event.submitted), tasks: [] },
  ].filter((group) => group.items.length > 0 || group.tasks.length > 0);

  const sidePanelEvents = selectedDay ? (byDay.get(selectedDay) ?? []) : upcoming.slice(0, 6);
  const sidePanelTasks = selectedDay
    ? (tasksByDay.get(selectedDay) ?? [])
    : taskEvents.filter((task) => daysUntilDate(task.day, now) <= 7).slice(0, 6);
  const sidePanelTitle = selectedDay
    ? new Intl.DateTimeFormat("en", { weekday: "long", day: "numeric", month: "long" }).format(
        new Date(`${selectedDay}T12:00:00`),
      )
    : "Coming up";

  function exportIcs() {
    const exportable = events.filter((event) => !event.submitted && daysUntil(event.iso, now) >= 0);
    const exportableTasks = taskEvents.filter((task) => daysUntilDate(task.day, now) >= 0);
    if (exportable.length === 0 && exportableTasks.length === 0) {
      toast({ tone: "info", title: "Nothing to export", description: "There are no upcoming deadlines in the current view." });
      return;
    }
    const origin = typeof window === "undefined" ? "" : window.location.origin;
    const deadlineEvents = exportable.map((event) => ({
      uid: event.tenderId,
      title: `Bid deadline: ${event.title}`,
      start: event.iso,
      description: `${event.buyerName}\nDecision: ${event.decision ? formatEnum(event.decision) : "Pending"}\nMatch score: ${Math.round(event.matchScore)}\nOpen tasks: ${event.openTasks}`,
      url: `${origin}/bid-decisions/${event.tenderId}`,
      reminders: [1440, 120],
    }));
    // Tasks are all-day events with a reminder at 9:00 the day before (15 hours before midnight).
    const dueEvents = exportableTasks.map((task) => ({
      uid: `task-${task.taskId}`,
      title: `Task due: ${task.title}`,
      start: task.day,
      allDay: true,
      description: `${task.bidTitle} (${task.buyerName})\nOwner: ${task.assignedTo ?? "None"}\nPriority: ${formatEnum(task.priority)}\nStatus: ${formatEnum(task.status)}`,
      url: `${origin}/bid-workspaces/${task.tenderId}/planning`,
      reminders: [900],
    }));
    const content = buildIcs([...deadlineEvents, ...dueEvents], "TenderPilot bid deadlines");
    downloadTextFile("tenderpilot-deadlines.ics", content, "text/calendar;charset=utf-8");
    toast({
      tone: "success",
      title: "Calendar file downloaded",
      description:
        exportableTasks.length === 0
          ? `${exportable.length} deadline${exportable.length === 1 ? "" : "s"} with 1-day and 2-hour reminders.`
          : `${exportable.length} deadline${exportable.length === 1 ? "" : "s"} and ${exportableTasks.length} task${exportableTasks.length === 1 ? "" : "s"}. Tasks remind you at 9:00 the day before.`,
    });
  }

  if (error) {
    return (
      <div className="mx-auto max-w-[1500px]">
        <PageHeader eyebrow="Pursuit" title="Deadline calendar" />
        <EmptyState icon={TriangleAlert} title="Deadlines could not be loaded" description={error} />
      </div>
    );
  }

  if (!dashboard) return <PageSkeleton />;

  return (
    <div className="mx-auto max-w-[1500px]">
      <PageHeader
        eyebrow="Pursuit"
        title="Deadline calendar"
        description="Every bid submission deadline across your portfolio, and with task due dates turned on, what is due inside each bid. Export them to Outlook, Google or Apple Calendar with built-in reminders."
        actions={
          <>
            <SegmentedControl<ViewMode>
              label="Calendar view"
              value={view}
              onChange={setView}
              options={[
                { value: "month", label: "Month", icon: CalendarDays },
                { value: "agenda", label: "Agenda", icon: List },
              ]}
            />
            <Button variant="primary" onClick={exportIcs} leading={<CalendarPlus className="size-4" />}>
              Add to calendar (.ics)
            </Button>
          </>
        }
      />

      <div className="mb-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {[
          { label: "Upcoming", value: upcoming.length, hint: "not yet submitted" },
          { label: "Next 7 days", value: thisWeek.length, hint: "need attention soon" },
          { label: "Overdue", value: overdue.length, hint: "deadline passed", danger: overdue.length > 0 },
          { label: "Submitted", value: events.filter((event) => event.submitted).length, hint: "already sent" },
        ].map((stat) => (
          <Card key={stat.label} className="p-4">
            <p className="text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">{stat.label}</p>
            <p className={cn("mt-1.5 text-2xl font-semibold tabular-nums", stat.danger ? "text-danger" : "text-ink")}>
              {stat.value}
            </p>
            <p className="text-xs text-muted">{stat.hint}</p>
          </Card>
        ))}
      </div>

      <div className="mb-5 flex flex-wrap items-center gap-3">
        <SegmentedControl<DecisionFilter>
          label="Filter by decision"
          size="sm"
          value={decision}
          onChange={setDecision}
          options={decisionFilters}
        />
        <label className="inline-flex cursor-pointer items-center gap-2 text-xs font-medium text-ink-2">
          <input
            type="checkbox"
            checked={hideSubmitted}
            onChange={(event) => setHideSubmitted(event.target.checked)}
            className="size-4 rounded border-line-strong accent-[var(--brand)]"
          />
          Hide submitted
        </label>
        <label className="inline-flex cursor-pointer items-center gap-2 text-xs font-medium text-ink-2">
          <input
            type="checkbox"
            checked={showTasks}
            onChange={(event) => setShowTasks(event.target.checked)}
            className="size-4 rounded border-line-strong accent-[var(--brand)]"
          />
          Task due dates
        </label>
        {showTasks ? (
          <label className="inline-flex cursor-pointer items-center gap-2 text-xs font-medium text-ink-2">
            <input
              type="checkbox"
              checked={onlyMine}
              onChange={(event) => setOnlyMine(event.target.checked)}
              className="size-4 rounded border-line-strong accent-[var(--brand)]"
            />
            Only my tasks
          </label>
        ) : null}
        <span className="ml-auto text-xs text-muted">
          {events.length} of {allEvents.length} deadlines shown
          {showTasks && !taskDates.loading
            ? ` \u00b7 ${taskEvents.length} task${taskEvents.length === 1 ? "" : "s"} due`
            : ""}
        </span>
      </div>

      {showTasks && (taskDates.loading || taskDates.failed > 0 || taskBids.skipped > 0 || taskDates.tasks.length === 0) ? (
        <p className="-mt-2 mb-5 text-xs text-muted" role="status">
          {taskDates.loading ? "Reading task due dates from your bids..." : ""}
          {!taskDates.loading && taskDates.failed > 0
            ? `Task due dates for ${taskDates.failed} bid${taskDates.failed === 1 ? "" : "s"} could not be loaded. `
            : ""}
          {!taskDates.loading && taskBids.skipped > 0
            ? `Task due dates are shown for the ${MAX_TASK_BIDS} bids with the nearest deadline; ${taskBids.skipped} more are not included. `
            : ""}
          {!taskDates.loading && taskDates.failed === 0 && taskDates.tasks.length === 0
            ? "None of your active bids has an open task with a due date."
            : ""}
        </p>
      ) : null}

      {allEvents.length === 0 ? (
        <EmptyState
          icon={CalendarDays}
          title="No deadlines yet"
          description="Deadlines appear here once opportunities in your portfolio have a submission date. Record a GO decision on a matched tender to start tracking it."
          action={
            <Link
              href="/matches"
              className="inline-flex h-10 items-center rounded-xl bg-navy px-4 text-sm font-semibold text-white transition hover:bg-navy-hover"
            >
              Review matches
            </Link>
          }
        />
      ) : view === "month" ? (
        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_22rem]">
          <Card flush>
            <div className="flex items-center justify-between gap-3 border-b border-line px-5 py-4">
              <h2 className="text-lg font-semibold tracking-tight text-ink">{monthLabel}</h2>
              <div className="flex items-center gap-1.5">
                <Button size="sm" variant="secondary" onClick={() => setMonthOffset(0)} disabled={monthOffset === 0}>
                  Today
                </Button>
                <button
                  type="button"
                  onClick={() => setMonthOffset((value) => value - 1)}
                  aria-label="Previous month"
                  className="grid size-8 place-items-center rounded-lg border border-line text-muted transition hover:border-line-strong hover:text-ink"
                >
                  <ChevronLeft className="size-4" />
                </button>
                <button
                  type="button"
                  onClick={() => setMonthOffset((value) => value + 1)}
                  aria-label="Next month"
                  className="grid size-8 place-items-center rounded-lg border border-line text-muted transition hover:border-line-strong hover:text-ink"
                >
                  <ChevronRight className="size-4" />
                </button>
              </div>
            </div>

            <div className="grid grid-cols-7 border-b border-line bg-surface-2 text-center text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
              {WEEKDAYS.map((weekday) => (
                <div key={weekday} className="py-2">
                  {weekday}
                </div>
              ))}
            </div>

            <div className="grid grid-cols-7">
              {grid.map((cell, index) => {
                const dayEvents = byDay.get(cell.key) ?? [];
                const dayTasks = tasksByDay.get(cell.key) ?? [];
                const isToday = cell.key === todayKey;
                const isSelected = cell.key === selectedDay;
                return (
                  <button
                    key={cell.key}
                    type="button"
                    onClick={() => setSelectedDay(isSelected ? null : cell.key)}
                    aria-pressed={isSelected}
                    aria-label={`${cell.date.toDateString()}, ${dayEvents.length} deadline${dayEvents.length === 1 ? "" : "s"}${showTasks ? `, ${dayTasks.length} task${dayTasks.length === 1 ? "" : "s"} due` : ""}`}
                    className={cn(
                      "flex min-h-[5.5rem] flex-col items-stretch gap-1 border-b border-r border-line p-1.5 text-left transition sm:min-h-[6.5rem] sm:p-2",
                      index % 7 === 6 && "border-r-0",
                      index >= 35 && "border-b-0",
                      cell.inMonth ? "bg-surface" : "bg-surface-2",
                      isSelected ? "ring-2 ring-inset ring-brand" : "hover:bg-surface-3",
                    )}
                  >
                    <span
                      className={cn(
                        "grid size-6 place-items-center rounded-full text-xs font-semibold tabular-nums",
                        isToday ? "bg-brand text-white" : cell.inMonth ? "text-ink-2" : "text-faint",
                      )}
                    >
                      {cell.date.getDate()}
                    </span>
                    {dayEvents.slice(0, 2).map((event) => (
                      <span
                        key={event.id}
                        className={cn(
                          "truncate rounded-md border px-1.5 py-0.5 text-[10px] font-semibold leading-4",
                          urgencyChip[urgencyOf(event, daysUntil(event.iso, now))],
                        )}
                      >
                        {event.title}
                      </span>
                    ))}
                    {dayTasks.slice(0, Math.max(0, 2 - dayEvents.length)).map((task) => (
                      <span
                        key={task.id}
                        title={`Task: ${task.title}`}
                        className={cn(
                          "truncate rounded-md border border-dashed px-1.5 py-0.5 text-[10px] font-medium leading-4",
                          daysUntilDate(task.day, now) < 0
                            ? "border-danger-line bg-danger-soft text-danger"
                            : "border-line-strong bg-surface text-ink-2",
                        )}
                      >
                        {task.title}
                      </span>
                    ))}
                    {dayEvents.length + dayTasks.length > 2 ? (
                      <span className="px-1 text-[10px] font-semibold text-muted">
                        +{dayEvents.length + dayTasks.length - 2} more
                      </span>
                    ) : null}
                  </button>
                );
              })}
            </div>
          </Card>

          <Card className="h-fit xl:sticky xl:top-24">
            <CardHeader
              eyebrow={selectedDay ? "Selected day" : "Agenda"}
              title={sidePanelTitle}
              actions={
                selectedDay ? (
                  <button
                    type="button"
                    onClick={() => setSelectedDay(null)}
                    className="text-xs font-semibold text-brand transition hover:text-brand-strong"
                  >
                    Clear
                  </button>
                ) : null
              }
            />
            {sidePanelEvents.length === 0 && sidePanelTasks.length === 0 ? (
              <p className="mt-6 rounded-2xl border border-dashed border-line-strong bg-surface-2 px-4 py-8 text-center text-sm text-muted">
                {showTasks ? "No deadlines or tasks" : "No deadlines"} {selectedDay ? "on this day" : "in view"}.
              </p>
            ) : (
              <>
                {sidePanelEvents.length > 0 ? (
                  <ul className="mt-4 space-y-3">
                    {sidePanelEvents.map((event) => (
                      <EventRow key={event.id} event={event} now={now} />
                    ))}
                  </ul>
                ) : null}
                {sidePanelTasks.length > 0 ? (
                  <div className={sidePanelEvents.length > 0 ? "mt-5" : "mt-4"}>
                    <h3 className="mb-2 text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">
                      {selectedDay ? "Tasks due" : "Tasks due soon"}
                    </h3>
                    <ul className="space-y-3">
                      {sidePanelTasks.map((task) => (
                        <TaskDueRow key={task.id} task={task} now={now} />
                      ))}
                    </ul>
                  </div>
                ) : null}
              </>
            )}
          </Card>
        </div>
      ) : (
        <div className="space-y-8">
          {agendaGroups.length === 0 ? (
            <EmptyState icon={Download} title="No deadlines match these filters" description="Clear the decision filter to see everything." />
          ) : (
            agendaGroups.map((group) => (
              <section key={group.title}>
                <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-ink">
                  {group.title}
                  <span className="rounded-full bg-surface-3 px-2 py-0.5 text-[11px] font-semibold text-muted">
                    {group.items.length + group.tasks.length}
                  </span>
                </h2>
                <ul className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
                  {group.items.map((event) => (
                    <EventRow key={event.id} event={event} now={now} />
                  ))}
                  {group.tasks.map((task) => (
                    <TaskDueRow key={task.id} task={task} now={now} />
                  ))}
                </ul>
              </section>
            ))
          )}
        </div>
      )}
    </div>
  );
}
