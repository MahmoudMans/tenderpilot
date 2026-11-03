import type { HTMLAttributes } from "react";

import { cn } from "@/lib/cn";

export function Skeleton({ className, ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      aria-hidden="true"
      className={cn("animate-shimmer rounded-xl bg-surface-3", className)}
      {...rest}
    />
  );
}

/** A generic page-level loading skeleton: header, stat row and a content block. */
export function PageSkeleton() {
  return (
    <div className="mx-auto max-w-7xl" role="status" aria-label="Loading">
      <Skeleton className="h-3 w-32" />
      <Skeleton className="mt-4 h-9 w-72 max-w-full" />
      <Skeleton className="mt-3 h-4 w-[28rem] max-w-full" />
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[0, 1, 2, 3].map((item) => (
          <Skeleton key={item} className="h-28 rounded-3xl" />
        ))}
      </div>
      <Skeleton className="mt-6 h-72 rounded-3xl" />
      <span className="sr-only">Loading content…</span>
    </div>
  );
}
