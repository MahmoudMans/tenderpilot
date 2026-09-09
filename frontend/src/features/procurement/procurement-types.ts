export type DataQualityGrade =

  | "EXCELLENT"
  | "GOOD"
  | "FAIR"
  | "POOR"
  | "CRITICAL";


export type DataQualitySeverity =

  | "CRITICAL"
  | "WARNING"
  | "INFO";


export type ProcurementSource =
  | "TED"
  | "BOAMP"
  | "DECP"
  | "MANUAL_DEMO";

export type ProcurementStatus =
  | "DRAFT"
  | "PUBLISHED"
  | "UPDATED"
  | "CLOSED"
  | "AWARDED"
  | "CANCELLED"
  | "ARCHIVED";

export type ProcurementProcedure =
  | "OPEN"
  | "RESTRICTED"
  | "COMPETITIVE_WITH_NEGOTIATION"
  | "COMPETITIVE_DIALOGUE"
  | "INNOVATION_PARTNERSHIP"
  | "NEGOTIATED"
  | "DESIGN_CONTEST"
  | "DIRECT_AWARD"
  | "FRAMEWORK_CALL_OFF"
  | "OTHER";

export type ContractNature =
  | "WORKS"
  | "SERVICES"
  | "SUPPLIES"
  | "MIXED"
  | "OTHER";

export type BuyerCategory =
  | "CENTRAL_GOVERNMENT"
  | "LOCAL_AUTHORITY"
  | "PUBLIC_AGENCY"
  | "EDUCATION"
  | "HEALTHCARE"
  | "SOCIAL_HOUSING"
  | "UTILITIES"
  | "TRANSPORT"
  | "OTHER";

export type CpvSummary = {
  code: string;
  label: string;
};

export type OpportunitySummary = {

  id: string;

  title: string;

  buyerName: string;

  status: ProcurementStatus;

  procedureType: ProcurementProcedure;

  contractNature: ContractNature;

  publicationDate: string;

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

  countryCode: string;

  primaryRegionName:
    string
    |
    null;

  cpvs: CpvSummary[];

  sources: ProcurementSource[];

  lotCount: number;

  awardedAmount:
    number
    |
    null;

  awardedCurrency:
    string
    |
    null;

  dataQualityScore:
    number
    |
    null;

  dataQualityGrade:
    DataQualityGrade
    |
    null;

  dataQualityIssueCount:
    number;

};

export type OpportunityDetail = {

  id: string;

  canonicalKey: string;

  title: string;

  description: string;

  status: ProcurementStatus;

  procedureType: ProcurementProcedure;

  contractNature: ContractNature;

  publicationDate: string;

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

  countryCode: string;

  primaryRegionCode:
    string
    |
    null;

  primaryRegionName:
    string
    |
    null;

  frameworkAgreement: boolean;

  jointProcurement: boolean;

  electronicSubmission: boolean;

  buyer: {
    id: string;
    legalName: string;
    category: BuyerCategory;
    countryCode: string;
    city: string | null;
    registrationNumber: string | null;
    websiteUrl: string | null;
  };

  cpvs: CpvSummary[];

  locations: Array<{
    countryCode: string;
    regionCode: string | null;
    regionName: string | null;
    city: string | null;
  }>;

  lots: Array<{
    id: string;
    lotNumber: string;
    title: string;
    description: string;
    estimatedMinValue: number | null;
    estimatedMaxValue: number | null;
    currency: string | null;
    cpvs: CpvSummary[];
  }>;

  sources: Array<{
    source: ProcurementSource;
    externalId: string;
    externalVersion: string | null;
    noticeType: string | null;
    sourceUrl: string | null;
    payloadChecksum: string | null;
    sourcePublishedAt: string | null;
    firstSeenAt: string;
    lastSeenAt: string;
  }>;

  quality:
    {
      score:
        number;

      grade:
        DataQualityGrade;

      criticalIssueCount:
        number;

      warningIssueCount:
        number;

      infoIssueCount:
        number;

      assessedAt:
        string;

      issues: Array<{

        code:
          string;

        severity:
          DataQualitySeverity;

        fieldName:
          string;

        message:
          string;

      }>;
    }
    |
    null;


  award:
    {
      notificationDate:
        string
        |
        null;

      awardedAmount:
        number
        |
        null;

      currency:
        string
        |
        null;

      durationMonths:
        number
        |
        null;

      priceForm:
        string
        |
        null;

      offersReceived:
        number
        |
        null;

      frameworkAgreementId:
        string
        |
        null;

      socialConsiderations:
        string
        |
        null;

      environmentalConsiderations:
        string
        |
        null;

      executionModalities:
        string
        |
        null;

      operatorGroupType:
        string
        |
        null;

      subcontractingDeclared:
        string
        |
        null;

      suppliers: Array<{
        identifier: string | null;
        identifierType: string | null;
        legalName: string | null;
      }>;
    }
    |
    null;

};
