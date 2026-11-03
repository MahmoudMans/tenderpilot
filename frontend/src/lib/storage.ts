"use client";

import { useCallback, useMemo, useSyncExternalStore } from "react";

const STORAGE_EVENT = "tenderpilot:storage";

function notify(key: string): void {
  try {
    window.dispatchEvent(new CustomEvent(STORAGE_EVENT, { detail: key }));
  } catch {
    // ignore
  }
}

function readRaw(key: string): string | null {
  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function parse<T>(raw: string | null, fallback: T): T {
  if (raw === null) return fallback;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

/** Safely reads a JSON value from localStorage (private mode and corrupt values fall back). */
export function readStorage<T>(key: string, fallback: T): T {
  return parse(readRaw(key), fallback);
}

export function writeStorage<T>(key: string, value: T): void {
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // Storage can be unavailable (private mode, quota); the UI keeps working in memory.
  }
  notify(key);
}

export function removeStorage(key: string): void {
  try {
    window.localStorage.removeItem(key);
  } catch {
    // ignore
  }
  notify(key);
}

function subscribe(onChange: () => void): () => void {
  window.addEventListener("storage", onChange);
  window.addEventListener(STORAGE_EVENT, onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener(STORAGE_EVENT, onChange);
  };
}

function getServerSnapshot(): string | null {
  return null;
}

/**
 * useState backed by localStorage, built on useSyncExternalStore so it is hydration-safe:
 * the server (and first client render) see `initial`, then the stored value takes over.
 * Pass a stable `initial` (a module constant or primitive).
 */
export function useStoredState<T>(
  key: string,
  initial: T,
): [T, (next: T | ((previous: T) => T)) => void] {
  const raw = useSyncExternalStore(
    subscribe,
    () => readRaw(key),
    getServerSnapshot,
  );

  const value = useMemo(() => parse<T>(raw, initial), [raw, initial]);

  const update = useCallback(
    (next: T | ((previous: T) => T)) => {
      const current = parse<T>(readRaw(key), initial);
      const resolved =
        typeof next === "function" ? (next as (previous: T) => T)(current) : next;
      writeStorage(key, resolved);
    },
    [key, initial],
  );

  return [value, update];
}
