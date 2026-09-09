export type DataQualityGrade =
  | "EXCELLENT"
  | "GOOD"
  | "FAIR"
  | "POOR"
  | "CRITICAL";


export type DataQualitySeverity =
  | "CRITICAL"
  | "WARNING"
  | "INFO";


export type DataQualityOverview = {

  totalTenders:
    number;

  assessedTenders:
    number;

  unassessedTenders:
    number;

  averageScore:
    number;

  excellentCount:
    number;

  goodCount:
    number;

  fairCount:
    number;

  poorCount:
    number;

  criticalCount:
    number;

  criticalIssueCount:
    number;

  warningIssueCount:
    number;

  infoIssueCount:
    number;

  lastAssessedAt:
    string
    |
    null;

};


export type DataQualityRecalculationResult = {

  recalculatedCount:
    number;

};
