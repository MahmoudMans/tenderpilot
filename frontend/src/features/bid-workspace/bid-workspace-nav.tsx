"use client";

import Link from "next/link";
import { useParams, usePathname } from "next/navigation";
import { cn } from "@/lib/cn";

type Section = {
  label: string;
  /** Appended to /bid-workspaces/{tenderId}. Empty means the workspace overview. */
  path: string;
};

const SECTIONS: Section[] = [
  { label: "Overview", path: "" },
  { label: "Planning", path: "/planning" },
  { label: "Compliance", path: "/compliance" },
  { label: "Q&A", path: "/qa" },
  { label: "AI analysis", path: "/ai" },
];

/**
 * One row of links shared by every page of a bid workspace, so you can move between
 * overview, planning, compliance, Q&A and AI analysis, or jump to the decision and the
 * printable brief, without going back to the hub first.
 */
export function BidWorkspaceNav() {
  const params = useParams<{ tenderId: string }>();
  const pathname = usePathname();
  const tenderId = params.tenderId;

  // The brief is a printable document with its own header and back link.
  if (typeof tenderId !== "string" || pathname.endsWith("/brief")) return null;

  const base = `/bid-workspaces/${tenderId}`;

  return (
    <nav aria-label="Bid workspace sections" className="mx-auto mb-6 max-w-[1450px] print:hidden">
      <div className="flex items-center gap-1 overflow-x-auto rounded-2xl border border-line bg-surface p-1 shadow-sm">
        {SECTIONS.map((section) => {
          const href = `${base}${section.path}`;
          const active = pathname === href;
          return (
            <Link
              key={section.label}
              href={href}
              aria-current={active ? "page" : undefined}
              className={cn(
                "shrink-0 rounded-xl px-3.5 py-2 text-sm font-semibold transition",
                active ? "bg-brand-soft text-brand" : "text-ink-2 hover:bg-surface-3 hover:text-ink",
              )}
            >
              {section.label}
            </Link>
          );
        })}
        <span aria-hidden="true" className="mx-1 h-5 w-px shrink-0 bg-line" />
        <Link
          href={`/bid-decisions/${tenderId}`}
          className="shrink-0 rounded-xl px-3.5 py-2 text-sm font-semibold text-ink-2 transition hover:bg-surface-3 hover:text-ink"
        >
          Go/No-Go decision
        </Link>
        <Link
          href={`/bid-workspaces/${tenderId}/brief`}
          className="shrink-0 rounded-xl px-3.5 py-2 text-sm font-semibold text-ink-2 transition hover:bg-surface-3 hover:text-ink"
        >
          Bid brief
        </Link>
      </div>
    </nav>
  );
}
