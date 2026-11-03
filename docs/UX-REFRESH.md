# TenderPilot UX refresh

A frontend-only update. The backend stays frozen at **v1.0.0** (Liquibase 001–015,
no API changes), and every string the release gates look for is still in place.

## What's new

**Design system**
- Semantic colour tokens (`bg-surface`, `text-ink`, `border-line`, `bg-brand-soft` ...) in
  `frontend/src/app/globals.css`, flipped by a `.dark` class on `<html>`.
- Light, dark and system themes. The choice is applied before first paint, so there is no flash.
- Compact/comfortable density and a reduced-motion switch (also honours the OS setting).
- Shared UI primitives in `frontend/src/components/ui/` (Button, Badge, Card, Dialog, Toast,
  EmptyState, Skeleton, Progress/RingGauge, Sparkline/BarList/Funnel, SegmentedControl, fields, pagination).

**App shell**
- Grouped, collapsible sidebar with an unread-notifications badge, and a mobile drawer.
- Top bar with breadcrumbs, search, theme toggle, notification bell and user menu.
- Skip-to-content link, focus rings, ARIA on menus and dialogs.
- Branded 404 and error pages (global, per-route and workspace).

**Command palette and shortcuts** (`⌘K` / `Ctrl+K` or `/`)
- Searches pages, actions, recent items and live opportunities (`/api/v1/search/opportunities`).

| Keys | Action |
| --- | --- |
| `⌘/Ctrl + K` or `/` | Open the palette |
| `?` | Show all shortcuts |
| `g` then `d` `o` `s` `m` `b` `c` `n` `p` `w` | Go to Dashboard, Opportunities, Search, Matches, Bid decisions, Calendar, Notifications, Company profile, Workspace map |
| `Esc` | Close dialogs and menus |

**New features**
- **Deadline calendar** (`/calendar`): month grid and agenda view, filter by decision, hide submitted,
  summary stats, and `.ics` export with 1-day and 2-hour reminders.
- **Printable bid brief** (`/bid-workspaces/{id}/brief`): tender snapshot, decision scorecard,
  compliance, plan and submission checklist on one page. Use the browser's *Print / Save as PDF*.
  Linked from the bid decision page.
- **Opportunities**: card/table toggle, debounced search, filters (status, source, minimum quality,
  open only), sorting, pagination, filter chips, CSV export, **saved views**.
- **CSV export** for opportunities, matches and bid decisions.
- **Saved views on Matches**: save the grade, minimum score and "include blocked" filters under a name and
  re-apply them in one click (stored in your browser, like the Opportunities and Search views).
- **Opportunity quick actions**: open the Go/No-Go assessment (with the match score), watch the tender,
  jump to buyer intelligence, add the deadline to your calendar (.ics), copy link, print. The Go/No-Go
  button is enabled only when the tender is in your match list, because the backend builds that workspace
  from a match.
- **One-click Watch buttons** for tenders, buyers and CPV codes (opportunity page, each CPV card and the
  buyer page). They use the existing watchlist API, so no pasted UUID is needed, and the Watching state is
  shared with the notifications page. CPV codes are sent without the check digit (`72000000-5` becomes
  `72000000`) because the API accepts 1 to 8 digits only.
- **Compare opportunities**: tick up to 3 cards or table rows on the Opportunities page, then open a
  side-by-side dialog with deadline, value, quality, lots, sources and CPV codes. The strongest value in a
  row is marked "Best", and CPV codes shared by several tenders are highlighted.
- **Search**: *Show more results* steps through 25, 50 and 100 hits (the backend returns at most 100 per
  request), and **saved searches** keep a query plus filters in your browser.
- **Dashboard insights**: dismissible onboarding checklist (driven by your real data), pipeline funnel
  and upcoming deadlines.
- **Match freshness**: matches are stored and only change when someone recalculates, so the Matches page
  says when the company profile is missing or was edited after the last calculation, and saving the
  company profile points to Matches.
- **Links that open filtered lists**: Opportunities, Search, Buyers, Suppliers and Matches read their
  starting filters from the address, for example `/search?q=72000000`,
  `/matches?grade=EXCELLENT&minScore=70` or `/opportunities?status=PUBLISHED&open=1`. Values that are not
  allowed are ignored. Search has a *Copy link* button. Pages read the address with Next's
  `useSearchParams`, and one `QueryBoundary` (`features/navigation/query-boundary.tsx`) restarts a page when
  the address changes.
- **Fewer dead ends**: frequent suppliers on a buyer page and awarded suppliers on an opportunity open the
  supplier profile (the supplier key is the cleaned-up name in URL-safe Base64, the same as the backend
  builds it), CPV codes have a *Similar* link to Search, and every bid workspace page shares one section
  bar (overview, planning, compliance, Q&A, AI analysis, decision, brief).
- **Phones**: a grid without a column setting no longer pushes a page sideways (one base rule in
  `globals.css`).
- **Bid task board**: the Bid tasks card on the planning page has a *List / Board* switch (remembered in
  your browser). The board shows To do, In progress, Blocked, Done and Cancelled as columns. Drag a card
  to another column, or use the menu on the card, which is the way to move cards on touch screens and with
  a keyboard. Moves use the existing task update call, so locked planning (a No-Go decision or a submitted
  bid) keeps the board read-only. The demo API remembers moves, and `MOCK_LOCKED=1` shows the locked state.
- **Tasks that need attention** (Dashboard): the dashboard already counted overdue, blocked and critical
  tasks; this card lists the tasks themselves, with owner, due date and a link to the bid's planning page.
  It reads the planning workspaces of the six most urgent bids (at most six requests, read-only), shows the
  six most severe tasks (overdue first, then blocked, then critical), says when more exist, and stays
  hidden until a bid has tasks. Bids with a No-Go decision or a submitted bid are left out.
- **Edit a task**: every task on the planning page (list and board) has an Edit button. The dialog changes
  the title, description, priority, milestone, owner and due date, and sends only the fields that changed.
  An empty description or owner clears it; a due date can be moved but not removed (the server keeps the
  old one when the field is left out). Status stays on the list menu and the board.
- **Edit a requirement**: every requirement on the bid workspace page has an Edit button. The dialog changes
  the title, description, category, mandatory flag, source document and reference, due date, owner and
  evidence notes, and sends only the fields that changed. An empty text clears a field; a due date or
  a source document can be changed but not removed (the server keeps the old one when the field is left
  out). The requirement card now shows its owner and evidence notes too. Status stays on its own menu.
- **Find and export tasks and requirements**: the planning page (above the list and the board) and the bid
  workspace page (above the requirement list) have a search box and filters (owner including "Assigned to
  me", priority or status, category, open or mandatory only), a "Showing X of Y" line with Clear filters,
  and an Export CSV button that downloads what is shown (`lib/csv.ts`). Owners are free text on the server,
  so "Assigned to me" matches the signed-in person's full name or email, ignoring case and extra spaces
  (`lib/owner.ts`). Filters are not remembered between visits.
- **Task due dates on the calendar**: a "Task due dates" switch on the Deadline calendar reads the plans of
  the active bids (not submitted, not No-Go, with open tasks; up to 30 bids, nearest deadline first) and shows
  each open task on its due day next to the bid deadlines: dashed chips in the month grid, a "Tasks due"
  list in the side panel, and the agenda groups. The decision filter applies to tasks too and "Only my
  tasks" uses the same owner matching as the task filters. The .ics export adds the tasks as all-day events
  with a reminder at 9:00 the day before (`allDay` in `lib/ics.ts`). The switch is remembered per browser.
- **Connection problems no longer sign you out**: a request that cannot reach the server now fails with a
  `NetworkError` ("You are offline..." or "TenderPilot could not be reached...") instead of the browser's
  "Failed to fetch". Opening the app while the server is down shows a "TenderPilot cannot be reached" screen
  (it retries every 10 seconds and when the browser comes back online) instead of the sign-in page, and a
  failed token refresh only ends the session when the server answers 401 or 403, so a dropped connection or
  a 5xx no longer discards what was on screen. An "You are offline" strip appears under the top bar while the
  browser reports it is offline.
- **Names and tab titles**: every search box, filter menu and form field on the Notifications, Search, Matches,
  Bid decisions, Bid workspace, Planning, Compliance, Q&A, Market, Buyers and Suppliers pages now has an
  accessible name (a screen reader used to read only "edit text" or "combo box"), and the status menu on each
  requirement and task says which one it changes. Each page sets its own browser tab title from its place in the
  navigation ("Planning, Bid workspace | TenderPilot") with the unread alert count in front ("(3) Calendar"), and
  the Q&A and AI pages read "Q&A" and "AI analysis" in the breadcrumb instead of "Qa" and "Ai".
- **Sign-in returns you where you were**: opening a link to a page while signed out, or losing the session in
  the middle of work, now leads to `/login?next=<page>` and signing in (or creating an account) goes back to
  that page instead of the dashboard (`features/auth/return-to.ts`; only paths inside the app are accepted).
  A session that ended by itself adds `reason=expired`, which shows "Your session has ended" on the sign-in
  page, while signing out on purpose goes to a plain sign-in page. Opening `/login` or `/register` while signed
  in goes straight to the app, and the links between the two pages keep `next`.
- **My tasks** (`/tasks`, shortcut G then T): the open tasks of all active bids in one list, grouped as Overdue,
  Due today, Next 7 days, Later and No due date. It reads the planning workspace of each bid like the calendar
  does (at most 30 bids, one request each) and starts with the tasks that name you as owner, or everyone's when
  none do. Search, owner, priority and status filters, CSV export, a tick to mark a task done and a status menu
  on each row (a saved change keeps the row in the list until you reload, so it can be undone). Rows link to
  the bid's planning board. The dashboard's "Tasks that need attention" card links to it.
- **Portfolio kit** (`./scripts/capture-portfolio.sh`): builds the demo, opens every page and many interface states
  (dialogs, the command palette, menus, the phone drawer, the offline screen) in light, dark and phone views, and
  writes the pictures with an explanation of each into `portfolio/`: `index.html` is a gallery, `README.md` and
  `pages/` hold the texts, `portfolio.json` has the same data, and `CASE-STUDY.md` is a write-up that is written once
  and never overwritten, so it can be edited. The texts live in `scripts/portfolio/catalog.mjs`, and
  `--docs-only` rewrites the Markdown and the gallery from them without taking new pictures. It uses the demo
  data only, so nothing real is in the pictures, and it installs its one dependency (`playwright-core`) into
  `portfolio/.tools`, not into the frontend. The folder holds a `.gitignore` that ignores everything in it, because
  the pictures are large and an untracked folder would make the release gates report a dirty tree.
- **Confirm before deleting**: deleting a task, a bid document or a requirement now asks first
  (`components/ui/confirm-dialog.tsx`, focus starts on Cancel, Escape cancels). The delete buttons also
  have accessible names.
- **Alert counts follow what you see**: after an alert is dismissed and alerts are refreshed, the server
  counts it as active again while it stays dismissed. The bell and the Notifications tiles now count
  the alerts that are actually shown, and both follow what is done on the Notifications page
  straight away (before, they waited for the next two-minute refresh).
- **Notifications** are polled every 2 minutes; acknowledge or mark all read from the bell.
- **Recently viewed** items surface in the palette.
- **Settings** (`/settings`): theme, density, motion, shortcuts reference, account details, clear
  recents, reset all local preferences.
- **Auth pages**: password show/hide and a strength meter on registration (the 10-character rule is unchanged).

## Running it

```bash
# Real stack, as before
./scripts/run-backend.sh
./scripts/run-frontend.sh

# UI only, no backend / Postgres / OpenSearch / Ollama (synthetic data, any login works)
./scripts/run-frontend-demo.sh
MOCK_FRESH=1 ./scripts/run-frontend-demo.sh      # brand-new account, shows onboarding

# Pictures and texts for a portfolio (demo data), written to ./portfolio
./scripts/capture-portfolio.sh

# Quality gates
./scripts/frontend-check.sh   # dark theme freshness + lint + tsc + production build
./scripts/check.sh            # your existing full gate (backend tests, lint, tsc, build, compose)
```

## Theming guide

- **New components:** use the semantic tokens (`bg-surface`, `bg-surface-2`, `text-ink`, `text-muted`,
  `border-line`, `bg-brand-soft`, `text-success`, `bg-danger-soft` ...). They adapt to dark mode on their own.
- **Existing pages** still use Tailwind colour classes (`bg-white`, `text-slate-500` ...). Those are
  mapped to a dark palette by `frontend/src/app/theme-dark.css`, which is **generated**:
  ```bash
  node scripts/generate-dark-theme.mjs
  ```
  Re-run it after adding classes such as `bg-teal-50`. `frontend-check.sh` does it for you and tells you
  if the file changed. Do not edit `theme-dark.css` by hand.
- Panels that should stay navy in both themes get the class `tp-dark-surface`.
- The capture route (`/showcase/capture`) is always light and is intentionally untouched.

## Where things live

| Area | Path |
| --- | --- |
| Tokens, motion, print rules | `frontend/src/app/globals.css` |
| Theme provider and no-flash script | `frontend/src/features/theme/` |
| UI primitives | `frontend/src/components/ui/` |
| Shell (sidebar, top bar, drawer, user menu) | `frontend/src/components/shell/` |
| Navigation registry, palette, shortcuts | `frontend/src/features/navigation/` |
| Notifications context and bell | `frontend/src/features/notifications/` |
| Calendar model and page | `frontend/src/features/calendar/`, `frontend/src/app/(workspace)/calendar/` |
| Helpers: CSV, ICS, storage, relative time | `frontend/src/lib/` |
| Demo API | `scripts/mock-api.mjs`, `scripts/mock-api-server.mjs` |
| Portfolio capture | `scripts/capture-portfolio.sh`, `scripts/portfolio/` (what is captured and the texts: `catalog.mjs`) |

Browser-side preferences (theme, density, sidebar state, saved views, recents) live in `localStorage`
under the keys in `frontend/src/lib/storage-keys.ts`. *Settings → Reset* removes exactly those.

## Release-gate compatibility

The v1 freeze contract is unchanged: the navigation registry keeps its quoted routes, `template.tsx`
still mounts `<ProductExperienceLayer />`, the palette keeps `metaKey`, `ctrlKey`, `Escape` and
"Search TenderPilot", the dashboard and showcase pages keep their links, and the capture page, showcase
feature, backend and gate scripts are not modified.

## Deliberately not done (needs backend work, so it waits for v1.1)

The backend is frozen, so these need new endpoints and are left out rather than faked:
- Team invitations and member management (the Organization page lists members read-only).
- Change password and profile editing (Settings shows account details read-only).
- Server-side saved views and per-user notification preferences (saved views are per browser).
- Search results beyond the first 100 hits (the search endpoint has no offset or page parameter).
- One endpoint for tasks across all bids (the dashboard's attention list reads the plans of the six most urgent bids and the calendar reads up to 30).
