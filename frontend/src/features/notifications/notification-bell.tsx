"use client";

import { Bell, BellRing, CheckCheck } from "lucide-react";
import Link from "next/link";
import { useRef, useState } from "react";

import { Badge, type BadgeTone } from "@/components/ui/badge";
import { useDismiss } from "@/components/ui/use-dismiss";
import { cn } from "@/lib/cn";
import { timeAgo } from "@/lib/relative-time";
import { useNow } from "@/lib/use-clock";

import type { NotificationSeverity } from "./notification-types";
import { useNotifications } from "./notifications-context";

const severityTone: Record<NotificationSeverity, BadgeTone> = {
  CRITICAL: "danger",
  HIGH: "warn",
  MEDIUM: "info",
  LOW: "neutral",
};

export function NotificationBell() {
  const { center, unread, critical, acknowledge, markAllRead } = useNotifications();
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  const now = useNow();
  useDismiss(rootRef, open, () => setOpen(false));

  const recent = (center?.notifications ?? []).filter((item) => item.active && item.status !== "DISMISSED").slice(0, 6);

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-haspopup="dialog"
        aria-expanded={open}
        aria-label={unread > 0 ? `Notifications, ${unread} unread` : "Notifications"}
        className={cn(
          "relative grid size-10 place-items-center rounded-xl border bg-surface text-muted shadow-sm transition hover:border-line-strong hover:text-ink",
          open ? "border-brand-line text-ink" : "border-line",
        )}
      >
        {critical > 0 ? <BellRing className="size-4 text-danger" /> : <Bell className="size-4" />}
        {unread > 0 ? (
          <span className="absolute -right-1 -top-1 grid h-[18px] min-w-[18px] place-items-center rounded-full bg-danger px-1 text-[10px] font-bold text-white ring-2 ring-surface">
            {unread > 99 ? "99+" : unread}
          </span>
        ) : null}
      </button>

      {open ? (
        <div
          role="dialog"
          aria-label="Notifications"
          className="absolute right-0 top-12 z-50 w-[min(24rem,calc(100vw-2rem))] animate-pop overflow-hidden rounded-2xl border border-line bg-surface shadow-pop"
        >
          <div className="flex items-center justify-between gap-3 border-b border-line px-4 py-3">
            <div>
              <p className="text-sm font-semibold text-ink">Notifications</p>
              <p className="text-xs text-muted">
                {unread > 0 ? `${unread} unread` : "You're all caught up"}
                {critical > 0 ? ` · ${critical} critical` : ""}
              </p>
            </div>
            {unread > 0 ? (
              <button
                type="button"
                onClick={() => void markAllRead()}
                className="inline-flex items-center gap-1.5 rounded-lg px-2 py-1 text-xs font-semibold text-brand transition hover:bg-brand-soft"
              >
                <CheckCheck className="size-3.5" />
                Mark all read
              </button>
            ) : null}
          </div>

          {recent.length === 0 ? (
            <div className="px-4 py-10 text-center">
              <Bell className="mx-auto size-6 text-faint" />
              <p className="mt-3 text-sm font-medium text-ink">Nothing needs your attention</p>
              <p className="mt-1 text-xs text-muted">Deadline and watchlist alerts will show up here.</p>
            </div>
          ) : (
            <ul className="max-h-[22rem] divide-y divide-line overflow-y-auto">
              {recent.map((item) => (
                <li key={item.id}>
                  <Link
                    href={item.href}
                    onClick={() => {
                      setOpen(false);
                      if (item.status === "UNREAD") void acknowledge(item.id);
                    }}
                    className="flex gap-3 px-4 py-3 transition hover:bg-surface-2"
                  >
                    <span
                      aria-hidden="true"
                      className={cn(
                        "mt-1.5 size-2 shrink-0 rounded-full",
                        item.status === "UNREAD" ? "bg-brand" : "bg-transparent",
                      )}
                    />
                    <span className="min-w-0 flex-1">
                      <span className="flex items-center gap-2">
                        <Badge tone={severityTone[item.severity]}>{item.severity.toLowerCase()}</Badge>
                        <span className="text-[11px] text-faint">{timeAgo(item.updatedAt, now)}</span>
                      </span>
                      <span className="mt-1 block truncate text-sm font-medium text-ink">{item.title}</span>
                      <span className="mt-0.5 line-clamp-2 block text-xs leading-5 text-muted">{item.detail}</span>
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )}

          <div className="border-t border-line bg-surface-2 px-4 py-2.5">
            <Link
              href="/notifications"
              onClick={() => setOpen(false)}
              className="text-xs font-semibold text-brand transition hover:text-brand-strong"
            >
              Open notification center →
            </Link>
          </div>
        </div>
      ) : null}
    </div>
  );
}
