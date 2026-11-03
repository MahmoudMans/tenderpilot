"use client";

import { Download } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { exportCsv, type CsvColumn } from "@/lib/csv";

import type { TenderMatch } from "./matching-types";

const columns: CsvColumn<TenderMatch>[] = [
  { header: "Title", value: (match) => match.opportunity.title },
  { header: "Buyer", value: (match) => match.opportunity.buyerName },
  { header: "Match score", value: (match) => match.score },
  { header: "Grade", value: (match) => match.grade },
  { header: "Blocked", value: (match) => match.blocked },
  { header: "CPV score", value: (match) => match.cpvScore },
  { header: "Geography score", value: (match) => match.geographyScore },
  { header: "Value score", value: (match) => match.valueScore },
  { header: "Buyer score", value: (match) => match.buyerScore },
  { header: "Capability score", value: (match) => match.capabilityScore },
  { header: "Timing score", value: (match) => match.timingScore },
  { header: "Quality score", value: (match) => match.qualityScore },
  { header: "Status", value: (match) => match.opportunity.status },
  { header: "Submission deadline", value: (match) => match.opportunity.submissionDeadline },
  { header: "Estimated max value", value: (match) => match.opportunity.estimatedMaxValue },
  { header: "Currency", value: (match) => match.opportunity.currency },
  { header: "Region", value: (match) => match.opportunity.primaryRegionName },
  { header: "Opportunity id", value: (match) => match.opportunity.id },
];

export function ExportMatchesButton({ matches }: { matches: readonly TenderMatch[] }) {
  const { toast } = useToast();
  return (
    <Button
      disabled={matches.length === 0}
      onClick={() => {
        exportCsv("tenderpilot-matches", matches, columns);
        toast({ tone: "success", title: "CSV exported", description: `${matches.length} matches with score breakdown.` });
      }}
      leading={<Download className="size-4" />}
    >
      Export matches (CSV)
    </Button>
  );
}
