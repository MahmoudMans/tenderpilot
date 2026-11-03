/** Every localStorage key TenderPilot writes. Settings > "Reset" clears exactly these. */
export const STORAGE_KEYS = {
  theme: "tp-theme",
  density: "tp-density",
  motion: "tp-motion",
  sidebar: "tp-sidebar",
  recent: "tp-recent",
  calendarView: "tp-calendar-view",
  savedViews: "tp-saved-views",
  onboarding: "tp-onboarding",
  opportunitiesView: "tp-opportunities-view",
  planningTasksView: "tp-planning-tasks-view",
} as const;

export const ALL_STORAGE_KEYS: readonly string[] = Object.values(STORAGE_KEYS);
