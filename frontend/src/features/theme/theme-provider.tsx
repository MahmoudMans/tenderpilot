"use client";

import { usePathname } from "next/navigation";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useSyncExternalStore,
  type ReactNode,
} from "react";

import { readStorage, useStoredState } from "@/lib/storage";

export type ThemeMode = "light" | "dark" | "system";
export type Density = "comfortable" | "compact";
export type MotionPreference = "system" | "reduced";

export const THEME_STORAGE_KEY = "tp-theme";
export const DENSITY_STORAGE_KEY = "tp-density";
export const MOTION_STORAGE_KEY = "tp-motion";

/**
 * Runs before first paint (inlined in <head>) so there is no light/dark flash.
 * Must stay in sync with the provider below.
 */
export const THEME_INIT_SCRIPT = `(function(){try{
var d=document.documentElement;
function get(k){var v=localStorage.getItem(k);return v?JSON.parse(v):null}
var m=get('${THEME_STORAGE_KEY}')||'system';
var dark=m==='dark'||(m==='system'&&window.matchMedia('(prefers-color-scheme: dark)').matches);
if(location.pathname.indexOf('/showcase/capture')===0)dark=false;
if(dark)d.classList.add('dark');
if(get('${DENSITY_STORAGE_KEY}')==='compact')d.setAttribute('data-density','compact');
if(get('${MOTION_STORAGE_KEY}')==='reduced')d.setAttribute('data-motion','reduced');
}catch(e){}})();`;

type ThemeContextValue = {
  mode: ThemeMode;
  resolved: "light" | "dark";
  setMode: (mode: ThemeMode) => void;
  /** Flips between light and dark based on what is currently shown. */
  toggle: () => void;
  density: Density;
  setDensity: (density: Density) => void;
  motion: MotionPreference;
  setMotion: (motion: MotionPreference) => void;
};

const ThemeContext = createContext<ThemeContextValue | null>(null);

function subscribeSystemTheme(onChange: () => void): () => void {
  const query = window.matchMedia("(prefers-color-scheme: dark)");
  query.addEventListener("change", onChange);
  return () => query.removeEventListener("change", onChange);
}

function systemPrefersDark(): boolean {
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
}

function serverPrefersDark(): boolean {
  return false;
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const [mode, setMode] = useStoredState<ThemeMode>(THEME_STORAGE_KEY, "system");
  const [density, setDensity] = useStoredState<Density>(DENSITY_STORAGE_KEY, "comfortable");
  const [motion, setMotion] = useStoredState<MotionPreference>(MOTION_STORAGE_KEY, "system");
  const systemDark = useSyncExternalStore(subscribeSystemTheme, systemPrefersDark, serverPrefersDark);

  // The screenshot capture route is deterministic and always light.
  const forcedLight = pathname.startsWith("/showcase/capture");
  const resolved: "light" | "dark" = forcedLight
    ? "light"
    : mode === "system"
      ? systemDark
        ? "dark"
        : "light"
      : mode;

  // Effects read the stored preference directly (not the render-time snapshot) so the
  // state applied by THEME_INIT_SCRIPT is never undone during hydration.
  useEffect(() => {
    const root = document.documentElement;

    function shouldBeDark(): boolean {
      if (window.location.pathname.startsWith("/showcase/capture")) return false;
      const stored = readStorage<ThemeMode>(THEME_STORAGE_KEY, "system");
      return (
        stored === "dark" ||
        (stored === "system" && window.matchMedia("(prefers-color-scheme: dark)").matches)
      );
    }

    root.classList.toggle("dark", shouldBeDark());

    // Printing always uses the light palette.
    const beforePrint = () => root.classList.remove("dark");
    const afterPrint = () => root.classList.toggle("dark", shouldBeDark());
    window.addEventListener("beforeprint", beforePrint);
    window.addEventListener("afterprint", afterPrint);
    return () => {
      window.removeEventListener("beforeprint", beforePrint);
      window.removeEventListener("afterprint", afterPrint);
    };
  }, [mode, systemDark, forcedLight]);

  useEffect(() => {
    const root = document.documentElement;
    const compact = readStorage<Density>(DENSITY_STORAGE_KEY, "comfortable") === "compact";
    if (compact) root.setAttribute("data-density", "compact");
    else root.removeAttribute("data-density");
  }, [density]);

  useEffect(() => {
    const root = document.documentElement;
    const reduced = readStorage<MotionPreference>(MOTION_STORAGE_KEY, "system") === "reduced";
    if (reduced) root.setAttribute("data-motion", "reduced");
    else root.removeAttribute("data-motion");
  }, [motion]);

  const toggle = useCallback(() => {
    setMode(resolved === "dark" ? "light" : "dark");
  }, [resolved, setMode]);

  const value = useMemo<ThemeContextValue>(
    () => ({ mode, resolved, setMode, toggle, density, setDensity, motion, setMotion }),
    [mode, resolved, setMode, toggle, density, setDensity, motion, setMotion],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme(): ThemeContextValue {
  const context = useContext(ThemeContext);
  if (context === null) {
    throw new Error("useTheme must be used inside ThemeProvider");
  }
  return context;
}
