"use client";

import { TriangleAlert } from "lucide-react";

import { Button, ButtonLink } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";

export default function WorkspaceError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <div className="mx-auto max-w-2xl pt-10">
      <EmptyState
        icon={TriangleAlert}
        title="This screen hit a problem"
        description={`Something unexpected happened while showing this page. Your data is safe — try again, or go back to the dashboard.${
          error.digest ? ` (Reference ${error.digest})` : ""
        }`}
        action={
          <div className="flex flex-wrap justify-center gap-3">
            <Button variant="primary" onClick={reset}>
              Try again
            </Button>
            <ButtonLink href="/dashboard">Go to dashboard</ButtonLink>
          </div>
        }
      />
    </div>
  );
}
