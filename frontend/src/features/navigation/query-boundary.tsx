"use client";

import { Fragment, Suspense, type ReactNode } from "react";
import { useSearchParams } from "next/navigation";

/**
 * Starts a page fresh whenever the address query changes, for example when a link
 * goes from "/search?q=a" to "/search?q=b". List pages read their starting filters
 * from the query, so they must be rebuilt when it changes.
 */
function QueryKeyed({ children }: { children: ReactNode }) {
  const key = useSearchParams().toString();
  return <Fragment key={key}>{children}</Fragment>;
}

export function QueryBoundary({ children }: { children: ReactNode }) {
  return (
    <Suspense fallback={null}>
      <QueryKeyed>{children}</QueryKeyed>
    </Suspense>
  );
}
