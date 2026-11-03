/** Where signing in leads when no other page was asked for. */
export const DEFAULT_RETURN_PATH = "/dashboard";

/** Pages that are never a place to come back to after signing in. */
const SIGN_IN_PATHS = ["/", "/login", "/register"];

/**
 * A page inside this app taken from the "next" address parameter, or null when it is missing or could lead
 * somewhere else ("//host", "/\host", "https://host", control characters), so a crafted link cannot send a
 * person off the site after they sign in.
 */
export function safeReturnPath(raw: string | null | undefined): string | null {
  if (!raw || raw.length > 2048) return null;
  if (raw[0] !== "/" || raw[1] === "/" || raw[1] === "\\") return null;
  for (const char of raw) {
    if (char.charCodeAt(0) < 32 || char === "\\") return null;
  }
  const path = raw.split(/[?#]/)[0].replace(/\/+$/, "") || "/";
  return SIGN_IN_PATHS.includes(path) ? null : raw;
}

/** The page to open after signing in, from a search string such as "?next=%2Fmatches". */
export function returnPathFromSearch(search: string): string {
  return safeReturnPath(new URLSearchParams(search).get("next")) ?? DEFAULT_RETURN_PATH;
}

/** The sign-in address that brings a person back to returnTo, with the reason they were sent there. */
export function signInHref(returnTo: string, reason?: "expired"): string {
  const params = new URLSearchParams();
  const target = safeReturnPath(returnTo);
  if (target && target !== DEFAULT_RETURN_PATH) params.set("next", target);
  if (reason) params.set("reason", reason);
  const query = params.toString();
  return query ? `/login?${query}` : "/login";
}

/** The sign-in or sign-up address that keeps the page to come back to, for the link between the two. */
export function withReturnPath(path: "/login" | "/register", search: string): string {
  const target = safeReturnPath(new URLSearchParams(search).get("next"));
  return target && target !== DEFAULT_RETURN_PATH ? `${path}?next=${encodeURIComponent(target)}` : path;
}

/** Signing out on purpose is not an expired session: it goes to a plain sign-in page. */
const SIGN_OUT_WINDOW_MS = 10_000;
let signedOutAt = 0;

export function markSignedOutOnPurpose(): void {
  signedOutAt = Date.now();
}

/** True once, shortly after markSignedOutOnPurpose. */
export function wasSignedOutOnPurpose(): boolean {
  const recent = Date.now() - signedOutAt < SIGN_OUT_WINDOW_MS;
  signedOutAt = 0;
  return recent;
}
