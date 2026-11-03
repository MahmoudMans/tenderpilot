"use client";

import { Eye, EyeOff, LockKeyhole } from "lucide-react";
import { useId, useState, type InputHTMLAttributes, type ReactNode } from "react";

import { cn } from "@/lib/cn";

const authInput =
  "h-12 w-full rounded-xl border border-line bg-surface pl-10 pr-4 text-sm text-ink outline-none transition placeholder:text-faint hover:border-line-strong focus:border-brand focus:ring-4 focus:ring-brand/15";

type AuthFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  icon: ReactNode;
  hint?: ReactNode;
  trailing?: ReactNode;
};

export function AuthField({ label, icon, hint, trailing, className, id, ...rest }: AuthFieldProps) {
  const generated = useId();
  const inputId = id ?? generated;
  return (
    <div>
      <label htmlFor={inputId} className="text-sm font-medium text-ink-2">
        {label}
      </label>
      <div className="relative mt-2">
        <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-faint [&>svg]:size-4">
          {icon}
        </span>
        <input id={inputId} className={cn(authInput, trailing ? "pr-12" : "", className)} {...rest} />
        {trailing}
      </div>
      {hint ? <div className="mt-2 text-xs leading-5 text-muted">{hint}</div> : null}
    </div>
  );
}

type PasswordFieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, "type"> & {
  label: string;
  hint?: ReactNode;
};

/** Password input with a show/hide toggle. */
export function PasswordField({ label, hint, ...rest }: PasswordFieldProps) {
  const [visible, setVisible] = useState(false);
  return (
    <AuthField
      label={label}
      icon={<LockKeyhole />}
      hint={hint}
      type={visible ? "text" : "password"}
      trailing={
        <button
          type="button"
          onClick={() => setVisible((value) => !value)}
          aria-label={visible ? "Hide password" : "Show password"}
          aria-pressed={visible}
          className="absolute right-2 top-1/2 grid size-8 -translate-y-1/2 place-items-center rounded-lg text-faint transition hover:bg-surface-3 hover:text-ink"
        >
          {visible ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
        </button>
      }
      {...rest}
    />
  );
}

type Strength = { score: 0 | 1 | 2 | 3 | 4; label: string };

export function passwordStrength(password: string): Strength {
  if (password.length === 0) return { score: 0, label: "" };
  let score = 0;
  if (password.length >= 10) score += 1;
  if (password.length >= 14) score += 1;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score += 1;
  if (/\d/.test(password) && /[^A-Za-z0-9]/.test(password)) score += 1;
  const clamped = Math.min(4, score) as Strength["score"];
  const labels = ["Too short", "Weak", "Fair", "Good", "Strong"] as const;
  return { score: password.length < 10 ? 0 : clamped, label: password.length < 10 ? labels[0] : labels[clamped] };
}

const segmentColors = ["bg-danger", "bg-danger", "bg-warn", "bg-success", "bg-success"];

export function PasswordStrengthMeter({ password }: { password: string }) {
  const { score, label } = passwordStrength(password);
  if (password.length === 0) return null;
  return (
    <div className="mt-3" role="status" aria-live="polite">
      <div className="flex gap-1.5" aria-hidden="true">
        {[1, 2, 3, 4].map((segment) => (
          <span
            key={segment}
            className={cn(
              "h-1.5 flex-1 rounded-full transition-colors",
              segment <= score ? segmentColors[score] : "bg-surface-3",
            )}
          />
        ))}
      </div>
      <p className="mt-1.5 text-xs text-muted">
        Password strength: <span className="font-semibold text-ink-2">{label}</span>
      </p>
    </div>
  );
}
