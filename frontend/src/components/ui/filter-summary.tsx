type FilterSummaryProps = {
  shown: number;
  total: number;
  /** Singular noun, for example "task". An "s" is added for any count other than one. */
  noun: string;
  /** True while any filter is on. */
  active: boolean;
  onClear: () => void;
};

/** "Showing 3 of 9 tasks" with a way to clear the filters. Announced politely to screen readers. */
export function FilterSummary({ shown, total, noun, active, onClear }: FilterSummaryProps) {
  const label = (count: number) => `${count} ${noun}${count === 1 ? "" : "s"}`;
  return (
    <p className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted" aria-live="polite">
      <span>{active ? `Showing ${shown} of ${label(total)}` : label(total)}</span>
      {active ? (
        <button type="button" onClick={onClear} className="font-semibold text-brand hover:underline">
          Clear filters
        </button>
      ) : null}
    </p>
  );
}
