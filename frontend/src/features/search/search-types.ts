import type {

  OpportunitySummary,

} from "@/features/procurement/procurement-types";


export type ProcurementSearchResponse = {

  engine:
    "OPENSEARCH"
    |
    "POSTGRESQL_FALLBACK";

  fallback:
    boolean;

  total:
    number;

  hits: Array<{

    score:
      number
      |
      null;

    opportunity:
      OpportunitySummary;

  }>;

};


export type SearchProjectionStatus = {

  enabled:
    boolean;

  reachable:
    boolean;

  indexExists:
    boolean;

  indexName:
    string;

  indexedDocuments:
    number;

  authority:
    "POSTGRESQL";

  fallbackEngine:
    "POSTGRESQL";

};


export type SearchReindexResponse = {

  discoveredCount:
    number;

  indexedCount:
    number;

  failedCount:
    number;

  lastError:
    string
    |
    null;

};
