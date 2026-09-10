export type SupplierConcentration =

  | "UNKNOWN"
  | "DIVERSIFIED"
  | "MODERATE"
  | "CONCENTRATED";


export type DeadlineTempo =

  | "UNKNOWN"
  | "SHORT"
  | "STANDARD"
  | "LONG";


export type BuyerIntelligenceSummary = {

  buyerId:
    string;

  name:
    string;

  category:
    string
    |
    null;

  countryCode:
    string
    |
    null;

  city:
    string
    |
    null;

  noticeCount:
    number;

  activeNoticeCount:
    number;

  awardCount:
    number;

  totalAwardedValue:
    number
    |
    null;

  awardCurrency:
    string
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

  lastPublicationDate:
    string
    |
    null;

  recurringProcurement:
    boolean;

  topCpvSharePercentage:
    number;

  supplierConcentration:
    SupplierConcentration;

  topSupplierSharePercentage:
    number;

  supplierHhi:
    number;

  medianResponseDays:
    number
    |
    null;

  deadlineTempo:
    DeadlineTempo;

};


export type BuyerIntelligenceDetail = {

  summary:
    BuyerIntelligenceSummary;

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

  topSuppliers: Array<{

    name:
      string;

    awardCount:
      number;

    sharePercentage:
      number;

  }>;

  monthlyActivity: Array<{

    month:
      number;

    noticeCount:
      number;

  }>;

  recentTenders: Array<{

    tenderId:
      string;

    title:
      string;

    status:
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

  }>;

};
