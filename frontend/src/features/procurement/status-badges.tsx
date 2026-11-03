import { Badge, type BadgeTone } from "@/components/ui/badge";

import { formatEnum } from "./procurement-format";
import type { DataQualityGrade, ProcurementSource, ProcurementStatus } from "./procurement-types";

const statusTones: Record<ProcurementStatus, BadgeTone> = {
  DRAFT: "neutral",
  PUBLISHED: "success",
  UPDATED: "info",
  CLOSED: "neutral",
  AWARDED: "brand",
  CANCELLED: "danger",
  ARCHIVED: "neutral",
};

export function StatusBadge({ status }: { status: ProcurementStatus }) {
  return (
    <Badge tone={statusTones[status]} dot>
      {formatEnum(status)}
    </Badge>
  );
}

const gradeTones: Record<DataQualityGrade, BadgeTone> = {
  EXCELLENT: "success",
  GOOD: "success",
  FAIR: "warn",
  POOR: "danger",
  CRITICAL: "danger",
};

export function QualityBadge({
  grade,
  score,
  compact = false,
}: {
  grade: DataQualityGrade | null;
  score: number | null;
  compact?: boolean;
}) {
  if (grade === null) return null;
  return (
    <Badge tone={gradeTones[grade]} title="Data quality of the source record">
      {compact ? "" : "Quality "}
      {score ?? "—"} · {formatEnum(grade)}
    </Badge>
  );
}

const sourceTones: Record<ProcurementSource, BadgeTone> = {
  TED: "info",
  BOAMP: "info",
  DECP: "info",
  MANUAL_DEMO: "neutral",
};

export function SourceBadge({ source }: { source: ProcurementSource }) {
  return <Badge tone={sourceTones[source]}>{source === "MANUAL_DEMO" ? "Demo" : source}</Badge>;
}
