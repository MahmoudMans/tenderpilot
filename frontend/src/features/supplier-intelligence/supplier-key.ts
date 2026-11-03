/** Java's String.trim(): removes every character up to and including the space (U+0020) at both ends. */
function trimLikeJava(value: string): string {
  let start = 0;
  let end = value.length;
  while (start < end && value.charCodeAt(start) <= 32) start += 1;
  while (end > start && value.charCodeAt(end - 1) <= 32) end -= 1;
  return value.slice(start, end);
}

/**
 * Builds the key the backend uses for a supplier profile: the name with spaces
 * cleaned up, encoded as URL-safe Base64 without padding. This mirrors
 * SupplierKeyCodec on the backend, so a supplier named on a buyer page can link
 * straight to its own profile.
 */
export function supplierKeyFor(name: string): string | null {
  const cleaned = trimLikeJava(name).replace(/[ \t\n\v\f\r]+/g, " ");
  if (cleaned === "") return null;
  let binary = "";
  for (const byte of new TextEncoder().encode(cleaned)) binary += String.fromCharCode(byte);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
