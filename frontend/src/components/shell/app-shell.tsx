"use client";

import { useState } from "react";

import { NotificationsProvider } from "@/features/notifications/notifications-context";

import { ConnectionBanner } from "./connection-banner";
import { WorkspaceTitle } from "./document-title";
import { MobileDrawer } from "./mobile-drawer";
import { Sidebar } from "./sidebar";
import { Topbar } from "./topbar";

export function AppShell({ children }: { children: React.ReactNode }) {
  const [navigationOpen, setNavigationOpen] = useState(false);

  return (
    <NotificationsProvider>
      <a
        href="#main-content"
        data-app-chrome
        className="sr-only z-[80] rounded-xl bg-navy px-4 py-2 text-sm font-semibold text-white focus:not-sr-only focus:fixed focus:left-4 focus:top-4"
      >
        Skip to main content
      </a>

      <div className="min-h-screen lg:flex">
        <Sidebar />
        <MobileDrawer open={navigationOpen} onClose={() => setNavigationOpen(false)} />

        <div className="min-w-0 flex-1">
          <Topbar onOpenNavigation={() => setNavigationOpen(true)} />
          <ConnectionBanner />
          <WorkspaceTitle />
          <main
            id="main-content"
            tabIndex={-1}
            data-print-reset
            className="px-4 py-6 outline-none sm:px-5 md:px-8 lg:px-10 lg:py-9"
          >
            {children}
          </main>
        </div>
      </div>
    </NotificationsProvider>
  );
}
