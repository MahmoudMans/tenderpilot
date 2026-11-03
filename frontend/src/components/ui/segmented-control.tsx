"use client";

import type { LucideIcon } from "lucide-react";

import { cn } from "@/lib/cn";

export type SegmentedOption<T extends string> = {
  value: T;
  label: string;
  icon?: LucideIcon;
};

type SegmentedControlProps<T extends string> = {
  value: T;
  onChange: (value: T) => void;
  options: readonly SegmentedOption<T>[];
  label: string;
  size?: "sm" | "md";
};

export function SegmentedControl<T extends string>({
  value,
  onChange,
  options,
  label,
  size = "md",
}: SegmentedControlProps<T>) {
  return (
    <div
      role="radiogroup"
      aria-label={label}
      className="inline-flex rounded-xl border border-line bg-surface-2 p-0.5"
    >
      {options.map((option) => {
        const Icon = option.icon;
        const active = option.value === value;
        return (
          <button
            key={option.value}
            type="button"
            role="radio"
            aria-checked={active}
            onClick={() => onChange(option.value)}
            className={cn(
              "inline-flex items-center gap-1.5 rounded-[10px] font-semibold transition",
              size === "sm" ? "h-7 px-2.5 text-xs" : "h-8 px-3 text-xs",
              active
                ? "bg-surface text-ink shadow-sm ring-1 ring-line"
                : "text-muted hover:text-ink",
            )}
          >
            {Icon ? <Icon className="size-3.5" /> : null}
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
