"use client";

import { Check, Monitor, Moon, RotateCcw, Sun, Trash2 } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { Kbd } from "@/components/ui/kbd";
import { PageHeader } from "@/components/ui/page-header";
import { useToast } from "@/components/ui/toast";
import { useAuth } from "@/features/auth/auth-context";
import { formatEnum } from "@/features/procurement/procurement-format";
import { useTheme, type ThemeMode } from "@/features/theme/theme-provider";
import { cn } from "@/lib/cn";
import { removeStorage } from "@/lib/storage";
import { ALL_STORAGE_KEYS, STORAGE_KEYS } from "@/lib/storage-keys";

const themeChoices: ReadonlyArray<{
  value: ThemeMode;
  label: string;
  description: string;
  icon: typeof Sun;
  swatch: [string, string, string];
}> = [
  {
    value: "light",
    label: "Light",
    description: "Bright surfaces, best in daylight.",
    icon: Sun,
    swatch: ["#f5f7fb", "#ffffff", "#2f6fed"],
  },
  {
    value: "dark",
    label: "Dark",
    description: "Low-glare navy for long sessions.",
    icon: Moon,
    swatch: ["#0a0f1c", "#111a2d", "#6b9aff"],
  },
  {
    value: "system",
    label: "Match system",
    description: "Follows your device setting.",
    icon: Monitor,
    swatch: ["#f5f7fb", "#111a2d", "#2f6fed"],
  },
];

const shortcutRows: ReadonlyArray<{ label: string; keys: string[] }> = [
  { label: "Open the command palette", keys: ["⌘ / Ctrl", "K"] },
  { label: "Search from anywhere", keys: ["/"] },
  { label: "Show all shortcuts", keys: ["?"] },
  { label: "Go to dashboard", keys: ["G", "D"] },
  { label: "Go to opportunities", keys: ["G", "O"] },
  { label: "Go to matches", keys: ["G", "M"] },
  { label: "Go to bid decisions", keys: ["G", "B"] },
  { label: "Go to calendar", keys: ["G", "C"] },
  { label: "Go to my tasks", keys: ["G", "T"] },
  { label: "Go to notifications", keys: ["G", "N"] },
];

function ChoiceCard({
  active,
  onSelect,
  title,
  description,
  children,
}: {
  active: boolean;
  onSelect: () => void;
  title: string;
  description: string;
  children?: React.ReactNode;
}) {
  return (
    <button
      type="button"
      role="radio"
      aria-checked={active}
      onClick={onSelect}
      className={cn(
        "relative rounded-2xl border p-4 text-left transition",
        active ? "border-brand bg-brand-soft ring-1 ring-brand" : "border-line bg-surface hover:border-line-strong",
      )}
    >
      {active ? (
        <span className="absolute right-3 top-3 grid size-5 place-items-center rounded-full bg-brand text-white">
          <Check className="size-3" />
        </span>
      ) : null}
      {children}
      <p className="text-sm font-semibold text-ink">{title}</p>
      <p className="mt-0.5 text-xs leading-5 text-muted">{description}</p>
    </button>
  );
}

export default function SettingsPage() {
  const { user } = useAuth();
  const { toast } = useToast();
  const { mode, setMode, density, setDensity, motion, setMotion } = useTheme();

  function clearRecent() {
    removeStorage(STORAGE_KEYS.recent);
    toast({ tone: "success", title: "Recently viewed cleared" });
  }

  function resetPreferences() {
    for (const key of ALL_STORAGE_KEYS) removeStorage(key);
    setMode("system");
    setDensity("comfortable");
    setMotion("system");
    toast({
      tone: "success",
      title: "Preferences reset",
      description: "Theme, layout, saved views and onboarding progress were restored to defaults.",
    });
  }

  return (
    <div className="mx-auto max-w-4xl">
      <PageHeader
        eyebrow="Account"
        title="Preferences"
        description="Personal display settings. They are stored in this browser only and never change what your colleagues see."
      />

      <div className="space-y-6">
        <Card>
          <CardHeader title="Theme" description="Choose how TenderPilot looks. Printed bid briefs always use the light palette." />
          <div role="radiogroup" aria-label="Theme" className="mt-5 grid gap-3 sm:grid-cols-3">
            {themeChoices.map((choice) => {
              const Icon = choice.icon;
              return (
                <ChoiceCard
                  key={choice.value}
                  active={mode === choice.value}
                  onSelect={() => setMode(choice.value)}
                  title={choice.label}
                  description={choice.description}
                >
                  <div
                    className="mb-3 flex h-16 items-end gap-1.5 overflow-hidden rounded-xl border border-line p-2"
                    style={{ background: choice.swatch[0] }}
                    aria-hidden="true"
                  >
                    <span className="h-full w-5 rounded-md" style={{ background: choice.swatch[1] }} />
                    <span className="h-3/4 flex-1 rounded-md" style={{ background: choice.swatch[1] }} />
                    <span className="h-2/5 w-6 rounded-md" style={{ background: choice.swatch[2] }} />
                    <Icon className="mb-0.5 size-3.5 self-start text-faint" />
                  </div>
                </ChoiceCard>
              );
            })}
          </div>
        </Card>

        <Card>
          <CardHeader title="Layout density" description="Compact shrinks type and spacing by about 6% to fit more on screen." />
          <div role="radiogroup" aria-label="Density" className="mt-5 grid gap-3 sm:grid-cols-2">
            <ChoiceCard
              active={density === "comfortable"}
              onSelect={() => setDensity("comfortable")}
              title="Comfortable"
              description="Roomier spacing. The default."
            />
            <ChoiceCard
              active={density === "compact"}
              onSelect={() => setDensity("compact")}
              title="Compact"
              description="Denser tables and cards for analysts."
            />
          </div>
        </Card>

        <Card>
          <CardHeader title="Motion" description="TenderPilot already respects your system's reduced-motion setting." />
          <div role="radiogroup" aria-label="Motion" className="mt-5 grid gap-3 sm:grid-cols-2">
            <ChoiceCard
              active={motion === "system"}
              onSelect={() => setMotion("system")}
              title="Follow system"
              description="Animate unless your device asks for less."
            />
            <ChoiceCard
              active={motion === "reduced"}
              onSelect={() => setMotion("reduced")}
              title="Reduce motion"
              description="Turn off transitions and entrance animations."
            />
          </div>
        </Card>

        <Card>
          <CardHeader title="Keyboard shortcuts" description="Ignored while you type in a field. Press ? anywhere for this list." />
          <dl className="mt-5 grid gap-x-10 gap-y-3 sm:grid-cols-2">
            {shortcutRows.map((row) => (
              <div key={row.label} className="flex items-center justify-between gap-3 text-sm">
                <dt className="text-ink-2">{row.label}</dt>
                <dd className="flex gap-1">
                  {row.keys.map((key) => (
                    <Kbd key={key}>{key}</Kbd>
                  ))}
                </dd>
              </div>
            ))}
          </dl>
        </Card>

        <Card>
          <CardHeader title="Account" description="Profile and password changes arrive with team management in a later release." />
          <dl className="mt-5 grid gap-4 text-sm sm:grid-cols-2">
            <div>
              <dt className="text-xs font-semibold uppercase tracking-[0.1em] text-faint">Name</dt>
              <dd className="mt-1 text-ink">{user?.fullName}</dd>
            </div>
            <div>
              <dt className="text-xs font-semibold uppercase tracking-[0.1em] text-faint">Email</dt>
              <dd className="mt-1 text-ink">{user?.email}</dd>
            </div>
            <div>
              <dt className="text-xs font-semibold uppercase tracking-[0.1em] text-faint">Organization</dt>
              <dd className="mt-1 text-ink">{user?.organization.name}</dd>
            </div>
            <div>
              <dt className="text-xs font-semibold uppercase tracking-[0.1em] text-faint">Role</dt>
              <dd className="mt-1">{user ? <Badge tone="brand">{formatEnum(user.role)}</Badge> : null}</dd>
            </div>
          </dl>
        </Card>

        <Card>
          <CardHeader title="Local data" description="Everything below lives in this browser. Nothing is sent to the server." />
          <div className="mt-5 flex flex-wrap gap-3">
            <Button onClick={clearRecent} leading={<Trash2 className="size-4" />}>
              Clear recently viewed
            </Button>
            <Button variant="danger" onClick={resetPreferences} leading={<RotateCcw className="size-4" />}>
              Reset all preferences
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
