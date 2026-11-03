"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

import { useAuth } from "@/features/auth/auth-context";

import type { NotificationCenter, WatchlistItem, WatchlistTargetType } from "./notification-types";
import { normalizeWatchKey } from "./watch-keys";

const POLL_INTERVAL_MS = 120_000;

type NotificationsContextValue = {
  center: NotificationCenter | null;
  unread: number;
  critical: number;
  failed: boolean;
  refresh: () => Promise<void>;
  /** Takes a newer copy of the centre from the Notifications page, which loads and changes its own. */
  adopt: (next: NotificationCenter) => void;
  acknowledge: (id: string) => Promise<void>;
  markAllRead: () => Promise<void>;
  watchlist: ReadonlyArray<WatchlistItem>;
  /** Returns the watchlist entry for a target, or null when it is not watched. */
  findWatch: (type: WatchlistTargetType, targetKey: string) => WatchlistItem | null;
  /** Adds a target to the watchlist. Resolves to true on success. Creating is idempotent. */
  watch: (type: WatchlistTargetType, targetKey: string, label: string | null) => Promise<boolean>;
  /** Removes a watchlist entry. Resolves to true on success. */
  unwatch: (id: string) => Promise<boolean>;
};

const NotificationsContext = createContext<NotificationsContextValue | null>(null);

/**
 * Keeps one shared, lightly polled copy of the notification centre for the
 * shell (sidebar badge + bell menu). The Notifications page keeps its own copy and hands every
 * change to `adopt`, so the bell and the badge follow what is done there.
 */
export function NotificationsProvider({ children }: { children: ReactNode }) {
  const { api } = useAuth();
  const [center, setCenter] = useState<NotificationCenter | null>(null);
  const [failed, setFailed] = useState(false);

  const refresh = useCallback(async () => {
    try {
      setCenter(await api<NotificationCenter>("/api/v1/notifications"));
      setFailed(false);
    } catch {
      setFailed(true);
    }
  }, [api]);

  useEffect(() => {
    let cancelled = false;

    function load() {
      api<NotificationCenter>("/api/v1/notifications")
        .then((result) => {
          if (cancelled) return;
          setCenter(result);
          setFailed(false);
        })
        .catch(() => {
          if (!cancelled) setFailed(true);
        });
    }

    function onVisible() {
      if (document.visibilityState === "visible") load();
    }

    load();
    const timer = window.setInterval(load, POLL_INTERVAL_MS);
    document.addEventListener("visibilitychange", onVisible);
    return () => {
      cancelled = true;
      window.clearInterval(timer);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, [api]);

  const adopt = useCallback((next: NotificationCenter) => {
    setCenter(next);
    setFailed(false);
  }, []);

  const acknowledge = useCallback(
    async (id: string) => {
      try {
        setCenter(await api<NotificationCenter>(`/api/v1/notifications/${id}/acknowledge`, { method: "POST" }));
      } catch {
        setFailed(true);
      }
    },
    [api],
  );

  const markAllRead = useCallback(async () => {
    try {
      setCenter(await api<NotificationCenter>("/api/v1/notifications/read-all", { method: "POST" }));
    } catch {
      setFailed(true);
    }
  }, [api]);

  const watchlist = useMemo<ReadonlyArray<WatchlistItem>>(() => center?.watchlist ?? [], [center]);

  const findWatch = useCallback(
    (type: WatchlistTargetType, targetKey: string): WatchlistItem | null => {
      const wanted = normalizeWatchKey(type, targetKey);
      return watchlist.find((item) => item.type === type && normalizeWatchKey(type, item.targetKey) === wanted) ?? null;
    },
    [watchlist],
  );

  const watch = useCallback(
    async (type: WatchlistTargetType, targetKey: string, label: string | null) => {
      try {
        setCenter(
          await api<NotificationCenter>("/api/v1/notifications/watchlist", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ type, targetKey: normalizeWatchKey(type, targetKey), label }),
          }),
        );
        return true;
      } catch {
        return false;
      }
    },
    [api],
  );

  const unwatch = useCallback(
    async (id: string) => {
      try {
        setCenter(await api<NotificationCenter>(`/api/v1/notifications/watchlist/${id}`, { method: "DELETE" }));
        return true;
      } catch {
        return false;
      }
    },
    [api],
  );

  // The server keeps counting an alert that was dismissed and then seen again by a refresh, so the
  // numbers come from the alerts that are actually shown.
  const counts = useMemo(() => {
    const shown = (center?.notifications ?? []).filter((item) => item.active && item.status !== "DISMISSED");
    return {
      unread: shown.filter((item) => item.status === "UNREAD").length,
      critical: shown.filter((item) => item.severity === "CRITICAL").length,
    };
  }, [center]);

  const value = useMemo<NotificationsContextValue>(
    () => ({
      center,
      unread: counts.unread,
      critical: counts.critical,
      failed,
      refresh,
      adopt,
      acknowledge,
      markAllRead,
      watchlist,
      findWatch,
      watch,
      unwatch,
    }),
    [center, counts, failed, refresh, adopt, acknowledge, markAllRead, watchlist, findWatch, watch, unwatch],
  );

  return <NotificationsContext.Provider value={value}>{children}</NotificationsContext.Provider>;
}

export function useNotifications(): NotificationsContextValue {
  const context = useContext(NotificationsContext);
  if (context === null) {
    throw new Error("useNotifications must be used inside NotificationsProvider");
  }
  return context;
}
