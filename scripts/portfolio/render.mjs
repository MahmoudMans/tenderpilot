#!/usr/bin/env node
/**
 * Writes the readable part of the portfolio folder from portfolio.json and the texts in catalog.mjs:
 *   README.md, index.html (a gallery you can open in a browser), pages/<nn>-<id>.md for each screen,
 *   and CASE-STUDY.md, which is only written when it does not exist, so your edits are never lost.
 *
 *   node scripts/portfolio/render.mjs [portfolio-folder]     (or: ./scripts/capture-portfolio.sh --docs-only)
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

import { PROFILES, SHOTS } from "./catalog.mjs";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(HERE, "..", "..");

const SAVED = new Map(SHOTS.map((shot) => [shot.id, shot]));
const TEXT_KEYS = ["section", "title", "tagline", "description", "highlights", "feature"];

function withCurrentText(record) {
  const current = SAVED.get(record.id);
  if (!current) return record;
  const merged = { ...record };
  for (const key of TEXT_KEYS) if (current[key] !== undefined) merged[key] = current[key];
  return merged;
}

const pageFile = (shot) => String(shot.number).padStart(2, "0") + "-" + shot.id + ".md";

function pictureLabel(image) {
  const profile = PROFILES[image.profile] || { label: image.profile, short: image.profile };
  return image.kind === "full" ? profile.short + ", full page" : profile.short;
}

function longPictureLabel(image) {
  const profile = PROFILES[image.profile] || { label: image.profile };
  return profile.label + (image.kind === "full" ? ", whole page" : "");
}

function sizeText(bytes) {
  return bytes >= 1048576 ? (bytes / 1048576).toFixed(1) + " MB" : Math.max(1, Math.round(bytes / 1024)) + " KB";
}

const sentence = (text) => (/[.!?]$/.test(text) ? text : text + ".");

function caption(shot) {
  const base = "TenderPilot - " + shot.title + ": " + sentence(shot.tagline);
  return base.length > 220 ? base.slice(0, 217) + "..." : base;
}

function sectionsInOrder(shots) {
  const order = [];
  for (const shot of shots) if (!order.includes(shot.section)) order.push(shot.section);
  return order.map((name) => ({ name, shots: shots.filter((shot) => shot.section === name) }));
}

function onScreenLines(shot) {
  const screen = shot.onScreen;
  if (!screen) return [];
  const lines = [];
  if (screen.heading) lines.push("Page heading: " + screen.heading);
  if (screen.sections && screen.sections.length) lines.push("Sections: " + screen.sections.join(", "));
  if (screen.actions && screen.actions.length) lines.push("Buttons: " + screen.actions.join(", "));
  if (screen.controls && screen.controls.length) lines.push("Fields and filters: " + screen.controls.join(", "));
  return lines;
}

// ---------------------------------------------------------------------------------------------------------
// One Markdown page per screen
// ---------------------------------------------------------------------------------------------------------
function pageMarkdown(shot) {
  const heroes = shot.images.filter((image) => image.kind === "hero");
  const lines = [];
  lines.push("# " + shot.title, "");
  lines.push("*" + shot.section + "* | address `" + shot.route + "` | screen " + String(shot.number).padStart(2, "0"), "");
  if (heroes[0]) lines.push("![" + shot.title + ", " + longPictureLabel(heroes[0]).toLowerCase() + "](../" + heroes[0].file + ")", "");
  lines.push("**" + sentence(shot.tagline) + "**", "");
  lines.push(shot.description, "");
  if (shot.highlights && shot.highlights.length) {
    lines.push("## What it does", "");
    for (const item of shot.highlights) lines.push("- " + item);
    lines.push("");
  }
  lines.push("## Pictures", "");
  for (const image of shot.images) {
    lines.push("### " + longPictureLabel(image), "");
    lines.push("![" + shot.title + ", " + longPictureLabel(image).toLowerCase() + "](../" + image.file + ")", "");
    lines.push("`" + image.file + "` - " + image.width + " x " + image.height + " px, " + sizeText(image.bytes), "");
  }
  const seen = onScreenLines(shot);
  if (seen.length) {
    lines.push("## On this screen", "");
    for (const line of seen) lines.push("- " + line);
    lines.push("", "_Read from the running demo app when the pictures were taken._", "");
  }
  lines.push("## Caption for your portfolio", "", "> " + caption(shot), "");
  return lines.join("\n");
}

// ---------------------------------------------------------------------------------------------------------
// README
// ---------------------------------------------------------------------------------------------------------
function readmeMarkdown(data, shots) {
  const date = data.generatedAt.slice(0, 10);
  const imageCount = shots.reduce((sum, shot) => sum + shot.images.length, 0);
  const bytes = shots.reduce((sum, shot) => sum + shot.images.reduce((s, image) => s + image.bytes, 0), 0);
  const lines = [];
  lines.push("# TenderPilot - portfolio kit", "");
  lines.push(
    "Screenshots and explanations of the whole TenderPilot interface, ready to use in a portfolio. " +
      "Taken on " + date + " from the demo app, which runs on **synthetic data**: no real tender, buyer or company information appears in any picture.",
    "",
  );
  lines.push(shots.length + " screens, " + imageCount + " pictures, " + sizeText(bytes) + ".", "");
  lines.push("## What is in this folder", "");
  lines.push("| Path | What it is |", "| --- | --- |");
  lines.push("| `index.html` | A gallery of everything. Open it in a browser to look through or to pick pictures. |");
  lines.push("| `CASE-STUDY.md` | A write-up of the project. It is yours to edit and is never overwritten. |");
  lines.push("| `pages/` | One Markdown page per screen: picture, explanation, features, caption. |");
  lines.push("| `screenshots/NN-name/` | The pictures. `desktop-light.png` is 1440 x 900, `desktop-dark.png` the same in the dark theme, `phone-light.png` a 390 x 844 phone at 2x, and the `-full` files show the whole page. |");
  lines.push("| `showcase-frames/` | The six fixed 1440 x 900 frames of the built-in capture mode. |");
  lines.push("| `portfolio.json` | All of it as data (titles, texts, files, sizes), for a site generator or a script. |");
  lines.push("");

  const featured = shots.filter((shot) => shot.feature);
  if (featured.length) {
    lines.push("## The best pictures to lead with", "");
    for (const shot of featured) {
      const hero = shot.images.find((image) => image.kind === "hero");
      if (!hero) continue;
      lines.push("### " + shot.title, "");
      lines.push("![" + shot.title + "](" + hero.file + ")", "");
      lines.push(sentence(shot.tagline) + " [Read more](pages/" + pageFile(shot) + ")", "");
    }
  }

  lines.push("## All screens", "");
  for (const section of sectionsInOrder(shots)) {
    lines.push("### " + section.name, "");
    lines.push("| Screen | What it shows |", "| --- | --- |");
    for (const shot of section.shots) {
      lines.push("| [" + shot.title + "](pages/" + pageFile(shot) + ") | " + sentence(shot.tagline).replace(/\|/g, "/") + " |");
    }
    lines.push("");
  }

  lines.push("## Using the pictures", "");
  lines.push("- For a project card or a hero image, use the `desktop-light.png` files: they are 16:10 and crop nothing.");
  lines.push("- To show that the interface adapts, put `desktop-light.png`, `desktop-dark.png` and `phone-light.png` of the same screen side by side.");
  lines.push("- The `-full` files show a page from top to bottom. Pages taller than 6,000 px (desktop) or 3,500 px (phone) are cut there.");
  lines.push("- Every page in `pages/` ends with a caption you can paste under the picture.");
  lines.push("- The demo data is fictional, so the pictures can be published. Check that before you capture a real backend.");
  lines.push("- Git ignores this folder, so copy it where you keep your portfolio (leave out `.tools`, which only holds the screenshot tool), or commit it with `git add -f portfolio`.");
  lines.push("");
  lines.push("## Taking the pictures again", "");
  lines.push("```bash");
  lines.push("./scripts/capture-portfolio.sh                 # everything, about 5 to 10 minutes");
  lines.push("TP_ONLY=dashboard,tasks ./scripts/capture-portfolio.sh   # only some screens");
  lines.push("TP_SCALE=2 ./scripts/capture-portfolio.sh      # sharper desktop pictures (larger files)");
  lines.push("./scripts/capture-portfolio.sh --docs-only     # rewrite the texts only, without new pictures");
  lines.push("```", "");
  lines.push("The texts live in `scripts/portfolio/catalog.mjs`. This README, `index.html`, `pages/` and `screenshots/` are rewritten on every run, so copy what you want to change somewhere else first.");
  lines.push("");
  if (data.skipped && data.skipped.length) {
    lines.push("## Not captured", "");
    for (const item of data.skipped) lines.push("- " + item.title + ": " + item.reason);
    lines.push("");
  }
  return lines.join("\n");
}

// ---------------------------------------------------------------------------------------------------------
// Gallery
// ---------------------------------------------------------------------------------------------------------
const esc = (text) =>
  String(text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");

const encodePath = (file) => file.split("/").map(encodeURIComponent).join("/");

function galleryHtml(data, shots) {
  const date = data.generatedAt.slice(0, 10);
  const sections = sectionsInOrder(shots);
  const featured = shots.filter((shot) => shot.feature);

  const card = (shot) => {
    const first = shot.images[0];
    const tabs = shot.images
      .map(
        (image, index) =>
          '<button type="button" role="tab" aria-selected="' + (index === 0) + '" data-src="' + esc(encodePath(image.file)) + '" data-w="' + image.width + '" data-h="' + image.height + '">' + esc(pictureLabel(image)) + "</button>",
      )
      .join("");
    const seen = onScreenLines(shot);
    const lists = (shot.highlights || []).map((item) => "<li>" + esc(item) + "</li>").join("");
    return [
      '<article class="shot" id="' + esc(shot.id) + '">',
      '<div class="shot-text">',
      '<p class="kicker">' + esc(shot.section) + " &middot; <code>" + esc(shot.route) + "</code></p>",
      "<h3>" + esc(shot.title) + "</h3>",
      '<p class="tagline">' + esc(sentence(shot.tagline)) + "</p>",
      "<p>" + esc(shot.description) + "</p>",
      lists ? "<ul>" + lists + "</ul>" : "",
      seen.length ? '<details><summary>On this screen</summary><ul class="seen">' + seen.map((line) => "<li>" + esc(line) + "</li>").join("") + "</ul></details>" : "",
      '<p class="cap"><strong>Caption:</strong> ' + esc(caption(shot)) + "</p>",
      '<p class="links"><a href="pages/' + esc(pageFile(shot)) + '">Markdown page</a></p>',
      "</div>",
      '<figure class="shot-pic">',
      shot.images.length > 1 ? '<div class="tabs" role="tablist" aria-label="Pictures of ' + esc(shot.title) + '">' + tabs + "</div>" : "",
      '<a class="pic" href="' + esc(encodePath(first.file)) + '" target="_blank" rel="noopener"><img loading="lazy" decoding="async" alt="' + esc(shot.title + ", " + longPictureLabel(first).toLowerCase()) + '" src="' + esc(encodePath(first.file)) + '" width="' + first.width + '" height="' + first.height + '"></a>',
      "</figure>",
      "</article>",
    ].join("\n");
  };

  const toc = sections.map((section) => '<a href="#section-' + esc(section.name.toLowerCase().replace(/[^a-z0-9]+/g, "-")) + '">' + esc(section.name) + " <span>" + section.shots.length + "</span></a>").join("");
  const body = sections
    .map((section) => '<section id="section-' + esc(section.name.toLowerCase().replace(/[^a-z0-9]+/g, "-")) + '"><h2>' + esc(section.name) + "</h2>\n" + section.shots.map(card).join("\n") + "</section>")
    .join("\n");
  const lead = featured
    .map((shot) => {
      const hero = shot.images.find((image) => image.kind === "hero") || shot.images[0];
      return '<a class="lead-card" href="#' + esc(shot.id) + '"><img loading="lazy" alt="" src="' + esc(encodePath(hero.file)) + '"><span>' + esc(shot.title) + "</span></a>";
    })
    .join("");

  return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>TenderPilot - portfolio kit</title>
<style>
:root{--bg:#f6f7fb;--card:#fff;--ink:#111827;--muted:#5b6475;--line:#e3e7ef;--brand:#2563eb;color-scheme:light}
@media (prefers-color-scheme:dark){:root{--bg:#0d1220;--card:#151c2e;--ink:#eef1f8;--muted:#a3adc2;--line:#27314a;--brand:#7aa2ff;color-scheme:dark}}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.55 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
header.top{padding:40px 24px 8px;max-width:1280px;margin:0 auto}
header.top h1{margin:0 0 6px;font-size:clamp(28px,4vw,40px);letter-spacing:-.02em}
header.top p{margin:0;color:var(--muted);max-width:70ch}
nav.toc{position:sticky;top:0;z-index:5;background:var(--bg);border-bottom:1px solid var(--line);margin-top:20px}
nav.toc div{max-width:1280px;margin:0 auto;padding:10px 24px;display:flex;gap:8px;overflow-x:auto;scrollbar-width:thin}
nav.toc a{white-space:nowrap;text-decoration:none;color:var(--ink);border:1px solid var(--line);background:var(--card);padding:5px 12px;border-radius:999px;font-size:14px}
nav.toc a span{color:var(--muted);margin-left:4px}
nav.toc a:hover{border-color:var(--brand)}
main{max-width:1280px;margin:0 auto;padding:8px 24px 80px}
h2{font-size:26px;margin:48px 0 16px;letter-spacing:-.01em}
.lead{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:14px;margin:24px 0 0}
.lead-card{display:block;text-decoration:none;color:var(--ink);background:var(--card);border:1px solid var(--line);border-radius:14px;overflow:hidden}
.lead-card img{display:block;width:100%;aspect-ratio:16/10;object-fit:cover;object-position:top;background:var(--line)}
.lead-card span{display:block;padding:10px 12px;font-size:14px;font-weight:600}
.shot{display:grid;grid-template-columns:minmax(0,5fr) minmax(0,7fr);gap:28px;background:var(--card);border:1px solid var(--line);border-radius:18px;padding:24px;margin:0 0 20px;scroll-margin-top:70px}
.shot h3{margin:2px 0 6px;font-size:22px}
.kicker{margin:0;color:var(--muted);font-size:13px;text-transform:uppercase;letter-spacing:.06em}
.kicker code,.shot code{font:12px ui-monospace,Menlo,monospace;text-transform:none;letter-spacing:0}
.tagline{font-weight:600;margin:0 0 10px}
.shot ul{padding-left:20px;margin:10px 0}
.shot li{margin:3px 0}
details{margin:12px 0;color:var(--muted);font-size:14px}
summary{cursor:pointer;color:var(--ink);font-weight:600}
.cap{font-size:14px;color:var(--muted)}
.links a{color:var(--brand)}
.shot-pic{margin:0;min-width:0}
.tabs{display:flex;gap:6px;flex-wrap:wrap;margin-bottom:10px}
.tabs button{font:inherit;font-size:13px;padding:4px 12px;border-radius:999px;border:1px solid var(--line);background:transparent;color:var(--ink);cursor:pointer}
.tabs button[aria-selected=true]{background:var(--brand);border-color:var(--brand);color:#fff}
.pic{display:block;border:1px solid var(--line);border-radius:12px;overflow:hidden;max-height:760px;overflow-y:auto}
.pic img{display:block;width:100%;height:auto}
@media (max-width:900px){.shot{grid-template-columns:1fr;padding:16px}}
footer{max-width:1280px;margin:0 auto;padding:0 24px 40px;color:var(--muted);font-size:14px}
</style>
</head>
<body>
<header class="top">
<h1>TenderPilot - portfolio kit</h1>
<p>${shots.length} screens taken on ${esc(date)} from the demo app, which uses synthetic data only. Pick a picture, copy its caption, or open the Markdown page of a screen. <a href="README.md" style="color:var(--brand)">README</a> &middot; <a href="CASE-STUDY.md" style="color:var(--brand)">Case study</a></p>
${featured.length ? '<div class="lead">' + lead + "</div>" : ""}
</header>
<nav class="toc" aria-label="Sections"><div>${toc}</div></nav>
<main>
${body}
</main>
<footer>Generated by scripts/capture-portfolio.sh. Opening this file needs no server.</footer>
<script>
document.querySelectorAll('.tabs').forEach(function (tabs) {
  var figure = tabs.parentElement;
  var img = figure.querySelector('img');
  var link = figure.querySelector('a.pic');
  tabs.addEventListener('click', function (event) {
    var button = event.target.closest('button[data-src]');
    if (!button) return;
    tabs.querySelectorAll('button').forEach(function (b) { b.setAttribute('aria-selected', String(b === button)); });
    img.src = button.dataset.src;
    img.width = Number(button.dataset.w);
    img.height = Number(button.dataset.h);
    link.href = button.dataset.src;
  });
});
</script>
</body>
</html>
`;
}

// ---------------------------------------------------------------------------------------------------------
// Case study (written once, then yours)
// ---------------------------------------------------------------------------------------------------------
function caseStudyMarkdown(shots) {
  const has = (id) => shots.some((shot) => shot.id === id);
  const link = (id, label) => (has(id) ? "[" + label + "](pages/" + pageFile(shots.find((shot) => shot.id === id)) + ")" : label);
  return `# TenderPilot - case study

_Add your role, the dates and the team size here._

TenderPilot is procurement intelligence for public tenders. It collects public procurement notices, scores them against a company's own profile, and walks a bid team from "is this worth it?" to a submitted bid. All pictures in this kit come from the demo build, which runs on synthetic data.

## The problem

Public tenders are published on several portals, in different shapes, and a small bid team cannot read them all. Time goes into notices that were never a good fit. Once a team commits to a bid, requirements, tasks and deadlines live in separate documents and are easy to lose track of.

## What TenderPilot does

- **Find.** Opportunities from TED, BOAMP and DECP in one canonical model, with full-text search, saved views, comparison and a deadline calendar. See ${link("opportunities", "Opportunities")}, ${link("search", "Search")} and ${link("calendar", "Deadline calendar")}.
- **Decide.** Company-to-tender matching with explained scores, and a Go / No-Go workspace per tender. See ${link("matches", "Matches")} and ${link("bid-decisions", "Bid decisions")}.
- **Bid.** Planning tasks, compliance checks, a Q&A log, a printable bid brief and an optional local AI assistant. See ${link("bid-workspace", "Bid workspace")}, ${link("bid-planning-board", "Planning board")} and ${link("tasks", "My tasks")}.
- **Understand the market.** Buyer and supplier intelligence, market analytics, notifications and watchlists. See ${link("market", "Market explorer")}, ${link("buyers", "Buyers")} and ${link("notifications", "Notifications")}.
- **Run it.** Data-source and ingestion monitoring for the people who operate the system. See ${link("data-operations", "Data operations")}.

## Interface and experience decisions

- **One design system.** Semantic colour tokens, light, dark and system themes applied before the first paint, a compact density, and a reduced-motion switch that also honours the operating-system setting. See ${link("dashboard", "the dashboard")} in both themes.
- **Keyboard first.** A command palette (Cmd/Ctrl+K or /) that finds pages, actions, recent items and live opportunities, and "go to" shortcuts such as G then T. See ${link("palette", "Command palette")} and ${link("shortcuts", "Keyboard shortcuts")}.
- **Links that carry state.** Lists open pre-filtered from the address (for example a grade or a CPV code), and saved views are kept in the browser.
- **Honest states.** A screen for a server that cannot be reached, a clear message when a session ends, a notice when part of a page could not be read, confirmation before anything is deleted, and plans that lock when a bid is No-Go or submitted. See ${link("offline", "Connection lost")}.
- **Accessible by default.** Skip link, visible focus, names for every form control and menu, and labelled dialogs.
- **Works on a phone.** Every page has a phone layout, with a navigation drawer. See ${link("mobile-menu", "Phone navigation")}.
- **Useful data out.** CSV export of lists, and .ics export of deadlines and task due dates.

## Architecture and stack

| Part | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1, Liquibase (15 migrations), Maven |
| Data | PostgreSQL 17 as the source of truth, OpenSearch 3.8.0 as a rebuildable search projection |
| Local AI (optional) | Ollama with local models, so bid documents never leave the machine |
| Frontend | Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 |

Shared public procurement intelligence is kept apart from organization-private bid data: the public market view is derived from the shared corpus and never receives private dashboards, compliance results, documents or planning data. See ${link("frame-architecture", "Architecture and trust boundaries")}.

## Quality and engineering practice

- A local release gate and a v1 readiness gate, with a release manifest, that expect at least 222 backend tests including 26 release-contract tests.
- A frontend gate that checks dark-theme freshness, ESLint, TypeScript and the production build.
- No CI/CD pipeline by design: every check runs on a developer machine with one command.
- A demo mode that serves synthetic data, so the interface can be developed and photographed without the backend, a database, a search index or a local AI.
- The backend was frozen at v1.0.0 while the interface was refined, so every frontend change had to work with the existing API.

## What comes next

Features that need new endpoints belong in v1.1: password reset, profile editing, invitations and roles, win/loss tracking, e-mail alerts, results beyond 100 hits, saved views on the server, an audit feed, comments, and an endpoint for tasks across bids.

## The screens

Open \`index.html\` for the gallery, or \`README.md\` for the list of all screens with their pictures and texts.
`;
}

// ---------------------------------------------------------------------------------------------------------
// Entry point
// ---------------------------------------------------------------------------------------------------------
export function renderDocs(out, data) {
  const shots = data.shots.map(withCurrentText).sort((a, b) => a.number - b.number);
  const written = [];

  fs.rmSync(path.join(out, "pages"), { recursive: true, force: true });
  fs.mkdirSync(path.join(out, "pages"), { recursive: true });
  for (const shot of shots) fs.writeFileSync(path.join(out, "pages", pageFile(shot)), pageMarkdown(shot));
  written.push("pages/ (" + shots.length + " files)");

  fs.writeFileSync(path.join(out, "README.md"), readmeMarkdown(data, shots));
  written.push("README.md");
  fs.writeFileSync(path.join(out, "index.html"), galleryHtml(data, shots));
  written.push("index.html");

  const caseStudy = path.join(out, "CASE-STUDY.md");
  if (!fs.existsSync(caseStudy)) {
    fs.writeFileSync(caseStudy, caseStudyMarkdown(shots));
    written.push("CASE-STUDY.md");
  }
  return written;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const out = path.resolve(process.argv[2] || path.join(ROOT, "portfolio"));
  const file = path.join(out, "portfolio.json");
  if (!fs.existsSync(file)) {
    process.stdout.write("There is no " + file + " yet. Run ./scripts/capture-portfolio.sh first.\n");
    process.exit(1);
  }
  const written = renderDocs(out, JSON.parse(fs.readFileSync(file, "utf8")));
  process.stdout.write("Wrote " + written.join(", ") + "\n");
}
