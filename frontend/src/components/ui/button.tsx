import Link from "next/link";
import { LoaderCircle } from "lucide-react";
import type { AnchorHTMLAttributes, ButtonHTMLAttributes, ReactNode } from "react";

import { cn } from "@/lib/cn";

export type ButtonVariant = "primary" | "secondary" | "ghost" | "soft" | "danger";
export type ButtonSize = "sm" | "md" | "lg";

const variants: Record<ButtonVariant, string> = {
  primary: "bg-navy text-white shadow-sm hover:bg-navy-hover",
  secondary: "border border-line bg-surface text-ink shadow-sm hover:border-line-strong hover:bg-surface-3",
  ghost: "text-ink-2 hover:bg-surface-3 hover:text-ink",
  soft: "bg-brand-soft text-brand hover:bg-brand-line/50",
  danger: "border border-danger-line bg-danger-soft text-danger hover:bg-danger-line/50",
};

const sizes: Record<ButtonSize, string> = {
  sm: "h-8 gap-1.5 rounded-lg px-3 text-xs",
  md: "h-10 gap-2 rounded-xl px-4 text-sm",
  lg: "h-12 gap-2 rounded-xl px-5 text-sm",
};

export function buttonClasses(
  variant: ButtonVariant = "secondary",
  size: ButtonSize = "md",
  className?: string,
): string {
  return cn(
    "inline-flex shrink-0 items-center justify-center font-semibold transition",
    "disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50",
    variants[variant],
    sizes[size],
    className,
  );
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  leading?: ReactNode;
  trailing?: ReactNode;
};

export function Button({
  variant = "secondary",
  size = "md",
  loading = false,
  leading,
  trailing,
  className,
  children,
  disabled,
  type = "button",
  ...rest
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={buttonClasses(variant, size, className)}
      {...rest}
    >
      {loading ? <LoaderCircle className="size-4 animate-spin" /> : leading}
      {children}
      {trailing}
    </button>
  );
}

type ButtonLinkProps = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, "href"> & {
  href: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
  leading?: ReactNode;
  trailing?: ReactNode;
};

export function ButtonLink({
  href,
  variant = "secondary",
  size = "md",
  leading,
  trailing,
  className,
  children,
  ...rest
}: ButtonLinkProps) {
  return (
    <Link href={href} className={buttonClasses(variant, size, className)} {...rest}>
      {leading}
      {children}
      {trailing}
    </Link>
  );
}
