import { cn } from "@/lib/cn";

type Tone = "brand" | "success" | "warn" | "danger" | "auto";

function toneFor(value: number): Exclude<Tone, "auto"> {
  if (value >= 80) return "success";
  if (value >= 50) return "brand";
  if (value >= 30) return "warn";
  return "danger";
}

const barColors = {
  brand: "bg-brand",
  success: "bg-success",
  warn: "bg-warn",
  danger: "bg-danger",
};

const strokeColors = {
  brand: "stroke-brand",
  success: "stroke-success",
  warn: "stroke-warn",
  danger: "stroke-danger",
};

type ProgressBarProps = {
  /** 0–100 */
  value: number;
  tone?: Tone;
  label?: string;
  className?: string;
};

export function ProgressBar({ value, tone = "auto", label, className }: ProgressBarProps) {
  const clamped = Math.max(0, Math.min(100, Math.round(value)));
  const resolved = tone === "auto" ? toneFor(clamped) : tone;
  return (
    <div
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={clamped}
      aria-label={label}
      className={cn("h-2 w-full overflow-hidden rounded-full bg-surface-3", className)}
    >
      <div
        className={cn("h-full rounded-full transition-[width] duration-500", barColors[resolved])}
        style={{ width: `${clamped}%` }}
      />
    </div>
  );
}

type RingGaugeProps = {
  /** 0–100 */
  value: number;
  size?: number;
  strokeWidth?: number;
  tone?: Tone;
  label?: string;
  /** Text in the centre; defaults to the rounded value. */
  children?: React.ReactNode;
};

export function RingGauge({
  value,
  size = 64,
  strokeWidth = 7,
  tone = "auto",
  label,
  children,
}: RingGaugeProps) {
  const clamped = Math.max(0, Math.min(100, Math.round(value)));
  const resolved = tone === "auto" ? toneFor(clamped) : tone;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference * (1 - clamped / 100);

  return (
    <div
      role="img"
      aria-label={label ?? `${clamped} percent`}
      className="relative grid shrink-0 place-items-center"
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={strokeWidth}
          className="stroke-surface-3"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          className={cn("transition-[stroke-dashoffset] duration-700", strokeColors[resolved])}
        />
      </svg>
      <span className="absolute text-[13px] font-semibold tabular-nums text-ink">
        {children ?? clamped}
      </span>
    </div>
  );
}
