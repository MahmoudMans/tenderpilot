export type BuyerConcentration =

  | "UNKNOWN"
  | "DIVERSIFIED"
  | "MODERATE"
  | "CONCENTRATED";


export type CpvSpecialization =

  | "UNKNOWN"
  | "BROAD"
  | "FOCUSED"
  | "SPECIALIZED";


export type SupplierIntelligenceSummary = {

  supplierKey:
    string;

  name:
    string;

  awardCount:
    number;

  uniqueBuyerCount:
    number;

  uniqueCountryCount:
    number;

  associatedAwardValue:
    number
    |
    null;

  averageAssociatedAwardValue:
    number
    |
    null;

  awardCurrency:
    string
    |
    null;

  lastObservedPublicationDate:
    string
    |
    null;

  topBuyerSharePercentage:
    number;

  buyerHhi:
    number;

  buyerConcentration:
    BuyerConcentration;

  repeatBuyerSignal:
    boolean;

  topCpvSharePercentage:
    number;

  cpvSpecialization:
    CpvSpecialization;

};


export type SupplierIntelligenceDetail = {

  summary:
    SupplierIntelligenceSummary;

  valueInterpretation:
    string;

  competitiveLandscapeInterpretation:
    string;

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

    city:
      string
      |
      null;

    awardCount:
      number;

    sharePercentage:
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

  coAwardSuppliers: Array<{

    supplierKey:
      string;

    name:
      string;

    sharedAwardCount:
      number;

    sharePercentage:
      number;

  }>;

  recentAwards: Array<{

    awardId:
      string
      |
      null;

    tenderId:
      string
      |
      null;

    tenderTitle:
      string
      |
      null;

    tenderStatus:
      string
      |
      null;

    buyerId:
      string
      |
      null;

    buyerName:
      string
      |
      null;

    buyerCountryCode:
      string
      |
      null;

    buyerCity:
      string
      |
      null;

    publicationDate:
      string
      |
      null;

    associatedAwardValue:
      number
      |
      null;

    awardCurrency:
      string
      |
      null;

    jointAward:
      boolean;

  }>;

};
