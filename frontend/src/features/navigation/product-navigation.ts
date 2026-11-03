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
  | "operations"
  | "search"
  | "calendar"
  | "tasks"
  | "company"
  | "organization"
  | "data-sources"
  | "settings";

export type ProductNavigationItem = {
  href: string;
  title: string;
  shortTitle: string;
  description: string;
  icon: ProductNavigationIcon;
  keywords: readonly string[];
  /** Reachable from the command palette and workspace map, but not shown in the sidebar. */
  secondary?: boolean;
};

export type ProductNavigationGroup = {
  title: string;
  description: string;
  items: readonly ProductNavigationItem[];
};

export const productNavigationGroups: readonly ProductNavigationGroup[] = [
  {
    title: "Command",
    description: "Organization-wide priorities, alerts and product navigation.",
    items: [
      {
        href: "/dashboard",
        title: "Executive command center",
        shortTitle: "Dashboard",
        description: "Portfolio priorities, readiness, deadlines and market pulse.",
        icon: "dashboard",
        keywords: ["dashboard", "portfolio", "command", "priority", "deadline", "readiness", "home"],
      },
      {
        href: "/notifications",
        title: "Notifications & watchlists",
        shortTitle: "Notifications",
        description: "Private alerts for tenders, buyers, CPVs and bid execution.",
        icon: "notifications",
        keywords: ["notification", "alert", "watchlist", "deadline", "buyer", "cpv"],
      },
      {
        href: "/workspace",
        title: "Workspace map",
        shortTitle: "Workspace",
        description: "See the full TenderPilot product map and jump to any workflow.",
        icon: "workspace",
        keywords: ["home", "workspace", "navigation", "product", "map", "modules"],
        secondary: true,
      },
      {
        href: "/showcase",
        title: "Portfolio showcase",
        shortTitle: "Showcase",
        description: "Explore the synthetic screenshot-ready TenderPilot demonstration.",
        icon: "workspace",
        keywords: ["showcase", "portfolio", "demo", "synthetic", "presentation", "screenshots"],
        secondary: true,
      },
      {
        href: "/showcase/capture",
        title: "Portfolio capture mode",
        shortTitle: "Capture mode",
        description: "Open six deterministic screenshot-ready synthetic portfolio frames.",
        icon: "workspace",
        keywords: ["capture", "screenshot", "portfolio", "presentation", "demo", "synthetic"],
        secondary: true,
      },
    ],
  },
  {
    title: "Pursuit",
    description: "Discover opportunities, assess fit and control bid decisions.",
    items: [
      {
        href: "/opportunities",
        title: "Procurement opportunities",
        shortTitle: "Opportunities",
        description: "Browse and inspect the normalized public procurement corpus.",
        icon: "opportunities",
        keywords: ["opportunity", "tender", "procurement", "ted", "boamp", "decp", "search"],
      },
      {
        href: "/search",
        title: "Full-text search",
        shortTitle: "Search",
        description: "Search the indexed corpus with filters for country, region, nature and quality.",
        icon: "search",
        keywords: ["search", "find", "opensearch", "index", "filter", "full text"],
      },
      {
        href: "/matches",
        title: "Company-tender matching",
        shortTitle: "Matches",
        description: "Rank public opportunities against your private company profile.",
        icon: "matches",
        keywords: ["matching", "match", "score", "fit", "company", "cpv"],
      },
      {
        href: "/bid-decisions",
        title: "Bid decision portfolio",
        shortTitle: "Bid decisions",
        description: "Manage PENDING, GO, HOLD and NO-GO pursuit decisions.",
        icon: "decisions",
        keywords: ["bid", "decision", "go", "hold", "no-go", "assessment"],
      },
      {
        href: "/calendar",
        title: "Deadline calendar",
        shortTitle: "Calendar",
        description: "Every submission deadline in one timeline, exportable to your calendar app.",
        icon: "calendar",
        keywords: ["calendar", "deadline", "schedule", "due", "ics", "reminder", "timeline"],
      },
      {
        href: "/tasks",
        title: "My tasks",
        shortTitle: "My tasks",
        description: "Open tasks across all your bids, grouped by when they are due.",
        icon: "tasks",
        keywords: ["task", "tasks", "todo", "to-do", "work", "assigned", "mine", "due", "overdue", "blocked", "planning"],
      },
    ],
  },
  {
    title: "Market intelligence",
    description: "Understand public buyers, observed suppliers and market structure.",
    items: [
      {
        href: "/market",
        title: "Procurement market explorer",
        shortTitle: "Market",
        description: "Explore CPVs, geography, concentration, value and market activity.",
        icon: "market",
        keywords: ["market", "analytics", "cpv", "geography", "hhi", "trend"],
      },
      {
        href: "/buyers",
        title: "Buyer intelligence",
        shortTitle: "Buyers",
        description: "Analyze buyer cadence, procurement behavior and public awards.",
        icon: "buyers",
        keywords: ["buyer", "authority", "public", "behavior", "award", "cadence"],
      },
      {
        href: "/suppliers",
        title: "Supplier intelligence",
        shortTitle: "Suppliers",
        description: "Analyze observed public-award supplier relationships.",
        icon: "suppliers",
        keywords: ["supplier", "award", "competitive", "buyer", "cpv", "concentration"],
      },
      {
        href: "/intelligence",
        title: "Intelligence hub",
        shortTitle: "Intelligence",
        description: "Access TenderPilot's connected public intelligence modules.",
        icon: "intelligence",
        keywords: ["intelligence", "research", "insight", "market", "buyer", "supplier"],
      },
    ],
  },
  {
    title: "Operations",
    description: "Monitor health and reliability of the shared procurement corpus.",
    items: [
      {
        href: "/data-operations",
        title: "Data operations",
        shortTitle: "Data operations",
        description: "Monitor TED, BOAMP and DECP ingestion health and reliability.",
        icon: "operations",
        keywords: ["operations", "data", "ingestion", "health", "ted", "boamp", "decp"],
      },
      {
        href: "/data-sources",
        title: "Data sources",
        shortTitle: "Data sources",
        description: "Review the TED, BOAMP and DECP source connections feeding the corpus.",
        icon: "data-sources",
        keywords: ["source", "connector", "ted", "boamp", "decp", "sync", "import"],
      },
    ],
  },
  {
    title: "Account",
    description: "Your company profile, team and personal preferences.",
    items: [
      {
        href: "/company",
        title: "Company profile",
        shortTitle: "Company",
        description: "Maintain the private profile that drives matching: CPVs, regions and capacity.",
        icon: "company",
        keywords: ["company", "profile", "capabilities", "cpv", "certification", "turnover"],
      },
      {
        href: "/organization",
        title: "Organization",
        shortTitle: "Organization",
        description: "Organization details, members and roles.",
        icon: "organization",
        keywords: ["organization", "team", "members", "roles", "owner", "users"],
      },
      {
        href: "/settings",
        title: "Preferences",
        shortTitle: "Settings",
        description: "Theme, density, motion and keyboard shortcuts.",
        icon: "settings",
        keywords: ["settings", "preferences", "theme", "dark", "density", "motion", "shortcuts"],
      },
    ],
  },
];

export const productNavigationItems = productNavigationGroups.flatMap((group) => group.items);

export type Breadcrumb = {
  label: string;
  href?: string;
};

const detailContexts: ReadonlyArray<{
  prefix: string;
  group: string;
  title: string;
  parent: string;
}> = [
  { prefix: "/bid-workspaces/", group: "Pursuit", title: "Bid workspace", parent: "/bid-decisions" },
  { prefix: "/bid-decisions/", group: "Pursuit", title: "Bid decision", parent: "/bid-decisions" },
  { prefix: "/opportunities/", group: "Pursuit", title: "Opportunity detail", parent: "/opportunities" },
  { prefix: "/buyers/", group: "Market intelligence", title: "Buyer detail", parent: "/buyers" },
  { prefix: "/suppliers/", group: "Market intelligence", title: "Supplier detail", parent: "/suppliers" },
];

/** Names for the bid-workspace sub-pages whose address is not simply the name in capitals. */
const subPageLabels: Record<string, string> = { qa: "Q&A", ai: "AI analysis" };

export function productContextForPath(pathname: string) {
  for (const detail of detailContexts) {
    if (pathname.startsWith(detail.prefix)) {
      return { group: detail.group, title: detail.title };
    }
  }

  for (const group of productNavigationGroups) {
    for (const item of group.items) {
      if (pathname === item.href) {
        return { group: group.title, title: item.shortTitle };
      }
    }
  }

  return { group: "TenderPilot", title: "Workspace" };
}

function itemForHref(href: string): ProductNavigationItem | undefined {
  return productNavigationItems.find((item) => item.href === href);
}

/** Breadcrumb trail for the topbar: Group › Section › (detail). */
export function breadcrumbsForPath(pathname: string): Breadcrumb[] {
  const context = productContextForPath(pathname);

  for (const detail of detailContexts) {
    if (pathname.startsWith(detail.prefix)) {
      const parent = itemForHref(detail.parent);
      const trail: Breadcrumb[] = [];
      if (parent) trail.push({ label: parent.shortTitle, href: parent.href });
      else trail.push({ label: detail.group });

      // Bid-workspace sub-pages: /bid-workspaces/{id}/planning etc.
      if (detail.prefix === "/bid-workspaces/") {
        const segments = pathname.split("/").filter(Boolean);
        const tenderId = segments[1];
        const sub = segments[2];
        if (tenderId && sub) {
          trail.push({ label: detail.title, href: `/bid-workspaces/${tenderId}` });
          trail.push({ label: subPageLabels[sub] ?? sub.charAt(0).toUpperCase() + sub.slice(1) });
          return trail;
        }
      }

      trail.push({ label: detail.title });
      return trail;
    }
  }

  if (context.group === "TenderPilot") return [{ label: context.title }];
  return [{ label: context.group }, { label: context.title }];
}
