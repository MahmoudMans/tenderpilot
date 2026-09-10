"use client";


import {

  AlertOctagon,

  Bell,

  BellRing,

  BookmarkPlus,

  Check,

  CheckCheck,

  Clock3,

  Eye,

  LoaderCircle,

  RefreshCcw,

  ShieldCheck,

  Trash2,

  TriangleAlert,

  X,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import Link from "next/link";


import {

  useEffect,

  useMemo,

  useState,

} from "react";


import {

  useAuth,

} from "@/features/auth/auth-context";


import {

  formatDate,

  formatEnum,

} from "@/features/procurement/procurement-format";


import type {

  NotificationCenter,

  NotificationSeverity,

  WatchlistTargetType,

} from "@/features/notifications/notification-types";


export default function NotificationsPage() {

  const {

    api,

  } = useAuth();


  const [

    center,

    setCenter,

  ] = useState<
    NotificationCenter
    |
    null
  >(
    null,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    refreshing,

    setRefreshing,

  ] = useState(
    false,
  );


  const [

    mutating,

    setMutating,

  ] = useState(
    false,
  );


  const [

    error,

    setError,

  ] = useState<
    string
    |
    null
  >(
    null,
  );


  const [

    watchType,

    setWatchType,

  ] = useState<
    WatchlistTargetType
  >(
    "TENDER",
  );


  const [

    targetKey,

    setTargetKey,

  ] = useState(
    "",
  );


  const [

    watchLabel,

    setWatchLabel,

  ] = useState(
    "",
  );


  useEffect(
    () => {

      let cancelled =
        false;


      api<NotificationCenter>(
        "/api/v1/notifications",
      )
        .then(
          (
            result,
          ) => {

            if (
              cancelled
            ) {
              return;
            }


            setCenter(
              result,
            );

            setError(
              null,
            );

          },
        )
        .catch(
          (
            cause,
          ) => {

            if (
              !cancelled
            ) {

              setError(
                cause instanceof Error
                  ? cause.message
                  : "Unable to load notifications.",
              );

            }

          },
        )
        .finally(
          () => {

            if (
              !cancelled
            ) {

              setLoading(
                false,
              );

            }

          },
        );


      return () => {

        cancelled =
          true;

      };

    },
    [
      api,
    ],
  );


  const activeNotifications =
    useMemo(
      () =>
        center?.notifications.filter(
          (
            notification,
          ) =>
            notification.active
            &&
            notification.status !==
              "DISMISSED",
        )
        ??
        [],
      [
        center,
      ],
    );


  const history =
    useMemo(
      () =>
        center?.notifications.filter(
          (
            notification,
          ) =>
            !notification.active
            ||
            notification.status ===
              "DISMISSED",
        )
        ??
        [],
      [
        center,
      ],
    );


  async function refreshAlerts() {

    setRefreshing(
      true,
    );

    setError(
      null,
    );


    try {

      const result =
        await api<NotificationCenter>(
          "/api/v1/notifications/refresh",
          {
            method:
              "POST",
          },
        );


      setCenter(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to refresh alerts.",
      );


    } finally {

      setRefreshing(
        false,
      );

    }

  }


  async function createWatchlistItem() {

    if (
      targetKey.trim() ===
      ""
    ) {

      setError(
        "Enter a watchlist target.",
      );

      return;

    }


    setMutating(
      true,
    );

    setError(
      null,
    );


    try {

      const result =
        await api<NotificationCenter>(
          "/api/v1/notifications/watchlist",
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                type:
                  watchType,

                targetKey:
                  targetKey.trim(),

                label:
                  watchLabel.trim() ===
                  ""
                    ? null
                    : watchLabel.trim(),
              }),
          },
        );


      setCenter(
        result,
      );

      setTargetKey(
        "",
      );

      setWatchLabel(
        "",
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to add watchlist item.",
      );


    } finally {

      setMutating(
        false,
      );

    }

  }


  async function deleteWatchlistItem(
    id:
      string,
  ) {

    setMutating(
      true,
    );


    try {

      const result =
        await api<NotificationCenter>(
          `/api/v1/notifications/watchlist/${id}`,
          {
            method:
              "DELETE",
          },
        );


      setCenter(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to remove watchlist item.",
      );


    } finally {

      setMutating(
        false,
      );

    }

  }


  async function acknowledge(
    id:
      string,
  ) {

    setMutating(
      true,
    );


    try {

      const result =
        await api<NotificationCenter>(
          `/api/v1/notifications/${id}/acknowledge`,
          {
            method:
              "POST",
          },
        );


      setCenter(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to acknowledge notification.",
      );


    } finally {

      setMutating(
        false,
      );

    }

  }


  async function dismiss(
    id:
      string,
  ) {

    setMutating(
      true,
    );


    try {

      const result =
        await api<NotificationCenter>(
          `/api/v1/notifications/${id}/dismiss`,
          {
            method:
              "POST",
          },
        );


      setCenter(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to dismiss notification.",
      );


    } finally {

      setMutating(
        false,
      );

    }

  }


  async function markAllRead() {

    setMutating(
      true,
    );


    try {

      const result =
        await api<NotificationCenter>(
          "/api/v1/notifications/read-all",
          {
            method:
              "POST",
          },
        );


      setCenter(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to mark notifications as read.",
      );


    } finally {

      setMutating(
        false,
      );

    }

  }


  if (
    loading
  ) {

    return (

      <div
        className="
          grid
          min-h-[60vh]
          place-items-center
        "
      >

        <LoaderCircle
          className="
            size-6
            animate-spin
            text-[#2f6fed]
          "
        />

      </div>

    );

  }


  return (

    <div
      className="
        mx-auto
        max-w-[1500px]
      "
    >

      <div
        className="
          flex
          flex-col
          gap-5
          xl:flex-row
          xl:items-end
          xl:justify-between
        "
      >

        <div>

          <p
            className="
              text-xs
              font-semibold
              uppercase
              tracking-[0.18em]
              text-[#2f6fed]
            "
          >
            Organization-private alerting
          </p>


          <h1
            className="
              mt-3
              text-4xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
            "
          >
            Notifications & watchlists
          </h1>


          <p
            className="
              mt-3
              max-w-3xl
              text-sm
              leading-6
              text-slate-500
            "
          >
            Track deadlines, high-fit opportunities,
            compliance gaps, overdue bid work, submission
            readiness, strategic buyers and CPV market
            activity.
          </p>

        </div>


        <div
          className="
            flex
            flex-wrap
            gap-2
          "
        >

          <button
            type="button"
            disabled={
              refreshing
            }
            onClick={
              () => {
                void refreshAlerts();
              }
            }
            className="
              inline-flex
              h-10
              items-center
              gap-2
              rounded-xl
              bg-[#14213d]
              px-4
              text-sm
              font-semibold
              text-white
              disabled:opacity-50
            "
          >

            {
              refreshing

                ? (

                  <LoaderCircle
                    className="
                      size-4
                      animate-spin
                    "
                  />

                )

                : (

                  <RefreshCcw
                    className="size-4"
                  />

                )
            }

            Refresh alerts

          </button>


          <button
            type="button"
            disabled={
              mutating
            }
            onClick={
              () => {
                void markAllRead();
              }
            }
            className="
              inline-flex
              h-10
              items-center
              gap-2
              rounded-xl
              border
              border-slate-200
              bg-white
              px-4
              text-sm
              font-semibold
              text-slate-700
            "
          >
            <CheckCheck
              className="size-4"
            />

            Mark all read
          </button>

        </div>

      </div>


      {
        error

          ? (

            <div
              className="
                mt-6
                rounded-2xl
                border
                border-red-200
                bg-red-50
                p-4
                text-sm
                text-red-700
              "
            >
              {
                error
              }
            </div>

          )

          : null
      }


      {
        center

          ? (

            <>

              <div
                className="
                  mt-7
                  grid
                  gap-4
                  md:grid-cols-2
                  xl:grid-cols-5
                "
              >

                <Metric
                  icon={BellRing}
                  label="Active alerts"
                  value={
                    center.summary.activeCount
                  }
                />


                <Metric
                  icon={Bell}
                  label="Unread"
                  value={
                    center.summary.unreadCount
                  }
                />


                <Metric
                  icon={AlertOctagon}
                  label="Critical"
                  value={
                    center.summary.criticalCount
                  }
                />


                <Metric
                  icon={TriangleAlert}
                  label="High"
                  value={
                    center.summary.highCount
                  }
                />


                <Metric
                  icon={Eye}
                  label="Watchlist"
                  value={
                    center.summary.watchlistCount
                  }
                />

              </div>


              <div
                className="
                  mt-6
                  grid
                  gap-6
                  xl:grid-cols-[1fr_0.72fr]
                "
              >

                <section
                  className="
                    rounded-3xl
                    border
                    border-slate-200
                    bg-white
                    p-6
                  "
                >

                  <div
                    className="
                      flex
                      items-center
                      justify-between
                      gap-4
                    "
                  >

                    <div>

                      <p
                        className="
                          text-xs
                          font-semibold
                          uppercase
                          tracking-[0.12em]
                          text-[#2f6fed]
                        "
                      >
                        Action center
                      </p>


                      <h2
                        className="
                          mt-2
                          text-xl
                          font-semibold
                          text-slate-950
                        "
                      >
                        Active notifications
                      </h2>

                    </div>


                    <BellRing
                      className="
                        size-5
                        text-slate-400
                      "
                    />

                  </div>


                  <div
                    className="
                      mt-6
                      space-y-3
                    "
                  >

                    {
                      activeNotifications.map(
                        (
                          notification,
                        ) => (

                          <article
                            key={
                              notification.id
                            }
                            className="
                              rounded-2xl
                              border
                              border-slate-200
                              p-4
                            "
                          >

                            <div
                              className="
                                flex
                                items-start
                                gap-4
                              "
                            >

                              <SeverityIcon
                                severity={
                                  notification.severity
                                }
                              />


                              <div
                                className="
                                  min-w-0
                                  flex-1
                                "
                              >

                                <div
                                  className="
                                    flex
                                    flex-wrap
                                    items-center
                                    gap-2
                                  "
                                >

                                  <SeverityBadge
                                    severity={
                                      notification.severity
                                    }
                                  />


                                  <span
                                    className="
                                      text-[10px]
                                      font-semibold
                                      uppercase
                                      tracking-[0.08em]
                                      text-slate-400
                                    "
                                  >
                                    {
                                      formatEnum(
                                        notification.type,
                                      )
                                    }
                                  </span>


                                  {
                                    notification.status ===
                                    "UNREAD"

                                      ? (

                                        <span
                                          className="
                                            rounded-full
                                            bg-blue-50
                                            px-2
                                            py-0.5
                                            text-[9px]
                                            font-semibold
                                            text-blue-700
                                          "
                                        >
                                          New
                                        </span>

                                      )

                                      : null
                                  }

                                </div>


                                <p
                                  className="
                                    mt-2
                                    text-sm
                                    font-semibold
                                    text-slate-950
                                  "
                                >
                                  {
                                    notification.title
                                  }
                                </p>


                                <p
                                  className="
                                    mt-1
                                    text-xs
                                    leading-5
                                    text-slate-500
                                  "
                                >
                                  {
                                    notification.detail
                                  }
                                </p>


                                <p
                                  className="
                                    mt-2
                                    text-[10px]
                                    text-slate-400
                                  "
                                >
                                  Last observed
                                  {" "}
                                  {
                                    formatDate(
                                      notification.lastObservedAt,
                                    )
                                  }
                                </p>


                                <div
                                  className="
                                    mt-4
                                    flex
                                    flex-wrap
                                    gap-2
                                  "
                                >

                                  <Link
                                    href={
                                      notification.href
                                    }
                                    className="
                                      inline-flex
                                      h-8
                                      items-center
                                      rounded-lg
                                      bg-[#14213d]
                                      px-3
                                      text-xs
                                      font-semibold
                                      text-white
                                    "
                                  >
                                    Open
                                  </Link>


                                  {
                                    notification.status ===
                                    "UNREAD"

                                      ? (

                                        <button
                                          type="button"
                                          disabled={
                                            mutating
                                          }
                                          onClick={
                                            () => {
                                              void acknowledge(
                                                notification.id,
                                              );
                                            }
                                          }
                                          className="
                                            inline-flex
                                            h-8
                                            items-center
                                            gap-1.5
                                            rounded-lg
                                            border
                                            border-slate-200
                                            px-3
                                            text-xs
                                            font-semibold
                                            text-slate-600
                                          "
                                        >
                                          <Check
                                            className="size-3.5"
                                          />

                                          Acknowledge
                                        </button>

                                      )

                                      : null
                                  }


                                  <button
                                    type="button"
                                    disabled={
                                      mutating
                                    }
                                    onClick={
                                      () => {
                                        void dismiss(
                                          notification.id,
                                        );
                                      }
                                    }
                                    className="
                                      inline-flex
                                      h-8
                                      items-center
                                      gap-1.5
                                      rounded-lg
                                      border
                                      border-slate-200
                                      px-3
                                      text-xs
                                      font-semibold
                                      text-slate-500
                                    "
                                  >
                                    <X
                                      className="size-3.5"
                                    />

                                    Dismiss
                                  </button>

                                </div>

                              </div>

                            </div>

                          </article>

                        ),
                      )
                    }


                    {
                      activeNotifications.length ===
                      0

                        ? (

                          <div
                            className="
                              rounded-2xl
                              border
                              border-dashed
                              border-slate-300
                              p-10
                              text-center
                            "
                          >

                            <CheckCheck
                              className="
                                mx-auto
                                size-7
                                text-emerald-500
                              "
                            />


                            <p
                              className="
                                mt-3
                                text-sm
                                font-semibold
                                text-slate-900
                              "
                            >
                              No active alerts
                            </p>


                            <p
                              className="
                                mt-1
                                text-xs
                                text-slate-400
                              "
                            >
                              Refresh alerts after your bid
                              portfolio or watchlist changes.
                            </p>

                          </div>

                        )

                        : null
                    }

                  </div>

                </section>


                <section
                  className="
                    rounded-3xl
                    border
                    border-slate-200
                    bg-white
                    p-6
                  "
                >

                  <BookmarkPlus
                    className="
                      size-5
                      text-[#2f6fed]
                    "
                  />


                  <h2
                    className="
                      mt-4
                      text-xl
                      font-semibold
                      text-slate-950
                    "
                  >
                    Watchlist
                  </h2>


                  <p
                    className="
                      mt-2
                      text-xs
                      leading-5
                      text-slate-500
                    "
                  >
                    Watch a tender UUID, buyer UUID, or a
                    1–8 digit CPV prefix.
                  </p>


                  <div
                    className="
                      mt-5
                      space-y-3
                    "
                  >

                    <select
                      value={
                        watchType
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setWatchType(
                            event.target.value as WatchlistTargetType,
                          )
                      }
                      className="
                        h-11
                        w-full
                        rounded-xl
                        border
                        border-slate-200
                        px-3
                        text-sm
                      "
                    >
                      <option value="TENDER">
                        Tender
                      </option>

                      <option value="BUYER">
                        Buyer
                      </option>

                      <option value="CPV">
                        CPV prefix
                      </option>
                    </select>


                    <input
                      value={
                        targetKey
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setTargetKey(
                            event.target.value,
                          )
                      }
                      placeholder={
                        watchType ===
                        "CPV"
                          ? "e.g. 4545"
                          : "UUID"
                      }
                      className="
                        h-11
                        w-full
                        rounded-xl
                        border
                        border-slate-200
                        px-3
                        text-sm
                      "
                    />


                    <input
                      value={
                        watchLabel
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setWatchLabel(
                            event.target.value,
                          )
                      }
                      placeholder="Optional label"
                      className="
                        h-11
                        w-full
                        rounded-xl
                        border
                        border-slate-200
                        px-3
                        text-sm
                      "
                    />


                    <button
                      type="button"
                      disabled={
                        mutating
                      }
                      onClick={
                        () => {
                          void createWatchlistItem();
                        }
                      }
                      className="
                        inline-flex
                        h-10
                        w-full
                        items-center
                        justify-center
                        gap-2
                        rounded-xl
                        bg-[#14213d]
                        px-4
                        text-sm
                        font-semibold
                        text-white
                        disabled:opacity-50
                      "
                    >
                      <BookmarkPlus
                        className="size-4"
                      />

                      Add to watchlist
                    </button>

                  </div>


                  <div
                    className="
                      mt-6
                      space-y-2
                    "
                  >

                    {
                      center.watchlist.map(
                        (
                          item,
                        ) => (

                          <div
                            key={
                              item.id
                            }
                            className="
                              rounded-2xl
                              bg-slate-50
                              p-4
                            "
                          >

                            <div
                              className="
                                flex
                                items-start
                                justify-between
                                gap-3
                              "
                            >

                              <div
                                className="
                                  min-w-0
                                "
                              >

                                <span
                                  className="
                                    rounded-full
                                    bg-white
                                    px-2
                                    py-1
                                    text-[9px]
                                    font-semibold
                                    text-slate-500
                                  "
                                >
                                  {
                                    item.type
                                  }
                                </span>


                                <p
                                  className="
                                    mt-2
                                    truncate
                                    text-sm
                                    font-semibold
                                    text-slate-900
                                  "
                                >
                                  {
                                    item.label
                                    ??
                                    item.targetKey
                                  }
                                </p>


                                {
                                  item.label

                                    ? (

                                      <p
                                        className="
                                          mt-1
                                          break-all
                                          text-[10px]
                                          text-slate-400
                                        "
                                      >
                                        {
                                          item.targetKey
                                        }
                                      </p>

                                    )

                                    : null
                                }

                              </div>


                              <button
                                type="button"
                                disabled={
                                  mutating
                                }
                                onClick={
                                  () => {
                                    void deleteWatchlistItem(
                                      item.id,
                                    );
                                  }
                                }
                                className="
                                  grid
                                  size-8
                                  shrink-0
                                  place-items-center
                                  rounded-lg
                                  text-slate-400
                                  hover:bg-white
                                  hover:text-red-600
                                "
                                aria-label="Remove watchlist item"
                              >
                                <Trash2
                                  className="size-4"
                                />
                              </button>

                            </div>

                          </div>

                        ),
                      )
                    }


                    {
                      center.watchlist.length ===
                      0

                        ? (

                          <p
                            className="
                              rounded-2xl
                              border
                              border-dashed
                              border-slate-300
                              p-8
                              text-center
                              text-xs
                              text-slate-400
                            "
                          >
                            Your organization watchlist is empty.
                          </p>

                        )

                        : null
                    }

                  </div>

                </section>

              </div>


              {
                history.length >
                0

                  ? (

                    <section
                      className="
                        mt-6
                        rounded-3xl
                        border
                        border-slate-200
                        bg-white
                        p-6
                      "
                    >

                      <div
                        className="
                          flex
                          items-center
                          gap-3
                        "
                      >

                        <Clock3
                          className="
                            size-5
                            text-slate-400
                          "
                        />


                        <h2
                          className="
                            text-lg
                            font-semibold
                            text-slate-950
                          "
                        >
                          Alert history
                        </h2>

                      </div>


                      <div
                        className="
                          mt-5
                          divide-y
                          divide-slate-100
                        "
                      >

                        {
                          history
                            .slice(
                              0,
                              20,
                            )
                            .map(
                              (
                                notification,
                              ) => (

                                <div
                                  key={
                                    notification.id
                                  }
                                  className="
                                    py-4
                                  "
                                >

                                  <div
                                    className="
                                      flex
                                      flex-col
                                      gap-2
                                      md:flex-row
                                      md:items-start
                                      md:justify-between
                                    "
                                  >

                                    <div>

                                      <p
                                        className="
                                          text-sm
                                          font-semibold
                                          text-slate-700
                                        "
                                      >
                                        {
                                          notification.title
                                        }
                                      </p>


                                      <p
                                        className="
                                          mt-1
                                          text-xs
                                          text-slate-400
                                        "
                                      >
                                        {
                                          formatEnum(
                                            notification.type,
                                          )
                                        }
                                        {" · "}
                                        {
                                          formatDate(
                                            notification.lastObservedAt,
                                          )
                                        }
                                      </p>

                                    </div>


                                    <span
                                      className="
                                        text-[10px]
                                        font-semibold
                                        text-slate-400
                                      "
                                    >
                                      {
                                        notification.status
                                      }
                                    </span>

                                  </div>

                                </div>

                              ),
                            )
                        }

                      </div>

                    </section>

                  )

                  : null
              }


              <div
                className="
                  mt-6
                  flex
                  items-start
                  gap-3
                  rounded-2xl
                  bg-slate-50
                  p-4
                  text-xs
                  leading-5
                  text-slate-500
                "
              >

                <ShieldCheck
                  className="
                    mt-0.5
                    size-4
                    shrink-0
                  "
                />

                Notifications and watchlists are private to the
                current organization. Alert refresh is
                deterministic and read-only against bid state:
                it never changes GO/HOLD/NO-GO decisions,
                compliance verdicts, tasks, or submission status.

              </div>

            </>

          )

          : null
      }

    </div>

  );

}


function Metric({

  icon:
    Icon,

  label,

  value,

}: {

  icon:
    LucideIcon;

  label:
    string;

  value:
    number;

}) {

  return (

    <article
      className="
        rounded-2xl
        border
        border-slate-200
        bg-white
        p-5
      "
    >

      <Icon
        className="
          size-4
          text-[#2f6fed]
        "
      />


      <p
        className="
          mt-4
          text-[10px]
          font-semibold
          uppercase
          tracking-[0.12em]
          text-slate-400
        "
      >
        {
          label
        }
      </p>


      <p
        className="
          mt-1
          text-2xl
          font-semibold
          text-slate-950
        "
      >
        {
          value.toLocaleString()
        }
      </p>

    </article>

  );

}


function SeverityIcon({

  severity,

}: {

  severity:
    NotificationSeverity;

}) {

  const config =
    severity ===
    "CRITICAL"

      ? {
          className:
            "bg-red-50 text-red-600",

          icon:
            AlertOctagon,
        }

      : severity ===
        "HIGH"

        ? {
            className:
              "bg-amber-50 text-amber-600",

            icon:
              TriangleAlert,
          }

        : {
            className:
              "bg-blue-50 text-blue-600",

            icon:
              Bell,
          };


  const Icon =
    config.icon;


  return (

    <div
      className={
        `
          grid
          size-9
          shrink-0
          place-items-center
          rounded-xl
          ${config.className}
        `
      }
    >
      <Icon
        className="size-4"
      />
    </div>

  );

}


function SeverityBadge({

  severity,

}: {

  severity:
    NotificationSeverity;

}) {

  const className =
    severity ===
    "CRITICAL"
      ? "bg-red-50 text-red-700"
      : severity ===
        "HIGH"
        ? "bg-amber-50 text-amber-700"
        : severity ===
          "MEDIUM"
          ? "bg-blue-50 text-blue-700"
          : "bg-slate-100 text-slate-600";


  return (

    <span
      className={
        `
          rounded-full
          px-2.5
          py-1
          text-[10px]
          font-semibold
          ${className}
        `
      }
    >
      {
        severity
      }
    </span>

  );

}
