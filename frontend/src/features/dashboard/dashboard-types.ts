export type DashboardActionPriority =

  | "CRITICAL"
  | "HIGH"
  | "MEDIUM"
  | "LOW";


export type DashboardCommandCenter = {

  generatedAt:
    string;

  summary: {

    matchedOpportunities:
      number;

    highPotentialMatches:
      number;

    blockedMatches:
      number;

    goBids:
      number;

    holdBids:
      number;

    noGoBids:
      number;

    pendingDecisionBids:
      number;

    complianceReadyBids:
      number;

    submissionReadyBids:
      number;

    submittedBids:
      number;

    openTasks:
      number;

    overdueTasks:
      number;

    blockedTasks:
      number;

    criticalOpenTasks:
      number;

    deadlinesNext7Days:
      number;

    deadlinesNext30Days:
      number;

  };

  market: {

    available:
      boolean;

    noticeCount30Days:
      number;

    activeNoticeCount30Days:
      number;

    uniqueBuyerCount30Days:
      number;

    uniqueObservedSupplierCount30Days:
      number;

    buyerConcentration:
      string;

    supplierConcentration:
      string;

    leadingCpv:
      string
      |
      null;

    leadingCpvSharePercentage:
      number
      |
      null;

    awardCoveragePercentage30Days:
      number;

  };

  priorities: Array<{

    priority:
      DashboardActionPriority;

    type:
      string;

    title:
      string;

    detail:
      string;

    tenderId:
      string
      |
      null;

    href:
      string;

  }>;

  portfolio: Array<{

    tenderId:
      string;

    buyerId:
      string
      |
      null;

    title:
      string;

    buyerName:
      string;

    matchScore:
      number;

    matchGrade:
      string;

    matchBlocked:
      boolean;

    decisionOutcome:
      string
      |
      null;

    submissionDeadline:
      string
      |
      null;

    daysToDeadline:
      number
      |
      null;

    complianceReady:
      boolean;

    complianceReadinessScore:
      number;

    openTasks:
      number;

    completedTasks:
      number;

    overdueTasks:
      number;

    blockedTasks:
      number;

    criticalOpenTasks:
      number;

    readyForSubmission:
      boolean;

    submitted:
      boolean;

  }>;

};
