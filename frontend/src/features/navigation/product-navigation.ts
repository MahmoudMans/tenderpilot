export type ProductNavigationIcon =

  | "workspace"
  | "dashboard"
  | "opportunities"
  | "matches"
  | "decisions"
  | "notifications"
  | "market"
  | "buyers"
  | "suppliers"
  | "intelligence"
  | "operations";


export type ProductNavigationItem = {

  href:
    string;

  title:
    string;

  shortTitle:
    string;

  description:
    string;

  icon:
    ProductNavigationIcon;

  keywords:
    readonly string[];

};


export type ProductNavigationGroup = {

  title:
    string;

  description:
    string;

  items:
    readonly ProductNavigationItem[];

};


export const productNavigationGroups:
readonly ProductNavigationGroup[] = [

  {
    title:
      "Command",

    description:
      "Organization-wide priorities, alerts and product navigation.",

    items: [

      {
        href:
          "/workspace",

        title:
          "Workspace map",

        shortTitle:
          "Workspace",

        description:
          "See the full TenderPilot product map and jump to any workflow.",

        icon:
          "workspace",

        keywords: [
          "home",
          "workspace",
          "navigation",
          "product",
          "map",
          "modules",
        ],
      },

      {
        href:
          "/showcase",

        title:
          "Portfolio showcase",

        shortTitle:
          "Showcase",

        description:
          "Explore the synthetic screenshot-ready TenderPilot demonstration.",

        icon:
          "workspace",

        keywords: [
          "showcase",
          "portfolio",
          "demo",
          "synthetic",
          "presentation",
          "screenshots",
        ],
      },


      {
        href:
          "/showcase/capture",

        title:
          "Portfolio capture mode",

        shortTitle:
          "Capture mode",

        description:
          "Open six deterministic screenshot-ready synthetic portfolio frames.",

        icon:
          "workspace",

        keywords: [
          "capture",
          "screenshot",
          "portfolio",
          "presentation",
          "demo",
          "synthetic",
        ],
      },


      {
        href:
          "/dashboard",

        title:
          "Executive command center",

        shortTitle:
          "Dashboard",

        description:
          "Portfolio priorities, readiness, deadlines and market pulse.",

        icon:
          "dashboard",

        keywords: [
          "dashboard",
          "portfolio",
          "command",
          "priority",
          "deadline",
          "readiness",
        ],
      },

      {
        href:
          "/notifications",

        title:
          "Notifications & watchlists",

        shortTitle:
          "Notifications",

        description:
          "Private alerts for tenders, buyers, CPVs and bid execution.",

        icon:
          "notifications",

        keywords: [
          "notification",
          "alert",
          "watchlist",
          "deadline",
          "buyer",
          "cpv",
        ],
      },

    ],
  },

  {
    title:
      "Pursuit",

    description:
      "Discover opportunities, assess fit and control bid decisions.",

    items: [

      {
        href:
          "/opportunities",

        title:
          "Procurement opportunities",

        shortTitle:
          "Opportunities",

        description:
          "Browse and inspect the normalized public procurement corpus.",

        icon:
          "opportunities",

        keywords: [
          "opportunity",
          "tender",
          "procurement",
          "ted",
          "boamp",
          "decp",
          "search",
        ],
      },

      {
        href:
          "/matches",

        title:
          "Company-tender matching",

        shortTitle:
          "Matches",

        description:
          "Rank public opportunities against your private company profile.",

        icon:
          "matches",

        keywords: [
          "matching",
          "match",
          "score",
          "fit",
          "company",
          "cpv",
        ],
      },

      {
        href:
          "/bid-decisions",

        title:
          "Bid decision portfolio",

        shortTitle:
          "Bid decisions",

        description:
          "Manage PENDING, GO, HOLD and NO-GO pursuit decisions.",

        icon:
          "decisions",

        keywords: [
          "bid",
          "decision",
          "go",
          "hold",
          "no-go",
          "assessment",
        ],
      },

    ],
  },

  {
    title:
      "Market intelligence",

    description:
      "Understand public buyers, observed suppliers and market structure.",

    items: [

      {
        href:
          "/market",

        title:
          "Procurement market explorer",

        shortTitle:
          "Market",

        description:
          "Explore CPVs, geography, concentration, value and market activity.",

        icon:
          "market",

        keywords: [
          "market",
          "analytics",
          "cpv",
          "geography",
          "hhi",
          "trend",
        ],
      },

      {
        href:
          "/buyers",

        title:
          "Buyer intelligence",

        shortTitle:
          "Buyers",

        description:
          "Analyze buyer cadence, procurement behavior and public awards.",

        icon:
          "buyers",

        keywords: [
          "buyer",
          "authority",
          "public",
          "behavior",
          "award",
          "cadence",
        ],
      },

      {
        href:
          "/suppliers",

        title:
          "Supplier intelligence",

        shortTitle:
          "Suppliers",

        description:
          "Analyze observed public-award supplier relationships.",

        icon:
          "suppliers",

        keywords: [
          "supplier",
          "award",
          "competitive",
          "buyer",
          "cpv",
          "concentration",
        ],
      },

      {
        href:
          "/intelligence",

        title:
          "Intelligence hub",

        shortTitle:
          "Intelligence",

        description:
          "Access TenderPilot's connected public intelligence modules.",

        icon:
          "intelligence",

        keywords: [
          "intelligence",
          "research",
          "insight",
          "market",
          "buyer",
          "supplier",
        ],
      },

    ],
  },

  {
    title:
      "Operations",

    description:
      "Monitor health and reliability of the shared procurement corpus.",

    items: [

      {
        href:
          "/data-operations",

        title:
          "Data operations",

        shortTitle:
          "Data operations",

        description:
          "Monitor TED, BOAMP and DECP ingestion health and reliability.",

        icon:
          "operations",

        keywords: [
          "operations",
          "data",
          "ingestion",
          "health",
          "ted",
          "boamp",
          "decp",
        ],
      },

    ],
  },

];


export const productNavigationItems =
  productNavigationGroups.flatMap(
    (
      group,
    ) =>
      group.items,
  );


export function productContextForPath(
  pathname:
    string,
) {

  if (
    pathname.startsWith(
      "/bid-workspaces/",
    )
  ) {

    return {
      group:
        "Pursuit",

      title:
        "Bid workspace",
    };

  }


  if (
    pathname.startsWith(
      "/opportunities/",
    )
  ) {

    return {
      group:
        "Pursuit",

      title:
        "Opportunity detail",
    };

  }


  if (
    pathname.startsWith(
      "/buyers/",
    )
  ) {

    return {
      group:
        "Market intelligence",

      title:
        "Buyer detail",
    };

  }


  if (
    pathname.startsWith(
      "/suppliers/",
    )
  ) {

    return {
      group:
        "Market intelligence",

      title:
        "Supplier detail",
    };

  }


  for (
    const group
    of
    productNavigationGroups
  ) {

    for (
      const item
      of
      group.items
    ) {

      if (
        pathname ===
        item.href
      ) {

        return {
          group:
            group.title,

          title:
            item.shortTitle,
        };

      }

    }

  }


  return {
    group:
      "TenderPilot",

    title:
      "Workspace",
  };

}
