export type MarketConcentration =

  | "UNKNOWN"
  | "DIVERSIFIED"
  | "MODERATE"
  | "CONCENTRATED";


export type MarketExplorer = {

  filters: {

    query:
      string
      |
      null;

    country:
      string
      |
      null;

    region:
      string
      |
      null;

    cpvPrefix:
      string
      |
      null;

    status:
      string
      |
      null;

    dateFrom:
      string
      |
      null;

    dateTo:
      string
      |
      null;

  };

  capabilities: {

    countryAvailable:
      boolean;

    regionAvailable:
      boolean;

    cpvAvailable:
      boolean;

    awardsAvailable:
      boolean;

    suppliersAvailable:
      boolean;

    procedureAvailable:
      boolean;

    contractNatureAvailable:
      boolean;

    publicationDateAvailable:
      boolean;

    deadlineAvailable:
      boolean;

  };

  summary: {

    noticeCount:
      number;

    activeNoticeCount:
      number;

    awardedNoticeCount:
      number;

    uniqueBuyerCount:
      number;

    uniqueObservedSupplierCount:
      number;

    cpvCoveragePercentage:
      number;

    geographyCoveragePercentage:
      number;

    awardCoveragePercentage:
      number;

    supplierCoveragePercentage:
      number;

    medianResponseDays:
      number
      |
      null;

    averageEstimatedValue:
      number
      |
      null;

    estimatedCurrency:
      string
      |
      null;

    totalAwardedValue:
      number
      |
      null;

    awardedCurrency:
      string
      |
      null;

    topBuyerSharePercentage:
      number;

    buyerHhi:
      number;

    buyerConcentration:
      MarketConcentration;

    topObservedSupplierSharePercentage:
      number;

    supplierHhi:
      number;

    supplierConcentration:
      MarketConcentration;

  };

  valueInterpretation:
    string;

  supplierInterpretation:
    string;

  monthlyTrend: Array<{

    month:
      string;

    noticeCount:
      number;

    activeNoticeCount:
      number;

    awardedNoticeCount:
      number;

  }>;

  topCpvs: Array<{

    code:
      string;

    description:
      string
      |
      null;

    occurrenceCount:
      number;

    sharePercentage:
      number;

  }>;

  topBuyers: Array<{

    buyerId:
      string
      |
      null;

    name:
      string
      |
      null;

    countryCode:
      string
      |
      null;

    noticeCount:
      number;

    sharePercentage:
      number;

  }>;

  topSuppliers: Array<{

    supplierKey:
      string;

    name:
      string;

    observedAwardCount:
      number;

    sharePercentage:
      number;

  }>;

  geographies: Array<{

    countryCode:
      string
      |
      null;

    regionCode:
      string
      |
      null;

    regionName:
      string
      |
      null;

    noticeCount:
      number;

    sharePercentage:
      number;

  }>;

  procedures: Array<{

    value:
      string;

    noticeCount:
      number;

    sharePercentage:
      number;

  }>;

  contractNatures: Array<{

    value:
      string;

    noticeCount:
      number;

    sharePercentage:
      number;

  }>;

  recentOpportunities: Array<{

    tenderId:
      string;

    buyerId:
      string
      |
      null;

    buyerName:
      string
      |
      null;

    title:
      string;

    status:
      string
      |
      null;

    procedure:
      string
      |
      null;

    contractNature:
      string
      |
      null;

    publicationDate:
      string
      |
      null;

    submissionDeadline:
      string
      |
      null;

    estimatedMinValue:
      number
      |
      null;

    estimatedMaxValue:
      number
      |
      null;

    currency:
      string
      |
      null;

    awarded:
      boolean;

  }>;

};
