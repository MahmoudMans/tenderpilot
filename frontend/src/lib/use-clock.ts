"use client";

import { useSyncExternalStore } from "react";

const MINUTE = 60_000;

function subscribe(onChange: () => void): () => void {
  const id = window.setInterval(onChange, MINUTE);
  return () => window.clearInterval(id);
}

function getSnapshot(): number {
  return Math.floor(Date.now() / MINUTE);
}

function getServerSnapshot(): number {
  return 0;
}

/** Current time in epoch milliseconds, rounded to the minute and refreshed every minute. */
export function useNow(): number {
  return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot) * MINUTE;
}
