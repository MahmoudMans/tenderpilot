"use client";

import { useEffect } from "react";

import { readStorage, useStoredState, writeStorage } from "@/lib/storage";

export type RecentItem = {
  href: string;
  title: string;
  subtitle?: string;
  kind: "opportunity" | "buyer" | "supplier" | "bid";
  /** Epoch milliseconds. */
  at: number;
};

const RECENT_KEY = "tp-recent";
const MAX_RECENT = 8;
const NONE: RecentItem[] = [];

export function pushRecent(item: Omit<RecentItem, "at">): void {
  const current = readStorage<RecentItem[]>(RECENT_KEY, NONE);
  const next = [
    { ...item, at: Date.now() },
    ...current.filter((entry) => entry.href !== item.href),
  ].slice(0, MAX_RECENT);
  writeStorage(RECENT_KEY, next);
}

export function useRecentItems(): RecentItem[] {
  const [items] = useStoredState<RecentItem[]>(RECENT_KEY, NONE);
  return items;
}

/** Records a detail page visit once its title is known. Pass null while loading. */
export function useRecordVisit(item: Omit<RecentItem, "at"> | null): void {
  const href = item?.href;
  const title = item?.title;
  const subtitle = item?.subtitle;
  const kind = item?.kind;

  useEffect(() => {
    if (!href || !title || !kind) return;
    pushRecent({ href, title, subtitle, kind });
  }, [href, title, subtitle, kind]);
}
