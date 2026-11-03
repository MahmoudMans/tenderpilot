"use client";

import { CalendarPlus, Landmark, Link2, Printer, Scale } from "lucide-react";

import { Button, ButtonLink } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { useTenderMatch } from "@/features/matching/use-tender-match";
import { WatchButton } from "@/features/notifications/watch-button";
import { downloadTextFile } from "@/lib/csv";
import { buildIcs } from "@/lib/ics";
import { useNow } from "@/lib/use-clock";

type OpportunityQuickActionsProps = {
  id: string;
  title: string;
  buyerId: string;
  buyerName: string;
  deadline: string | null;
};

export function OpportunityQuickActions({ id, title, buyerId, buyerName, deadline }: OpportunityQuickActionsProps) {
  const { toast } = useToast();
  const matchState = useTenderMatch(id);
  const now = useNow();
  const hasFutureDeadline = deadline !== null && new Date(deadline).getTime() > now;

  function addToCalendar() {
    if (deadline === null) return;
    const content = buildIcs(
      [
        {
          uid: id,
          title: `Tender deadline: ${title}`,
          start: deadline,
          description: `${buyerName}\n${title}`,
          url: `${window.location.origin}/opportunities/${id}`,
          reminders: [1440, 120],
        },
      ],
      "TenderPilot deadline",
    );
    downloadTextFile(`tender-deadline-${id.slice(0, 8)}.ics`, content, "text/calendar;charset=utf-8");
    toast({
      tone: "success",
      title: "Calendar file downloaded",
      description: "Open it to add the deadline with 1-day and 2-hour reminders.",
    });
  }

  async function copyLink() {
    try {
      await navigator.clipboard.writeText(`${window.location.origin}/opportunities/${id}`);
      toast({ tone: "success", title: "Link copied" });
    } catch {
      toast({ tone: "error", title: "Could not copy the link", description: "Copy it from the address bar instead." });
    }
  }

  const match = matchState.status === "ready" ? matchState.match : null;

  return (
    <div data-print-hide className="mt-5 flex flex-wrap items-center gap-2">
      {match !== null ? (
        <ButtonLink
          href={`/bid-decisions/${id}`}
          size="sm"
          variant="primary"
          leading={<Scale className="size-3.5" />}
          title="Open the Go/No-Go assessment for this tender"
        >
          Go/No-Go assessment
          <span className="rounded-full bg-white/20 px-1.5 text-[10px] font-bold">{match.score}</span>
        </ButtonLink>
      ) : (
        <Button
          size="sm"
          disabled
          leading={<Scale className="size-3.5" />}
          title={
            matchState.status === "loading"
              ? "Checking your match list…"
              : "This tender is not in your match list yet. Recalculate matches to assess it."
          }
        >
          Go/No-Go assessment
        </Button>
      )}
      <WatchButton type="TENDER" targetKey={id} label={title} />
      <ButtonLink
        href={`/buyers/${buyerId}`}
        size="sm"
        leading={<Landmark className="size-3.5" />}
        title={`Buyer intelligence for ${buyerName}`}
      >
        Buyer intelligence
      </ButtonLink>
      <span className="mx-1 hidden h-5 w-px bg-line sm:block" aria-hidden="true" />
      <Button
        size="sm"
        onClick={addToCalendar}
        disabled={!hasFutureDeadline}
        title={hasFutureDeadline ? "Download a calendar event for the submission deadline" : "No upcoming deadline"}
        leading={<CalendarPlus className="size-3.5" />}
      >
        Add deadline to calendar
      </Button>
      <Button size="sm" onClick={() => void copyLink()} leading={<Link2 className="size-3.5" />}>
        Copy link
      </Button>
      <Button size="sm" onClick={() => window.print()} leading={<Printer className="size-3.5" />}>
        Print
      </Button>
    </div>
  );
}
