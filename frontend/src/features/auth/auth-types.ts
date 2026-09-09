export type MembershipRole =

  | "OWNER"
  | "BID_MANAGER"
  | "ANALYST"
  | "CONTRIBUTOR";


export type OrganizationSummary = {

  id:
    string;

  name:
    string;

  slug:
    string;

};


export type AuthUser = {

  id:
    string;

  fullName:
    string;

  email:
    string;

  organization:
    OrganizationSummary;

  role:
    MembershipRole;

};


export type AuthSession = {

  accessToken:
    string;

  expiresInSeconds:
    number;

  user:
    AuthUser;

};


export type LoginInput = {

  email:
    string;

  password:
    string;

};


export type RegisterInput = {

  fullName:
    string;

  organizationName:
    string;

  email:
    string;

  password:
    string;

};
