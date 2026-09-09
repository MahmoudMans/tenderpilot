export type IngestionRunStatus =

  | "RUNNING"
  | "COMPLETED"
  | "PARTIAL"
  | "FAILED";


export type TedLastRun = {

  id:
    string;

  status:
    IngestionRunStatus;

  query:
    string;

  requestedLimit:
    number;

  discoveredCount:
    number;

  processedCount:
    number;

  failedCount:
    number;

  startedAt:
    string;

  completedAt:
    string
    |
    null;

  lastError:
    string
    |
    null;

};


export type TedDataSourceStatus = {

  source:
    "TED";

  apiVersion:
    string;

  baseUrl:
    string;

  searchPath:
    string;

  authenticationRequired:
    boolean;

  manualSyncEnabled:
    boolean;

  canonicalNoticeCount:
    number;

  lastNoticeSeenAt:
    string
    |
    null;

  lastRun:
    TedLastRun
    |
    null;

};


export type TedSyncResult = TedLastRun;
