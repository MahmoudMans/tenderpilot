"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { useAuth } from "@/features/auth/auth-context";
import { navigationIcons } from "@/features/navigation/navigation-icons";
import { productNavigationGroups } from "@/features/navigation/product-navigation";
import { useNotifications } from "@/features/notifications/notifications-context";
import { cn } from "@/lib/cn";

export function roleLabel(role: string): string {
  return role
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function isActive(pathname: string, href: string): boolean {
  return pathname === href || pathname.startsWith(`${href}/`);
}

function activeHref(pathname: string): string | null {
  if (pathname.startsWith("/bid-workspaces/")) return "/bid-decisions";
  let best: string | null = null;
  for (const group of productNavigationGroups) {
    for (const item of group.items) {
      if (item.secondary) continue;
      if (isActive(pathname, item.href) && (best === null || item.href.length > best.length)) {
        best = item.href;
      }
    }
  }
  return best;
}

type SidebarNavProps = {
  collapsed?: boolean;
  onNavigate?: () => void;
};

export function SidebarNav({ collapsed = false, onNavigate }: SidebarNavProps) {
  const pathname = usePathname();
  const { unread } = useNotifications();
  const current = activeHref(pathname);

  return (
    <nav aria-label="Primary" className="flex-1 space-y-6 overflow-y-auto px-3 pb-4 pt-2">
      {productNavigationGroups.map((group) => {
        const items = group.items.filter((item) => !item.secondary);
        if (items.length === 0) return null;
        return (
          <div key={group.title}>
            {collapsed ? (
              <div className="mx-3 mb-2 h-px bg-line" aria-hidden="true" />
            ) : (
              <p className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-[0.14em] text-faint">
                {group.title}
              </p>
            )}
            <ul className="space-y-0.5">
              {items.map((item) => {
                const Icon = navigationIcons[item.icon];
                const active = item.href === current;
                const badge = item.href === "/notifications" && unread > 0 ? unread : null;
                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      onClick={onNavigate}
                      title={collapsed ? item.shortTitle : undefined}
                      aria-current={active ? "page" : undefined}
                      className={cn(
                        "group relative flex items-center rounded-xl text-sm font-medium transition",
                        collapsed ? "h-10 justify-center" : "gap-3 px-3 py-2",
                        active
                          ? "bg-brand-soft text-brand"
                          : "text-ink-2 hover:bg-surface-3 hover:text-ink",
                      )}
                    >
                      {active ? (
                        <span
                          aria-hidden="true"
                          className="absolute inset-y-2 left-0 w-[3px] rounded-r-full bg-brand"
                        />
                      ) : null}
                      <Icon className="size-[18px] shrink-0" strokeWidth={active ? 2.2 : 1.9} />
                      {collapsed ? null : <span className="min-w-0 flex-1 truncate">{item.shortTitle}</span>}
                      {badge !== null ? (
                        collapsed ? (
                          <span
                            aria-label={`${badge} unread`}
                            className="absolute right-1.5 top-1.5 size-2 rounded-full bg-danger ring-2 ring-surface"
                          />
                        ) : (
                          <span className="ml-auto grid h-5 min-w-5 place-items-center rounded-full bg-danger px-1.5 text-[10px] font-bold text-white">
                            {badge > 99 ? "99+" : badge}
                          </span>
                        )
                      ) : null}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        );
      })}
    </nav>
  );
}

export function SidebarOrganization({ collapsed = false }: { collapsed?: boolean }) {
  const { user } = useAuth();
  if (collapsed) return null;
  return (
    <div className="mx-3 mb-3 rounded-2xl border border-line bg-surface-2 p-3.5">
      <p className="truncate text-sm font-semibold text-ink">{user?.organization.name ?? "Organization"}</p>
      <div className="mt-1.5 flex items-center justify-between gap-2">
        <p className="truncate text-xs text-muted">{user?.email ?? ""}</p>
        {user ? (
          <span className="shrink-0 rounded-full border border-line bg-surface px-2 py-0.5 text-[10px] font-semibold text-ink-2">
            {roleLabel(user.role)}
          </span>
        ) : null}
      </div>
    </div>
  );
}
