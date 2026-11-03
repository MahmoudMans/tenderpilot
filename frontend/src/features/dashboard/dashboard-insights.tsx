"use client";

import { ArrowRight, CalendarDays, Check, Rocket, X } from "lucide-react";
import Link from "next/link";

import { Badge, type BadgeTone } from "@/components/ui/badge";
import { Card, CardHeader } from "@/components/ui/card";
import { Funnel } from "@/components/ui/charts";
import { RingGauge } from "@/components/ui/progress";
import { useNotifications } from "@/features/notifications/notifications-context";
import { cn } from "@/lib/cn";
import { daysUntil, deadlineLabel } from "@/lib/relative-time";
import { useStoredState } from "@/lib/storage";
import { STORAGE_KEYS } from "@/lib/storage-keys";
import { useNow } from "@/lib/use-clock";

import { AttentionTasks } from "./attention-tasks";
import type { DashboardCommandCenter } from "./dashboard-types";

type OnboardingState = { dismissed: boolean };
const ONBOARDING_DEFAULT: OnboardingState = { dismissed: false };

type Step = {
  id: string;
  title: string;
  detail: string;
  href: string;
  done: boolean;
};

function buildSteps(dashboard: DashboardCommandCenter, watchlistCount: number | null): Step[] {
  const { summary, portfolio } = dashboard;
  const decided = summary.goBids + summary.holdBids + summary.noGoBids;
  const hasPlan = portfolio.some((bid) => bid.openTasks + bid.completedTasks > 0);
  return [
    {
      id: "profile",
      title: "Complete your company profile",
      detail: "CPV codes, regions and capacity power every match score.",
      href: "/company",
      done: summary.matchedOpportunities > 0,
    },
    {
      id: "matches",
      title: "Review your best matches",
      detail: "Open the ranked list and inspect why a tender fits.",
      href: "/matches",
      done: summary.highPotentialMatches > 0 && decided + summary.pendingDecisionBids > 0,
    },
    {
      id: "decision",
      title: "Record a bid decision",
      detail: "Mark a tender GO, HOLD or NO-GO with a written rationale.",
      href: "/bid-decisions",
      done: decided > 0,
    },
    {
      id: "plan",
      title: "Plan the bid",
      detail: "Create tasks, owners and due dates in a bid workspace.",
      href: "/bid-decisions",
      done: hasPlan,
    },
    {
      id: "watch",
      title: "Watch a buyer or CPV",
      detail: "Get alerts when new notices appear that match your focus.",
      href: "/notifications",
      done: (watchlistCount ?? 0) > 0,
    },
  ];
}

function OnboardingChecklist({ dashboard }: { dashboard: DashboardCommandCenter }) {
  const { center } = useNotifications();
  const [state, setState] = useStoredState<OnboardingState>(STORAGE_KEYS.onboarding, ONBOARDING_DEFAULT);

  const steps = buildSteps(dashboard, center ? center.summary.watchlistCount : null);
  const completed = steps.filter((step) => step.done).length;

  if (state.dismissed || completed === steps.length) return null;

  const next = steps.find((step) => !step.done);

  return (
    <Card>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <RingGauge
            value={(completed / steps.length) * 100}
            size={64}
            strokeWidth={7}
            tone="brand"
            label={`${completed} of ${steps.length} steps done`}
          >
            {completed}/{steps.length}
          </RingGauge>
          <div>
            <p className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-brand">
              <Rocket className="size-3.5" /> Getting started
            </p>
            <h2 className="mt-1 text-lg font-semibold tracking-tight text-ink">Set up your bid engine</h2>
            {next ? (
              <Link
                href={next.href}
                className="mt-0.5 inline-flex items-center gap-1 text-xs font-semibold text-brand hover:text-brand-strong"
              >
                Next: {next.title} <ArrowRight className="size-3" />
              </Link>
            ) : null}
          </div>
        </div>
        <button
          type="button"
          onClick={() => setState({ dismissed: true })}
          className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-semibold text-muted transition hover:bg-surface-3 hover:text-ink"
        >
          <X className="size-3.5" /> Hide checklist
        </button>
      </div>

      <ol className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
        {steps.map((step, index) => (
          <li key={step.id}>
            <Link
              href={step.href}
              className={cn(
                "flex h-full flex-col rounded-2xl border p-3.5 transition",
                step.done
                  ? "border-success-line bg-success-soft"
                  : "border-line bg-surface-2 hover:border-brand-line hover:bg-brand-soft",
              )}
            >
              <span
                className={cn(
                  "grid size-6 place-items-center rounded-full text-[11px] font-bold",
                  step.done ? "bg-success text-white" : "border border-line-strong bg-surface text-muted",
                )}
              >
                {step.done ? <Check className="size-3.5" /> : index + 1}
              </span>
              <span className={cn("mt-2.5 text-sm font-semibold leading-5", step.done ? "text-success" : "text-ink")}>
                {step.title}
              </span>
              <span className="mt-1 text-xs leading-5 text-muted">{step.detail}</span>
            </Link>
          </li>
        ))}
      </ol>
    </Card>
  );
}

function PipelineCard({ dashboard }: { dashboard: DashboardCommandCenter }) {
  const { summary } = dashboard;
  const stages = [
    { label: "Matched opportunities", value: summary.matchedOpportunities },
    { label: "High-potential matches", value: summary.highPotentialMatches },
    { label: "GO decisions", value: summary.goBids },
    { label: "Compliance ready", value: summary.complianceReadyBids },
    { label: "Submission ready", value: summary.submissionReadyBids },
    { label: "Submitted", value: summary.submittedBids },
  ];

  return (
    <Card>
      <CardHeader
        eyebrow="Pipeline"
        title="From match to submission"
        description="How many tenders survive each gate. Percentages compare each stage with the previous one."
        actions={
          <Link href="/bid-decisions" className="text-xs font-semibold text-brand hover:text-brand-strong">
            Open decisions →
          </Link>
        }
      />
      <div className="mt-5">
        <Funnel stages={stages} />
      </div>
    </Card>
  );
}

function urgencyTone(days: number): BadgeTone {
  if (days < 0) return "danger";
  if (days <= 3) return "danger";
  if (days <= 14) return "warn";
  return "info";
}

function DeadlinesCard({ dashboard }: { dashboard: DashboardCommandCenter }) {
  const now = useNow();
  const upcoming = dashboard.portfolio
    .filter((bid) => bid.submissionDeadline !== null && !bid.submitted)
    .map((bid) => ({ bid, days: daysUntil(bid.submissionDeadline as string, now) }))
    .sort((a, b) => a.days - b.days)
    .slice(0, 5);

  return (
    <Card>
      <CardHeader
        eyebrow="Time-critical"
        title="Next submission deadlines"
        actions={
          <Link
            href="/calendar"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand hover:text-brand-strong"
          >
            <CalendarDays className="size-3.5" /> Open calendar
          </Link>
        }
      />
      {upcoming.length === 0 ? (
        <p className="mt-5 rounded-2xl border border-dashed border-line-strong bg-surface-2 px-4 py-8 text-center text-sm text-muted">
          No open deadlines. Record a GO decision to start tracking one.
        </p>
      ) : (
        <ul className="mt-4 divide-y divide-line">
          {upcoming.map(({ bid, days }) => (
            <li key={bid.tenderId}>
              <Link
                href={`/bid-decisions/${bid.tenderId}`}
                className="flex items-center gap-3 py-3 transition hover:opacity-80"
              >
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-sm font-semibold text-ink">{bid.title}</span>
                  <span className="block truncate text-xs text-muted">
                    {bid.buyerName} · {bid.openTasks} open task{bid.openTasks === 1 ? "" : "s"}
                    {bid.overdueTasks > 0 ? ` · ${bid.overdueTasks} overdue` : ""}
                  </span>
                </span>
                <Badge tone={urgencyTone(days)} dot>
                  {deadlineLabel(days)}
                </Badge>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}

export function DashboardInsights({ dashboard }: { dashboard: DashboardCommandCenter }) {
  return (
    <div className="mt-6 space-y-6">
      <OnboardingChecklist dashboard={dashboard} />
      <AttentionTasks dashboard={dashboard} />
      <div className="grid gap-6 xl:grid-cols-2">
        <PipelineCard dashboard={dashboard} />
        <DeadlinesCard dashboard={dashboard} />
      </div>
    </div>
  );
}
