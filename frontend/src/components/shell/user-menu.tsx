"use client";

import { ChevronDown, Grid2X2, Keyboard, LogOut, Monitor, Moon, Settings, Sun } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useRef, useState } from "react";

import { useDismiss } from "@/components/ui/use-dismiss";
import { useAuth } from "@/features/auth/auth-context";
import { markSignedOutOnPurpose } from "@/features/auth/return-to";
import { openShortcutsHelp } from "@/features/navigation/palette-events";
import { useTheme, type ThemeMode } from "@/features/theme/theme-provider";
import { cn } from "@/lib/cn";

import { roleLabel } from "./sidebar-nav";

function initials(value: string): string {
  return value
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0))
    .join("")
    .toUpperCase();
}

const themeOptions: ReadonlyArray<{ value: ThemeMode; label: string; icon: typeof Sun }> = [
  { value: "light", label: "Light", icon: Sun },
  { value: "dark", label: "Dark", icon: Moon },
  { value: "system", label: "Auto", icon: Monitor },
];

export function UserMenu() {
  const router = useRouter();
  const { user, logout } = useAuth();
  const { mode, setMode } = useTheme();
  const [open, setOpen] = useState(false);
  const [signingOut, setSigningOut] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  useDismiss(rootRef, open, () => setOpen(false));

  async function signOut() {
    setSigningOut(true);
    try {
      markSignedOutOnPurpose();
      await logout();
      router.replace("/login");
    } finally {
      setSigningOut(false);
    }
  }

  const itemClass =
    "flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm text-ink-2 transition hover:bg-surface-3 hover:text-ink";

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label="Account menu"
        className={cn(
          "flex h-10 items-center gap-2.5 rounded-xl border bg-surface pl-1.5 pr-2.5 shadow-sm transition hover:border-line-strong",
          open ? "border-brand-line" : "border-line",
        )}
      >
        <span className="tp-dark-surface grid size-7 place-items-center rounded-lg bg-[#14213d] text-[11px] font-semibold text-white">
          {user ? initials(user.fullName) : "TP"}
        </span>
        <span className="hidden max-w-36 text-left xl:block">
          <span className="block truncate text-xs font-semibold leading-4 text-ink">
            {user?.fullName ?? "TenderPilot"}
          </span>
          <span className="block truncate text-[11px] leading-4 text-muted">{user?.organization.name ?? ""}</span>
        </span>
        <ChevronDown className="size-3.5 text-faint" aria-hidden="true" />
      </button>

      {open ? (
        <div
          role="menu"
          aria-label="Account"
          className="absolute right-0 top-12 z-50 w-72 animate-pop rounded-2xl border border-line bg-surface p-2 shadow-pop"
        >
          <div className="px-2.5 pb-2 pt-1.5">
            <p className="truncate text-sm font-semibold text-ink">{user?.fullName}</p>
            <p className="truncate text-xs text-muted">{user?.email}</p>
            {user ? (
              <p className="mt-2 inline-flex rounded-full border border-line bg-surface-2 px-2 py-0.5 text-[10px] font-semibold text-ink-2">
                {roleLabel(user.role)} · {user.organization.name}
              </p>
            ) : null}
          </div>

          <div className="mx-1 my-1 h-px bg-line" />

          <div className="px-1 py-1.5">
            <p className="px-1.5 pb-1.5 text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">Theme</p>
            <div role="radiogroup" aria-label="Theme" className="grid grid-cols-3 gap-1 rounded-xl bg-surface-2 p-1">
              {themeOptions.map((option) => {
                const Icon = option.icon;
                const active = mode === option.value;
                return (
                  <button
                    key={option.value}
                    type="button"
                    role="radio"
                    aria-checked={active}
                    onClick={() => setMode(option.value)}
                    className={cn(
                      "flex h-8 items-center justify-center gap-1.5 rounded-lg text-xs font-semibold transition",
                      active ? "bg-surface text-ink shadow-sm ring-1 ring-line" : "text-muted hover:text-ink",
                    )}
                  >
                    <Icon className="size-3.5" />
                    {option.label}
                  </button>
                );
              })}
            </div>
          </div>

          <div className="mx-1 my-1 h-px bg-line" />

          <Link href="/settings" role="menuitem" onClick={() => setOpen(false)} className={itemClass}>
            <Settings className="size-4" /> Preferences
          </Link>
          <Link href="/workspace" role="menuitem" onClick={() => setOpen(false)} className={itemClass}>
            <Grid2X2 className="size-4" /> Workspace map
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={() => {
              setOpen(false);
              openShortcutsHelp();
            }}
            className={itemClass}
          >
            <Keyboard className="size-4" /> Keyboard shortcuts
            <span className="ml-auto text-xs text-faint">?</span>
          </button>

          <div className="mx-1 my-1 h-px bg-line" />

          <button
            type="button"
            role="menuitem"
            disabled={signingOut}
            onClick={() => void signOut()}
            className={cn(itemClass, "text-danger hover:bg-danger-soft hover:text-danger disabled:opacity-50")}
          >
            <LogOut className="size-4" /> {signingOut ? "Signing out…" : "Sign out"}
          </button>
        </div>
      ) : null}
    </div>
  );
}
