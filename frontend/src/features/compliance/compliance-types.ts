import type {

  BidRequirementCategory,

  BidRequirementStatus,

} from "@/features/bid-workspace/bid-workspace-types";


export type ComplianceVerdict =

  | "UNASSESSED"
  | "COMPLIANT"
  | "PARTIAL"
  | "NON_COMPLIANT"
  | "NOT_APPLICABLE";


export type ComplianceRiskLevel =

  | "UNASSESSED"
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "CRITICAL";


export type EvidenceStrength =

  | "NONE"
  | "WEAK"
  | "MODERATE"
  | "STRONG";


export type CompliancePriority =

  | "BLOCKER"
  | "CRITICAL"
  | "HIGH"
  | "MEDIUM"
  | "LOW";


export type BidComplianceMatrix = {

  tenderId:
    string;

  decisionOutcome:
    "PENDING"
    |
    "GO"
    |
    "HOLD"
    |
    "NO_GO"
    |
    null;

  writable:
    boolean;

  writeLockReason:
    string
    |
    null;

  statistics: {

    totalRequirements:
      number;

    reviewedRequirements:
      number;

    mandatoryRequirements:
      number;

    compliantRequirements:
      number;

    partialRequirements:
      number;

    nonCompliantRequirements:
      number;

    unassessedRequirements:
      number;

    mandatoryBlockerCount:
      number;

    mandatoryIncompleteCount:
      number;

    criticalRiskCount:
      number;

    readinessScore:
      number;

    readyForSubmission:
      boolean;

  };

  rows: Array<{

    requirementId:
      string;

    title:
      string;

    description:
      string
      |
      null;

    category:
      BidRequirementCategory;

    requirementStatus:
      BidRequirementStatus;

    mandatory:
      boolean;

    sourceDocumentId:
      string
      |
      null;

    sourceDocumentName:
      string
      |
      null;

    sourceReference:
      string
      |
      null;

    dueDate:
      string
      |
      null;

    assignedTo:
      string
      |
      null;

    evidenceNotes:
      string
      |
      null;

    verdict:
      ComplianceVerdict;

    riskLevel:
      ComplianceRiskLevel;

    evidenceStrength:
      EvidenceStrength;

    readinessContribution:
      number;

    priority:
      CompliancePriority;

    nextAction:
      string;

    reviewRationale:
      string
      |
      null;

    mitigationAction:
      string
      |
      null;

    reviewedBy:
      string
      |
      null;

    reviewedAt:
      string
      |
      null;

  }>;

};
