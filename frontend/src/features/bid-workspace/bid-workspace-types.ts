import type {

  OpportunitySummary,

} from "@/features/procurement/procurement-types";


export type BidDocumentCategory =

  | "TENDER_NOTICE"
  | "SPECIFICATIONS"
  | "ADMINISTRATIVE"
  | "TECHNICAL"
  | "PRICING"
  | "FORMS"
  | "ANNEX"
  | "INTERNAL_DRAFT"
  | "EVIDENCE"
  | "OTHER";


export type BidRequirementCategory =

  | "ADMINISTRATIVE"
  | "TECHNICAL"
  | "COMMERCIAL"
  | "FINANCIAL"
  | "LEGAL"
  | "SUBMISSION"
  | "OTHER";


export type BidRequirementStatus =

  | "NOT_REVIEWED"
  | "IN_PROGRESS"
  | "SATISFIED"
  | "GAP"
  | "NOT_APPLICABLE";


export type BidDecisionOutcome =

  | "PENDING"
  | "GO"
  | "HOLD"
  | "NO_GO";


export type BidWorkspace = {

  tenderId:
    string;

  matchScore:
    number;

  matchGrade:
    string;

  decisionOutcome:
    BidDecisionOutcome
    |
    null;

  writable:
    boolean;

  writeLockReason:
    string
    |
    null;

  opportunity:
    OpportunitySummary;

  statistics: {

    documentCount:
      number;

    requirementCount:
      number;

    completedRequirementCount:
      number;

    gapRequirementCount:
      number;

    mandatoryGapCount:
      number;

    completionPercentage:
      number;

  };

  documents: Array<{

    id:
      string;

    category:
      BidDocumentCategory;

    originalFilename:
      string;

    contentType:
      string
      |
      null;

    sizeBytes:
      number;

    sha256:
      string;

    note:
      string
      |
      null;

    uploadedBy:
      string;

    uploadedAt:
      string;

  }>;

  requirements: Array<{

    id:
      string;

    sourceDocumentId:
      string
      |
      null;

    sourceDocumentName:
      string
      |
      null;

    title:
      string;

    description:
      string
      |
      null;

    category:
      BidRequirementCategory;

    status:
      BidRequirementStatus;

    mandatory:
      boolean;

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

    createdBy:
      string;

    updatedBy:
      string;

    createdAt:
      string;

    updatedAt:
      string;

  }>;

};


export type BidDocumentContent = {

  id:
    string;

  originalFilename:
    string;

  contentType:
    string
    |
    null;

  contentBase64:
    string;

};
