import type {

  OpportunitySummary,

} from "@/features/procurement/procurement-types";


export type BidDecisionOutcome =

  | "PENDING"
  | "GO"
  | "HOLD"
  | "NO_GO";


export type TenderMatchGrade =

  | "BLOCKED"
  | "EXCELLENT"
  | "STRONG"
  | "GOOD"
  | "WEAK"
  | "POOR";


export type BidDecisionQueueItem = {

  matchId:
    string;

  tenderId:
    string;

  matchScore:
    number;

  matchGrade:
    TenderMatchGrade;

  blocked:
    boolean;

  opportunity:
    OpportunitySummary;

  decision:
    {
      id:
        string;

      outcome:
        BidDecisionOutcome;

      recommendedOutcome:
        BidDecisionOutcome;

      decisionScore:
        number;

      updatedAt:
        string;
    }
    |
    null;

};


export type BidDecisionOverview = {

  candidateCount:
    number;

  totalDecisions:
    number;

  pendingCount:
    number;

  goCount:
    number;

  holdCount:
    number;

  noGoCount:
    number;

  averageDecisionScore:
    number;

  lastUpdatedAt:
    string
    |
    null;

};


export type BidDecisionWorkspace = {

  tenderId:
    string;

  matchScore:
    number;

  matchGrade:
    TenderMatchGrade;

  matchBlocked:
    boolean;

  opportunity:
    OpportunitySummary;

  decision:
    {
      id:
        string;

      outcome:
        BidDecisionOutcome;

      recommendedOutcome:
        BidDecisionOutcome;

      decisionScore:
        number;

      matchScoreSnapshot:
        number;

      matchGradeSnapshot:
        TenderMatchGrade;

      strategicFit:
        number;

      capabilityConfidence:
        number;

      commercialAttractiveness:
        number;

      deliveryFeasibility:
        number;

      resourceCapacity:
        number;

      riskConfidence:
        number;

      recommendationReason:
        string;

      rationale:
        string
        |
        null;

      keyRisks:
        string
        |
        null;

      assumptions:
        string
        |
        null;

      decidedBy:
        string
        |
        null;

      decidedAt:
        string
        |
        null;

      createdAt:
        string;

      updatedAt:
        string;
    }
    |
    null;

  history: Array<{

    id:
      string;

    eventType:
      "ASSESSMENT_UPDATED"
      |
      "OUTCOME_CHANGED"
      |
      "REOPENED";

    previousOutcome:
      BidDecisionOutcome
      |
      null;

    newOutcome:
      BidDecisionOutcome;

    decisionScore:
      number;

    actor:
      string;

    note:
      string
      |
      null;

    createdAt:
      string;

  }>;

};
