"use client";

import { ChevronRight } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import { breadcrumbsForPath } from "@/features/navigation/product-navigation";
import { cn } from "@/lib/cn";

export function Breadcrumbs() {
  const pathname = usePathname();
  const trail = breadcrumbsForPath(pathname);

  return (
    <nav aria-label="Breadcrumb" className="min-w-0">
      <ol className="flex min-w-0 items-center gap-1.5 text-sm">
        {trail.map((crumb, index) => {
          const last = index === trail.length - 1;
          return (
            <li
              key={`${crumb.label}-${index}`}
              className={cn("flex min-w-0 items-center gap-1.5", !last && "hidden sm:flex")}
            >
              {crumb.href && !last ? (
                <Link href={crumb.href} className="truncate text-muted transition hover:text-ink">
                  {crumb.label}
                </Link>
              ) : (
                <span
                  aria-current={last ? "page" : undefined}
                  className={cn("truncate", last ? "font-semibold text-ink" : "text-muted")}
                >
                  {crumb.label}
                </span>
              )}
              {last ? null : <ChevronRight className="size-3.5 shrink-0 text-faint" aria-hidden="true" />}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
