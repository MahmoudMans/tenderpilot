#!/usr/bin/env node
/**
 * TenderPilot dark-theme generator.
 *
 * The product UI is written with Tailwind colour utilities (bg-white,
 * text-slate-500, border-slate-200, ...). Instead of rewriting hundreds of class
 * names, this script compiles every colour utility the source actually uses
 * against a dark palette and emits `.dark`-scoped overrides.
 *
 * Output: frontend/src/app/theme-dark.css (imported by globals.css)
 *
 * Usage (from the project root, after `npm install` in frontend/):
 *   node scripts/generate-dark-theme.mjs
 *
 * Re-run it whenever you add new colour classes (for example `bg-teal-50`).
 * New components should prefer the semantic tokens from globals.css
 * (bg-surface, text-ink, border-line, ...), which adapt without this step.
 */

import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath, pathToFileURL } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const FRONTEND = path.join(ROOT, "frontend");
const SRC = path.join(FRONTEND, "src");
const OUTPUT = path.join(SRC, "app", "theme-dark.css");

/* ------------------------------------------------------------------ palette */

const tint = (hex, alpha) => `${hex}${alpha}`;
const statusBg = (rgb) => ({
  50: tint(rgb, "1a"),
  100: tint(rgb, "26"),
  200: tint(rgb, "40"),
  300: tint(rgb, "66"),
});
const statusBd = (rgb) => ({
  50: tint(rgb, "26"),
  100: tint(rgb, "33"),
  200: tint(rgb, "59"),
  300: tint(rgb, "80"),
  400: tint(rgb, "99"),
});

const RED = "#ef4444";
const AMBER = "#f59e0b";
const EMERALD = "#10b981";
const BLUE = "#3b82f6";
const VIOLET = "#8b5cf6";
const ORANGE = "#f97316";

/** Background-like properties (background-color, gradient stops). */
const BG = {
  white: "#111a2d",
  slate: {
    50: "#151f36",
    100: "#1a2640",
    200: "#243252",
    300: "#30406a",
    400: "#4a5c85",
    500: "#5f729b",
    600: "#7387b0",
  },
  red: statusBg(RED),
  amber: statusBg(AMBER),
  emerald: statusBg(EMERALD),
  blue: statusBg(BLUE),
  violet: statusBg(VIOLET),
  orange: statusBg(ORANGE),
};
const BG_HEX = {
  "#14213d": "#2a4fb8",
  "#1b2d52": "#3460d0",
  "#edf4ff": "#6b9aff24",
  "#f5f7fb": "#0a0f1c",
  "#f8fafc": "#151f36",
  // Gradient stops used by bg-[linear-gradient(...)] (data-source panel header).
  "#f8fbff": "#151f36",
  "#ffffff": "#111a2d",
};

/** Foreground-like properties (text, icon fill/stroke). */
const FG = {
  slate: {
    50: "#22304f",
    100: "#2b3a5c",
    200: "#3a4a6e",
    300: "#4f5f82",
    400: "#6b7b9c",
    500: "#8d9bb7",
    600: "#a9b6cf",
    700: "#c4cee3",
    800: "#dbe3f2",
    900: "#e9eef9",
    950: "#f3f6fc",
  },
  red: { 400: "#f87171", 500: "#f87171", 600: "#f87171", 700: "#fca5a5", 800: "#fecaca", 900: "#fee2e2" },
  amber: { 500: "#fbbf24", 600: "#fbbf24", 700: "#fcd34d", 800: "#fde68a", 900: "#fef3c7" },
  emerald: { 500: "#34d399", 600: "#34d399", 700: "#6ee7b7", 800: "#a7f3d0", 900: "#d1fae5" },
  blue: { 400: "#60a5fa", 500: "#60a5fa", 600: "#7db3ff", 700: "#93c5fd", 800: "#bfdbfe", 900: "#dbeafe" },
  violet: { 600: "#a78bfa", 700: "#c4b5fd", 800: "#ddd6fe" },
  orange: { 600: "#fb923c", 700: "#fdba74" },
};
const FG_HEX = {
  "#2f6fed": "#6b9aff",
  "#245fcd": "#8bb2ff",
  "#14213d": "#e9eef9",
};

/** Border / ring / divide / outline colours. */
const BD = {
  slate: {
    50: "#18233e",
    100: "#1a2640",
    200: "#243252",
    300: "#30406a",
    400: "#4a5c85",
  },
  red: statusBd(RED),
  amber: statusBd(AMBER),
  emerald: statusBd(EMERALD),
  blue: statusBd(BLUE),
  violet: statusBd(VIOLET),
  orange: statusBd(ORANGE),
};
const BD_HEX = {
  "#2f6fed": "#6b9aff",
};

const FAMILIES = {
  bg: { prefixes: ["bg-", "from-", "via-", "to-", "ring-offset-"], table: BG, hex: BG_HEX },
  fg: { prefixes: ["text-", "fill-", "stroke-", "decoration-", "caret-", "accent-"], table: FG, hex: FG_HEX },
  bd: { prefixes: ["border-", "ring-", "divide-", "outline-"], table: BD, hex: BD_HEX },
};

function themeCss(table) {
  const lines = [];
  for (const [name, value] of Object.entries(table)) {
    if (typeof value === "string") lines.push(`  --color-${name}: ${value};`);
    else for (const [step, hex] of Object.entries(value)) lines.push(`  --color-${name}-${step}: ${hex};`);
  }
  return `@theme {\n${lines.join("\n")}\n}`;
}

function lookup(table, name) {
  const match = name.match(/^([a-z]+)(?:-(\d+))?$/);
  if (!match) return undefined;
  const [, color, step] = match;
  const entry = table[color];
  if (entry === undefined) return undefined;
  if (typeof entry === "string") return step === undefined ? entry : undefined;
  return step === undefined ? undefined : entry[step];
}

/* ------------------------------------------------------------- css parsing */

function parseBlock(css, state) {
  const nodes = [];
  while (state.i < css.length) {
    while (state.i < css.length && /\s/.test(css[state.i])) state.i++;
    if (css[state.i] === "}") {
      state.i++;
      return nodes;
    }
    const start = state.i;
    let paren = 0;
    while (state.i < css.length) {
      const ch = css[state.i];
      if (ch === "(") paren++;
      else if (ch === ")") paren--;
      else if (paren === 0 && (ch === "{" || ch === ";" || ch === "}")) break;
      state.i++;
    }
    const ch = css[state.i];
    if (ch === "{") {
      const prelude = css.slice(start, state.i).trim();
      state.i++;
      nodes.push({ prelude, children: parseBlock(css, state) });
    } else {
      const text = css.slice(start, state.i).trim();
      if (text) nodes.push({ decl: text });
      if (ch === ";") state.i++;
    }
  }
  return nodes;
}

function findLayer(nodes, name) {
  for (const node of nodes) {
    if (node.prelude === `@layer ${name}`) return node;
    if (node.children) {
      const found = findLayer(node.children, name);
      if (found) return found;
    }
  }
  return null;
}

function serialize(node, indent = "") {
  if (node.decl !== undefined) return `${indent}${node.decl};`;
  const inner = node.children.map((child) => serialize(child, indent + "  ")).join("\n");
  return `${indent}${node.prelude} {\n${inner}\n${indent}}`;
}

/* --------------------------------------------------------- rule processing */

function firstClass(selector) {
  const match = selector.match(/\.((?:[^\s.:,>+~()[\]\\]|\\.)+)/);
  return match ? match[1].replace(/\\(.)/g, "$1") : null;
}

function utilityPart(className) {
  let depth = 0;
  let cut = -1;
  for (let i = 0; i < className.length; i++) {
    const ch = className[i];
    if (ch === "[") depth++;
    else if (ch === "]") depth--;
    else if (ch === ":" && depth === 0) cut = i;
  }
  return className.slice(cut + 1).replace(/^[!-]/, "");
}

function familyFor(utility) {
  if (utility.startsWith("ring-offset-")) return "bg";
  for (const [key, family] of Object.entries(FAMILIES)) {
    if (family.prefixes.some((prefix) => utility.startsWith(prefix))) return key;
  }
  return null;
}

function recolor(text, family) {
  let changed = false;
  let out = text.replace(/var\(--color-([a-z]+(?:-\d+)?)\)/g, (whole, name) => {
    const value = lookup(family.table, name);
    if (value === undefined) return whole;
    changed = true;
    return value;
  });
  out = out.replace(/#[0-9a-fA-F]{6}\b/g, (hex) => {
    const mapped = family.hex[hex.toLowerCase()];
    if (mapped === undefined) return hex;
    changed = true;
    return mapped;
  });
  return { text: out, changed };
}

function transform(node, family, state) {
  if (node.decl !== undefined) {
    const { text, changed } = recolor(node.decl, family);
    if (changed) state.changed = true;
    return { decl: text };
  }
  return { prelude: node.prelude, children: node.children.map((child) => transform(child, family, state)) };
}

const KEEP_LIGHT = ":not(:where(.tp-dark-surface, .tp-dark-surface *))";

/**
 * `.dark` scope + exclusion of "always-dark" panels (class `tp-dark-surface`):
 * everything inside such a panel keeps its light-mode (light-on-dark) colours.
 */
function prefixSelector(prelude) {
  return prelude
    .split(/,(?![^(]*\))/)
    .map((part) => {
      const trimmed = part.trim();
      const match = trimmed.match(/\.((?:[^\s.:,>+~()[\]\\]|\\.)+)/);
      if (!match) return `.dark ${trimmed}`;
      const end = match.index + match[0].length;
      return `.dark ${trimmed.slice(0, end)}${KEEP_LIGHT}${trimmed.slice(end)}`;
    })
    .join(",\n");
}

function isStyleRule(node) {
  return node.children && !node.prelude.startsWith("@") && node.children.some((c) => c.decl !== undefined || c.children);
}

function collect(nodes, family, familyKey, out) {
  for (const node of nodes) {
    if (node.decl !== undefined || !node.children) continue;
    if (node.prelude.startsWith("@")) {
      const inner = [];
      collect(node.children, family, familyKey, inner);
      if (inner.length) out.push({ prelude: node.prelude, children: inner });
      continue;
    }
    if (!isStyleRule(node)) continue;
    const className = firstClass(node.prelude);
    if (!className) continue;
    const utility = utilityPart(className);
    if (familyFor(utility) !== familyKey) continue;
    // Translucent white on dark hero panels must stay a light highlight.
    if (className.includes("print:")) continue;
    const alpha = utility.match(/^(?:bg|from|via|to)-white\/(?:(\d+)|\[(0?\.\d+)\])$/);
    if (familyKey === "bg" && alpha) {
      const fraction = alpha[1] !== undefined ? Number(alpha[1]) / 100 : Number(alpha[2]);
      if (fraction < 0.5) continue;
    }
    const state = { changed: false };
    const transformed = transform(node, family, state);
    if (!state.changed) continue;
    out.push({ prelude: prefixSelector(node.prelude), children: transformed.children });
  }
}

/* -------------------------------------------------------------- candidates */

function listFiles(dir, extensions, found = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) listFiles(full, extensions, found);
    else if (extensions.some((ext) => entry.name.endsWith(ext))) found.push(full);
  }
  return found;
}

function collectCandidates() {
  const candidates = new Set();
  for (const file of listFiles(SRC, [".ts", ".tsx", ".js", ".jsx", ".html"])) {
    const text = fs.readFileSync(file, "utf8");
    for (const match of text.matchAll(/[^\s"'`<>={}]+/g)) candidates.add(match[0]);
  }
  return [...candidates];
}

/* -------------------------------------------------------------------- main */

async function loadTailwind() {
  const override = process.env.TENDERPILOT_TAILWIND_DIR;
  let dir = override;
  if (!dir) {
    const requireFromFrontend = createRequire(path.join(FRONTEND, "package.json"));
    try {
      dir = path.dirname(requireFromFrontend.resolve("tailwindcss/package.json"));
    } catch {
      console.error("Cannot find tailwindcss. Run `npm install` inside frontend/ first.");
      process.exit(1);
    }
  }
  const lib = await import(pathToFileURL(path.join(dir, "dist", "lib.mjs")).href);
  return { compile: lib.compile, dir };
}

async function main() {
  const { compile, dir } = await loadTailwind();
  const candidates = collectCandidates();

  const loadStylesheet = async (id, base) => {
    let file;
    if (id === "tailwindcss") file = path.join(dir, "index.css");
    else if (id.startsWith("tailwindcss/")) file = path.join(dir, id.slice("tailwindcss/".length) + (id.endsWith(".css") ? "" : ".css"));
    else file = path.resolve(base, id);
    return { path: file, base: path.dirname(file), content: fs.readFileSync(file, "utf8") };
  };

  const sections = [];
  let total = 0;

  for (const [key, family] of Object.entries(FAMILIES)) {
    const source = [
      '@import "tailwindcss/theme.css" layer(theme);',
      '@import "tailwindcss/utilities.css" layer(utilities);',
      themeCss(family.table),
    ].join("\n");
    const compiler = await compile(source, { base: dir, loadStylesheet });
    const built = compiler.build(candidates);
    const layer = findLayer(parseBlock(built, { i: 0 }), "utilities");
    if (!layer) throw new Error("Tailwind output did not contain an utilities layer.");
    const rules = [];
    collect(layer.children, family, key, rules);
    total += rules.length;
    sections.push(`/* ${key} */\n${rules.map((rule) => serialize(rule)).join("\n")}`);
  }

  const banner = [
    "/*",
    " * GENERATED FILE — do not edit by hand.",
    " * Source: scripts/generate-dark-theme.mjs",
    " * Dark-mode overrides for the Tailwind colour utilities used in frontend/src.",
    " */",
  ].join("\n");

  fs.writeFileSync(OUTPUT, `${banner}\n\n${sections.join("\n\n")}\n`);
  console.log(`Wrote ${path.relative(ROOT, OUTPUT)} (${total} rules).`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
