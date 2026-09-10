export const CAPTURE_WIDTH =
  1440;


export const CAPTURE_HEIGHT =
  900;


export const CAPTURE_DATASET_ID =
  "tenderpilot-portfolio-showcase-v1";


export const captureManifest = [

  {
    order:
      1,

    id:
      "executive",

    anchor:
      "capture-executive",

    title:
      "Executive command center",

    subtitle:
      "Portfolio priorities, bid readiness and immediate execution pressure.",

    recommendedFilename:
      "tenderpilot-01-executive-command-center.png",

    caption:
      "TenderPilot executive command center showing high-fit opportunities, bid decisions, readiness and deadline pressure.",

  },

  {
    order:
      2,

    id:
      "pipeline",

    anchor:
      "capture-pipeline",

    title:
      "Bid portfolio pipeline",

    subtitle:
      "Match intelligence, GO/HOLD/NO-GO decisions and submission readiness.",

    recommendedFilename:
      "tenderpilot-02-bid-portfolio-pipeline.png",

    caption:
      "TenderPilot bid portfolio combining match score, decision status, compliance, task health and submission readiness.",

  },

  {
    order:
      3,

    id:
      "market",

    anchor:
      "capture-market",

    title:
      "Procurement market intelligence",

    subtitle:
      "Buyer, observed-supplier and CPV market structure from the public corpus.",

    recommendedFilename:
      "tenderpilot-03-market-intelligence.png",

    caption:
      "TenderPilot procurement market intelligence with buyer concentration, observed suppliers and CPV activity.",

  },

  {
    order:
      4,

    id:
      "alerts",

    anchor:
      "capture-alerts",

    title:
      "Operational alert center",

    subtitle:
      "Deterministic signals that turn bid state into prioritized human action.",

    recommendedFilename:
      "tenderpilot-04-operational-alert-center.png",

    caption:
      "TenderPilot operational alert center prioritizing overdue work, submission readiness and compliance gaps.",

  },

  {
    order:
      5,

    id:
      "operations",

    anchor:
      "capture-operations",

    title:
      "Procurement data operations",

    subtitle:
      "TED, BOAMP and DECP ingestion reliability and source-health visibility.",

    recommendedFilename:
      "tenderpilot-05-data-operations.png",

    caption:
      "TenderPilot data operations center monitoring procurement-source health and ingestion reliability.",

  },

  {
    order:
      6,

    id:
      "architecture",

    anchor:
      "capture-architecture",

    title:
      "Architecture & trust boundaries",

    subtitle:
      "Shared public intelligence separated from organization-private bid intelligence.",

    recommendedFilename:
      "tenderpilot-06-architecture-boundaries.png",

    caption:
      "TenderPilot architecture separating shared public procurement intelligence from private bid data and local AI.",

  },

] as const;
