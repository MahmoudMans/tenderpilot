export type CalendarEvent = {
  uid: string;
  title: string;
  /** ISO timestamp, or a calendar date such as 2026-10-15 when `allDay` is set. */
  start: string;
  /** An all-day event on the date in `start`; `end` is ignored. */
  allDay?: boolean;
  /** Defaults to start + 30 minutes. */
  end?: string;
  description?: string;
  url?: string;
  /** Reminder offsets in minutes before the event. */
  reminders?: number[];
};

function formatUtc(date: Date): string {
  return date.toISOString().replace(/[-:]/g, "").replace(/\.\d{3}/, "");
}

function escapeText(value: string): string {
  return value
    .replace(/\\/g, "\\\\")
    .replace(/;/g, "\\;")
    .replace(/,/g, "\\,")
    .replace(/\r?\n/g, "\\n");
}

/** RFC 5545 line folding (75 octets; this folds on characters, which is safe for ASCII-heavy content). */
function fold(line: string): string[] {
  const parts: string[] = [];
  let rest = line;
  while (rest.length > 74) {
    parts.push(rest.slice(0, 74));
    rest = ` ${rest.slice(74)}`;
  }
  parts.push(rest);
  return parts;
}

function formatDay(date: Date): string {
  const month = String(date.getUTCMonth() + 1).padStart(2, "0");
  const day = String(date.getUTCDate()).padStart(2, "0");
  return `${String(date.getUTCFullYear()).padStart(4, "0")}${month}${day}`;
}

/** The DTSTART and DTEND lines of an event, or null when its date cannot be read. */
function eventTimes(event: CalendarEvent): { start: string; end: string } | null {
  if (event.allDay) {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(event.start);
    if (match === null) return null;
    const [year, month, day] = [Number(match[1]), Number(match[2]), Number(match[3])];
    const first = new Date(Date.UTC(year, month - 1, day));
    if (first.getUTCMonth() !== month - 1 || first.getUTCDate() !== day) return null;
    const next = new Date(Date.UTC(year, month - 1, day + 1));
    return { start: `DTSTART;VALUE=DATE:${formatDay(first)}`, end: `DTEND;VALUE=DATE:${formatDay(next)}` };
  }
  const start = new Date(event.start);
  if (Number.isNaN(start.getTime())) return null;
  const end = event.end ? new Date(event.end) : new Date(start.getTime() + 30 * 60 * 1000);
  return { start: `DTSTART:${formatUtc(start)}`, end: `DTEND:${formatUtc(end)}` };
}

export function buildIcs(events: readonly CalendarEvent[], calendarName: string): string {
  const stamp = formatUtc(new Date());
  const lines: string[] = [
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//TenderPilot//Bid deadlines//EN",
    "CALSCALE:GREGORIAN",
    "METHOD:PUBLISH",
    `X-WR-CALNAME:${escapeText(calendarName)}`,
  ];

  for (const event of events) {
    const times = eventTimes(event);
    if (times === null) continue;


    lines.push(
      "BEGIN:VEVENT",
      `UID:${event.uid}@tenderpilot`,
      `DTSTAMP:${stamp}`,
      times.start,
      times.end,
      `SUMMARY:${escapeText(event.title)}`,
    );
    if (event.description) lines.push(`DESCRIPTION:${escapeText(event.description)}`);
    if (event.url) lines.push(`URL:${event.url}`);
    for (const minutes of event.reminders ?? []) {
      lines.push(
        "BEGIN:VALARM",
        "ACTION:DISPLAY",
        `DESCRIPTION:${escapeText(event.title)}`,
        `TRIGGER:-PT${Math.max(1, Math.round(minutes))}M`,
        "END:VALARM",
      );
    }
    lines.push("END:VEVENT");
  }

  lines.push("END:VCALENDAR");
  return `${lines.flatMap(fold).join("\r\n")}\r\n`;
}
