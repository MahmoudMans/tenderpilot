"use client";

import { TriangleAlert } from "lucide-react";
import { useSyncExternalStore } from "react";

function subscribe(onChange: () => void): () => void {
  window.addEventListener("online", onChange);
  window.addEventListener("offline", onChange);
  return () => {
    window.removeEventListener("online", onChange);
    window.removeEventListener("offline", onChange);
  };
}

function isOnline(): boolean {
  return navigator.onLine;
}

function assumeOnline(): boolean {
  return true;
}

/** A strip under the top bar while the browser reports that it is offline. It disappears by itself. */
export function ConnectionBanner() {
  const online = useSyncExternalStore(subscribe, isOnline, assumeOnline);
  if (online) return null;
  return (
    <div
      role="status"
      data-app-chrome
      className="sticky top-16 z-20 border-b border-warn-line bg-warn-soft px-4 py-2 text-center text-xs font-semibold text-warn md:px-8"
    >
      <TriangleAlert className="mr-1.5 inline size-3.5 align-[-2px]" aria-hidden="true" />
      You are offline. TenderPilot cannot load or save anything until your connection returns.
    </div>
  );
}
