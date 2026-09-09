export type IngestionRunStatus =

  | "RUNNING"
  | "COMPLETED"
  | "PARTIAL"
  | "FAILED";


export type DataSourceLastRun = {

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
    DataSourceLastRun
    |
    null;

};


export type BoampDataSourceStatus = {

  source:
    "BOAMP";

  apiVersion:
    string;

  baseUrl:
    string;

  recordsPath:
    string;

  datasetId:
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
    DataSourceLastRun
    |
    null;

};


export type DataSourceSyncResult =
  DataSourceLastRun;
