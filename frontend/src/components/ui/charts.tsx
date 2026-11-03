import { cn } from "@/lib/cn";

type SparklineProps = {
  values: readonly number[];
  width?: number;
  height?: number;
  className?: string;
  label?: string;
};

/** Minimal area sparkline. Colour comes from `currentColor`. */
export function Sparkline({ values, width = 120, height = 36, className, label }: SparklineProps) {
  if (values.length < 2) return null;
  const max = Math.max(...values);
  const min = Math.min(...values);
  const span = max - min || 1;
  const step = width / (values.length - 1);
  const points = values.map((value, index) => {
    const x = index * step;
    const y = height - 3 - ((value - min) / span) * (height - 6);
    return [x, y] as const;
  });
  const line = points.map(([x, y], index) => `${index === 0 ? "M" : "L"}${x.toFixed(1)},${y.toFixed(1)}`).join(" ");
  const area = `${line} L${width},${height} L0,${height} Z`;

  return (
    <svg
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      role="img"
      aria-label={label ?? "Trend"}
      className={cn("text-brand", className)}
    >
      <path d={area} fill="currentColor" opacity="0.12" />
      <path d={line} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export type BarDatum = {
  label: string;
  value: number;
  /** Optional right-aligned text (defaults to the value). */
  display?: string;
  href?: string;
};

type BarListProps = {
  data: readonly BarDatum[];
  className?: string;
  barClassName?: string;
};

/** Horizontal labelled bars scaled to the largest value. */
export function BarList({ data, className, barClassName }: BarListProps) {
  const max = Math.max(1, ...data.map((item) => item.value));
  return (
    <ul className={cn("space-y-3", className)}>
      {data.map((item) => (
        <li key={item.label}>
          <div className="flex items-baseline justify-between gap-3 text-xs">
            <span className="truncate font-medium text-ink-2">{item.label}</span>
            <span className="shrink-0 font-semibold tabular-nums text-ink">{item.display ?? item.value}</span>
          </div>
          <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-surface-3">
            <div
              className={cn("h-full rounded-full bg-brand", barClassName)}
              style={{ width: `${Math.max(2, (item.value / max) * 100)}%` }}
            />
          </div>
        </li>
      ))}
    </ul>
  );
}

export type FunnelStage = {
  label: string;
  value: number;
  hint?: string;
};

/** Vertical funnel: each stage's bar is scaled to the first stage. */
export function Funnel({ stages }: { stages: readonly FunnelStage[] }) {
  const top = Math.max(1, stages[0]?.value ?? 1);
  return (
    <ol className="space-y-2.5">
      {stages.map((stage, index) => {
        const width = Math.max(4, (stage.value / top) * 100);
        const previous = index === 0 ? null : stages[index - 1].value;
        const conversion = previous && previous > 0 ? Math.round((stage.value / previous) * 100) : null;
        return (
          <li key={stage.label}>
            <div className="flex items-baseline justify-between gap-3 text-xs">
              <span className="font-medium text-ink-2">{stage.label}</span>
              <span className="tabular-nums text-ink">
                <span className="font-semibold">{stage.value}</span>
                {conversion !== null ? <span className="ml-2 text-faint">{conversion}%</span> : null}
              </span>
            </div>
            <div className="mt-1 h-7 overflow-hidden rounded-lg bg-surface-3">
              <div
                className="flex h-full items-center rounded-lg bg-gradient-to-r from-brand to-brand/60 px-2.5 text-[11px] font-medium text-white transition-[width] duration-700"
                style={{ width: `${width}%` }}
              >
                {stage.hint ?? ""}
              </div>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
