"use client";

import { X } from "lucide-react";
import { useEffect, useRef } from "react";

import { useDismiss } from "@/components/ui/use-dismiss";

import { BrandMark } from "./brand-mark";
import { SidebarNav, SidebarOrganization } from "./sidebar-nav";

type MobileDrawerProps = {
  open: boolean;
  onClose: () => void;
};

export function MobileDrawer({ open, onClose }: MobileDrawerProps) {
  const panelRef = useRef<HTMLDivElement>(null);
  useDismiss(panelRef, open, onClose);

  useEffect(() => {
    if (!open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [open]);

  if (!open) return null;

  return (
    <div data-app-chrome className="fixed inset-0 z-50 lg:hidden">
      <div className="absolute inset-0 animate-fade bg-[var(--scrim)] backdrop-blur-sm" aria-hidden="true" />
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-label="Navigation"
        className="absolute inset-y-0 left-0 flex w-[min(20rem,88vw)] animate-pop flex-col border-r border-line bg-surface shadow-pop"
      >
        <div className="flex h-16 shrink-0 items-center justify-between gap-3 px-5">
          <div className="flex min-w-0 items-center gap-3">
            <BrandMark />
            <p className="text-lg font-semibold tracking-tight text-ink">TenderPilot</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close navigation"
            className="grid size-9 place-items-center rounded-xl text-muted transition hover:bg-surface-3 hover:text-ink"
          >
            <X className="size-4" />
          </button>
        </div>
        <SidebarNav onNavigate={onClose} />
        <SidebarOrganization />
      </div>
    </div>
  );
}
