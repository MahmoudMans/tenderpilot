import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import type { ReactNode } from "react";

type PageHeaderProps = {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
  back?: { href: string; label: string };
};

export function PageHeader({ eyebrow, title, description, actions, back }: PageHeaderProps) {
  return (
    <header className="mb-7">
      {back ? (
        <Link
          href={back.href}
          className="mb-4 inline-flex items-center gap-2 text-sm font-medium text-muted transition hover:text-ink"
        >
          <ArrowLeft className="size-4" />
          {back.label}
        </Link>
      ) : null}
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div className="min-w-0">
          {eyebrow ? (
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-brand">{eyebrow}</p>
          ) : null}
          <h1 className="mt-2 text-3xl font-semibold tracking-[-0.03em] text-ink">{title}</h1>
          {description ? (
            <p className="mt-2 max-w-3xl text-sm leading-6 text-muted">{description}</p>
          ) : null}
        </div>
        {actions ? (
          <div data-print-hide className="flex shrink-0 flex-wrap items-center gap-2">
            {actions}
          </div>
        ) : null}
      </div>
    </header>
  );
}
