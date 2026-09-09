export type EmployeeBand =

  | "NOT_DISCLOSED"
  | "ONE_TO_NINE"
  | "TEN_TO_FORTY_NINE"
  | "FIFTY_TO_TWO_FORTY_NINE"
  | "TWO_FIFTY_TO_NINE_NINETY_NINE"
  | "THOUSAND_PLUS";


export type AnnualRevenueBand =

  | "NOT_DISCLOSED"
  | "UNDER_500K"
  | "FROM_500K_TO_2M"
  | "FROM_2M_TO_10M"
  | "FROM_10M_TO_50M"
  | "FROM_50M_TO_250M"
  | "ABOVE_250M";


export type BuyerType =

  | "CENTRAL_GOVERNMENT"
  | "LOCAL_AUTHORITY"
  | "PUBLIC_AGENCY"
  | "EDUCATION"
  | "HEALTHCARE"
  | "SOCIAL_HOUSING"
  | "UTILITIES"
  | "TRANSPORT"
  | "OTHER";


export type OperatingRegion = {

  countryCode:
    string;

  regionCode:
    string;

  regionName:
    string;

};


export type TargetCpv = {

  code:
    string;

  label:
    string;

};


export type Certification = {

  name:
    string;

  issuer:
    string;

  certificateNumber:
    string
    |
    null;

  validFrom:
    string
    |
    null;

  expiresOn:
    string
    |
    null;

  validityStatus:
    string;

};


export type ReferenceProject = {

  projectName:
    string;

  clientName:
    string;

  countryCode:
    string;

  regionName:
    string
    |
    null;

  contractValue:
    number
    |
    null;

  currency:
    string
    |
    null;

  startedOn:
    string
    |
    null;

  completedOn:
    string
    |
    null;

  description:
    string;

};


export type CompletenessSection = {

  key:
    string;

  label:
    string;

  points:
    number;

  maximumPoints:
    number;

};


export type CompanyProfile = {

  exists:
    boolean;

  organizationId:
    string;

  profileId:
    string
    |
    null;

  legalName:
    string
    |
    null;

  tradingName:
    string
    |
    null;

  industry:
    string
    |
    null;

  description:
    string
    |
    null;

  websiteUrl:
    string
    |
    null;

  countryCode:
    string
    |
    null;

  headquartersCity:
    string
    |
    null;

  employeeBand:
    EmployeeBand
    |
    null;

  annualRevenueBand:
    AnnualRevenueBand
    |
    null;

  preferredMinContractValue:
    number
    |
    null;

  preferredMaxContractValue:
    number
    |
    null;

  preferredCurrency:
    string
    |
    null;

  yearsExperience:
    number
    |
    null;

  minimumNoticeDays:
    number
    |
    null;

  capabilities:
    string[];

  operatingRegions:
    OperatingRegion[];

  targetCpvs:
    TargetCpv[];

  certifications:
    Certification[];

  referenceProjects:
    ReferenceProject[];

  preferredBuyerTypes:
    BuyerType[];

  excludedKeywords:
    string[];

  completeness: {

    score:
      number;

    sections:
      CompletenessSection[];

  };

  createdAt:
    string
    |
    null;

  updatedAt:
    string
    |
    null;

};


export type CompanyProfileInput = {

  legalName:
    string;

  tradingName:
    string
    |
    null;

  industry:
    string;

  description:
    string;

  websiteUrl:
    string
    |
    null;

  countryCode:
    string;

  headquartersCity:
    string;

  employeeBand:
    EmployeeBand;

  annualRevenueBand:
    AnnualRevenueBand;

  preferredMinContractValue:
    number
    |
    null;

  preferredMaxContractValue:
    number
    |
    null;

  preferredCurrency:
    string
    |
    null;

  yearsExperience:
    number
    |
    null;

  minimumNoticeDays:
    number
    |
    null;

  capabilities:
    string[];

  operatingRegions:
    OperatingRegion[];

  targetCpvs:
    TargetCpv[];

  certifications: Array<{

    name:
      string;

    issuer:
      string;

    certificateNumber:
      string
      |
      null;

    validFrom:
      string
      |
      null;

    expiresOn:
      string
      |
      null;

  }>;

  referenceProjects: Array<{

    projectName:
      string;

    clientName:
      string;

    countryCode:
      string;

    regionName:
      string
      |
      null;

    contractValue:
      number
      |
      null;

    currency:
      string
      |
      null;

    startedOn:
      string
      |
      null;

    completedOn:
      string
      |
      null;

    description:
      string;

  }>;

  preferredBuyerTypes:
    BuyerType[];

  excludedKeywords:
    string[];

};
