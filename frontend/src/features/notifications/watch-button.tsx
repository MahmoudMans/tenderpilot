"use client";

import { Check, Eye } from "lucide-react";
import { useState } from "react";

import { Button, type ButtonSize } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { cn } from "@/lib/cn";

import type { WatchlistTargetType } from "./notification-types";
import { useNotifications } from "./notifications-context";
import { normalizeWatchKey } from "./watch-keys";

type WatchButtonProps = {
  type: WatchlistTargetType;
  /** Tender id, buyer id or CPV code. The button normalises it for the API. */
  targetKey: string;
  /** Saved with the watchlist entry so it is recognisable on the notifications page. */
  label: string;
  size?: ButtonSize;
  /** Icon-only button for tight spaces such as list rows. */
  compact?: boolean;
  className?: string;
};

const TYPE_NOUN: Record<WatchlistTargetType, string> = {
  TENDER: "tender",
  BUYER: "buyer",
  CPV: "CPV code",
};

export function WatchButton({ type, targetKey, label, size = "sm", compact = false, className }: WatchButtonProps) {
  const { center, failed, findWatch, watch, unwatch } = useNotifications();
  const { toast } = useToast();
  const [busy, setBusy] = useState(false);

  const noun = TYPE_NOUN[type];
  const key = normalizeWatchKey(type, targetKey);
  const existing = findWatch(type, key);
  const watching = existing !== null;
  const unavailable = key === "" || (center === null && !failed);

  async function toggle() {
    setBusy(true);
    try {
      if (existing !== null) {
        const ok = await unwatch(existing.id);
        toast(
          ok
            ? { tone: "info", title: `Stopped watching this ${noun}` }
            : { tone: "error", title: `Could not remove the ${noun} from your watchlist`, description: "Try again in a moment." },
        );
      } else {
        const ok = await watch(type, key, label.slice(0, 255));
        toast(
          ok
            ? {
                tone: "success",
                title: `Watching this ${noun}`,
                description: "You will get notifications when it changes. Manage the list under Notifications.",
              }
            : { tone: "error", title: `Could not add the ${noun} to your watchlist`, description: "Try again in a moment." },
        );
      }
    } finally {
      setBusy(false);
    }
  }

  const text = watching ? "Watching" : "Watch";
  const description = watching ? `Stop watching this ${noun}` : `Get notified about this ${noun}`;
  const icon = watching ? <Check className="size-3.5" /> : <Eye className="size-3.5" />;

  return (
    <Button
      size={size}
      variant={watching ? "soft" : "secondary"}
      loading={busy}
      disabled={unavailable}
      onClick={() => void toggle()}
      aria-pressed={watching}
      aria-label={compact ? `${text} ${noun}: ${label}` : undefined}
      title={description}
      leading={icon}
      className={cn(compact && "px-2", className)}
    >
      {compact ? null : text}
    </Button>
  );
}
