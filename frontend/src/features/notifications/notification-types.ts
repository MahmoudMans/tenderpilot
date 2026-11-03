export type WatchlistTargetType =

  | "TENDER"
  | "BUYER"
  | "CPV";


export type NotificationSeverity =

  | "CRITICAL"
  | "HIGH"
  | "MEDIUM"
  | "LOW";


export type NotificationStatus =

  | "UNREAD"
  | "READ"
  | "DISMISSED";


export type NotificationCenter = {

  summary: {

    activeCount:
      number;

    unreadCount:
      number;

    criticalCount:
      number;

    highCount:
      number;

    watchlistCount:
      number;

  };

  notifications: Array<{

    id:
      string;

    type:
      string;

    severity:
      NotificationSeverity;

    title:
      string;

    detail:
      string;

    tenderId:
      string
      |
      null;

    href:
      string;

    status:
      NotificationStatus;

    active:
      boolean;

    createdAt:
      string;

    updatedAt:
      string;

    lastObservedAt:
      string;

  }>;

  watchlist: Array<{

    id:
      string;

    type:
      WatchlistTargetType;

    targetKey:
      string;

    label:
      string
      |
      null;

    createdAt:
      string;

  }>;

};

export type WatchlistItem = NotificationCenter["watchlist"][number];
