import type { WatchlistTargetType } from "./notification-types";

/**
 * Converts a value into the key format the watchlist API stores:
 * - TENDER and BUYER use the lower-case UUID.
 * - CPV uses only the digits of the code, at most 8 of them. A code such as
 *   "72000000-5" becomes "72000000", because the API rejects the check digit.
 */
export function normalizeWatchKey(type: WatchlistTargetType, value: string): string {
  if (type === "CPV") {
    const [main] = value.trim().split("-");
    return main.replace(/\D/g, "").slice(0, 8);
  }
  return value.trim().toLowerCase();
}
