"use client";

import { Menu, Search } from "lucide-react";

import { NotificationBell } from "@/features/notifications/notification-bell";
import { openCommandPalette } from "@/features/navigation/palette-events";
import { Kbd } from "@/components/ui/kbd";

import { Breadcrumbs } from "./breadcrumbs";
import { ThemeToggle } from "./theme-toggle";
import { UserMenu } from "./user-menu";

function modifierLabel(): string {
  return typeof navigator !== "undefined" && /Mac|iPhone|iPad/.test(navigator.userAgent) ? "⌘" : "Ctrl";
}

export function Topbar({ onOpenNavigation }: { onOpenNavigation: () => void }) {
  return (
    <header
      data-app-chrome
      className="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-line bg-surface/80 px-4 backdrop-blur-md md:px-8"
    >
      <button
        type="button"
        onClick={onOpenNavigation}
        aria-label="Open navigation"
        className="grid size-10 shrink-0 place-items-center rounded-xl border border-line bg-surface text-muted shadow-sm transition hover:text-ink lg:hidden"
      >
        <Menu className="size-4" />
      </button>

      <div className="min-w-0 flex-1 md:flex-none md:max-w-[16rem] lg:max-w-[22rem]">
        <Breadcrumbs />
      </div>

      <div className="hidden min-w-0 flex-1 justify-center md:flex">
        <button
          type="button"
          onClick={openCommandPalette}
          aria-label="Search TenderPilot"
          className="flex h-10 w-full max-w-md items-center gap-2.5 rounded-xl border border-line bg-surface-2 px-3.5 text-left text-sm text-faint transition hover:border-line-strong hover:bg-surface"
        >
          <Search className="size-4 shrink-0" aria-hidden="true" />
          <span className="flex-1 truncate">Search tenders, buyers, pages…</span>
          <span className="flex items-center gap-1">
            <Kbd>{modifierLabel()}</Kbd>
            <Kbd>K</Kbd>
          </span>
        </button>
      </div>

      <div className="ml-auto flex shrink-0 items-center gap-2 md:ml-0">
        <button
          type="button"
          onClick={openCommandPalette}
          aria-label="Search TenderPilot"
          className="grid size-10 place-items-center rounded-xl border border-line bg-surface text-muted shadow-sm transition hover:text-ink md:hidden"
        >
          <Search className="size-4" />
        </button>
        <ThemeToggle />
        <NotificationBell />
        <UserMenu />
      </div>
    </header>
  );
}
