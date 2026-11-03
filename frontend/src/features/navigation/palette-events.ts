export const OPEN_PALETTE_EVENT = "tenderpilot:open-palette";
export const OPEN_SHORTCUTS_EVENT = "tenderpilot:open-shortcuts";

export function openCommandPalette(): void {
  window.dispatchEvent(new CustomEvent(OPEN_PALETTE_EVENT));
}

export function openShortcutsHelp(): void {
  window.dispatchEvent(new CustomEvent(OPEN_SHORTCUTS_EVENT));
}
