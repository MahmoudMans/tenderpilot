"use client";

import { Download } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { exportCsv, type CsvColumn } from "@/lib/csv";

import type { BidDecisionQueueItem } from "./bid-decision-types";

const columns: CsvColumn<BidDecisionQueueItem>[] = [
  { header: "Title", value: (item) => item.opportunity.title },
  { header: "Buyer", value: (item) => item.opportunity.buyerName },
  { header: "Decision", value: (item) => item.decision?.outcome ?? "PENDING" },
  { header: "Recommended", value: (item) => item.decision?.recommendedOutcome },
  { header: "Decision score", value: (item) => item.decision?.decisionScore },
  { header: "Match score", value: (item) => item.matchScore },
  { header: "Match grade", value: (item) => item.matchGrade },
  { header: "Blocked", value: (item) => item.blocked },
  { header: "Submission deadline", value: (item) => item.opportunity.submissionDeadline },
  { header: "Estimated max value", value: (item) => item.opportunity.estimatedMaxValue },
  { header: "Currency", value: (item) => item.opportunity.currency },
  { header: "Last updated", value: (item) => item.decision?.updatedAt },
  { header: "Tender id", value: (item) => item.tenderId },
];

export function ExportDecisionsButton({ items }: { items: readonly BidDecisionQueueItem[] }) {
  const { toast } = useToast();
  return (
    <Button
      disabled={items.length === 0}
      onClick={() => {
        exportCsv("tenderpilot-bid-decisions", items, columns);
        toast({ tone: "success", title: "CSV exported", description: `${items.length} decision candidates.` });
      }}
      leading={<Download className="size-4" />}
    >
      Export decisions (CSV)
    </Button>
  );
}
