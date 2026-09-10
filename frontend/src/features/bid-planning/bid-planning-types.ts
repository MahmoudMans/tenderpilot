export type BidTaskStatus =

  | "TODO"
  | "IN_PROGRESS"
  | "BLOCKED"
  | "DONE"
  | "CANCELLED";


export type BidTaskPriority =

  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "CRITICAL";


export type BidTaskMilestone =

  | "KICKOFF"
  | "DOCUMENT_REVIEW"
  | "COMPLIANCE"
  | "TECHNICAL_RESPONSE"
  | "COMMERCIAL_RESPONSE"
  | "INTERNAL_REVIEW"
  | "APPROVAL"
  | "SUBMISSION"
  | "OTHER";


export type BidPlanningWorkspace = {

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

  complianceReady:
    boolean;

  complianceReadinessScore:
    number;

  writable:
    boolean;

  writeLockReason:
    string
    |
    null;

  statistics: {

    readinessScore:
      number;

    totalTasks:
      number;

    openTasks:
      number;

    completedTasks:
      number;

    blockedTasks:
      number;

    overdueTasks:
      number;

    criticalOpenTasks:
      number;

    taskCompletionPercentage:
      number;

    checklistCompletionPercentage:
      number;

    readyForSubmission:
      boolean;

  };

  submissionControl: {

    portalReady:
      boolean;

    signaturesReady:
      boolean;

    technicalResponseReady:
      boolean;

    commercialResponseReady:
      boolean;

    administrativePackReady:
      boolean;

    finalReviewReady:
      boolean;

    submissionAuthorized:
      boolean;

    plannedSubmissionAt:
      string
      |
      null;

    submittedAt:
      string
      |
      null;

    submissionReference:
      string
      |
      null;

    submittedBy:
      string
      |
      null;

    notes:
      string
      |
      null;

    updatedBy:
      string
      |
      null;

  };

  tasks: Array<{

    id:
      string;

    requirementId:
      string
      |
      null;

    requirementTitle:
      string
      |
      null;

    title:
      string;

    description:
      string
      |
      null;

    status:
      BidTaskStatus;

    priority:
      BidTaskPriority;

    milestone:
      BidTaskMilestone;

    assignedTo:
      string
      |
      null;

    dueDate:
      string
      |
      null;

    overdue:
      boolean;

    completedAt:
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

  events: Array<{

    id:
      string;

    eventType:
      string;

    taskId:
      string
      |
      null;

    actor:
      string;

    detail:
      string;

    createdAt:
      string;

  }>;

};
