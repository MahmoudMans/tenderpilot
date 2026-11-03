/** The part of URLSearchParams that pages need to read their starting filters. */
export type ParamReader = {
  get(name: string): string | null;
};

/** Reads a free-text parameter, trimmed and limited in length. */
export function readText(params: ParamReader, name: string, maxLength = 120): string {
  return (params.get(name) ?? "").trim().slice(0, maxLength);
}

/** Reads a parameter that must be one of a fixed list of values. Anything else gives the fallback. */
export function readOption<T extends string>(
  params: ParamReader,
  name: string,
  allowed: readonly T[],
  fallback: T,
): T {
  const value = params.get(name);
  const match = allowed.find((option) => option === value);
  return match === undefined ? fallback : match;
}

/** Builds "/path?a=1&b=2" and leaves out empty values. */
export function linkWithParams(path: string, values: Record<string, string | undefined>): string {
  const params = new URLSearchParams();
  for (const [name, value] of Object.entries(values)) {
    if (value !== undefined && value.trim() !== "") params.set(name, value.trim());
  }
  const text = params.toString();
  return text === "" ? path : `${path}?${text}`;
}
