"use client";

import { PanelLeftClose, PanelLeftOpen } from "lucide-react";

import { useStoredState } from "@/lib/storage";
import { cn } from "@/lib/cn";

import { BrandMark } from "./brand-mark";
import { SidebarNav, SidebarOrganization } from "./sidebar-nav";

const SIDEBAR_KEY = "tp-sidebar";

export function Sidebar() {
  const [state, setState] = useStoredState<"expanded" | "collapsed">(SIDEBAR_KEY, "expanded");
  const collapsed = state === "collapsed";

  return (
    <aside
      data-app-chrome
      className={cn(
        "sticky top-0 hidden h-screen shrink-0 flex-col border-r border-line bg-surface/95 transition-[width] duration-200 lg:flex",
        collapsed ? "w-[76px]" : "w-72",
      )}
    >
      <div className={cn("flex h-16 shrink-0 items-center", collapsed ? "justify-center" : "gap-3 px-5")}>
        <BrandMark />
        {collapsed ? null : (
          <div className="min-w-0">
            <p className="text-lg font-semibold leading-5 tracking-tight text-ink">TenderPilot</p>
            <p className="text-xs text-muted">Procurement intelligence</p>
          </div>
        )}
      </div>

      <SidebarNav collapsed={collapsed} />
      <SidebarOrganization collapsed={collapsed} />

      <div className="border-t border-line p-3">
        <button
          type="button"
          onClick={() => setState(collapsed ? "expanded" : "collapsed")}
          aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          className={cn(
            "flex h-9 w-full items-center rounded-xl text-xs font-medium text-muted transition hover:bg-surface-3 hover:text-ink",
            collapsed ? "justify-center" : "gap-2.5 px-3",
          )}
        >
          {collapsed ? <PanelLeftOpen className="size-4" /> : <PanelLeftClose className="size-4" />}
          {collapsed ? null : "Collapse"}
        </button>
      </div>
    </aside>
  );
}
