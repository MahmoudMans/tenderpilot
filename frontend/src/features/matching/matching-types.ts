import type {

  OpportunitySummary,

} from "@/features/procurement/procurement-types";


export type TenderMatchGrade =

  | "BLOCKED"
  | "EXCELLENT"
  | "STRONG"
  | "GOOD"
  | "WEAK"
  | "POOR";


export type MatchSignalImpact =

  | "POSITIVE"
  | "NEUTRAL"
  | "NEGATIVE"
  | "BLOCKER";


export type MatchSignal = {

  code:
    string;

  impact:
    MatchSignalImpact;

  dimension:
    string;

  earnedScore:
    number;

  maximumScore:
    number;

  message:
    string;

};


export type TenderMatch = {

  id:
    string;

  score:
    number;

  grade:
    TenderMatchGrade;

  blocked:
    boolean;

  cpvScore:
    number;

  geographyScore:
    number;

  valueScore:
    number;

  buyerScore:
    number;

  capabilityScore:
    number;

  timingScore:
    number;

  qualityScore:
    number;

  evaluatedAt:
    string;

  opportunity:
    OpportunitySummary;

  signals:
    MatchSignal[];

};


export type TenderMatchOverview = {

  totalMatches:
    number;

  actionableMatches:
    number;

  blockedMatches:
    number;

  excellentMatches:
    number;

  strongMatches:
    number;

  goodMatches:
    number;

  averageScore:
    number;

  lastEvaluatedAt:
    string
    |
    null;

};


export type TenderMatchRecalculation = {

  evaluatedCount:
    number;

  actionableCount:
    number;

  blockedCount:
    number;

  excellentCount:
    number;

  strongCount:
    number;

};
