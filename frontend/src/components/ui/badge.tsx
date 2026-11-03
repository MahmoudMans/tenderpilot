import type { HTMLAttributes } from "react";

import { cn } from "@/lib/cn";

export type BadgeTone = "neutral" | "brand" | "success" | "warn" | "danger" | "info";

const tones: Record<BadgeTone, string> = {
  neutral: "border-line bg-surface-3 text-ink-2",
  brand: "border-brand-line bg-brand-soft text-brand",
  success: "border-success-line bg-success-soft text-success",
  warn: "border-warn-line bg-warn-soft text-warn",
  danger: "border-danger-line bg-danger-soft text-danger",
  info: "border-info-line bg-info-soft text-info",
};

type BadgeProps = HTMLAttributes<HTMLSpanElement> & {
  tone?: BadgeTone;
  /** Adds a small status dot. */
  dot?: boolean;
};

export function Badge({ tone = "neutral", dot = false, className, children, ...rest }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border px-2.5 py-0.5 text-[11px] font-semibold leading-5",
        tones[tone],
        className,
      )}
      {...rest}
    >
      {dot ? <span className="size-1.5 rounded-full bg-current" aria-hidden="true" /> : null}
      {children}
    </span>
  );
}
