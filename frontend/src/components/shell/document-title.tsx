"use client";

import { usePathname } from "next/navigation";
import { useEffect } from "react";

import { breadcrumbsForPath } from "@/features/navigation/product-navigation";
import { useNotifications } from "@/features/notifications/notifications-context";

/** Sets the browser tab title to "<title> | TenderPilot". */
export function useDocumentTitle(title: string): void {
  useEffect(() => {
    document.title = `${title} | TenderPilot`;
  }, [title]);
}

/**
 * Titles every workspace page from its place in the navigation ("Planning, Bid workspace"), so tabs that
 * are open side by side can be told apart, and puts the number of unread alerts in front: "(3) Calendar".
 */
export function WorkspaceTitle() {
  const pathname = usePathname();
  const { unread } = useNotifications();
  const trail = breadcrumbsForPath(pathname);
  const page = (trail.length > 1 ? trail.slice(1) : trail)
    .map((crumb) => crumb.label)
    .reverse()
    .join(" \u00b7 ");
  useDocumentTitle(`${unread > 0 ? `(${unread > 99 ? "99+" : unread}) ` : ""}${page}`);
  return null;
}
