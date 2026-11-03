# TenderPilot

Procurement intelligence for public tenders. TenderPilot collects public procurement notices,
scores them against your company profile, and walks a bid team from "is this worth it?" to a
submitted bid.

- **Find**: opportunities from TED, BOAMP and DECP in one canonical model, with full-text search
  (OpenSearch, with a PostgreSQL fallback), saved views, comparison and a deadline calendar.
- **Decide**: company-to-tender matching with explained scores, and a Go / No-Go workspace per tender.
- **Bid**: planning tasks, compliance checks, a Q&A log, a printable bid brief and an optional local AI assistant.
- **Understand the market**: buyer and supplier intelligence, market analytics, notifications and watchlists.
- **Run it**: data-source and ingestion monitoring for the people who operate the system.

## Stack

| Part | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1, Liquibase (15 migrations), Maven |
| Data | PostgreSQL 17 (source of truth), OpenSearch 3.8.0 (search projection, rebuildable) |
| Local AI (optional) | Ollama, models `qwen3:4b` and `nomic-embed-text-v2-moe` by default |
| Frontend | Next.js 16.3.3 (App Router, Turbopack), React 19.2.8, TypeScript, Tailwind CSS 4 |

## Quick start

Everything is started from `./scripts`. Run `./scripts/doctor.sh` first to check that Java, Maven,
Node, npm, Git, Docker and Docker Compose are installed.

### UI only, no backend needed

```bash
./scripts/run-frontend-demo.sh
```

Starts the frontend against a synthetic demo API (any login works). Use `MOCK_FRESH=1` for a
brand-new account with the onboarding checklist, `MOCK_MORE=1` to see Search's "Show more
results" button, `MOCK_STALE=1` to see the out-of-date matches banner, and `MOCK_LOCKED=1` to see locked bid planning. The demo data is fake and exists only for design work and screenshots.

### Full stack

```bash
cd frontend && npm install && cd ..   # first time only
./scripts/infra-up.sh                 # PostgreSQL and OpenSearch in Docker
./scripts/run-backend.sh              # API on http://localhost:8080
./scripts/run-frontend.sh             # UI on http://localhost:3000
```

Open http://localhost:3000, create an account on the register page and sign in. Roles are OWNER,
BID_MANAGER, ANALYST and CONTRIBUTOR.

| Script | What it does |
| --- | --- |
| `./scripts/status.sh` | Checks the backend, the system contract, OpenSearch and the frontend |
| `./scripts/infra-down.sh` | Stops the Docker services (data stays in Docker volumes) |
| `./scripts/prepare-local-ai.sh` | Pulls the Ollama models used by the AI assistant (Ollama must be installed and running) |
| `./scripts/capture-portfolio.sh` | Takes pictures of every screen on the demo data and writes them, with an explanation of each, into `portfolio/` |

Default local ports: frontend 3000, backend 8080, PostgreSQL 55432, OpenSearch 19200, Ollama 11434.

## Quality checks

Checks run on your machine. There is no CI/CD pipeline by design.

| Command | What it runs |
| --- | --- |
| `./scripts/frontend-check.sh` | Dark-theme freshness, ESLint, TypeScript, production build |
| `./scripts/check.sh` | Shell syntax, backend tests, frontend lint, TypeScript and build, Docker Compose validation |
| `./scripts/release-gate.sh` | Local release gate. Use `TENDERPILOT_RELEASE_ALLOW_DIRTY=1` during development |
| `./scripts/v1-readiness-gate.sh` | Final v1.0 certification. Use `TENDERPILOT_V1_ALLOW_DIRTY=1 TENDERPILOT_V1_REQUIRE_TAG=0` during development |

## Configuration

`run-backend.sh` and `run-frontend.sh` set sensible local defaults. Override any of these with
environment variables.

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:55432/tenderpilot`, `tenderpilot`, `tenderpilot_dev` | PostgreSQL connection |
| `OPENSEARCH_URL` | `http://localhost:19200` | Search projection |
| `OLLAMA_URL` | `http://localhost:11434` | Local AI |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Origins allowed to call the API |
| `ACCESS_TOKEN_SECRET` | development value | Signs access tokens. Change it before any real deployment |
| `ACCESS_TOKEN_TTL`, `REFRESH_TOKEN_TTL` | `PT15M`, `P7D` | Token lifetimes |
| `REFRESH_COOKIE_SECURE` | `false` | Set to `true` behind HTTPS |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | API address used by the browser |

The defaults are for local development only. Do not reuse the database password or token secret anywhere else.

## Repository layout

```text
backend/    Spring Boot API, one package per module under src/main/java/com/tenderpilot
            (procurement, matching, biddecision, bidplanning, bidworkspace, compliance, buyerintelligence,
             supplierintelligence, marketanalytics, notification, search, rag, localai, datasource, ...)
frontend/   Next.js app
  src/app/          routes: (auth), (workspace), (capture)
  src/features/     per-domain types and logic (matching, notifications, saved-views, procurement, ...)
  src/components/   shell, UI primitives, shared panels
scripts/    run, check and release scripts, the demo API and the portfolio capture
docs/       UX-REFRESH.md: design system, shortcuts, theming guide, release-gate compatibility
release/    v1.0 release manifest used by the gates
compose.yaml  PostgreSQL and OpenSearch for local development
```

## Using the app

- Press `Cmd+K` or `Ctrl+K` (or `/`) anywhere for the command palette, and `?` for all shortcuts.
- Light, dark and system themes and a compact density are under Settings.
- Saved views and recent items are stored in your browser, not on the server.
- Search, Opportunities, Matches, Buyers and Suppliers open pre-filtered from links, for example
  `/search?q=72000000` or `/matches?grade=EXCELLENT`. Search has a *Copy link* button.
- The portfolio showcase at `/showcase` and its capture mode at `/showcase/capture` use a synthetic dataset.
- `./scripts/capture-portfolio.sh` builds the demo, opens every page in light, dark and phone views and writes the
  pictures, with an explanation of each, into `portfolio/`. Open `portfolio/index.html` to look through them.
  Git ignores that folder, so the release gates stay clean (`git add -f portfolio` to commit it).

## Release status

v1.0.0 is certified by the local gates (`release/tenderpilot-v1-release.json`). The backend is
feature-frozen at v1.0.0, so features that need new endpoints belong in v1.1. See
`docs/UX-REFRESH.md` for what the frontend adds, what the gates expect it to keep, and what was
deliberately left out.
