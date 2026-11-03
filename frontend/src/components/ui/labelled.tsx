import type { ReactNode } from "react";

type LabelledProps = {
  label: string;
  /** A short line under the field, for a rule the person cannot see from the field itself. */
  hint?: string;
  children: ReactNode;
};

/** A form field with its label above it and an optional hint below it. Used by the edit dialogs. */
export function Labelled({ label, hint, children }: LabelledProps) {
  return (
    <label className="block">
      <span className="text-xs font-semibold text-ink-2">{label}</span>
      <span className="mt-1 block">{children}</span>
      {hint ? <span className="mt-1 block text-[11px] text-muted">{hint}</span> : null}
    </label>
  );
}
