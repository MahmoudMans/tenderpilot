export const SHOWCASE_DATASET_KIND =
  "SYNTHETIC_NON_PRODUCTION";


export const SHOWCASE_DATASET_ID =
  "tenderpilot-portfolio-showcase-v1";


export const showcaseData = {

  metadata: {

    id:
      SHOWCASE_DATASET_ID,

    kind:
      SHOWCASE_DATASET_KIND,

    title:
      "TenderPilot portfolio showcase",

    disclaimer:
      "All organizations, opportunities, metrics, alerts and procurement observations on this page are synthetic demonstration data.",

    generatedFromLiveData:
      false,

    persistent:
      false,

  },


  executive: {

    matchedOpportunities:
      42,

    highPotentialMatches:
      11,

    goBids:
      6,

    holdBids:
      3,

    noGoBids:
      2,

    pendingDecisions:
      5,

    complianceReady:
      4,

    submissionReady:
      2,

    submitted:
      1,

    openTasks:
      18,

    overdueTasks:
      3,

    criticalTasks:
      2,

    deadlines7Days:
      4,

  },


  bids: [

    {
      id:
        "demo-bid-001",

      title:
        "Energy retrofit — municipal administration complex",

      buyer:
        "Métropole Démo Val-de-Rive",

      matchScore:
        94,

      grade:
        "EXCELLENT",

      decision:
        "GO",

      deadline:
        "12 Sep 2026",

      deadlineSignal:
        "2 days",

      compliance:
        92,

      openTasks:
        3,

      overdueTasks:
        1,

      readiness:
        "AT_RISK",

      readinessLabel:
        "At risk",

    },

    {
      id:
        "demo-bid-002",

      title:
        "School complex HVAC and insulation renovation",

      buyer:
        "Ville Démo de Montfleur",

      matchScore:
        88,

      grade:
        "EXCELLENT",

      decision:
        "GO",

      deadline:
        "15 Sep 2026",

      deadlineSignal:
        "5 days",

      compliance:
        100,

      openTasks:
        0,

      overdueTasks:
        0,

      readiness:
        "READY",

      readinessLabel:
        "Submission ready",

    },

    {
      id:
        "demo-bid-003",

      title:
        "Historic library envelope restoration",

      buyer:
        "Agglomération Démo des Rives",

      matchScore:
        82,

      grade:
        "STRONG",

      decision:
        "HOLD",

      deadline:
        "28 Sep 2026",

      deadlineSignal:
        "18 days",

      compliance:
        74,

      openTasks:
        4,

      overdueTasks:
        0,

      readiness:
        "REVIEW",

      readinessLabel:
        "Under review",

    },

    {
      id:
        "demo-bid-004",

      title:
        "Administrative center accessibility upgrade",

      buyer:
        "Département Démo Rhône-Alpes",

      matchScore:
        79,

      grade:
        "STRONG",

      decision:
        "PENDING",

      deadline:
        "22 Sep 2026",

      deadlineSignal:
        "12 days",

      compliance:
        48,

      openTasks:
        2,

      overdueTasks:
        0,

      readiness:
        "DECISION",

      readinessLabel:
        "Decision required",

    },

    {
      id:
        "demo-bid-005",

      title:
        "Public housing façade refurbishment programme",

      buyer:
        "Office Habitat Démo Grand-Ouest",

      matchScore:
        91,

      grade:
        "EXCELLENT",

      decision:
        "GO",

      deadline:
        "18 Sep 2026",

      deadlineSignal:
        "8 days",

      compliance:
        86,

      openTasks:
        5,

      overdueTasks:
        2,

      readiness:
        "AT_RISK",

      readinessLabel:
        "At risk",

    },

    {
      id:
        "demo-bid-006",

      title:
        "Municipal sports hall modernization",

      buyer:
        "Ville Démo Côte-Verte",

      matchScore:
        67,

      grade:
        "GOOD",

      decision:
        "NO_GO",

      deadline:
        "09 Oct 2026",

      deadlineSignal:
        "29 days",

      compliance:
        20,

      openTasks:
        0,

      overdueTasks:
        0,

      readiness:
        "CLOSED",

      readinessLabel:
        "Closed",

    },

  ],


  market: {

    notices30Days:
      486,

    activeNotices30Days:
      172,

    uniqueBuyers30Days:
      61,

    observedSuppliers30Days:
      37,

    awardCoverage:
      58,

    buyerConcentration:
      "DIVERSIFIED",

    supplierConcentration:
      "MODERATE",

    leadingCpv:
      "45453000",

    leadingCpvLabel:
      "Overhaul and refurbishment work",

    leadingCpvShare:
      22,

    trend:
      [
        43,
        51,
        47,
        58,
        61,
        73,
        69,
        84,
      ],

  },


  buyers: [

    {
      name:
        "Métropole Démo Val-de-Rive",

      noticeShare:
        12,

      awards:
        16,

      cadence:
        "Recurring",
    },

    {
      name:
        "Ville Démo de Montfleur",

      noticeShare:
        9,

      awards:
        11,

      cadence:
        "Regular",
    },

    {
      name:
        "Agglomération Démo des Rives",

      noticeShare:
        7,

      awards:
        8,

      cadence:
        "Periodic",
    },

  ],


  suppliers: [

    {
      name:
        "DemoBuild Horizon",

      awardShare:
        14,

      specialization:
        "Refurbishment",
    },

    {
      name:
        "DemoConstruct Atlas",

      awardShare:
        11,

      specialization:
        "Public buildings",
    },

    {
      name:
        "DemoRenova Structures",

      awardShare:
        8,

      specialization:
        "Envelope works",
    },

  ],


  alerts: [

    {
      id:
        "demo-alert-001",

      severity:
        "CRITICAL",

      title:
        "Resolve overdue bid work",

      detail:
        "Energy retrofit has one overdue task and a deadline in two days.",

    },

    {
      id:
        "demo-alert-002",

      severity:
        "HIGH",

      title:
        "Submission package ready",

      detail:
        "School complex renovation has cleared the submission-readiness gate.",

    },

    {
      id:
        "demo-alert-003",

      severity:
        "HIGH",

      title:
        "Compliance gaps on GO bid",

      detail:
        "Public housing façade programme remains below full compliance readiness.",

    },

    {
      id:
        "demo-alert-004",

      severity:
        "MEDIUM",

      title:
        "Strong opportunity awaiting decision",

      detail:
        "Accessibility upgrade has a 79% fit score and remains PENDING.",

    },

  ],


  sourceHealth: [

    {
      source:
        "TED",

      health:
        "HEALTHY",

      reliability:
        100,

      runs7d:
        14,

      records:
        2842,
    },

    {
      source:
        "BOAMP",

      health:
        "HEALTHY",

      reliability:
        96,

      runs7d:
        14,

      records:
        1936,
    },

    {
      source:
        "DECP",

      health:
        "DEGRADED",

      reliability:
        88,

      runs7d:
        8,

      records:
        724,
    },

  ],


  architecture: [

    {
      title:
        "Shared public corpus",

      detail:
        "TED, BOAMP, DECP, public buyers, awards, observed suppliers and market analytics.",

    },

    {
      title:
        "Organization-private intelligence",

      detail:
        "Company profile, matching, bid decisions, documents, compliance, planning and notifications.",

    },

    {
      title:
        "Local AI boundary",

      detail:
        "Document analysis and private RAG stay local and never determine human bid decisions.",

    },

    {
      title:
        "Search boundary",

      detail:
        "Global OpenSearch contains public procurement data only; private bid data is excluded.",

    },

  ],


  captureTargets: [

    {
      number:
        "01",

      title:
        "Executive dashboard",

      detail:
        "Show portfolio KPIs, priorities and deadline pressure.",
    },

    {
      number:
        "02",

      title:
        "Bid pipeline",

      detail:
        "Show fit score, decision, compliance and readiness together.",
    },

    {
      number:
        "03",

      title:
        "Market intelligence",

      detail:
        "Show buyer/supplier concentration and CPV market activity.",
    },

    {
      number:
        "04",

      title:
        "Alerts",

      detail:
        "Show operational prioritization and human-action signals.",
    },

    {
      number:
        "05",

      title:
        "Data operations",

      detail:
        "Show ingestion reliability and source-health visibility.",
    },

    {
      number:
        "06",

      title:
        "Architecture boundaries",

      detail:
        "Show public/private separation and local-AI constraints.",
    },

  ],

} as const;
