"use client";

import {
  Building2,
  CalendarClock,
  Database,
  Download,
  FileSearch,
  GitCompareArrows,
  Layers3,
  LayoutGrid,
  MapPinned,
  RotateCcw,
  Table2,
  TriangleAlert,
  WalletCards,
  X,
} from "lucide-react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { SearchInput, SelectField } from "@/components/ui/field";
import { PageHeader } from "@/components/ui/page-header";
import { Pagination } from "@/components/ui/pagination";
import { SegmentedControl } from "@/components/ui/segmented-control";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { useAuth } from "@/features/auth/auth-context";
import {
  formatDate,
  formatEnum,
  formatMoney,
  formatValueRange,
} from "@/features/procurement/procurement-format";
import type {
  OpportunitySummary,
  ProcurementSource,
  ProcurementStatus,
} from "@/features/procurement/procurement-types";
import { CompareDialog, MAX_COMPARE } from "@/features/procurement/compare-dialog";
import { QualityBadge, SourceBadge, StatusBadge } from "@/features/procurement/status-badges";
import { SavedViewsMenu } from "@/features/saved-views/saved-views-menu";
import { useSavedViews } from "@/features/saved-views/use-saved-views";
import { cn } from "@/lib/cn";
import { exportCsv, type CsvColumn } from "@/lib/csv";
import { daysUntil, deadlineLabel } from "@/lib/relative-time";
import { useStoredState } from "@/lib/storage";
import { STORAGE_KEYS } from "@/lib/storage-keys";
import { useNow } from "@/lib/use-clock";
import { readOption, readText, type ParamReader } from "@/features/navigation/url-filters";

type SortKey = "deadline" | "published" | "value" | "quality";
type ViewMode = "cards" | "table";

type FilterState = {
  query: string;
  status: "" | ProcurementStatus;
  source: "" | ProcurementSource;
  minQuality: "" | "50" | "70" | "85";
  openOnly: boolean;
  sort: SortKey;
};

const DEFAULT_FILTERS: FilterState = {
  query: "",
  status: "",
  source: "",
  minQuality: "",
  openOnly: false,
  sort: "deadline",
};

const STATUS_OPTIONS: ProcurementStatus[] = ["PUBLISHED", "UPDATED", "CLOSED", "AWARDED", "CANCELLED", "ARCHIVED"];
const SOURCE_OPTIONS: ProcurementSource[] = ["TED", "BOAMP", "DECP", "MANUAL_DEMO"];

const SORT_LABELS: Record<SortKey, string> = {
  deadline: "Soonest deadline",
  published: "Newest published",
  value: "Highest value",
  quality: "Best data quality",
};

const PAGE_SIZE: Record<ViewMode, number> = { cards: 12, table: 25 };

function serverPath(filters: FilterState, query: string): string {
  const parameters = new URLSearchParams();
  if (query.trim() !== "") parameters.set("q", query.trim());
  if (filters.status !== "") parameters.set("status", filters.status);
  if (filters.source !== "") parameters.set("source", filters.source);
  if (filters.minQuality !== "") parameters.set("minQualityScore", filters.minQuality);
  const text = parameters.toString();
  return text === "" ? "/api/v1/opportunities" : `/api/v1/opportunities?${text}`;
}

/** Reads the starting filters from the address, so links like /opportunities?status=PUBLISHED&open=1 work. */
function filtersFromParams(params: ParamReader): FilterState {
  return {
    query: readText(params, "q"),
    status: readOption<FilterState["status"]>(params, "status", STATUS_OPTIONS, ""),
    source: readOption<FilterState["source"]>(params, "source", SOURCE_OPTIONS, ""),
    minQuality: readOption<FilterState["minQuality"]>(params, "minQualityScore", ["50", "70", "85"], ""),
    openOnly: params.get("open") === "1",
    sort: readOption<SortKey>(params, "sort", Object.keys(SORT_LABELS) as SortKey[], DEFAULT_FILTERS.sort),
  };
}

function valueOf(item: OpportunitySummary): number {
  return item.estimatedMaxValue ?? item.estimatedMinValue ?? item.awardedAmount ?? -1;
}

function isOpenForBids(item: OpportunitySummary, now: number): boolean {
  if (item.status !== "PUBLISHED" && item.status !== "UPDATED") return false;
  if (item.submissionDeadline === null) return true;
  return new Date(item.submissionDeadline).getTime() >= now;
}

function sortItems(items: OpportunitySummary[], sort: SortKey, now: number): OpportunitySummary[] {
  const list = [...items];
  switch (sort) {
    case "deadline":
      return list.sort((a, b) => {
        const aOpen = a.submissionDeadline !== null && new Date(a.submissionDeadline).getTime() >= now;
        const bOpen = b.submissionDeadline !== null && new Date(b.submissionDeadline).getTime() >= now;
        if (aOpen !== bOpen) return aOpen ? -1 : 1;
        if (a.submissionDeadline === null) return b.submissionDeadline === null ? 0 : 1;
        if (b.submissionDeadline === null) return -1;
        return aOpen
          ? a.submissionDeadline.localeCompare(b.submissionDeadline)
          : b.submissionDeadline.localeCompare(a.submissionDeadline);
      });
    case "published":
      return list.sort((a, b) => b.publicationDate.localeCompare(a.publicationDate));
    case "value":
      return list.sort((a, b) => valueOf(b) - valueOf(a));
    case "quality":
      return list.sort((a, b) => (b.dataQualityScore ?? -1) - (a.dataQualityScore ?? -1));
  }
}

const csvColumns: CsvColumn<OpportunitySummary>[] = [
  { header: "Id", value: (item) => item.id },
  { header: "Title", value: (item) => item.title },
  { header: "Buyer", value: (item) => item.buyerName },
  { header: "Status", value: (item) => item.status },
  { header: "Procedure", value: (item) => item.procedureType },
  { header: "Contract nature", value: (item) => item.contractNature },
  { header: "Country", value: (item) => item.countryCode },
  { header: "Region", value: (item) => item.primaryRegionName },
  { header: "Published", value: (item) => item.publicationDate },
  { header: "Submission deadline", value: (item) => item.submissionDeadline },
  { header: "Estimated min value", value: (item) => item.estimatedMinValue },
  { header: "Estimated max value", value: (item) => item.estimatedMaxValue },
  { header: "Currency", value: (item) => item.currency },
  { header: "Awarded amount", value: (item) => item.awardedAmount },
  { header: "CPV codes", value: (item) => item.cpvs.map((cpv) => cpv.code).join(" ") },
  { header: "Sources", value: (item) => item.sources.join(" ") },
  { header: "Lots", value: (item) => item.lotCount },
  { header: "Data quality score", value: (item) => item.dataQualityScore },
];

function DeadlineChip({ item, now }: { item: OpportunitySummary; now: number }) {
  if (item.submissionDeadline === null) return <span className="text-muted">No deadline</span>;
  const open = item.status === "PUBLISHED" || item.status === "UPDATED";
  const days = daysUntil(item.submissionDeadline, now);
  if (!open || days < 0) {
    return <span className="text-muted">{formatDate(item.submissionDeadline)}</span>;
  }
  return (
    <span
      className={cn(
        "font-semibold",
        days <= 3 ? "text-danger" : days <= 14 ? "text-warn" : "text-ink",
      )}
    >
      {deadlineLabel(days)}
    </span>
  );
}

type CompareProps = {
  selectedIds: ReadonlySet<string>;
  onToggle: (item: OpportunitySummary) => void;
};

function CompareToggle({ item, selectedIds, onToggle }: CompareProps & { item: OpportunitySummary }) {
  const checked = selectedIds.has(item.id);
  return (
    <label
      className={cn(
        "relative z-10 inline-flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1 text-xs font-semibold transition",
        checked ? "bg-brand-soft text-brand" : "text-muted hover:bg-surface-3 hover:text-ink",
      )}
    >
      <input
        type="checkbox"
        checked={checked}
        onChange={() => onToggle(item)}
        aria-label={`Compare ${item.title}`}
        className="size-4 rounded border-line-strong accent-[var(--brand)]"
      />
      Compare
    </label>
  );
}

function OpportunityCard({
  item,
  now,
  selectedIds,
  onToggle,
}: { item: OpportunitySummary; now: number } & CompareProps) {
  const awarded = item.awardedAmount !== null;
  return (
    <Card className="group relative flex flex-col p-5 transition hover:border-line-strong hover:shadow-pop">
      <div className="flex flex-wrap items-center gap-1.5">
        <StatusBadge status={item.status} />
        <Badge>{formatEnum(item.contractNature)}</Badge>
        {item.sources.map((source) => (
          <SourceBadge key={source} source={source} />
        ))}
        <span className="ml-auto">
          <QualityBadge grade={item.dataQualityGrade} score={item.dataQualityScore} />
        </span>
      </div>

      <h2 className="mt-3 text-[17px] font-semibold leading-snug tracking-tight text-ink">
        <Link
          href={`/opportunities/${item.id}`}
          className="after:absolute after:inset-0 after:rounded-3xl focus-visible:outline-none focus-visible:after:ring-2 focus-visible:after:ring-brand"
        >
          {item.title}
        </Link>
      </h2>

      <div className="mt-2.5 flex flex-wrap gap-x-4 gap-y-1.5 text-xs text-muted">
        <span className="inline-flex items-center gap-1.5">
          <Building2 className="size-3.5" aria-hidden="true" />
          {item.buyerName}
        </span>
        <span className="inline-flex items-center gap-1.5">
          <MapPinned className="size-3.5" aria-hidden="true" />
          {item.primaryRegionName ?? item.countryCode}
        </span>
        <span className="inline-flex items-center gap-1.5">
          <CalendarClock className="size-3.5" aria-hidden="true" />
          Published {formatDate(item.publicationDate)}
        </span>
      </div>

      {item.cpvs.length > 0 ? (
        <ul className="mt-3 flex flex-wrap gap-1.5">
          {item.cpvs.slice(0, 3).map((cpv) => (
            <li
              key={cpv.code}
              className="max-w-full truncate rounded-lg border border-line bg-surface-2 px-2 py-1 text-[11px] text-ink-2"
              title={`${cpv.code} ${cpv.label}`}
            >
              <span className="font-semibold">{cpv.code}</span> {cpv.label}
            </li>
          ))}
          {item.cpvs.length > 3 ? (
            <li className="px-1 py-1 text-[11px] font-medium text-muted">+{item.cpvs.length - 3} more</li>
          ) : null}
        </ul>
      ) : null}

      <div className="mt-auto pt-5">
        <dl className="grid grid-cols-3 gap-3 border-t border-line pt-4 text-xs">
          <div className="min-w-0">
            <dt className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
              <WalletCards className="size-3.5" aria-hidden="true" />
              {awarded ? "Awarded" : "Value"}
            </dt>
            <dd className="mt-1 font-semibold leading-5 text-ink">
              {awarded
                ? formatMoney(item.awardedAmount, item.awardedCurrency)
                : formatValueRange(item.estimatedMinValue, item.estimatedMaxValue, item.currency)}
            </dd>
          </div>
          <div className="min-w-0">
            <dt className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
              <CalendarClock className="size-3.5" aria-hidden="true" />
              Deadline
            </dt>
            <dd className="mt-1 leading-5">
              <DeadlineChip item={item} now={now} />
            </dd>
          </div>
          <div className="min-w-0">
            <dt className="flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
              <Layers3 className="size-3.5" aria-hidden="true" />
              Lots
            </dt>
            <dd className="mt-1 font-semibold leading-5 text-ink">{item.lotCount}</dd>
          </div>
        </dl>
        <div data-print-hide className="mt-3 flex justify-end">
          <CompareToggle item={item} selectedIds={selectedIds} onToggle={onToggle} />
        </div>
      </div>
    </Card>
  );
}

function OpportunityTable({ items, now, selectedIds, onToggle }: { items: OpportunitySummary[]; now: number } & CompareProps) {
  return (
    <Card flush>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[56rem] text-left text-sm">
          <thead>
            <tr className="border-b border-line bg-surface-2 text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
              <th scope="col" className="w-12 px-5 py-3">
                <span className="sr-only">Compare</span>
              </th>
              <th scope="col" className="px-3 py-3">Opportunity</th>
              <th scope="col" className="px-3 py-3">Status</th>
              <th scope="col" className="px-3 py-3">Deadline</th>
              <th scope="col" className="px-3 py-3">Estimated value</th>
              <th scope="col" className="px-3 py-3">Quality</th>
              <th scope="col" className="px-5 py-3">Source</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {items.map((item) => (
              <tr key={item.id} className="relative transition hover:bg-surface-2">
                <td className="relative z-10 px-5 py-3.5">
                  <input
                    type="checkbox"
                    checked={selectedIds.has(item.id)}
                    onChange={() => onToggle(item)}
                    aria-label={`Compare ${item.title}`}
                    className="size-4 rounded border-line-strong accent-[var(--brand)]"
                  />
                </td>
                <td className="max-w-sm px-3 py-3.5">
                  <Link
                    href={`/opportunities/${item.id}`}
                    className="font-semibold leading-5 text-ink after:absolute after:inset-0 hover:text-brand focus-visible:outline-none focus-visible:after:ring-2 focus-visible:after:ring-inset focus-visible:after:ring-brand"
                  >
                    {item.title}
                  </Link>
                  <p className="mt-0.5 truncate text-xs text-muted">
                    {item.buyerName} · {item.primaryRegionName ?? item.countryCode}
                  </p>
                </td>
                <td className="px-3 py-3.5">
                  <StatusBadge status={item.status} />
                </td>
                <td className="whitespace-nowrap px-3 py-3.5 text-xs">
                  <DeadlineChip item={item} now={now} />
                  {item.submissionDeadline ? (
                    <p className="mt-0.5 text-[11px] text-faint">{formatDate(item.submissionDeadline)}</p>
                  ) : null}
                </td>
                <td className="whitespace-nowrap px-3 py-3.5 text-xs font-medium text-ink">
                  {item.awardedAmount !== null
                    ? `${formatMoney(item.awardedAmount, item.awardedCurrency)} awarded`
                    : formatValueRange(item.estimatedMinValue, item.estimatedMaxValue, item.currency)}
                </td>
                <td className="px-3 py-3.5">
                  <QualityBadge compact grade={item.dataQualityGrade} score={item.dataQualityScore} />
                </td>
                <td className="px-5 py-3.5">
                  <div className="flex flex-wrap gap-1">
                    {item.sources.map((source) => (
                      <SourceBadge key={source} source={source} />
                    ))}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}

function ListSkeleton({ mode }: { mode: ViewMode }) {
  return (
    <div
      role="status"
      aria-label="Loading opportunities"
      className={mode === "cards" ? "grid gap-4 md:grid-cols-2 2xl:grid-cols-3" : "space-y-2"}
    >
      {Array.from({ length: mode === "cards" ? 6 : 8 }, (_, index) => (
        <Skeleton key={index} className={mode === "cards" ? "h-56 rounded-3xl" : "h-14"} />
      ))}
    </div>
  );
}

export default function OpportunitiesPage() {
  const { api } = useAuth();
  const { toast } = useToast();
  const now = useNow();

  const searchParams = useSearchParams();
  const [filters, setFilters] = useState<FilterState>(() => filtersFromParams(searchParams));
  const [debouncedQuery, setDebouncedQuery] = useState(filters.query);
  const [page, setPage] = useState(1);
  const [reloadKey, setReloadKey] = useState(0);
  const [view, setView] = useStoredState<ViewMode>(STORAGE_KEYS.opportunitiesView, "cards");
  const [result, setResult] = useState<{ key: string; data: OpportunitySummary[] } | null>(null);
  const [failure, setFailure] = useState<{ key: string; message: string } | null>(null);

  const { views, save, remove } = useSavedViews<FilterState>("opportunities");

  const [compareItems, setCompareItems] = useState<OpportunitySummary[]>([]);
  const [compareOpen, setCompareOpen] = useState(false);
  const selectedIds = useMemo(() => new Set(compareItems.map((item) => item.id)), [compareItems]);

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(filters.query), 350);
    return () => window.clearTimeout(timer);
  }, [filters.query]);

  const requestPath = serverPath(filters, debouncedQuery);
  const requestKey = `${requestPath}#${reloadKey}`;

  useEffect(() => {
    let cancelled = false;
    api<OpportunitySummary[]>(requestPath)
      .then((data) => {
        if (!cancelled) setResult({ key: requestKey, data });
      })
      .catch((cause) => {
        if (!cancelled) {
          setFailure({
            key: requestKey,
            message: cause instanceof Error ? cause.message : "Unable to load opportunities.",
          });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [api, requestPath, requestKey]);

  const loading = result?.key !== requestKey && failure?.key !== requestKey;
  const error = failure?.key === requestKey ? failure.message : null;

  const visible = useMemo(() => {
    const base = result?.data ?? [];
    const filtered = filters.openOnly ? base.filter((item) => isOpenForBids(item, now)) : base;
    return sortItems(filtered, filters.sort, now);
  }, [result, filters.openOnly, filters.sort, now]);

  const stats = useMemo(() => {
    const open = visible.filter((item) => isOpenForBids(item, now));
    const closingSoon = open.filter(
      (item) => item.submissionDeadline !== null && daysUntil(item.submissionDeadline, now) <= 7,
    );
    const scored = visible.filter((item) => item.dataQualityScore !== null);
    const average =
      scored.length === 0
        ? null
        : Math.round(scored.reduce((sum, item) => sum + (item.dataQualityScore ?? 0), 0) / scored.length);
    return { open: open.length, closingSoon: closingSoon.length, average };
  }, [visible, now]);

  const pageSize = PAGE_SIZE[view];
  const pageCount = Math.max(1, Math.ceil(visible.length / pageSize));
  const currentPage = Math.min(page, pageCount);
  const pageItems = visible.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  function update(patch: Partial<FilterState>) {
    setFilters((previous) => ({ ...previous, ...patch }));
    setPage(1);
  }

  function reset() {
    setFilters(DEFAULT_FILTERS);
    setDebouncedQuery("");
    setPage(1);
  }

  const dirty =
    filters.query !== "" ||
    filters.status !== "" ||
    filters.source !== "" ||
    filters.minQuality !== "" ||
    filters.openOnly;

  const chips: Array<{ label: string; clear: () => void }> = [];
  if (filters.query.trim() !== "") chips.push({ label: `“${filters.query.trim()}”`, clear: () => update({ query: "" }) });
  if (filters.status !== "") chips.push({ label: formatEnum(filters.status), clear: () => update({ status: "" }) });
  if (filters.source !== "") chips.push({ label: filters.source, clear: () => update({ source: "" }) });
  if (filters.minQuality !== "") {
    chips.push({ label: `Quality ≥ ${filters.minQuality}`, clear: () => update({ minQuality: "" }) });
  }
  if (filters.openOnly) chips.push({ label: "Open for bids", clear: () => update({ openOnly: false }) });

  function toggleCompare(item: OpportunitySummary) {
    if (selectedIds.has(item.id)) {
      setCompareItems((previous) => previous.filter((entry) => entry.id !== item.id));
      return;
    }
    if (compareItems.length >= MAX_COMPARE) {
      toast({
        tone: "info",
        title: `Compare up to ${MAX_COMPARE} opportunities`,
        description: "Remove one from the selection first.",
      });
      return;
    }
    setCompareItems((previous) => [...previous, item]);
  }

  function removeFromCompare(id: string) {
    const remaining = compareItems.filter((entry) => entry.id !== id);
    setCompareItems(remaining);
    if (remaining.length < 2) setCompareOpen(false);
  }

  function clearCompare() {
    setCompareItems([]);
    setCompareOpen(false);
  }

  function exportVisible() {
    if (visible.length === 0) return;
    exportCsv("tenderpilot-opportunities", visible, csvColumns);
    toast({
      tone: "success",
      title: "CSV exported",
      description: `${visible.length} opportunit${visible.length === 1 ? "y" : "ies"} with the current filters and sort order.`,
    });
  }

  return (
    <div className="mx-auto max-w-[1500px]">
      <PageHeader
        eyebrow="Pursuit"
        title="Opportunities"
        description="Source-neutral public procurement records normalized into TenderPilot's canonical tender model."
        actions={
          <>
            <SavedViewsMenu
              views={views}
              canSave={dirty}
              onApply={(value) => {
                setFilters({ ...DEFAULT_FILTERS, ...value });
                setDebouncedQuery(value.query ?? "");
                setPage(1);
              }}
              onSave={(name) => {
                save(name, filters);
                toast({ tone: "success", title: "View saved", description: name.trim() });
              }}
              onRemove={remove}
            />
            <Button onClick={exportVisible} disabled={visible.length === 0} leading={<Download className="size-4" />}>
              Export CSV
            </Button>
          </>
        }
      />

      <div className="mb-5 grid grid-cols-2 gap-3 lg:grid-cols-4">
        {[
          { label: "Results", value: loading && !result ? "…" : String(visible.length), hint: "match your filters" },
          { label: "Open for bids", value: loading && !result ? "…" : String(stats.open), hint: "accepting submissions" },
          { label: "Closing ≤ 7 days", value: loading && !result ? "…" : String(stats.closingSoon), hint: "act now", danger: stats.closingSoon > 0 },
          { label: "Avg. data quality", value: stats.average === null ? "—" : String(stats.average), hint: "out of 100" },
        ].map((stat) => (
          <Card key={stat.label} className="p-4">
            <p className="text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">{stat.label}</p>
            <p className={cn("mt-1 text-2xl font-semibold tabular-nums", stat.danger ? "text-danger" : "text-ink")}>
              {stat.value}
            </p>
            <p className="text-xs text-muted">{stat.hint}</p>
          </Card>
        ))}
      </div>

      <Card className="mb-5 p-4">
        <div className="flex flex-col gap-3 xl:flex-row xl:items-center">
          <SearchInput
            value={filters.query}
            onValueChange={(query) => update({ query })}
            placeholder="Search title, buyer or CPV…"
            aria-label="Search opportunities"
            className="xl:w-80"
          />
          <div className="grid flex-1 grid-cols-2 gap-3 md:grid-cols-[repeat(3,minmax(0,1fr))_minmax(0,1.5fr)]">
            <SelectField
              label="Status"
              value={filters.status}
              onChange={(event) => update({ status: event.target.value as FilterState["status"] })}
            >
              <option value="">Any status</option>
              {STATUS_OPTIONS.map((status) => (
                <option key={status} value={status}>
                  {formatEnum(status)}
                </option>
              ))}
            </SelectField>
            <SelectField
              label="Source"
              value={filters.source}
              onChange={(event) => update({ source: event.target.value as FilterState["source"] })}
            >
              <option value="">Any source</option>
              {SOURCE_OPTIONS.map((source) => (
                <option key={source} value={source}>
                  {source === "MANUAL_DEMO" ? "Demo data" : source}
                </option>
              ))}
            </SelectField>
            <SelectField
              label="Minimum data quality"
              value={filters.minQuality}
              onChange={(event) => update({ minQuality: event.target.value as FilterState["minQuality"] })}
            >
              <option value="">Any quality</option>
              <option value="50">Quality 50+</option>
              <option value="70">Quality 70+</option>
              <option value="85">Quality 85+</option>
            </SelectField>
            <SelectField
              label="Sort by"
              value={filters.sort}
              onChange={(event) => update({ sort: event.target.value as SortKey })}
            >
              {(Object.keys(SORT_LABELS) as SortKey[]).map((key) => (
                <option key={key} value={key}>
                  {SORT_LABELS[key]}
                </option>
              ))}
            </SelectField>
          </div>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-3">
          <label className="inline-flex cursor-pointer items-center gap-2 text-xs font-medium text-ink-2">
            <input
              type="checkbox"
              checked={filters.openOnly}
              onChange={(event) => update({ openOnly: event.target.checked })}
              className="size-4 rounded border-line-strong accent-[var(--brand)]"
            />
            Open for bids only
          </label>

          {chips.map((chip) => (
            <button
              key={chip.label}
              type="button"
              onClick={chip.clear}
              className="inline-flex items-center gap-1.5 rounded-full border border-brand-line bg-brand-soft px-2.5 py-1 text-xs font-semibold text-brand transition hover:bg-brand-line/50"
            >
              {chip.label}
              <X className="size-3" aria-label="Remove filter" />
            </button>
          ))}

          <div className="ml-auto flex items-center gap-3">
            {dirty ? (
              <button
                type="button"
                onClick={reset}
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-muted transition hover:text-ink"
              >
                <RotateCcw className="size-3.5" />
                Reset
              </button>
            ) : null}
            <SegmentedControl<ViewMode>
              label="Layout"
              size="sm"
              value={view}
              onChange={(next) => {
                setView(next);
                setPage(1);
              }}
              options={[
                { value: "cards", label: "Cards", icon: LayoutGrid },
                { value: "table", label: "Table", icon: Table2 },
              ]}
            />
          </div>
        </div>
      </Card>

      {error ? (
        <EmptyState
          icon={TriangleAlert}
          title="Opportunities could not be loaded"
          description={error}
          action={
            <Button variant="primary" onClick={() => setReloadKey((value) => value + 1)}>
              Try again
            </Button>
          }
        />
      ) : loading && !result ? (
        <ListSkeleton mode={view} />
      ) : visible.length === 0 ? (
        <EmptyState
          icon={dirty ? FileSearch : Database}
          title={dirty ? "No opportunities match these filters" : "No opportunities yet"}
          description={
            dirty
              ? "Try removing a filter or searching by buyer name or CPV code."
              : "Once TED, BOAMP or DECP ingestion has run, normalized opportunities appear here."
          }
          action={
            dirty ? (
              <Button variant="primary" onClick={reset} leading={<RotateCcw className="size-4" />}>
                Reset filters
              </Button>
            ) : (
              <Link
                href="/data-sources"
                className="inline-flex h-10 items-center rounded-xl bg-navy px-4 text-sm font-semibold text-white transition hover:bg-navy-hover"
              >
                Review data sources
              </Link>
            )
          }
        />
      ) : (
        <div className={cn("transition-opacity", loading && "opacity-60")} aria-busy={loading}>
          {view === "cards" ? (
            <div className="grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
              {pageItems.map((item) => (
                <OpportunityCard
                  key={item.id}
                  item={item}
                  now={now}
                  selectedIds={selectedIds}
                  onToggle={toggleCompare}
                />
              ))}
            </div>
          ) : (
            <OpportunityTable items={pageItems} now={now} selectedIds={selectedIds} onToggle={toggleCompare} />
          )}
          <Pagination page={currentPage} pageSize={pageSize} total={visible.length} onPageChange={setPage} />
        </div>
      )}

      {compareItems.length > 0 ? (
        <div
          data-print-hide
          role="region"
          aria-label="Comparison selection"
          className="fixed inset-x-4 bottom-4 z-40 mx-auto flex max-w-xl animate-pop items-center gap-3 rounded-2xl border border-line bg-surface px-4 py-3 shadow-pop"
        >
          <GitCompareArrows className="size-5 shrink-0 text-brand" aria-hidden="true" />
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold text-ink">
              {compareItems.length} of {MAX_COMPARE} selected
            </p>
            <p className="truncate text-xs text-muted">
              {compareItems.length < 2
                ? "Pick at least one more to compare"
                : compareItems.map((item) => item.title).join(" · ")}
            </p>
          </div>
          <Button variant="ghost" size="sm" onClick={clearCompare}>
            Clear
          </Button>
          <Button variant="primary" size="sm" disabled={compareItems.length < 2} onClick={() => setCompareOpen(true)}>
            Compare
          </Button>
        </div>
      ) : null}

      <CompareDialog
        open={compareOpen && compareItems.length >= 2}
        onClose={() => setCompareOpen(false)}
        items={compareItems}
        now={now}
        onRemove={removeFromCompare}
      />
    </div>
  );
}
