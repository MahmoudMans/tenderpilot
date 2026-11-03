"use client";

import { ExternalLink, X } from "lucide-react";
import Link from "next/link";
import type { ReactNode } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { cn } from "@/lib/cn";
import { daysUntil, deadlineLabel } from "@/lib/relative-time";

import { formatDate, formatEnum, formatMoney, formatValueRange } from "./procurement-format";
import type { OpportunitySummary } from "./procurement-types";
import { QualityBadge, SourceBadge, StatusBadge } from "./status-badges";

export const MAX_COMPARE = 3;

const MAX_CPVS_SHOWN = 6;

type CompareDialogProps = {
  open: boolean;
  onClose: () => void;
  items: ReadonlyArray<OpportunitySummary>;
  /** Epoch milliseconds from `useNow()`, so deadlines are not computed during render. */
  now: number;
  onRemove: (id: string) => void;
};

function isOpenStatus(item: OpportunitySummary): boolean {
  return item.status === "PUBLISHED" || item.status === "UPDATED";
}

/** The number used to rank opportunities by value: the largest estimate, else the award. */
function rankingValue(item: OpportunitySummary): number | null {
  return item.estimatedMaxValue ?? item.estimatedMinValue ?? item.awardedAmount;
}

function rankingCurrency(item: OpportunitySummary): string | null {
  return item.estimatedMaxValue !== null || item.estimatedMinValue !== null ? item.currency : item.awardedCurrency;
}

/**
 * Indexes holding the largest value. Returns nothing when fewer than two values exist
 * or when they are all equal, because nothing is "best" then.
 */
function bestIndexes(values: ReadonlyArray<number | null>): number[] {
  const present = values.filter((value): value is number => value !== null);
  if (present.length < 2) return [];
  const best = Math.max(...present);
  if (present.every((value) => value === best)) return [];
  return values.flatMap((value, index) => (value === best ? [index] : []));
}

function CompareRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <tr className="border-t border-line align-top">
      <th
        scope="row"
        className="sticky left-0 z-[1] w-28 bg-surface px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-[0.1em] text-faint sm:w-36"
      >
        {label}
      </th>
      {children}
    </tr>
  );
}

function CompareCell({ children, best = false }: { children: ReactNode; best?: boolean }) {
  return (
    <td className={cn("min-w-52 px-4 py-3 text-sm leading-5 text-ink", best && "bg-success-soft/60")}>
      {children}
      {best ? (
        <Badge tone="success" className="ml-2 align-middle">
          Best
        </Badge>
      ) : null}
    </td>
  );
}

export function CompareDialog({ open, onClose, items, now, onRemove }: CompareDialogProps) {
  const sameValueCurrency = new Set(items.map(rankingCurrency)).size <= 1;
  const bestValue = sameValueCurrency ? bestIndexes(items.map(rankingValue)) : [];
  const bestQuality = bestIndexes(items.map((item) => item.dataQualityScore));
  const bestTime = bestIndexes(
    items.map((item) =>
      isOpenStatus(item) && item.submissionDeadline !== null && new Date(item.submissionDeadline).getTime() >= now
        ? new Date(item.submissionDeadline).getTime()
        : null,
    ),
  );

  const cpvCounts = new Map<string, number>();
  for (const item of items) {
    for (const cpv of item.cpvs) cpvCounts.set(cpv.code, (cpvCounts.get(cpv.code) ?? 0) + 1);
  }
  const sharedCpvs = [...cpvCounts.entries()].filter(([, count]) => count > 1).map(([code]) => code);
  const sharedBuyer = new Set(items.map((item) => item.buyerName)).size === 1 && items.length > 1;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      size="xl"
      title="Compare opportunities"
      description={
        sharedCpvs.length > 0
          ? `${sharedCpvs.length} CPV code${sharedCpvs.length === 1 ? " is" : "s are"} shared. Green cells mark the strongest value in a row.`
          : "No CPV codes are shared. Green cells mark the strongest value in a row."
      }
      footer={<Button onClick={onClose}>Close</Button>}
    >
      <div className="-mx-6 -my-5 overflow-x-auto">
        <table className="w-full min-w-[40rem] border-collapse text-left">
          <thead>
            <tr>
              <th scope="col" className="sticky left-0 z-[1] bg-surface px-4 py-4">
                <span className="sr-only">Attribute</span>
              </th>
              {items.map((item) => (
                <th key={item.id} scope="col" className="min-w-52 px-4 py-4 align-top font-normal">
                  <div className="flex items-start justify-between gap-2">
                    <Link
                      href={`/opportunities/${item.id}`}
                      className="text-sm font-semibold leading-5 text-ink hover:text-brand"
                    >
                      {item.title}
                    </Link>
                    <button
                      type="button"
                      onClick={() => onRemove(item.id)}
                      aria-label={`Remove ${item.title} from comparison`}
                      className="grid size-7 shrink-0 place-items-center rounded-lg text-faint transition hover:bg-surface-3 hover:text-ink"
                    >
                      <X className="size-4" />
                    </button>
                  </div>
                  <Link
                    href={`/opportunities/${item.id}`}
                    className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-brand"
                  >
                    Open details
                    <ExternalLink className="size-3" aria-hidden="true" />
                  </Link>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            <CompareRow label="Status">
              {items.map((item) => (
                <CompareCell key={item.id}>
                  <StatusBadge status={item.status} />
                </CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Buyer">
              {items.map((item) => (
                <CompareCell key={item.id}>
                  {item.buyerName}
                  {sharedBuyer ? <p className="mt-1 text-xs text-muted">Same buyer</p> : null}
                </CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Location">
              {items.map((item) => (
                <CompareCell key={item.id}>{item.primaryRegionName ?? item.countryCode}</CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Deadline">
              {items.map((item, index) => {
                const stillOpen = isOpenStatus(item) && item.submissionDeadline !== null;
                const days = item.submissionDeadline === null ? 0 : daysUntil(item.submissionDeadline, now);
                return (
                  <CompareCell key={item.id} best={bestTime.includes(index)}>
                    {item.submissionDeadline === null ? (
                      <span className="text-muted">No deadline</span>
                    ) : (
                      <>
                        <span className={cn("font-semibold", stillOpen && days >= 0 && days <= 3 && "text-danger")}>
                          {stillOpen && days >= 0 ? deadlineLabel(days) : "Closed"}
                        </span>
                        <p className="mt-0.5 text-xs text-muted">{formatDate(item.submissionDeadline)}</p>
                      </>
                    )}
                  </CompareCell>
                );
              })}
            </CompareRow>

            <CompareRow label="Value">
              {items.map((item, index) => (
                <CompareCell key={item.id} best={bestValue.includes(index)}>
                  <span className="font-semibold">
                    {item.awardedAmount !== null && item.estimatedMaxValue === null && item.estimatedMinValue === null
                      ? `${formatMoney(item.awardedAmount, item.awardedCurrency)} awarded`
                      : formatValueRange(item.estimatedMinValue, item.estimatedMaxValue, item.currency)}
                  </span>
                </CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Contract">
              {items.map((item) => (
                <CompareCell key={item.id}>
                  {formatEnum(item.contractNature)}
                  <p className="mt-0.5 text-xs text-muted">{formatEnum(item.procedureType)}</p>
                </CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Lots">
              {items.map((item) => (
                <CompareCell key={item.id}>{item.lotCount}</CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Published">
              {items.map((item) => (
                <CompareCell key={item.id}>{formatDate(item.publicationDate)}</CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Data quality">
              {items.map((item, index) => (
                <CompareCell key={item.id} best={bestQuality.includes(index)}>
                  <QualityBadge grade={item.dataQualityGrade} score={item.dataQualityScore} />
                  {item.dataQualityIssueCount > 0 ? (
                    <p className="mt-1 text-xs text-muted">
                      {item.dataQualityIssueCount} open issue{item.dataQualityIssueCount === 1 ? "" : "s"}
                    </p>
                  ) : null}
                </CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="Sources">
              {items.map((item) => (
                <CompareCell key={item.id}>
                  <div className="flex flex-wrap gap-1">
                    {item.sources.map((source) => (
                      <SourceBadge key={source} source={source} />
                    ))}
                  </div>
                </CompareCell>
              ))}
            </CompareRow>

            <CompareRow label="CPV codes">
              {items.map((item) => (
                <CompareCell key={item.id}>
                  {item.cpvs.length === 0 ? (
                    <span className="text-muted">None listed</span>
                  ) : (
                    <ul className="flex flex-wrap gap-1.5">
                      {item.cpvs.slice(0, MAX_CPVS_SHOWN).map((cpv) => (
                        <li
                          key={cpv.code}
                          title={cpv.label}
                          className={cn(
                            "rounded-lg border px-2 py-1 text-[11px] font-semibold",
                            (cpvCounts.get(cpv.code) ?? 0) > 1
                              ? "border-brand-line bg-brand-soft text-brand"
                              : "border-line bg-surface-2 text-ink-2",
                          )}
                        >
                          {cpv.code}
                        </li>
                      ))}
                      {item.cpvs.length > MAX_CPVS_SHOWN ? (
                        <li className="px-1 py-1 text-[11px] font-medium text-muted">
                          +{item.cpvs.length - MAX_CPVS_SHOWN} more
                        </li>
                      ) : null}
                    </ul>
                  )}
                </CompareCell>
              ))}
            </CompareRow>
          </tbody>
        </table>
      </div>
    </Dialog>
  );
}
