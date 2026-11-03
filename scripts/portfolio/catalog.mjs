/**
 * What gets captured and what is said about it. Plain data: edit the texts here and run
 *   ./scripts/capture-portfolio.sh --docs-only
 * to rewrite the Markdown and the gallery without taking the screenshots again.
 *
 * A shot is one screen or one state of the interface:
 *   id, section, title, tagline, description, highlights   the text
 *   path       address to open; {opportunity} {buyer} {supplier} {tender} are replaced by a real record
 *   steps      what to do before the picture (click, press, fill, select, type, hover, check, wait, waitFor)
 *   net        "signed-out", "offline" or "fresh" (a brand-new account)
 *   variants   which of desktop-light, desktop-dark and phone-light to take (default: all three)
 *   full       false: no full-page image
 *   focus      scroll to this part of the page before the picture: a selector, or { selector, offset }
 *   scope      the part of the page the "On this screen" list is read from (default: the main area)
 *   viewportHeight   a taller desktop window, for dialogs that do not fit in 900 px
 *   feature    true: one of the best pictures to lead a portfolio with
 *
 * Keep the text plain ASCII.
 */

export const PROFILES = {
  "desktop-light": { label: "Desktop, light theme", short: "Light", width: 1440, height: 900, scheme: "light", kinds: ["hero", "full"] },
  "desktop-dark": { label: "Desktop, dark theme", short: "Dark", width: 1440, height: 900, scheme: "dark", kinds: ["hero"] },
  "phone-light": { label: "Phone, light theme", short: "Phone", width: 390, height: 844, scheme: "light", kinds: ["hero", "full"], mobile: true },
  frame: { label: "Showcase frame", short: "Frame", width: 1440, height: 900, scheme: "light", kinds: ["hero"] },
};

const shot = (id, section, title, path, text, extra = {}) => ({
  id,
  section,
  title,
  path,
  tagline: text[0],
  description: text[1],
  highlights: text[2] || [],
  ...extra,
});

const OVERLAY = ["desktop-light", "desktop-dark"];

export const SHOTS = [
  // ---------------------------------------------------------------------------------------------------
  shot("login", "Getting in", "Sign in", "/login", [
    "A calm sign-in page that says what the product is for.",
    "The sign-in page pairs the form with a short statement of the product and its three promises: opportunity intelligence, market intelligence and explainable decisions. The form has a show/hide password control, and a theme switch is available before anyone is signed in.",
    [
      "Email and password with a show/hide control",
      "Light and dark theme switch on the sign-in page itself",
      "Opening a link while signed out leads here and returns you to that page after signing in",
      "When a session ends by itself the page says so instead of silently dropping you here",
    ],
  ], { net: "signed-out" }),

  shot("register", "Getting in", "Create an account", "/register", [
    "Creating an organization and its owner account in one short form.",
    "Registration creates the organization and its first user together. The password field shows a strength meter while you type and states the 10-character rule, so the requirement is clear before the form is submitted. The picture shows the form filled in with fictional details.",
    [
      "Full name, organization name, work email and password",
      "Live password-strength meter and a plain statement of the length rule",
      "Show/hide password control",
      "The link back to sign in keeps the page you were trying to reach",
    ],
  ], {
    net: "signed-out",
    steps: [
      { fill: ['input[autocomplete="name"]', "Alex Morgan"] },
      { fill: ['input[autocomplete="organization"]', "Northwind Systems"] },
      { fill: ['input[autocomplete="email"]', "alex@northwind.example"] },
      { fill: ['input[autocomplete="new-password"]', "Tender-Pilot-2026!"] },
    ],
  }),

  // ---------------------------------------------------------------------------------------------------
  shot("dashboard", "Command", "Executive command center", "/dashboard", [
    "Where the organization should focus right now.",
    "The dashboard turns the whole bid portfolio into one page: eight headline numbers, the tasks that need attention, how many tenders survive each stage, the next submission deadlines, operational priorities and a pulse of the public market. Everything on it links to the page where you can act.",
    [
      "Eight headline tiles: high-potential matches, GO portfolio, submission ready, overdue work, pending decisions, compliance ready, open bid tasks and deadlines within 7 days",
      "Tasks that need attention: overdue first, then blocked, then critical, with owner and due date, and a link to the full task list",
      "Pipeline funnel from matched opportunities to submitted bids",
      "Next submission deadlines with days left, and operational priorities ranked by severity",
      "Public market pulse from the shared corpus, kept apart from private bid data",
      "Bid portfolio cards with match score, compliance, open tasks and deadline",
    ],
  ], { feature: true }),

  shot("dashboard-onboarding", "Command", "First-run dashboard", "/dashboard", [
    "A new account is guided instead of shown empty charts.",
    "For an organization with no data yet, the dashboard shows zeros honestly and adds a dismissible checklist that is driven by the real state of the account: complete the company profile, review the best matches, record a bid decision, plan the bid and watch a buyer or a CPV code.",
    [
      "Five-step checklist with a progress ring and a Next step link",
      "Steps tick themselves when the real data changes, nothing is clicked by hand",
      "Can be hidden once it is no longer needed",
      "The tiles stay honest: zero means zero",
    ],
  ], { net: "fresh", focus: { selector: "h2:has-text(\"Set up your bid engine\")", offset: 200 } }),

  shot("notifications", "Command", "Notifications and watchlists", "/notifications", [
    "Private alerts for tenders, buyers, CPV codes and bid execution.",
    "The page tracks deadlines, high-fit opportunities, compliance gaps, overdue bid work, submission readiness, strategic buyers and CPV market activity. Every alert has a severity and a type, and can be opened, acknowledged or dismissed. A watchlist lets the team follow a tender, a buyer or a CPV prefix.",
    [
      "Summary tiles for active alerts, unread, critical, high and the size of the watchlist",
      "Open, acknowledge or dismiss each alert, mark all as read, refresh on demand",
      "Add to the watchlist by tender UUID, buyer UUID or a 1 to 8 digit CPV prefix, with an optional label",
      "The bell in the top bar and the sidebar badge count the alerts that are actually shown",
      "Alerts are polled every two minutes",
    ],
  ]),

  shot("workspace", "Command", "Workspace map", "/workspace", [
    "The whole product on one page, organized the way people work.",
    "The workspace map groups every module under Command, Pursuit, Market intelligence, Operations and Account, with a description of each, so a new user can see what exists and jump straight to it. The same registry feeds the sidebar, the command palette and this page.",
    [
      "Five groups that mirror the sidebar",
      "One-line explanation of every module",
      "A shortcut hint for the command palette",
      "Single navigation registry shared by the sidebar, palette, breadcrumbs and this page",
    ],
  ]),

  shot("showcase", "Command", "Portfolio showcase", "/showcase", [
    "A presentation page built from a synthetic dataset.",
    "The showcase page presents the product as a story, from discovery to controlled submission, using only a fixed synthetic dataset. It is the page the six capture frames are cut from, and it states plainly that no customer or procurement data appears in it.",
    [
      "A hero statement, with chips that jump to each of the six sections",
      "Executive center, pipeline, market, alerts, operations and architecture sections",
      "Shared public corpus shown separately from organization-private intelligence",
      "A link to the capture mode with the six fixed frames",
    ],
  ]),

  // ---------------------------------------------------------------------------------------------------
  shot("opportunities", "Pursuit", "Opportunities", "/opportunities", [
    "The public procurement corpus in one source-neutral model.",
    "Notices from TED, BOAMP and DECP are normalized into one tender model, so a team browses opportunities rather than portals. Four tiles summarize the current result, and search, status, source, minimum data quality and sort narrow the list.",
    [
      "Debounced search plus filters for status, source, minimum data quality and open-for-bids only",
      "Filter chips and a Reset, so the active filters are always visible",
      "Cards and table layouts",
      "Saved views kept in the browser, CSV export, and links that open pre-filtered lists",
      "Tick up to three opportunities to compare them side by side",
    ],
  ], { feature: true }),

  shot("opportunities-table", "Pursuit", "Opportunities as a table", "/opportunities", [
    "The same list, denser, for scanning.",
    "The table layout trades cards for rows, with status, deadline, estimated value, data quality and sources in aligned columns, which is quicker to scan when the list is long.",
    [
      "Status, deadline with days left, estimated value, quality score and source badges per row",
      "Selection boxes for the comparison",
      "Same filters, saved views and export as the card layout",
    ],
  ], { steps: [{ click: 'role=radio[name="Table"]' }] }),

  shot("opportunities-compare", "Pursuit", "Compare opportunities", "/opportunities", [
    "Up to three tenders side by side, with the strongest value marked.",
    "Selecting opportunities opens a floating bar, and the comparison dialog lines them up row by row: status, buyer, location, deadline, value, contract, lots, publication date, data quality, sources and CPV codes. The best value in each row is marked, and CPV codes shared by several tenders are highlighted.",
    [
      "Deadline, value and data quality compared in one table",
      "Best value in a row marked in green",
      "Shared CPV codes highlighted",
      "Open details link and a remove button on every column",
    ],
  ], {
    variants: OVERLAY,
    full: false,
    viewportHeight: 1000,
    scope: "[role=dialog]",
    steps: [
      { click: "role=checkbox[name=/^Compare /] >> nth=0" },
      { click: "role=checkbox[name=/^Compare /] >> nth=1" },
      { click: 'role=region[name="Comparison selection"] >> role=button[name="Compare"]' },
      { waitFor: "role=dialog" },
    ],
  }),

  shot("opportunity-detail", "Pursuit", "Opportunity detail", "/opportunities/{opportunity}", [
    "Everything known about one tender, and the next action beside it.",
    "The detail page keeps the tender's facts, lots, classifications, buyer and data lineage in one place, and puts the actions that follow from them at the top: open the Go/No-Go assessment, watch the tender, jump to buyer intelligence, add the deadline to a calendar, copy the link or print.",
    [
      "Quick actions: Go/No-Go assessment, Watch, buyer intelligence, add deadline to calendar, copy link, print",
      "CPV classifications, each with a Watch button and a Similar link into search",
      "Contracting authority, places of performance, tender lots and procurement characteristics",
      "Data quality assessment with a score and the issues behind it",
      "Source lineage showing which portals the record came from",
    ],
  ]),

  shot("search", "Pursuit", "Full-text search", "/search?q=renovation", [
    "Search across the whole catalogue, with the filters that matter.",
    "Search queries the indexed corpus with OpenSearch and falls back to PostgreSQL when the index is not available. Filters narrow by status, source, contract type and minimum data quality, and a search can be copied as a link or saved.",
    [
      "Full-text query with status, source, contract type and data-quality filters",
      "Copy link button and saved searches kept in the browser",
      "Show more results steps through 25, 50 and 100 hits",
      "Index status and a rebuild action for the people who operate the system",
    ],
  ]),

  shot("matches", "Pursuit", "Company-tender matching", "/matches", [
    "Public tenders ranked against the company's private profile, with the reasons.",
    "Each opportunity gets a score out of 100 that is explained, not just shown: the card breaks the score into CPV fit, geography, contract value, buyer preference, capabilities, timing and data quality, each with its own score and its weight. Matches are stored, and the page tells you when they are out of date.",
    [
      "Score breakdown per criterion, each with its score and its weight",
      "Filter by match grade and minimum score, and optionally include blocked opportunities",
      "Recalculate matches on demand, with a notice when the company profile changed since the last run",
      "Saved views and CSV export",
      "Straight link into the Go/No-Go workspace",
    ],
  ], { feature: true }),

  shot("bid-decisions", "Pursuit", "Bid decision portfolio", "/bid-decisions", [
    "Every pursuit decision, with its status, in one queue.",
    "The decision portfolio lists the matched opportunities with the state of their Go/No-Go decision: pending, GO, HOLD or NO-GO. Summary tiles show how many candidates exist and the average decision score, and each row opens the workspace where the decision is made.",
    [
      "Tiles for candidates, GO, HOLD and the average decision score, with the pending and no-go counts beneath them",
      "Filter by minimum match score, and include blocked matches",
      "Each row shows the match, value, deadline and recommendation",
      "Export decisions as CSV",
    ],
  ]),

  shot("bid-decision-detail", "Pursuit", "Go / No-Go decision", "/bid-decisions/{tender}", [
    "A structured way to decide, and a record of why.",
    "The decision workspace scores six dimensions from 0 to 5, asks for the rationale, the key risks and the assumptions, and produces a decision score with a recommendation. The person then records GO, HOLD or NO-GO, and every change lands in the decision history with who made it and when.",
    [
      "Scorecard: strategic fit, capability confidence, commercial attractiveness, delivery feasibility, resource capacity and risk confidence",
      "Decision score out of 100 and a written recommendation",
      "GO, HOLD, NO-GO and Reopen decision",
      "Rationale, key risks and assumptions that flow into the bid brief",
      "Decision history with the person and the date",
    ],
  ], { feature: true }),

  shot("calendar", "Pursuit", "Deadline calendar", "/calendar", [
    "Every submission deadline in one timeline.",
    "The calendar shows the deadlines of the bid portfolio as a month grid, with a side panel for what is coming up. It can be filtered by decision, hide submitted bids, and exported to Outlook, Google or Apple Calendar with built-in reminders.",
    [
      "Month grid and agenda view",
      "Filter by decision: All, GO, HOLD, pending, NO-GO",
      "Hide submitted bids",
      "Add to calendar (.ics) with a reminder one day and two hours before",
      "Summary tiles for upcoming, next 7 days, overdue and submitted",
    ],
  ]),

  shot("calendar-agenda", "Pursuit", "Calendar agenda with task due dates", "/calendar", [
    "Deadlines and the work that leads to them, grouped by day.",
    "Switching to the agenda lists the days in order. With task due dates turned on, the calendar also reads the plans of the active bids and shows each open task on its due day next to the bid deadlines.",
    [
      "Agenda grouped by day",
      "Task due dates switch, remembered per browser",
      "Only my tasks option that uses the same owner matching as the task list",
      "Task due dates are exported to the calendar file as all-day events",
    ],
  ], {
    steps: [
      { click: 'role=radio[name="Agenda"]' },
      { check: 'label:has-text("Task due dates") input[type="checkbox"]' },
      { settle: true },
    ],
  }),

  shot("tasks", "Pursuit", "My tasks", "/tasks", [
    "The open tasks of every active bid in one list, most urgent first.",
    "Instead of opening the planning board of each bid in turn, My tasks gathers the open tasks across bids and groups them by when they are due. It starts with the tasks assigned to the signed-in person, and a task can be ticked off or moved to another status without leaving the page.",
    [
      "Groups: overdue, due today, next 7 days, later, no due date and finished",
      "Starts with your own tasks, or everyone's when none name you",
      "Search, owner, priority and status filters, and CSV export",
      "Tick to mark done, status menu on every row, and a task you just changed stays where it was so it can be undone",
      "Plans that are locked say why, and plans that could not be read are reported with a Try again",
      "Reachable with G then T",
    ],
  ], { feature: true }),

  // ---------------------------------------------------------------------------------------------------
  shot("bid-workspace", "Bid workspace", "Bid workspace overview", "/bid-workspaces/{tender}", [
    "Documents and requirements for one bid, in one workspace.",
    "The preparation workspace is where a bid starts to take shape: the tender documents are uploaded to a private library, and the requirements they impose become a tracked list with an owner, a due date, evidence notes and a source reference. Every bid page shares one section bar.",
    [
      "Section bar: overview, planning, compliance, Q&A, AI analysis, decision and brief",
      "Document library with category and note, private to the organization",
      "Requirements with category, mandatory flag, source document and page reference, due date, owner and evidence",
      "Search, filters and CSV export over the requirements, and an Edit dialog that sends only the fields that changed",
      "Completion and mandatory-gap tiles",
    ],
  ]),

  shot("bid-planning", "Bid workspace", "Planning and submission control", "/bid-workspaces/{tender}/planning", [
    "Tasks, blockers and the submission checklist for one bid.",
    "Planning turns the bid into tasks with an owner, a priority, a milestone and a due date, and watches the numbers that matter: submission readiness, open and overdue tasks and critical items. A submission-control checklist and an authorization step guard the final release.",
    [
      "Add, edit and delete tasks, with a confirmation before anything is deleted",
      "Search and filters, including Assigned to me, and CSV export",
      "Submission control checklist, authorize and mark bid submitted",
      "Planning audit trail",
      "Plans lock automatically after a No-Go decision or a submission",
    ],
  ], { focus: { selector: 'select[aria-label^="Status of task"]', offset: 260 } }),

  shot("bid-planning-board", "Bid workspace", "Task board", "/bid-workspaces/{tender}/planning", [
    "The same tasks as a board, to move work along by hand.",
    "The board shows the tasks as columns: To do, In progress, Blocked, Done and Cancelled. A card is dragged to another column, or moved with the menu on the card, which also works on touch screens and from a keyboard.",
    [
      "Drag and drop between status columns",
      "Menu on each card as the touch and keyboard alternative",
      "List and Board switch that remembers the choice",
      "Read-only when the planning is locked",
    ],
  ], {
    feature: true,
    steps: [{ click: 'role=radio[name="Board"]' }, { waitFor: 'h3:has-text("In progress")' }],
    focus: { selector: 'h3:has-text("In progress")', offset: 230 },
  }),

  shot("bid-compliance", "Bid workspace", "Compliance matrix", "/bid-workspaces/{tender}/compliance", [
    "Every requirement reviewed by a person, with the evidence and the risk.",
    "The compliance matrix lists each tender requirement with a verdict, a risk level and the strength of its evidence. A reviewer writes the rationale and the mitigation, and mandatory blockers are easy to see. The page states that AI is not allowed to make compliance decisions automatically, so the decision stays with a person.",
    [
      "Verdict, risk and evidence strength per requirement",
      "Review rationale and a mitigation action",
      "Filters by verdict and risk, and a mandatory-only switch",
      "Readiness score that feeds the dashboard and the bid brief",
    ],
  ]),

  shot("bid-qa", "Bid workspace", "Ask your bid documents", "/bid-workspaces/{tender}/qa", [
    "Questions answered only from the bid's own documents.",
    "The Q&A page lets the team ask questions about the organization's private tender documents. Answers are generated only from retrieved document evidence and include exact source citations. Retrieval uses only the documents of this bid, and the history keeps the cited excerpts as they were at the time.",
    [
      "Question box over the bid's private documents",
      "Evidence-only answers with exact source citations",
      "Retrieval index status with document and chunk counts and a rebuild action",
      "Private Q&A history with the cited excerpts frozen",
    ],
  ]),

  shot("bid-ai", "Bid workspace", "Document analysis with local AI", "/bid-workspaces/{tender}/ai", [
    "Requirements, deadlines and deliverables pulled out of the documents, locally.",
    "The analysis page runs a local model (through Ollama) over each uploaded document and lists the structured findings: requirements, mandatory points, risks and deadlines. A finding can be added to the requirements list with one click. The documents never leave the environment.",
    [
      "Analyze all documents, or one at a time, and re-analyze",
      "Structured findings with type and confidence",
      "Add to requirements in one click",
      "Runs locally, with the model and the data boundary stated on the page",
    ],
  ]),

  shot("bid-brief", "Bid workspace", "Printable bid brief", "/bid-workspaces/{tender}/brief", [
    "The whole bid on one printable page.",
    "The brief gathers the tender snapshot, the decision scorecard and rationale, key risks and assumptions, compliance readiness, the bid plan and the submission checklist into a single document. Print or Save as PDF produces a clean page without the application chrome.",
    [
      "Tender snapshot, Go/No-Go decision and why",
      "Key risks, assumptions and compliance readiness",
      "Bid plan and submission checklist",
      "Print / Save as PDF with the navigation hidden",
    ],
  ]),

  // ---------------------------------------------------------------------------------------------------
  shot("market", "Market intelligence", "Procurement market explorer", "/market", [
    "How a market is shaped: who buys, who wins, what and where.",
    "The market explorer analyzes the shared public corpus for a chosen slice (a buyer, a country or region, a CPV prefix, a period) and shows its size, concentration, activity trend, top CPVs, top buyers, observed suppliers, geographies and procedures.",
    [
      "Filters for opportunity or buyer, country, region, CPV prefix, status and dates",
      "Tiles for notices, public buyers, observed suppliers, award notices, buyer and supplier concentration, median response time and CPV coverage",
      "Market value, data coverage and an activity trend",
      "Top CPVs, buyers, award suppliers, geographies, procedures and contract nature",
      "Built only from the shared public corpus, never from private bid data",
    ],
  ], { feature: true }),

  shot("buyers", "Market intelligence", "Buyer intelligence", "/buyers", [
    "Public buyers compared by how they actually buy.",
    "Each card summarizes a buyer's notices, awards, median response window and supplier concentration, with labels such as Diversified or Recurring buyer. Search by name and filter by country.",
    [
      "Notices, awards, median response time and supplier concentration per buyer",
      "Behavior labels derived deterministically from public history",
      "Search by buyer name and country filter",
    ],
  ]),

  shot("buyer-detail", "Market intelligence", "Buyer detail", "/buyers/{buyer}", [
    "One buyer's procurement behavior over time.",
    "The buyer page shows how often a buyer publishes, which CPV categories it uses most, which suppliers it awards to and its most recent procurements. A single Watch button follows the buyer from the notifications page.",
    [
      "Procurement behavior signals with the top CPV share and supplier concentration",
      "Publication cadence by month",
      "Frequent CPVs and frequent suppliers, linked to their own pages",
      "Recent procurement and a one-click Watch",
    ],
  ]),

  shot("suppliers", "Market intelligence", "Supplier intelligence", "/suppliers", [
    "Observed public-award relationships, not guesses.",
    "Suppliers are known only from public award notices, and the page says so. Each card shows awards, distinct buyers, top buyer share and top CPV, with a label for how concentrated or diversified the supplier's buyers are.",
    [
      "Awards, buyers, top buyer share and top CPV per supplier",
      "Signals such as Diversified, Focused and Repeat buyer signal",
      "Search by supplier name and country filter",
    ],
  ]),

  shot("supplier-detail", "Market intelligence", "Supplier detail", "/suppliers/{supplier}", [
    "A competitor's public footprint in one page.",
    "The supplier page shows the buyers a supplier wins from, its CPV specialization, the suppliers it is awarded alongside and its recent awards. A note explains that award values are the full contract values and not the supplier's own share.",
    [
      "Award behavior and associated award value",
      "Frequent public buyers and CPV specialization",
      "Co-award relationships with other suppliers",
      "Recent observed awards",
    ],
  ]),

  shot("intelligence", "Market intelligence", "Intelligence hub: data quality", "/intelligence", [
    "How trustworthy the data behind every score is.",
    "Source-neutral quality scoring detects missing classifications, geography, deadlines, provenance and inconsistent award-stage data before matching and analytics use the catalogue, and rolls the result up into a distribution from Excellent to Critical. Every deduction is a structured, explainable issue rather than an opaque score.",
    [
      "Average quality score and the Excellent, Good, Fair, Poor and Critical distribution",
      "Findings grouped as critical issues, warnings and information gaps",
      "Recalculate the catalogue on demand",
      "Links on to decisions, matching and search",
    ],
  ]),

  // ---------------------------------------------------------------------------------------------------
  shot("data-operations", "Operations", "Data operations", "/data-operations", [
    "Ingestion health for the people who run the system.",
    "The operations page shows whether TED, BOAMP and DECP are healthy, how reliable recent ingestion runs were and what needs attention, so a failing source is found by looking at one page instead of reading logs.",
    [
      "Status of each source with success rate and last success",
      "Recent ingestion runs with records imported and rejected",
      "Refresh health on demand",
    ],
  ]),

  shot("data-sources", "Operations", "Data sources", "/data-sources", [
    "The three public sources feeding the shared corpus.",
    "Each source has its own panel: what it is, how it connects, its latest ingestion run, and a manual sync with a choice of time window and maximum number of records. The page keeps tender and award semantics separate, as the sources themselves do.",
    [
      "TED, BOAMP and DECP, each with its connection details and last run",
      "Run a sync by hand with a publication window and a record limit",
      "Tender data and award data kept apart",
    ],
  ]),

  // ---------------------------------------------------------------------------------------------------
  shot("company", "Account", "Company profile", "/company", [
    "The private profile that drives matching.",
    "The company profile holds what the matching engine compares tenders against: capabilities, operating regions, target CPV categories, certifications, reference projects, preferred buyer types and keywords to exclude. A completeness score says how ready the profile is, and saving it points to the matches that need recalculating.",
    [
      "Profile completeness score by section",
      "Capabilities, regions, target CPV codes, certifications and references",
      "Preferred buyer types and excluded keywords",
      "Private to the organization",
    ],
  ]),

  shot("organization", "Account", "Organization", "/organization", [
    "The organization and its members.",
    "The organization page shows the tenant the signed-in user works in and lists its members with their roles: owner, bid manager, analyst and contributor. Private application data is scoped to the organization in the sign-in token, and member administration is limited to the owner and bid manager roles.",
    [
      "Organization and tenant details",
      "Members with their roles",
      "Tenant isolation and the role rule stated on the page",
    ],
  ]),

  shot("settings", "Account", "Preferences", "/settings", [
    "Theme, density, motion and shortcuts, kept in the browser.",
    "Preferences are personal display settings: light, dark or follow the system, a compact density for data-heavy work, and reduced motion, which also follows the operating system. The page lists every keyboard shortcut and can clear recent items or reset everything.",
    [
      "Light, dark and system themes, applied before the first paint so there is no flash",
      "Comfortable or compact layout density",
      "Reduced-motion switch that also honours the system setting",
      "Keyboard shortcut reference, clear recently viewed, reset all preferences",
    ],
  ]),

  // ---------------------------------------------------------------------------------------------------
  shot("palette", "Around the app", "Command palette", "/dashboard", [
    "Press Cmd+K (or Ctrl+K, or /) anywhere to go anywhere.",
    "The palette searches pages, actions, recently viewed items and live opportunities from one box, and is fully usable from the keyboard. Here a search for the word task offers a full-text search and the My tasks page.",
    [
      "Pages, actions, recent items and live opportunity results",
      "Arrow keys and Enter, Escape to close",
      "Opens from the top bar search field too",
    ],
  ], {
    feature: true,
    variants: OVERLAY,
    full: false,
    scope: "[role=dialog]",
    steps: [{ press: "Control+K" }, { waitFor: "role=dialog" }, { type: "task" }, { wait: 800 }],
  }),

  shot("shortcuts", "Around the app", "Keyboard shortcuts", "/dashboard", [
    "Work without leaving the keyboard.",
    "Pressing ? opens the list of shortcuts: the palette, search, and the go-to sequences, such as G then D for the dashboard or G then T for My tasks. Shortcuts are ignored while typing in a field.",
    [
      "Press ? for this help, / to search, Esc to close",
      "G followed by a letter goes to a page",
      "Ignored while typing in a field",
    ],
  ], { variants: OVERLAY, full: false, scope: "[role=dialog]", steps: [{ press: "Shift+?" }, { waitFor: "role=dialog" }] }),

  shot("account-menu", "Around the app", "Account menu", "/dashboard", [
    "Who you are, which organization, and the theme, in one menu.",
    "The account menu shows the signed-in person and role, a three-way theme switch, and links to preferences, the workspace map and the keyboard shortcuts, plus Sign out.",
    [
      "Name, email, organization and role",
      "Light, dark and automatic theme switch",
      "Preferences, workspace map, keyboard shortcuts and sign out",
    ],
  ], { variants: OVERLAY, full: false, scope: '[aria-label="Account"]', steps: [{ click: 'role=button[name="Account menu"]' }, { wait: 300 }] }),

  shot("mobile-menu", "Around the app", "Phone navigation", "/dashboard", [
    "The same navigation, as a drawer on a phone.",
    "On a narrow screen the sidebar becomes a drawer opened from the top bar, with the same groups and the unread-alerts badge. Every page also has its own phone layout.",
    [
      "Drawer with the same five groups",
      "Unread alerts badge",
      "A close button in the drawer header",
    ],
  ], { variants: ["phone-light"], full: false, scope: '[aria-label="Navigation"]', steps: [{ click: 'role=button[name="Open navigation"]' }, { wait: 400 }] }),

  shot("not-found", "Around the app", "Page not found", "/this-page-does-not-exist", [
    "Even a wrong address leads somewhere useful.",
    "An unknown address shows a branded 404 page with two ways back: the dashboard and the product map.",
    ["Go to dashboard", "Open product map"],
  ], { allowErrorText: true, full: false }),

  shot("offline", "Around the app", "Connection lost", "/dashboard", [
    "A dropped connection does not sign you out.",
    "When the server cannot be reached, TenderPilot says so, says that the person has not been signed out, retries every few seconds and when the connection returns, and opens the workspace again by itself.",
    [
      "Clear message instead of the sign-in page",
      "Automatic retry, and a Try again button",
      "A failed refresh only ends the session when the server answers 401 or 403",
      "An offline strip appears under the top bar while the browser is offline",
    ],
  ], { net: "offline", allowErrorText: true, full: false }),
];
