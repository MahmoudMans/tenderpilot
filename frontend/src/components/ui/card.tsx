import type { HTMLAttributes, ReactNode } from "react";

import { cn } from "@/lib/cn";

type CardProps = HTMLAttributes<HTMLDivElement> & {
  /** Removes the default padding (for tables and lists that bleed to the edges). */
  flush?: boolean;
};

export function Card({ flush = false, className, children, ...rest }: CardProps) {
  return (
    <div
      className={cn(
        "rounded-3xl border border-line bg-surface shadow-card",
        flush ? "overflow-hidden" : "p-6",
        className,
      )}
      {...rest}
    >
      {children}
    </div>
  );
}

type CardHeaderProps = {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
  className?: string;
};

export function CardHeader({ eyebrow, title, description, actions, className }: CardHeaderProps) {
  return (
    <div className={cn("flex flex-wrap items-start justify-between gap-3", className)}>
      <div className="min-w-0">
        {eyebrow ? (
          <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-brand">{eyebrow}</p>
        ) : null}
        <h2 className="mt-1 text-base font-semibold tracking-tight text-ink">{title}</h2>
        {description ? <p className="mt-1 text-xs leading-5 text-muted">{description}</p> : null}
      </div>
      {actions ? <div className="flex shrink-0 items-center gap-2">{actions}</div> : null}
    </div>
  );
}
