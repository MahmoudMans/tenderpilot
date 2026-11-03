"use client";

import { useEffect, type RefObject } from "react";

/** Calls `onDismiss` on Escape or on a pointer press outside `ref` while `active`. */
export function useDismiss(
  ref: RefObject<HTMLElement | null>,
  active: boolean,
  onDismiss: () => void,
): void {
  useEffect(() => {
    if (!active) return;

    function onPointerDown(event: PointerEvent) {
      const element = ref.current;
      if (element && event.target instanceof Node && !element.contains(event.target)) {
        onDismiss();
      }
    }

    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onDismiss();
    }

    document.addEventListener("pointerdown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [ref, active, onDismiss]);
}
