"use client";

import {
  ArrowRight,
  CornerDownLeft,
  FileSearch,
  Keyboard,
  LoaderCircle,
  Moon,
  Rows3,
  Search,
  Sun,
  Clock,
  type LucideIcon,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useId, useMemo, useRef, useState } from "react";

import { Dialog } from "@/components/ui/dialog";
import { Kbd } from "@/components/ui/kbd";
import { useAuth } from "@/features/auth/auth-context";
import { useTheme } from "@/features/theme/theme-provider";
import type { ProcurementSearchResponse } from "@/features/search/search-types";
import { cn } from "@/lib/cn";

import { navigationIcons } from "./navigation-icons";
import { OPEN_PALETTE_EVENT, OPEN_SHORTCUTS_EVENT } from "./palette-events";
import { productNavigationItems } from "./product-navigation";
import { useRecentItems, type RecentItem } from "./recent";

/* -------------------------------------------------------------------------- */
/* Keyboard shortcuts                                                         */
/* -------------------------------------------------------------------------- */

const GO_SHORTCUTS: ReadonlyArray<{ key: string; href: string; label: string }> = [
  { key: "d", href: "/dashboard", label: "Dashboard" },
  { key: "o", href: "/opportunities", label: "Opportunities" },
  { key: "s", href: "/search", label: "Search" },
  { key: "m", href: "/matches", label: "Matches" },
  { key: "b", href: "/bid-decisions", label: "Bid decisions" },
  { key: "c", href: "/calendar", label: "Calendar" },
  { key: "t", href: "/tasks", label: "My tasks" },
  { key: "n", href: "/notifications", label: "Notifications" },
  { key: "p", href: "/company", label: "Company profile" },
  { key: "w", href: "/workspace", label: "Workspace map" },
];

const GO_WINDOW_MS = 1200;

function isTypingTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false;
  const tag = target.tagName;
  return tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT" || target.isContentEditable;
}

/* -------------------------------------------------------------------------- */
/* Palette model                                                              */
/* -------------------------------------------------------------------------- */

type PaletteItem = {
  id: string;
  group: string;
  title: string;
  subtitle?: string;
  icon: LucideIcon;
  href?: string;
  perform?: () => void;
  hint?: string;
};

const recentIcons: Record<RecentItem["kind"], LucideIcon> = {
  opportunity: FileSearch,
  buyer: navigationIcons.buyers,
  supplier: navigationIcons.suppliers,
  bid: navigationIcons.decisions,
};

function searchTokens(query: string): string[] {
  return query.toLowerCase().split(/\s+/).filter(Boolean);
}

function pageScore(
  item: (typeof productNavigationItems)[number],
  tokens: readonly string[],
): number | null {
  if (tokens.length === 0) return 0;
  const title = `${item.title} ${item.shortTitle}`.toLowerCase();
  const haystack = `${title} ${item.description} ${item.keywords.join(" ")}`.toLowerCase();
  if (!tokens.every((token) => haystack.includes(token))) return null;
  return tokens.reduce((score, token) => score + (title.includes(token) ? 2 : 1), 0);
}

type RemoteState = { query: string; hits: ProcurementSearchResponse["hits"] } | null;

function PaletteDialog({ onClose, onShortcuts }: { onClose: () => void; onShortcuts: () => void }) {
  const router = useRouter();
  const { api } = useAuth();
  const { resolved, toggle, density, setDensity } = useTheme();
  const recent = useRecentItems();

  const [query, setQuery] = useState("");
  const [debounced, setDebounced] = useState("");
  const [cursor, setCursor] = useState(0);
  const [remote, setRemote] = useState<RemoteState>(null);

  const inputRef = useRef<HTMLInputElement>(null);
  const listId = useId();

  useEffect(() => {
    inputRef.current?.focus();
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, []);

  // Debounce the query used for live search.
  useEffect(() => {
    const timer = window.setTimeout(() => setDebounced(query.trim()), 250);
    return () => window.clearTimeout(timer);
  }, [query]);

  // Live opportunity search through the existing search endpoint.
  useEffect(() => {
    if (debounced.length < 2) return;
    const controller = new AbortController();
    api<ProcurementSearchResponse>(
      `/api/v1/search/opportunities?q=${encodeURIComponent(debounced)}&size=5`,
      { signal: controller.signal },
    )
      .then((response) => setRemote({ query: debounced, hits: response.hits }))
      .catch(() => {
        if (!controller.signal.aborted) setRemote({ query: debounced, hits: [] });
      });
    return () => controller.abort();
  }, [api, debounced]);

  const searching = debounced.length >= 2 && remote?.query !== debounced;

  const items = useMemo<PaletteItem[]>(() => {
    const tokens = searchTokens(query);
    const trimmed = query.trim();
    const list: PaletteItem[] = [];

    if (trimmed.length >= 2) {
      if (remote?.query === debounced) {
        for (const hit of remote.hits) {
          list.push({
            id: `hit-${hit.opportunity.id}`,
            group: "Opportunities",
            title: hit.opportunity.title,
            subtitle: hit.opportunity.buyerName,
            icon: FileSearch,
            href: `/opportunities/${hit.opportunity.id}`,
          });
        }
      }
      list.push({
        id: "search-all",
        group: "Opportunities",
        title: `Search all opportunities for “${trimmed}”`,
        icon: Search,
        href: `/search?q=${encodeURIComponent(trimmed)}`,
        hint: "Full-text search",
      });
    }

    if (trimmed.length === 0) {
      for (const item of recent.slice(0, 5)) {
        list.push({
          id: `recent-${item.href}`,
          group: "Recently viewed",
          title: item.title,
          subtitle: item.subtitle,
          icon: recentIcons[item.kind] ?? Clock,
          href: item.href,
        });
      }
    }

    const pages = productNavigationItems
      .map((item) => ({ item, score: pageScore(item, tokens) }))
      .filter((entry): entry is { item: (typeof productNavigationItems)[number]; score: number } => entry.score !== null)
      .sort((a, b) => b.score - a.score);

    for (const { item } of pages) {
      list.push({
        id: `page-${item.href}`,
        group: "Go to",
        title: item.shortTitle,
        subtitle: item.description,
        icon: navigationIcons[item.icon],
        href: item.href,
      });
    }

    const actions: PaletteItem[] = [
      {
        id: "action-theme",
        group: "Actions",
        title: resolved === "dark" ? "Switch to light theme" : "Switch to dark theme",
        icon: resolved === "dark" ? Sun : Moon,
        perform: toggle,
      },
      {
        id: "action-density",
        group: "Actions",
        title: density === "compact" ? "Use comfortable density" : "Use compact density",
        icon: Rows3,
        perform: () => setDensity(density === "compact" ? "comfortable" : "compact"),
      },
      {
        id: "action-shortcuts",
        group: "Actions",
        title: "Show keyboard shortcuts",
        icon: Keyboard,
        perform: onShortcuts,
        hint: "?",
      },
    ];

    for (const action of actions) {
      if (tokens.length === 0 || tokens.every((token) => action.title.toLowerCase().includes(token))) {
        list.push(action);
      }
    }

    return list;
  }, [query, debounced, remote, recent, resolved, toggle, density, setDensity, onShortcuts]);

  const activeIndex = Math.min(cursor, Math.max(0, items.length - 1));
  const activeId = items[activeIndex] ? `${listId}-${activeIndex}` : undefined;

  useEffect(() => {
    if (!activeId) return;
    document.getElementById(activeId)?.scrollIntoView({ block: "nearest" });
  }, [activeId]);

  const run = useCallback(
    (item: PaletteItem) => {
      onClose();
      if (item.perform) {
        item.perform();
      } else if (item.href) {
        router.push(item.href);
      }
    },
    [onClose, router],
  );

  function onKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key === "ArrowDown") {
      event.preventDefault();
      setCursor(items.length === 0 ? 0 : (activeIndex + 1) % items.length);
    } else if (event.key === "ArrowUp") {
      event.preventDefault();
      setCursor(items.length === 0 ? 0 : (activeIndex - 1 + items.length) % items.length);
    } else if (event.key === "Home") {
      event.preventDefault();
      setCursor(0);
    } else if (event.key === "End") {
      event.preventDefault();
      setCursor(Math.max(0, items.length - 1));
    } else if (event.key === "Enter") {
      event.preventDefault();
      const item = items[activeIndex];
      if (item) run(item);
    } else if (event.key === "Escape") {
      event.preventDefault();
      onClose();
    }
  }

  const startsGroup = items.map((item, index) => index === 0 || items[index - 1].group !== item.group);

  return (
    <div
      data-app-chrome
      className="fixed inset-0 z-[75] flex items-start justify-center bg-[var(--scrim)] px-3 pt-[10vh] backdrop-blur-sm animate-fade sm:px-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Search TenderPilot"
        className="w-full max-w-2xl animate-pop overflow-hidden rounded-3xl border border-line bg-surface shadow-pop"
      >
        <div className="flex items-center gap-3 border-b border-line px-4">
          {searching ? (
            <LoaderCircle className="size-4 shrink-0 animate-spin text-brand" aria-hidden="true" />
          ) : (
            <Search className="size-4 shrink-0 text-faint" aria-hidden="true" />
          )}
          <input
            ref={inputRef}
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
              setCursor(0);
            }}
            onKeyDown={onKeyDown}
            role="combobox"
            aria-expanded="true"
            aria-controls={listId}
            aria-activedescendant={activeId}
            aria-autocomplete="list"
            aria-label="Search TenderPilot"
            placeholder="Search tenders, buyers, pages and actions…"
            autoComplete="off"
            spellCheck={false}
            className="h-14 min-w-0 flex-1 bg-transparent text-[15px] text-ink outline-none placeholder:text-faint"
          />
          <Kbd>Esc</Kbd>
        </div>

        <ul id={listId} role="listbox" aria-label="Results" className="max-h-[min(26rem,55vh)] overflow-y-auto p-2">
          {items.length === 0 ? (
            <li className="px-4 py-10 text-center text-sm text-muted">
              No matches for “{query.trim()}”. Try a module name, a buyer or a CPV keyword.
            </li>
          ) : null}
          {items.map((item, index) => {
            const Icon = item.icon;
            const heading = startsGroup[index];
            const active = index === activeIndex;
            return (
              <li key={item.id} role="presentation">
                {heading ? (
                  <p
                    className={cn(
                      "px-3 pb-1 text-[11px] font-semibold uppercase tracking-[0.12em] text-faint",
                      index === 0 ? "pt-1" : "pt-3",
                    )}
                  >
                    {item.group}
                  </p>
                ) : null}
                <div
                  id={`${listId}-${index}`}
                  role="option"
                  aria-selected={active}
                  onMouseMove={() => setCursor(index)}
                  onClick={() => run(item)}
                  className={cn(
                    "flex cursor-pointer items-center gap-3 rounded-xl px-3 py-2.5",
                    active ? "bg-brand-soft" : "",
                  )}
                >
                  <span
                    className={cn(
                      "grid size-8 shrink-0 place-items-center rounded-lg border",
                      active ? "border-brand-line bg-surface text-brand" : "border-line bg-surface-2 text-muted",
                    )}
                  >
                    <Icon className="size-4" />
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className={cn("block truncate text-sm font-medium", active ? "text-brand" : "text-ink")}>
                      {item.title}
                    </span>
                    {item.subtitle ? (
                      <span className="block truncate text-xs text-muted">{item.subtitle}</span>
                    ) : null}
                  </span>
                  {item.hint ? <span className="shrink-0 text-xs text-faint">{item.hint}</span> : null}
                  {active ? <ArrowRight className="size-4 shrink-0 text-brand" aria-hidden="true" /> : null}
                </div>
              </li>
            );
          })}
        </ul>

        <div className="flex items-center justify-between gap-4 border-t border-line bg-surface-2 px-4 py-2.5 text-[11px] text-muted">
          <span className="flex items-center gap-3">
            <span className="flex items-center gap-1">
              <Kbd>↑</Kbd>
              <Kbd>↓</Kbd> navigate
            </span>
            <span className="flex items-center gap-1">
              <Kbd>
                <CornerDownLeft className="size-3" aria-label="Enter" />
              </Kbd>{" "}
              open
            </span>
          </span>
          <span className="hidden sm:block">Tip: press / anywhere to search, ? for shortcuts</span>
        </div>
      </div>
    </div>
  );
}

/* -------------------------------------------------------------------------- */
/* Shortcuts help                                                             */
/* -------------------------------------------------------------------------- */

function ShortcutsDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title="Keyboard shortcuts"
      description="Work without leaving the keyboard. Shortcuts are ignored while you type in a field."
    >
      <div className="grid gap-6 sm:grid-cols-2">
        <section>
          <h3 className="text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">General</h3>
          <dl className="mt-3 space-y-2.5 text-sm">
            <div className="flex items-center justify-between gap-3">
              <dt className="text-ink-2">Command palette</dt>
              <dd className="flex gap-1">
                <Kbd>⌘ / Ctrl</Kbd>
                <Kbd>K</Kbd>
              </dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-ink-2">Search</dt>
              <dd>
                <Kbd>/</Kbd>
              </dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-ink-2">This help</dt>
              <dd>
                <Kbd>?</Kbd>
              </dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-ink-2">Close dialogs</dt>
              <dd>
                <Kbd>Esc</Kbd>
              </dd>
            </div>
          </dl>
        </section>
        <section>
          <h3 className="text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">
            Go to… (press G, then)
          </h3>
          <dl className="mt-3 space-y-2.5 text-sm">
            {GO_SHORTCUTS.map((shortcut) => (
              <div key={shortcut.key} className="flex items-center justify-between gap-3">
                <dt className="text-ink-2">{shortcut.label}</dt>
                <dd className="flex gap-1">
                  <Kbd>G</Kbd>
                  <Kbd>{shortcut.key.toUpperCase()}</Kbd>
                </dd>
              </div>
            ))}
          </dl>
        </section>
      </div>
    </Dialog>
  );
}

/* -------------------------------------------------------------------------- */
/* Layer                                                                      */
/* -------------------------------------------------------------------------- */

/**
 * Mounted once by the workspace template. Owns the global command palette
 * (metaKey / ctrlKey + K, or "/"), the "g then …" navigation shortcuts and the
 * shortcut reference. The topbar opens the same palette through window events.
 */
export function ProductExperienceLayer() {
  const router = useRouter();
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [shortcutsOpen, setShortcutsOpen] = useState(false);
  const goPendingSince = useRef(0);

  const closePalette = useCallback(() => setPaletteOpen(false), []);
  const closeShortcuts = useCallback(() => setShortcutsOpen(false), []);
  const openShortcuts = useCallback(() => {
    setPaletteOpen(false);
    setShortcutsOpen(true);
  }, []);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setShortcutsOpen(false);
        setPaletteOpen((value) => !value);
        return;
      }

      if (event.key === "Escape") {
        setPaletteOpen(false);
        return;
      }

      if (event.metaKey || event.ctrlKey || event.altKey || event.defaultPrevented) return;
      if (isTypingTarget(event.target)) return;

      if (event.key === "/") {
        event.preventDefault();
        setPaletteOpen(true);
        return;
      }

      if (event.key === "?") {
        event.preventDefault();
        setShortcutsOpen(true);
        return;
      }

      const key = event.key.toLowerCase();
      const now = performance.now();

      if (key === "g") {
        goPendingSince.current = now;
        return;
      }

      if (goPendingSince.current > 0 && now - goPendingSince.current < GO_WINDOW_MS) {
        const target = GO_SHORTCUTS.find((shortcut) => shortcut.key === key);
        goPendingSince.current = 0;
        if (target) {
          event.preventDefault();
          router.push(target.href);
        }
      }
    }

    function onOpenPalette() {
      setShortcutsOpen(false);
      setPaletteOpen(true);
    }

    function onOpenShortcuts() {
      setPaletteOpen(false);
      setShortcutsOpen(true);
    }

    window.addEventListener("keydown", onKeyDown);
    window.addEventListener(OPEN_PALETTE_EVENT, onOpenPalette);
    window.addEventListener(OPEN_SHORTCUTS_EVENT, onOpenShortcuts);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener(OPEN_PALETTE_EVENT, onOpenPalette);
      window.removeEventListener(OPEN_SHORTCUTS_EVENT, onOpenShortcuts);
    };
  }, [router]);

  return (
    <>
      {paletteOpen ? <PaletteDialog onClose={closePalette} onShortcuts={openShortcuts} /> : null}
      <ShortcutsDialog open={shortcutsOpen} onClose={closeShortcuts} />
    </>
  );
}
