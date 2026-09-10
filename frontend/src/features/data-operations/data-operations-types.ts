export type SourceHealthStatus =

  | "HEALTHY"
  | "RUNNING"
  | "DEGRADED"
  | "STALE"
  | "UNHEALTHY"
  | "UNKNOWN";


export type IngestionRunOutcome =

  | "SUCCESS"
  | "FAILURE"
  | "RUNNING"
  | "UNKNOWN";


export type DataOperations = {

  generatedAt:
    string;

  summary: {

    sourceCount:
      number;

    healthySourceCount:
      number;

    degradedSourceCount:
      number;

    unhealthySourceCount:
      number;

    staleSourceCount:
      number;

    runningSourceCount:
      number;

    unknownSourceCount:
      number;

    runs24Hours:
      number;

    failures24Hours:
      number;

    runs7Days:
      number;

    successRate7Days:
      number
      |
      null;

  };

  sources: Array<{

    source:
      string;

    health:
      SourceHealthStatus;

    runCount7Days:
      number;

    successCount7Days:
      number;

    failureCount7Days:
      number;

    successRate7Days:
      number
      |
      null;

    failureStreak:
      number;

    lastStartedAt:
      string
      |
      null;

    lastFinishedAt:
      string
      |
      null;

    lastSuccessAt:
      string
      |
      null;

    lastFailureAt:
      string
      |
      null;

    importedCount7Days:
      number;

    rejectedCount7Days:
      number;

  }>;

  recentRuns: Array<{

    source:
      string;

    outcome:
      IngestionRunOutcome;

    rawStatus:
      string
      |
      null;

    startedAt:
      string;

    finishedAt:
      string
      |
      null;

    durationSeconds:
      number
      |
      null;

    importedCount:
      number
      |
      null;

    rejectedCount:
      number
      |
      null;

  }>;

};
