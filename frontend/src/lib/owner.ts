/**
 * Owners are free text on the server, so a person is matched by their name or their email,
 * ignoring case and extra spaces. These helpers drive the "Owner" filters on the bid pages.
 */

export const OWNER_ANY = "any";
export const OWNER_ME = "me";
export const OWNER_NONE = "none";

const NAME_PREFIX = "name:";

export function normalizeOwner(value: string | null | undefined): string {
  return (value ?? "").trim().replace(/\s+/g, " ").toLowerCase();
}

/** What the signed-in person may be written as: their full name or their email. */
export function ownerAliases(user: { fullName: string; email: string } | null): string[] {
  if (user === null) return [];
  return [normalizeOwner(user.fullName), normalizeOwner(user.email)].filter((alias) => alias !== "");
}

/** Whether an item's owner passes the chosen owner filter. */
export function matchesOwner(owner: string | null, choice: string, aliases: readonly string[]): boolean {
  if (choice === OWNER_ANY) return true;
  const key = normalizeOwner(owner);
  if (choice === OWNER_NONE) return key === "";
  if (key === "") return false;
  if (choice === OWNER_ME) return aliases.includes(key);
  return choice === `${NAME_PREFIX}${key}`;
}

export type OwnerOption = { value: string; label: string };

/**
 * The choices for an owner menu: Any owner, then "Assigned to me" and "Unassigned" when some item fits,
 * then every other owner by name. The current choice always stays in the list.
 */
export function ownerOptions(
  owners: ReadonlyArray<string | null>,
  aliases: readonly string[],
  selected: string,
): OwnerOption[] {
  const names = new Map<string, string>();
  let mine = false;
  let unassigned = false;
  for (const owner of owners) {
    const key = normalizeOwner(owner);
    if (key === "") {
      unassigned = true;
    } else if (aliases.includes(key)) {
      mine = true;
    } else if (!names.has(key)) {
      names.set(key, (owner ?? "").trim().replace(/\s+/g, " "));
    }
  }

  const options: OwnerOption[] = [{ value: OWNER_ANY, label: "Any owner" }];
  if (mine || selected === OWNER_ME) options.push({ value: OWNER_ME, label: "Assigned to me" });
  if (unassigned || selected === OWNER_NONE) options.push({ value: OWNER_NONE, label: "Unassigned" });
  const sorted = [...names.entries()].sort((a, b) => a[1].localeCompare(b[1]));
  for (const [key, label] of sorted) {
    options.push({ value: `${NAME_PREFIX}${key}`, label });
  }
  if (selected.startsWith(NAME_PREFIX) && !names.has(selected.slice(NAME_PREFIX.length))) {
    options.push({ value: selected, label: selected.slice(NAME_PREFIX.length) });
  }
  return options;
}
