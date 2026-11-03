"use client";

import { Check, CircleDashed, Printer, TriangleAlert } from "lucide-react";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

import { Badge, type BadgeTone } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/ui/page-header";
import { ProgressBar } from "@/components/ui/progress";
import { PageSkeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-context";
import type { BidDecisionOutcome, BidDecisionWorkspace } from "@/features/bid-decision/bid-decision-types";
import type { BidPlanningWorkspace } from "@/features/bid-planning/bid-planning-types";
import type { BidComplianceMatrix } from "@/features/compliance/compliance-types";
import {
  formatDate,
  formatDateTime,
  formatEnum,
  formatValueRange,
} from "@/features/procurement/procurement-format";
import { cn } from "@/lib/cn";
import { daysUntil, deadlineLabel } from "@/lib/relative-time";
import { useNow } from "@/lib/use-clock";

type Loaded = {
  decision: BidDecisionWorkspace | null;
  planning: BidPlanningWorkspace | null;
  compliance: BidComplianceMatrix | null;
};

const outcomeTone: Record<BidDecisionOutcome, BadgeTone> = {
  GO: "success",
  HOLD: "warn",
  NO_GO: "danger",
  PENDING: "neutral",
};

const priorityRank: Record<string, number> = { BLOCKER: 0, CRITICAL: 1, HIGH: 2, MEDIUM: 3, LOW: 4 };
const taskPriorityRank: Record<string, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };

function Section({ title, children, className }: { title: string; children: React.ReactNode; className?: string }) {
  return (
    <section className={cn("print-avoid-break rounded-3xl border border-line bg-surface p-6 print:rounded-none print:border-x-0 print:border-t-0 print:px-0", className)}>
      <h2 className="text-sm font-semibold uppercase tracking-[0.14em] text-brand">{title}</h2>
      <div className="mt-4">{children}</div>
    </section>
  );
}

function Fact({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="min-w-0">
      <dt className="text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">{label}</dt>
      <dd className="mt-1 text-sm font-medium text-ink">{value}</dd>
    </div>
  );
}

function Text({ label, value }: { label: string; value: string | null }) {
  if (!value) return null;
  return (
    <div>
      <h3 className="text-xs font-semibold text-ink-2">{label}</h3>
      <p className="mt-1 whitespace-pre-line text-sm leading-6 text-muted">{value}</p>
    </div>
  );
}

export default function BidBriefPage() {
  const params = useParams<{ tenderId: string }>();
  const tenderId = params.tenderId;
  const { api } = useAuth();
  const now = useNow();
  const [loaded, setLoaded] = useState<{ tenderId: string; data: Loaded } | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([
      api<BidDecisionWorkspace>(`/api/v1/bid-decisions/${tenderId}`),
      api<BidPlanningWorkspace>(`/api/v1/bid-planning/${tenderId}`),
      api<BidComplianceMatrix>(`/api/v1/compliance/bid-workspaces/${tenderId}`),
    ]).then(([decision, planning, compliance]) => {
      if (cancelled) return;
      setLoaded({
        tenderId,
        data: {
          decision: decision.status === "fulfilled" ? decision.value : null,
          planning: planning.status === "fulfilled" ? planning.value : null,
          compliance: compliance.status === "fulfilled" ? compliance.value : null,
        },
      });
    });
    return () => {
      cancelled = true;
    };
  }, [api, tenderId]);

  if (loaded === null || loaded.tenderId !== tenderId) return <PageSkeleton />;

  const { decision, planning, compliance } = loaded.data;

  if (decision === null && planning === null && compliance === null) {
    return (
      <div className="mx-auto max-w-3xl">
        <PageHeader
          eyebrow="Bid brief"
          title="Brief unavailable"
          back={{ href: `/bid-decisions/${tenderId}`, label: "Back to decision" }}
        />
        <div className="flex items-start gap-3 rounded-2xl border border-warn-line bg-warn-soft p-5 text-sm text-warn">
          <TriangleAlert className="mt-0.5 size-4 shrink-0" />
          <p>
            No bid data could be loaded for this tender. Open the opportunity, record a decision, and the brief will
            assemble itself.
          </p>
        </div>
      </div>
    );
  }

  const opportunity = decision?.opportunity ?? null;
  const call = decision?.decision ?? null;
  const days = opportunity?.submissionDeadline ? daysUntil(opportunity.submissionDeadline, now) : null;

  const scorecard: Array<[string, number]> = call
    ? [
        ["Strategic fit", call.strategicFit],
        ["Capability confidence", call.capabilityConfidence],
        ["Commercial attractiveness", call.commercialAttractiveness],
        ["Delivery feasibility", call.deliveryFeasibility],
        ["Resource capacity", call.resourceCapacity],
        ["Risk confidence", call.riskConfidence],
      ]
    : [];

  const complianceIssues = (compliance?.rows ?? [])
    .filter((row) => row.verdict !== "COMPLIANT" && row.verdict !== "NOT_APPLICABLE")
    .sort(
      (a, b) =>
        Number(b.mandatory) - Number(a.mandatory) ||
        (priorityRank[a.priority] ?? 9) - (priorityRank[b.priority] ?? 9),
    )
    .slice(0, 12);

  const openTasks = (planning?.tasks ?? [])
    .filter((task) => task.status !== "DONE" && task.status !== "CANCELLED")
    .sort(
      (a, b) =>
        (a.dueDate ?? "9999").localeCompare(b.dueDate ?? "9999") ||
        (taskPriorityRank[a.priority] ?? 9) - (taskPriorityRank[b.priority] ?? 9),
    )
    .slice(0, 15);

  const checklist: Array<[string, boolean]> = planning
    ? [
        ["Technical response ready", planning.submissionControl.technicalResponseReady],
        ["Commercial response ready", planning.submissionControl.commercialResponseReady],
        ["Administrative pack ready", planning.submissionControl.administrativePackReady],
        ["Signatures ready", planning.submissionControl.signaturesReady],
        ["Submission portal ready", planning.submissionControl.portalReady],
        ["Final review complete", planning.submissionControl.finalReviewReady],
        ["Submission authorized", planning.submissionControl.submissionAuthorized],
      ]
    : [];

  return (
    <div className="mx-auto max-w-4xl space-y-5" data-print-reset>
      <PageHeader
        eyebrow="Bid brief"
        title={opportunity?.title ?? "Bid brief"}
        description={opportunity ? `${opportunity.buyerName} · generated ${formatDateTime(new Date(now).toISOString())}` : undefined}
        back={{ href: `/bid-decisions/${tenderId}`, label: "Back to decision" }}
        actions={
          <Button variant="primary" onClick={() => window.print()} leading={<Printer className="size-4" />}>
            Print / Save as PDF
          </Button>
        }
      />

      {opportunity ? (
        <Section title="Tender snapshot">
          <dl className="grid gap-x-6 gap-y-4 sm:grid-cols-2 lg:grid-cols-3">
            <Fact label="Status" value={formatEnum(opportunity.status)} />
            <Fact label="Procedure" value={formatEnum(opportunity.procedureType)} />
            <Fact label="Contract nature" value={formatEnum(opportunity.contractNature)} />
            <Fact
              label="Submission deadline"
              value={
                opportunity.submissionDeadline ? (
                  <>
                    {formatDateTime(opportunity.submissionDeadline)}
                    {days !== null ? <span className="ml-2 text-xs text-muted">({deadlineLabel(days)})</span> : null}
                  </>
                ) : (
                  "Not specified"
                )
              }
            />
            <Fact
              label="Estimated value"
              value={formatValueRange(opportunity.estimatedMinValue, opportunity.estimatedMaxValue, opportunity.currency)}
            />
            <Fact label="Lots" value={opportunity.lotCount} />
            <Fact label="Published" value={formatDate(opportunity.publicationDate)} />
            <Fact label="Region" value={opportunity.primaryRegionName ?? opportunity.countryCode} />
            <Fact label="Sources" value={opportunity.sources.join(", ")} />
          </dl>
          {opportunity.cpvs.length > 0 ? (
            <ul className="mt-5 flex flex-wrap gap-1.5">
              {opportunity.cpvs.map((cpv) => (
                <li key={cpv.code} className="rounded-lg border border-line bg-surface-2 px-2 py-1 text-[11px] text-ink-2">
                  <span className="font-semibold">{cpv.code}</span> {cpv.label}
                </li>
              ))}
            </ul>
          ) : null}
        </Section>
      ) : null}

      {decision ? (
        <Section title="Go / No-Go decision">
          <div className="flex flex-wrap items-center gap-3">
            <Badge tone={outcomeTone[call?.outcome ?? "PENDING"]} dot>
              {formatEnum(call?.outcome ?? "PENDING")}
            </Badge>
            {call ? <Badge>Recommended: {formatEnum(call.recommendedOutcome)}</Badge> : null}
            <Badge tone="brand">
              Match {Math.round(decision.matchScore)} · {formatEnum(decision.matchGrade)}
            </Badge>
            {call ? <Badge tone="info">Decision score {call.decisionScore.toFixed(1)}/100</Badge> : null}
          </div>

          {call ? (
            <>
              <ul className="mt-5 grid gap-x-8 gap-y-3 sm:grid-cols-2">
                {scorecard.map(([label, value]) => (
                  <li key={label}>
                    <div className="flex justify-between text-xs">
                      <span className="font-medium text-ink-2">{label}</span>
                      <span className="font-semibold tabular-nums text-ink">{value}/5</span>
                    </div>
                    <ProgressBar value={(value / 5) * 100} className="mt-1.5" label={label} />
                  </li>
                ))}
              </ul>
              <div className="mt-6 space-y-4">
                <Text label="Why this recommendation" value={call.recommendationReason} />
                <Text label="Rationale" value={call.rationale} />
                <Text label="Key risks" value={call.keyRisks} />
                <Text label="Assumptions" value={call.assumptions} />
              </div>
              {call.decidedBy ? (
                <p className="mt-5 text-xs text-muted">
                  Decided by {call.decidedBy}
                  {call.decidedAt ? ` on ${formatDateTime(call.decidedAt)}` : ""}.
                </p>
              ) : null}
            </>
          ) : (
            <p className="mt-4 text-sm text-muted">No scorecard has been recorded for this tender yet.</p>
          )}
        </Section>
      ) : null}

      {compliance ? (
        <Section title="Compliance readiness">
          <div className="flex flex-wrap items-center gap-x-8 gap-y-3">
            <div className="min-w-48 flex-1">
              <div className="flex justify-between text-xs">
                <span className="font-medium text-ink-2">Readiness score</span>
                <span className="font-semibold tabular-nums text-ink">{Math.round(compliance.statistics.readinessScore)}%</span>
              </div>
              <ProgressBar value={compliance.statistics.readinessScore} className="mt-1.5" label="Compliance readiness" />
            </div>
            <dl className="grid grid-cols-2 gap-x-8 gap-y-2 text-xs sm:grid-cols-4">
              <Fact label="Requirements" value={compliance.statistics.totalRequirements} />
              <Fact label="Reviewed" value={compliance.statistics.reviewedRequirements} />
              <Fact label="Mandatory blockers" value={compliance.statistics.mandatoryBlockerCount} />
              <Fact label="Critical risks" value={compliance.statistics.criticalRiskCount} />
            </dl>
          </div>

          {complianceIssues.length > 0 ? (
            <div className="mt-5 overflow-x-auto">
              <table className="w-full min-w-[34rem] text-left text-xs">
                <thead>
                  <tr className="border-b border-line text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
                    <th className="py-2 pr-3">Requirement</th>
                    <th className="px-3 py-2">Verdict</th>
                    <th className="px-3 py-2">Risk</th>
                    <th className="py-2 pl-3">Next action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-line align-top">
                  {complianceIssues.map((row) => (
                    <tr key={row.requirementId}>
                      <td className="py-2.5 pr-3 font-medium text-ink">
                        {row.title}
                        {row.mandatory ? <span className="ml-1.5 text-[10px] font-bold text-danger">MANDATORY</span> : null}
                      </td>
                      <td className="px-3 py-2.5">{formatEnum(row.verdict)}</td>
                      <td className="px-3 py-2.5">{formatEnum(row.riskLevel)}</td>
                      <td className="py-2.5 pl-3 text-muted">{row.nextAction}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="mt-4 text-sm text-success">No open compliance issues.</p>
          )}
        </Section>
      ) : null}

      {planning ? (
        <Section title="Bid plan">
          <dl className="grid grid-cols-2 gap-x-6 gap-y-4 sm:grid-cols-4">
            <Fact label="Task completion" value={`${Math.round(planning.statistics.taskCompletionPercentage)}%`} />
            <Fact label="Open tasks" value={planning.statistics.openTasks} />
            <Fact label="Overdue" value={planning.statistics.overdueTasks} />
            <Fact label="Blocked" value={planning.statistics.blockedTasks} />
          </dl>

          {openTasks.length > 0 ? (
            <div className="mt-5 overflow-x-auto">
              <table className="w-full min-w-[34rem] text-left text-xs">
                <thead>
                  <tr className="border-b border-line text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
                    <th className="py-2 pr-3">Task</th>
                    <th className="px-3 py-2">Milestone</th>
                    <th className="px-3 py-2">Owner</th>
                    <th className="px-3 py-2">Due</th>
                    <th className="py-2 pl-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-line align-top">
                  {openTasks.map((task) => (
                    <tr key={task.id}>
                      <td className="py-2.5 pr-3 font-medium text-ink">
                        {task.title}
                        <span className="ml-1.5 text-[10px] font-semibold text-muted">{formatEnum(task.priority)}</span>
                      </td>
                      <td className="px-3 py-2.5">{formatEnum(task.milestone)}</td>
                      <td className="px-3 py-2.5">{task.assignedTo ?? "Unassigned"}</td>
                      <td className={cn("whitespace-nowrap px-3 py-2.5", task.overdue && "font-semibold text-danger")}>
                        {task.dueDate ? formatDate(task.dueDate) : "—"}
                      </td>
                      <td className="py-2.5 pl-3">{formatEnum(task.status)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="mt-4 text-sm text-muted">No open tasks.</p>
          )}
        </Section>
      ) : null}

      {planning ? (
        <Section title="Submission checklist">
          <ul className="grid gap-2 sm:grid-cols-2">
            {checklist.map(([label, done]) => (
              <li key={label} className="flex items-center gap-2.5 text-sm">
                {done ? (
                  <Check className="size-4 text-success" aria-label="Done" />
                ) : (
                  <CircleDashed className="size-4 text-faint" aria-label="Pending" />
                )}
                <span className={done ? "text-ink" : "text-muted"}>{label}</span>
              </li>
            ))}
          </ul>
          <dl className="mt-5 grid gap-4 sm:grid-cols-3">
            <Fact
              label="Planned submission"
              value={planning.submissionControl.plannedSubmissionAt ? formatDateTime(planning.submissionControl.plannedSubmissionAt) : "Not planned"}
            />
            <Fact
              label="Submitted"
              value={planning.submissionControl.submittedAt ? formatDateTime(planning.submissionControl.submittedAt) : "Not yet"}
            />
            <Fact label="Reference" value={planning.submissionControl.submissionReference ?? "—"} />
          </dl>
        </Section>
      ) : null}

      <p className="pb-4 text-center text-[11px] text-faint">
        Generated by TenderPilot from your organization&apos;s private bid data. Confidential — for internal use.
      </p>
    </div>
  );
}
