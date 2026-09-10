export type LocalAiStatus = {

  enabled:
    boolean;

  reachable:
    boolean;

  modelAvailable:
    boolean;

  provider:
    string;

  model:
    string;

  baseUrl:
    string;

  mode:
    "LOCAL_ONLY";

  dataBoundary:
    "ORGANIZATION_PRIVATE";

};


export type BidAiFindingType =

  | "REQUIREMENT"
  | "RISK"
  | "DEADLINE"
  | "DELIVERABLE"
  | "QUALIFICATION"
  | "COMMERCIAL"
  | "OTHER";


export type BidDocumentAnalysisStatus =

  | "NOT_ANALYZED"
  | "COMPLETED"
  | "FAILED";


export type BidDocumentAiAnalysis = {

  documentId:
    string;

  filename:
    string;

  documentCategory:
    string;

  status:
    BidDocumentAnalysisStatus;

  provider:
    string;

  model:
    string;

  promptVersion:
    string;

  confidence:
    number
    |
    null;

  summary:
    string
    |
    null;

  errorMessage:
    string
    |
    null;

  extractedCharacterCount:
    number
    |
    null;

  extractionTruncated:
    boolean
    |
    null;

  detectedMediaType:
    string
    |
    null;

  analyzedAt:
    string
    |
    null;

  findings: Array<{

    type:
      BidAiFindingType;

    title:
      string;

    detail:
      string;

    evidenceSnippet:
      string
      |
      null;

    mandatory:
      boolean
      |
      null;

    requirementCategory:
      string
      |
      null;

  }>;

};
