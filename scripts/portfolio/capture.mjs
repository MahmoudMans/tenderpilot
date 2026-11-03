#!/usr/bin/env node
/**
 * TenderPilot portfolio capture.
 *
 * Opens a running TenderPilot (normally the demo build with synthetic data, started by
 * scripts/capture-portfolio.sh), visits every page and many interface states in light, dark and phone
 * views, and writes screenshots plus a text for each of them into the portfolio folder.
 *
 *   TP_BASE_URL   address of the running app (required), for example http://localhost:3000
 *   TP_OUT        output folder (default: <project>/portfolio)
 *   TP_TOOLS_DIR  folder that has node_modules/playwright-core (default: <TP_OUT>/.tools)
 *   TP_ONLY       comma separated shot ids to capture, for example dashboard,tasks
 *   TP_QUICK=1    desktop light only, no full-page images
 *   TP_SCALE      pixel density of the desktop images (default 1, use 2 for sharper images)
 *   TP_FORMAT     png (default) or jpeg
 *   TP_BROWSER_PATH, TP_NO_SANDBOX=1   use a specific browser binary
 *   TP_EMAIL, TP_PASSWORD   only needed when the app is a real backend that asks you to sign in
 *
 * The demo data is synthetic. A real backend would put real tender and company data into the images.
 */
import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

import { PROFILES, SHOTS } from "./catalog.mjs";
import { renderDocs } from "./render.mjs";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(HERE, "..", "..");

const cfg = {
  base: (process.env.TP_BASE_URL || "").replace(/\/+$/, ""),
  out: path.resolve(process.env.TP_OUT || path.join(ROOT, "portfolio")),
  only: (process.env.TP_ONLY || "").split(",").map((s) => s.trim()).filter(Boolean),
  quick: process.env.TP_QUICK === "1",
  scale: Number(process.env.TP_SCALE || 1) || 1,
  format: process.env.TP_FORMAT === "jpeg" ? "jpeg" : "png",
};
cfg.tools = path.resolve(process.env.TP_TOOLS_DIR || path.join(cfg.out, ".tools"));

const MAX_FULL_DESKTOP = 6000;
const MAX_FULL_PHONE = 3500;
const EXT = cfg.format === "jpeg" ? "jpg" : "png";

function log(message) {
  process.stdout.write(message + "\n");
}

if (!cfg.base) {
  log("TP_BASE_URL is not set. Run ./scripts/capture-portfolio.sh instead of this file.");
  process.exit(2);
}

const require = createRequire(path.join(cfg.tools, "x.js"));
let playwright;
try {
  playwright = require("playwright-core");
} catch {
  log("playwright-core was not found in " + path.join(cfg.tools, "node_modules"));
  log("Run ./scripts/capture-portfolio.sh, which installs it.");
  process.exit(2);
}

// ---------------------------------------------------------------------------------------------------------
// The six deterministic frames of the existing capture mode are read from its manifest (never modified).
// ---------------------------------------------------------------------------------------------------------
function readFrameManifest() {
  const file = path.join(ROOT, "frontend/src/features/showcase/capture-manifest.ts");
  if (!fs.existsSync(file)) return [];
  const text = fs.readFileSync(file, "utf8");
  const frames = [];
  for (const block of text.split(/\{\s*order\s*:/).slice(1)) {
    const pick = (key) => {
      const m = block.match(new RegExp(key + "\\s*:\\s*\"([^\"]*)\""));
      return m ? m[1] : "";
    };
    const order = Number((block.match(/^\s*(\d+)/) || [])[1]);
    if (!order) continue;
    frames.push({
      order,
      id: pick("id"),
      title: pick("title"),
      subtitle: pick("subtitle"),
      filename: pick("recommendedFilename"),
      caption: pick("caption"),
    });
  }
  return frames;
}

function frameShots() {
  return readFrameManifest().map((frame) => ({
    id: "frame-" + frame.id,
    section: "Showcase frames",
    title: frame.title + " (showcase frame)",
    tagline: frame.subtitle,
    description:
      frame.caption +
      " This is one of the six fixed frames of the capture mode: a 1440 x 900 composition built from a synthetic dataset, made to be used as-is in a portfolio or a presentation.",
    highlights: [
      "Fixed 1440 x 900 frame, the same on every run",
      "Built only from the synthetic showcase dataset, so no real tender or company data appears",
      "Original file name kept: " + frame.filename,
    ],
    path: "/showcase/capture",
    frame: frame,
    variants: ["frame"],
    feature: frame.order <= 2,
  }));
}

// ---------------------------------------------------------------------------------------------------------
// Browser
// ---------------------------------------------------------------------------------------------------------
async function launchBrowser() {
  const args = process.env.TP_NO_SANDBOX === "1" ? ["--no-sandbox"] : [];
  const tries = process.env.TP_BROWSER_PATH
    ? [{ executablePath: process.env.TP_BROWSER_PATH }]
    : [{ channel: "chrome" }, { channel: "msedge" }, {}];
  for (const extra of tries) {
    try {
      return await playwright.chromium.launch({ headless: true, args, ...extra });
    } catch {
      // try the next one
    }
  }
  log("No installed Chrome or Edge was found. Downloading Chromium once (about 150 MB)...");
  const cli = path.join(path.dirname(require.resolve("playwright-core/package.json")), "cli.js");
  const installed = spawnSync(process.execPath, [cli, "install", "chromium"], { stdio: "inherit" });
  if (installed.status !== 0) {
    throw new Error("Could not download Chromium. Install Google Chrome and run the script again.");
  }
  return playwright.chromium.launch({ headless: true, args });
}

function viewportHeightFor(profile, shot) {
  return shot && shot.viewportHeight && !profile.mobile ? shot.viewportHeight : profile.height;
}

function contextOptions(profile, shot) {
  const options = {
    viewport: { width: profile.width, height: viewportHeightFor(profile, shot) },
    colorScheme: profile.scheme,
    deviceScaleFactor: profile.mobile ? 2 : cfg.scale,
    reducedMotion: "reduce",
    locale: "en-US",
    timezoneId: "Europe/Paris",
  };
  if (profile.mobile) {
    options.isMobile = true;
    options.hasTouch = true;
  }
  return options;
}

// ---------------------------------------------------------------------------------------------------------
// Page helpers
// ---------------------------------------------------------------------------------------------------------
const BUSY = '[aria-busy="true"], .animate-shimmer, [role="status"][aria-label="Loading"]';
const ERROR_TEXT = /could not be loaded|cannot be reached|something went wrong|failed to fetch/i;

async function settle(page) {
  await page.waitForLoadState("load").catch(() => {});
  await page.waitForLoadState("networkidle", { timeout: 4000 }).catch(() => {});
  const calm = await page
    .waitForFunction((selector) => !document.querySelector(selector), BUSY, { timeout: 15000 })
    .then(() => true)
    .catch(() => false);
  await page.evaluate(() => (document.fonts ? document.fonts.ready : null)).catch(() => {});
  await page.waitForTimeout(300);
  return calm;
}

async function applyNetwork(page, mode) {
  const origin = new URL(cfg.base).origin;
  const cors = {
    "access-control-allow-origin": origin,
    "access-control-allow-credentials": "true",
    "access-control-allow-headers": "*",
    "access-control-allow-methods": "*",
  };
  if (mode === "signed-out") {
    await page.route("**/api/v1/auth/refresh", (route) => {
      if (route.request().method() === "OPTIONS") return route.fulfill({ status: 204, headers: cors });
      return route.fulfill({
        status: 401,
        contentType: "application/json",
        headers: cors,
        body: JSON.stringify({ status: 401, message: "Refresh session is unavailable" }),
      });
    });
  } else if (mode === "offline") {
    await page.route("**/api/v1/**", (route) => route.abort("connectionrefused"));
  } else if (mode === "fresh") {
    // A brand-new account: the same change the demo API makes with MOCK_FRESH=1.
    await page.route("**/api/v1/dashboard/command-center", async (route) => {
      if (route.request().method() !== "GET") return route.continue();
      const response = await route.fetch();
      const body = await response.json();
      for (const key of Object.keys(body.summary || {})) body.summary[key] = 0;
      body.portfolio = [];
      body.priorities = [];
      return route.fulfill({ response, json: body });
    });
  }
}

async function runStep(page, step) {
  const locator = (selector) => page.locator(selector).first();
  try {
    if (step.click) await locator(step.click).click({ timeout: 8000 });
    else if (step.hover) await locator(step.hover).hover({ timeout: 8000 });
    else if (step.check) await locator(step.check).check({ timeout: 8000 });
    else if (step.fill) await locator(step.fill[0]).fill(step.fill[1], { timeout: 8000 });
    else if (step.type) await page.keyboard.type(step.type, { delay: 25 });
    else if (step.press) await page.keyboard.press(step.press);
    else if (step.select) await locator(step.select[0]).selectOption(step.select[1], { timeout: 8000 });
    else if (step.scroll) await locator(step.scroll).scrollIntoViewIfNeeded({ timeout: 8000 });
    else if (step.waitFor) await locator(step.waitFor).waitFor({ state: "visible", timeout: 10000 });
    else if (step.wait) await page.waitForTimeout(step.wait);
    else if (step.eval) await page.evaluate(step.eval);
    else if (step.settle) await settle(page);
  } catch (error) {
    if (step.optional) return;
    throw new Error("step " + JSON.stringify(step) + " failed: " + String(error.message).split("\n")[0]);
  }
}

/** What is on the screen, read from the live page, so the text can name real sections and buttons. */
async function describeScreen(page, scope) {
  return page.evaluate((scopeSelector) => {
    const clean = (value) => (value || "").replace(/\s+/g, " ").trim();
    const shown = (element) => {
      const box = element.getBoundingClientRect();
      const style = getComputedStyle(element);
      return box.width > 0 && box.height > 0 && style.visibility !== "hidden" && style.display !== "none";
    };
    const scoped = scopeSelector ? [...document.querySelectorAll(scopeSelector)].find(shown) : null;
    const root = scoped || document.querySelector("main") || document.body;
    const unique = (list) => [...new Set(list.filter(Boolean))];
    const nameOf = (element) => clean(element.getAttribute("aria-label") || element.textContent);
    // Labels that name a record ("Edit task Draft memo", "Compare Smart metering...") are listed once.
    const generic = (text) => {
      const words = text.replace(/\d+/g, "#").split(" ");
      return words.length > 3 ? words.slice(0, 2).join(" ") : words.join(" ");
    };
    // The words of a heading, without a count badge such as the 4 in "Next 7 days 4".
    const headingText = (element) =>
      clean(
        [...element.childNodes]
          .filter((node) => !(node.nodeType === 1 && /^\d+$/.test(clean(node.textContent))))
          .map((node) => node.textContent)
          .join(" "),
      );
    const seen = new Set();
    const actions = [];
    for (const element of root.querySelectorAll("button, a[role=button]")) {
      if (!shown(element)) continue;
      const text = nameOf(element);
      if (text.length < 2 || text.length > 32 || seen.has(generic(text))) continue;
      seen.add(generic(text));
      actions.push(text);
    }
    const labelFor = (element) => {
      const direct = element.getAttribute("aria-label");
      if (direct) return clean(direct);
      if (element.id) {
        const label = document.querySelector('label[for="' + CSS.escape(element.id) + '"]');
        if (label) return clean(label.textContent);
      }
      const wrapping = element.closest("label");
      return wrapping ? clean(wrapping.textContent) : clean(element.getAttribute("placeholder"));
    };
    return {
      heading: clean((root.querySelector("h1") || {}).textContent),
      sections: unique(
        [...root.querySelectorAll("h2, h3")]
          .filter((e) => shown(e) && !e.closest("a, li, tr, table, [role=listitem]"))
          .map(headingText)
          .filter((t) => t.length > 1 && t.length <= 42),
      ).slice(0, 12),
      actions: actions.slice(0, 14),
      controls: unique([...root.querySelectorAll("input, select, textarea")].filter(shown).map(labelFor).filter((t) => t && t.length <= 32)).slice(0, 12),
    };
  }, scope || null);
}

// ---------------------------------------------------------------------------------------------------------
// Finding real records to open (the first opportunity, buyer, supplier and bid in the app)
// ---------------------------------------------------------------------------------------------------------
async function discoverRecords(browser) {
  const found = {};
  const context = await browser.newContext(contextOptions(PROFILES["desktop-light"]));
  const page = await context.newPage();
  const firstLink = async (listPath, pattern) => {
    try {
      await page.goto(cfg.base + listPath, { waitUntil: "domcontentloaded", timeout: 45000 });
      await settle(page);
      return await page.evaluate((source) => {
        const re = new RegExp(source);
        for (const a of document.querySelectorAll("main a[href]")) {
          const href = a.getAttribute("href") || "";
          const m = href.match(re);
          if (m) return m[1];
        }
        return null;
      }, pattern.source);
    } catch {
      return null;
    }
  };
  found.opportunity = await firstLink("/opportunities", /^\/opportunities\/([^/?#]+)$/);
  found.buyer = await firstLink("/buyers", /^\/buyers\/([^/?#]+)$/);
  found.supplier = await firstLink("/suppliers", /^\/suppliers\/([^/?#]+)$/);
  found.tender = await firstLink("/bid-decisions", /^\/(?:bid-decisions|bid-workspaces)\/([^/?#]+)/);
  await context.close();
  return found;
}

function resolvePath(template, records) {
  let missing = null;
  const resolved = template.replace(/\{(\w+)\}/g, (_, key) => {
    if (!records[key]) missing = key;
    return records[key] || "";
  });
  return missing ? { missing } : { path: resolved };
}

// ---------------------------------------------------------------------------------------------------------
// One shot in one profile
// ---------------------------------------------------------------------------------------------------------
function slugDir(number, id) {
  return String(number).padStart(2, "0") + "-" + id;
}

async function fullPageImage(page, profile, file, baseHeight) {
  if (profile.mobile) {
    const height = await page.evaluate(() => Math.max(document.documentElement.scrollHeight, document.body.scrollHeight));
    const clip = { x: 0, y: 0, width: profile.width, height: Math.min(height, MAX_FULL_PHONE) };
    await page.screenshot({ path: file, fullPage: true, clip, type: cfg.format });
    return { height: clip.height, cut: height > MAX_FULL_PHONE };
  }
  const height = await page.evaluate(() => Math.max(document.documentElement.scrollHeight, document.body.scrollHeight));
  const target = Math.min(height, MAX_FULL_DESKTOP);
  // Tall viewport so the sticky sidebar and other full-height parts stretch the whole way.
  await page.setViewportSize({ width: profile.width, height: target });
  await page.waitForTimeout(350);
  await page.screenshot({ path: file, type: cfg.format });
  await page.setViewportSize({ width: profile.width, height: baseHeight });
  return { height: target, cut: height > MAX_FULL_DESKTOP };
}

async function captureOne(browser, shot, number, profileName, records, record) {
  const profile = PROFILES[profileName];
  const resolved = resolvePath(shot.path, records);
  if (resolved.missing) throw new Error("no " + resolved.missing + " found in the app to open");

  const baseHeight = viewportHeightFor(profile, shot);
  const context = await browser.newContext(contextOptions(profile, shot));
  const page = await context.newPage();
  const problems = [];
  page.on("pageerror", (error) => problems.push("page error: " + error.message));
  page.on("response", (response) => {
    const url = response.url();
    if (url.includes("/api/") && response.status() >= 400 && !url.includes("/auth/refresh")) {
      problems.push("api " + response.status() + " " + response.request().method() + " " + new URL(url).pathname);
    }
  });

  try {
    await applyNetwork(page, shot.net);
    await page.goto(cfg.base + resolved.path, { waitUntil: "domcontentloaded", timeout: 60000 });
    await settle(page);

    // A real backend asks you to sign in first.
    if (!shot.net && new URL(page.url()).pathname.startsWith("/login") && !resolved.path.startsWith("/login")) {
      if (!process.env.TP_EMAIL || !process.env.TP_PASSWORD) {
        throw new Error("the app asks to sign in; set TP_EMAIL and TP_PASSWORD");
      }
      await page.fill('input[type="email"]', process.env.TP_EMAIL);
      await page.fill('input[type="password"]', process.env.TP_PASSWORD);
      await page.keyboard.press("Enter");
      await page.waitForURL((url) => !url.pathname.startsWith("/login"), { timeout: 20000 });
      await page.goto(cfg.base + resolved.path, { waitUntil: "domcontentloaded", timeout: 60000 });
      await settle(page);
    }

    for (const step of shot.steps || []) await runStep(page, step);
    let calm = await settle(page);
    if (!calm) {
      await page.reload({ waitUntil: "domcontentloaded" });
      for (const step of shot.steps || []) await runStep(page, step);
      calm = await settle(page);
    }
    if (!calm) throw new Error("the page was still loading after 15 seconds");
    if (shot.ready) await page.locator(shot.ready).first().waitFor({ state: "visible", timeout: 10000 });

    const mainText = await page.evaluate(() => (document.querySelector("main") || document.body).innerText || "");
    if (!shot.allowErrorText && ERROR_TEXT.test(mainText)) {
      throw new Error("the page shows an error: " + (mainText.match(ERROR_TEXT) || [""])[0]);
    }

    await page.evaluate(() => {
      if (document.activeElement && document.activeElement !== document.body) document.activeElement.blur();
      window.scrollTo(0, 0);
    });
    // Some pictures should open further down, at the part the screen is about.
    const focus = typeof shot.focus === "string" ? { selector: shot.focus } : shot.focus;
    if (focus) {
      const top = await page
        .locator(focus.selector)
        .first()
        .evaluate((element, offset) => element.getBoundingClientRect().top + window.scrollY - offset, focus.offset === undefined ? 120 : focus.offset);
      await page.evaluate((y) => window.scrollTo(0, Math.max(0, y)), top);
    }
    await page.mouse.move(0, 0);
    await page.waitForTimeout(150);

    const dir = path.join(cfg.out, "screenshots", slugDir(number, shot.id));
    fs.mkdirSync(dir, { recursive: true });
    const images = [];
    const add = (kind, file, width, height) =>
      images.push({
        profile: profileName,
        kind,
        file: path.relative(cfg.out, file).split(path.sep).join("/"),
        width,
        height,
        bytes: fs.statSync(file).size,
      });

    const heroFile = path.join(dir, profileName + "." + EXT);
    await page.screenshot({ path: heroFile, type: cfg.format });
    add("hero", heroFile, profile.width * (profile.mobile ? 2 : cfg.scale), baseHeight * (profile.mobile ? 2 : cfg.scale));

    const tall = await page.evaluate(() => Math.max(document.documentElement.scrollHeight, document.body.scrollHeight));
    const wantsFull = shot.full !== false && profile.kinds.includes("full") && !cfg.quick && tall > baseHeight + 120;
    if (wantsFull) {
      const fullFile = path.join(dir, profileName + "-full." + EXT);
      const info = await fullPageImage(page, profile, fullFile, baseHeight);
      const factor = profile.mobile ? 2 : cfg.scale;
      add("full", fullFile, profile.width * factor, info.height * factor);
      if (info.cut && !profile.mobile) problems.push("the full-page image was cut at " + info.height + " px");
    }

    if (!record.onScreen) {
      record.onScreen = await describeScreen(page, shot.scope);
    }
    record.images.push(...images);
    record.warnings.push(...problems.map((p) => profileName + ": " + p));
  } finally {
    await context.close();
  }
}

async function captureFrames(browser, shot, number, record) {
  const profile = PROFILES["desktop-light"];
  const context = await browser.newContext(contextOptions(profile));
  const page = await context.newPage();
  try {
    await page.goto(cfg.base + "/showcase/capture", { waitUntil: "domcontentloaded", timeout: 60000 });
    await settle(page);
    const frame = page.locator('[data-capture-id="' + shot.frame.id + '"]').first();
    await frame.waitFor({ state: "visible", timeout: 15000 });
    await frame.scrollIntoViewIfNeeded();
    const dir = path.join(cfg.out, "showcase-frames");
    fs.mkdirSync(dir, { recursive: true });
    const name = shot.frame.filename || "tenderpilot-" + String(shot.frame.order).padStart(2, "0") + "-" + shot.frame.id + ".png";
    const file = path.join(dir, name);
    await frame.screenshot({ path: file, type: "png" });
    const box = await frame.boundingBox();
    record.images.push({
      profile: "frame",
      kind: "hero",
      file: path.relative(cfg.out, file).split(path.sep).join("/"),
      width: Math.round(box ? box.width : 1440),
      height: Math.round(box ? box.height : 900),
      bytes: fs.statSync(file).size,
    });
    record.onScreen = await describeScreen(page, '[data-capture-id="' + shot.frame.id + '"]');
  } finally {
    await context.close();
  }
}

// ---------------------------------------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------------------------------------
async function main() {
  const catalog = [...SHOTS];
  const frames = frameShots();
  const showcaseAt = catalog.findIndex((shot) => shot.id === "showcase");
  catalog.splice(showcaseAt >= 0 ? showcaseAt + 1 : catalog.length, 0, ...frames);

  const wanted = (shot) => cfg.only.length === 0 || cfg.only.includes(shot.id);
  const numbered = catalog.map((shot, index) => ({ shot, number: index + 1 })).filter(({ shot }) => wanted(shot));
  if (numbered.length === 0) {
    log("No shot matches TP_ONLY=" + cfg.only.join(","));
    process.exit(2);
  }

  const whole = cfg.only.length === 0;
  fs.mkdirSync(cfg.out, { recursive: true });

  try {
    const response = await fetch(cfg.base + "/login", { signal: AbortSignal.timeout(15000) });
    if (!response.ok) throw new Error("answered " + response.status);
  } catch (error) {
    log("The app at " + cfg.base + " does not answer (" + (error && error.message ? error.message : error) + ").");
    log("Start it first, or run ./scripts/capture-portfolio.sh without TP_BASE_URL to build and start the demo.");
    process.exit(2);
  }

  const browser = await launchBrowser();

  // Only now, with the app answering and a browser running, is the previous run replaced.
  if (whole) {
    for (const name of ["screenshots", "showcase-frames", "pages"]) {
      fs.rmSync(path.join(cfg.out, name), { recursive: true, force: true });
    }
  }

  const startedAt = Date.now();
  log("Reading the app at " + cfg.base + " ...");
  const records = await discoverRecords(browser);
  for (const [key, value] of Object.entries(records)) log("  " + key + ": " + (value || "none found"));

  const results = [];
  const skipped = [];
  let done = 0;
  for (const { shot, number } of numbered) {
    done += 1;
    const record = {
      id: shot.id,
      number,
      section: shot.section,
      title: shot.title,
      tagline: shot.tagline,
      description: shot.description,
      highlights: shot.highlights || [],
      feature: Boolean(shot.feature),
      route: shot.path,
      images: [],
      onScreen: null,
      warnings: [],
    };
    const profileNames = shot.frame ? ["frame"] : (shot.variants || ["desktop-light", "desktop-dark", "phone-light"]).filter((name) => !cfg.quick || name === "desktop-light");
    const failures = [];
    try {
      if (shot.frame) {
        await captureFrames(browser, shot, number, record);
      } else {
        for (const profileName of profileNames) {
          try {
            await captureOne(browser, shot, number, profileName, records, record);
          } catch (error) {
            failures.push(profileName + ": " + String(error.message).split("\n")[0]);
          }
        }
      }
    } catch (error) {
      failures.push(String(error.message).split("\n")[0]);
    }
    const progress = "[" + done + "/" + numbered.length + "] " + shot.id;
    if (record.images.length === 0) {
      skipped.push({ id: shot.id, title: shot.title, reason: failures.join("; ") || "nothing captured" });
      log(progress + "  SKIPPED  " + (failures[0] || ""));
      continue;
    }
    record.warnings.push(...failures.map((f) => "not captured, " + f));
    results.push(record);
    const kinds = record.images.map((image) => image.profile.replace("desktop-", "") + (image.kind === "full" ? "+full" : "")).join(" ");
    log(progress + "  " + kinds + (record.warnings.length ? "  (" + record.warnings.length + " warning" + (record.warnings.length === 1 ? "" : "s") + ")" : ""));
  }
  await browser.close();

  let previous = [];
  const dataFile = path.join(cfg.out, "portfolio.json");
  if (!whole && fs.existsSync(dataFile)) {
    try {
      previous = JSON.parse(fs.readFileSync(dataFile, "utf8")).shots || [];
    } catch {
      previous = [];
    }
  }
  const ids = new Set(results.map((r) => r.id));
  const merged = [...previous.filter((r) => !ids.has(r.id)), ...results].sort((a, b) => a.number - b.number);

  const data = {
    app: "TenderPilot",
    generatedAt: new Date().toISOString(),
    data: "synthetic demo data",
    desktop: { width: PROFILES["desktop-light"].width, height: PROFILES["desktop-light"].height, scale: cfg.scale },
    shots: merged,
    skipped,
  };
  fs.writeFileSync(dataFile, JSON.stringify(data, null, 2) + "\n");
  const written = renderDocs(cfg.out, data);

  const imageCount = merged.reduce((sum, shot) => sum + shot.images.length, 0);
  const bytes = merged.reduce((sum, shot) => sum + shot.images.reduce((s, image) => s + image.bytes, 0), 0);
  const warned = merged.filter((shot) => shot.warnings.length).length;
  log("");
  log("Captured " + merged.length + " screens, " + imageCount + " images (" + (bytes / 1048576).toFixed(1) + " MB) in " + Math.round((Date.now() - startedAt) / 1000) + " s.");
  if (skipped.length) {
    log(skipped.length + " skipped:");
    for (const item of skipped) log("  " + item.id + ": " + item.reason);
  }
  if (warned) log(warned + " screens have warnings; see the warnings lists in portfolio.json.");
  log("Wrote " + written.join(", "));
  process.exit(skipped.length > 0 && merged.length === 0 ? 1 : 0);
}

main().catch((error) => {
  log("Capture failed: " + (error && error.stack ? error.stack : error));
  process.exit(1);
});
