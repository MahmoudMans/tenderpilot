"use client";

import { useCallback, useMemo } from "react";

import { useStoredState } from "@/lib/storage";
import { STORAGE_KEYS } from "@/lib/storage-keys";

export type SavedView<T> = {
  id: string;
  name: string;
  value: T;
  createdAt: number;
};

type Store = Record<string, Array<SavedView<unknown>>>;

const EMPTY_STORE: Store = {};
const MAX_VIEWS_PER_SCOPE = 12;

/** Per-browser saved filter presets, grouped by `scope` (for example "opportunities"). */
export function useSavedViews<T>(scope: string) {
  const [store, setStore] = useStoredState<Store>(STORAGE_KEYS.savedViews, EMPTY_STORE);

  const views = useMemo(() => (store[scope] ?? []) as Array<SavedView<T>>, [store, scope]);

  const save = useCallback(
    (name: string, value: T) => {
      const trimmed = name.trim();
      if (trimmed === "") return;
      setStore((previous) => {
        const existing = previous[scope] ?? [];
        const entry: SavedView<unknown> = {
          id: `${scope}-${Date.now().toString(36)}-${Math.floor(performance.now())}`,
          name: trimmed.slice(0, 60),
          value,
          createdAt: Date.now(),
        };
        const withoutSameName = existing.filter((view) => view.name.toLowerCase() !== trimmed.toLowerCase());
        return { ...previous, [scope]: [entry, ...withoutSameName].slice(0, MAX_VIEWS_PER_SCOPE) };
      });
    },
    [scope, setStore],
  );

  const remove = useCallback(
    (id: string) => {
      setStore((previous) => ({
        ...previous,
        [scope]: (previous[scope] ?? []).filter((view) => view.id !== id),
      }));
    },
    [scope, setStore],
  );

  return { views, save, remove };
}
