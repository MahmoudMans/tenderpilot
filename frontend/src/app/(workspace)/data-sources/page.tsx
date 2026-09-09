"use client";


import {

  Activity,

  CheckCircle2,

  Clock3,

  Database,

  ExternalLink,

  KeyRound,

  LoaderCircle,

  Play,

  RefreshCw,

  Search,

  Server,

  ShieldCheck,

  TriangleAlert,

} from "lucide-react";


import {

  type FormEvent,

  useEffect,

  useState,

} from "react";


import {

  useAuth,

} from "@/features/auth/auth-context";


import type {

  TedDataSourceStatus,

  TedSyncResult,

} from "@/features/data-source/ted-types";


function formatDateTime(
  value:
    string
    |
    null,
) {

  if (
    value ===
    null
  ) {

    return "Never";

  }


  return new Intl.DateTimeFormat(

    "en",

    {

      dateStyle:
        "medium",

      timeStyle:
        "short",

    },

  ).format(
    new Date(
      value,
    ),
  );

}


export default function DataSourcesPage() {

  const {

    api,

    user,

  } = useAuth();


  const [

    status,

    setStatus,

  ] = useState<
    TedDataSourceStatus
    |
    null
  >(
    null,
  );


  const [

    lookbackDays,

    setLookbackDays,

  ] = useState(
    7,
  );


  const [

    limit,

    setLimit,

  ] = useState(
    25,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    syncing,

    setSyncing,

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

    syncResult,

    setSyncResult,

  ] = useState<
    TedSyncResult
    |
    null
  >(
    null,
  );


  useEffect(
    () => {

      let cancelled =
        false;


      api<TedDataSourceStatus>(

        "/api/v1/data-sources/ted",

      )
        .then(
          (
            result,
          ) => {

            if (
              !cancelled
            ) {

              setStatus(
                result,
              );

            }

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

                  : "Unable to load data-source status.",

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


  async function runSync(
    event:
      FormEvent<HTMLFormElement>,
  ) {

    event.preventDefault();


    setSyncing(
      true,
    );


    setError(
      null,
    );


    setSyncResult(
      null,
    );


    try {

      const result =

        await api<TedSyncResult>(

          `/api/v1/data-sources/ted/sync?days=${lookbackDays}&limit=${limit}`,

          {

            method:
              "POST",

          },

        );


      setSyncResult(
        result,
      );


      const refreshed =

        await api<TedDataSourceStatus>(

          "/api/v1/data-sources/ted",

        );


      setStatus(
        refreshed,
      );


    } catch (
      cause
    ) {

      setError(

        cause instanceof Error

          ? cause.message

          : "TED synchronization failed.",

      );


    } finally {

      setSyncing(
        false,
      );

    }

  }


  const canSync =

    user?.role ===
    "OWNER"

    &&
    status?.manualSyncEnabled ===
    true;


  return (

    <div
      className="
        mx-auto
        max-w-[1400px]
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

            Procurement ingestion

          </p>


          <h1
            className="
              mt-3
              text-3xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
              md:text-4xl
            "
          >

            Data sources

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

            Monitor public procurement feeds and normalize their
            notices into TenderPilot&apos;s shared canonical catalogue.

          </p>

        </div>


        <div
          className="
            inline-flex
            w-fit
            items-center
            gap-2
            rounded-full
            border
            border-emerald-200
            bg-emerald-50
            px-4
            py-2
            text-xs
            font-semibold
            text-emerald-700
          "
        >

          <ShieldCheck
            className="
              size-4
            "
          />

          Zero paid integrations

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
        loading

          ? (

            <div
              className="
                grid
                min-h-[420px]
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

          )

          : status

            ? (

              <div
                className="
                  mt-7
                  space-y-6
                "
              >

                <section
                  className="
                    overflow-hidden
                    rounded-3xl
                    border
                    border-slate-200
                    bg-white
                    shadow-[0_10px_36px_rgba(15,23,42,0.045)]
                  "
                >

                  <div
                    className="
                      border-b
                      border-slate-100
                      bg-[linear-gradient(135deg,#f8fbff_0%,#ffffff_62%)]
                      p-6
                    "
                  >

                    <div
                      className="
                        flex
                        flex-col
                        gap-5
                        xl:flex-row
                        xl:items-start
                        xl:justify-between
                      "
                    >

                      <div
                        className="
                          flex
                          items-start
                          gap-4
                        "
                      >

                        <div
                          className="
                            grid
                            size-12
                            shrink-0
                            place-items-center
                            rounded-2xl
                            bg-[#edf4ff]
                            text-[#2f6fed]
                          "
                        >

                          <Database
                            className="
                              size-5
                            "
                          />

                        </div>


                        <div>

                          <div
                            className="
                              flex
                              flex-wrap
                              items-center
                              gap-2
                            "
                          >

                            <h2
                              className="
                                text-xl
                                font-semibold
                                text-slate-950
                              "
                            >

                              Tenders Electronic Daily

                            </h2>


                            <span
                              className="
                                rounded-full
                                bg-emerald-100
                                px-2.5
                                py-1
                                text-[11px]
                                font-semibold
                                text-emerald-700
                              "
                            >

                              Connected

                            </span>


                            <span
                              className="
                                rounded-full
                                bg-blue-100
                                px-2.5
                                py-1
                                text-[11px]
                                font-semibold
                                text-blue-700
                              "
                            >

                              {
                                status.apiVersion
                              }

                            </span>

                          </div>


                          <p
                            className="
                              mt-2
                              max-w-3xl
                              text-sm
                              leading-6
                              text-slate-500
                            "
                          >

                            Official EU published-notice Search API.
                            TenderPilot requests structured TED fields,
                            normalizes them, preserves source lineage and
                            writes them idempotently into the canonical
                            procurement model.

                          </p>

                        </div>

                      </div>


                      <a
                        href="https://docs.ted.europa.eu/api/latest/search.html"
                        target="_blank"
                        rel="noreferrer"
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
                          text-slate-600
                          transition
                          hover:bg-slate-50
                        "
                      >

                        API documentation

                        <ExternalLink
                          className="
                            size-4
                          "
                        />

                      </a>

                    </div>

                  </div>


                  <div
                    className="
                      grid
                      gap-px
                      bg-slate-100
                      md:grid-cols-2
                      xl:grid-cols-4
                    "
                  >

                    <Metric

                      icon={
                        KeyRound
                      }

                      label="
                        Authentication
                      "

                      value={
                        status.authenticationRequired

                          ? "Required"

                          : "No API key"
                      }

                      detail="
                        Published-notice search
                      "

                    />


                    <Metric

                      icon={
                        Database
                      }

                      label="
                        Canonical notices
                      "

                      value={
                        status.canonicalNoticeCount
                          .toLocaleString()
                      }

                      detail="
                        TED lineage records
                      "

                    />


                    <Metric

                      icon={
                        Clock3
                      }

                      label="
                        Last notice seen
                      "

                      value={
                        formatDateTime(
                          status.lastNoticeSeenAt,
                        )
                      }

                      detail="
                        Latest TED source touch
                      "

                    />


                    <Metric

                      icon={
                        Server
                      }

                      label="
                        Search endpoint
                      "

                      value={
                        status.searchPath
                      }

                      detail={
                        status.baseUrl
                      }

                    />

                  </div>


                  <div
                    className="
                      grid
                      gap-6
                      p-6
                      xl:grid-cols-[0.8fr_1.2fr]
                    "
                  >

                    <form
                      onSubmit={
                        runSync
                      }
                      className="
                        rounded-2xl
                        border
                        border-slate-200
                        bg-slate-50/60
                        p-5
                      "
                    >

                      <div
                        className="
                          flex
                          items-center
                          gap-3
                        "
                      >

                        <div
                          className="
                            grid
                            size-9
                            place-items-center
                            rounded-xl
                            bg-white
                            text-[#2f6fed]
                            shadow-sm
                          "
                        >

                          <RefreshCw
                            className="
                              size-4
                            "
                          />

                        </div>


                        <div>

                          <h3
                            className="
                              text-sm
                              font-semibold
                              text-slate-900
                            "
                          >

                            Local development sync

                          </h3>


                          <p
                            className="
                              mt-1
                              text-xs
                              text-slate-500
                            "
                          >

                            Bounded recent-publication ingestion.

                          </p>

                        </div>

                      </div>


                      <div
                        className="
                          mt-5
                          grid
                          gap-4
                          sm:grid-cols-2
                        "
                      >

                        <label>

                          <span
                            className="
                              text-xs
                              font-medium
                              text-slate-600
                            "
                          >

                            Publication window

                          </span>


                          <select
                            value={
                              lookbackDays
                            }
                            onChange={
                              (
                                event,
                              ) =>
                                setLookbackDays(
                                  Number(
                                    event.target.value,
                                  ),
                                )
                            }
                            className="
                              mt-2
                              h-11
                              w-full
                              rounded-xl
                              border
                              border-slate-200
                              bg-white
                              px-3
                              text-sm
                              text-slate-700
                              outline-none
                              focus:border-[#2f6fed]
                            "
                          >

                            <option value={1}>
                              Today
                            </option>

                            <option value={3}>
                              Last 3 days
                            </option>

                            <option value={7}>
                              Last 7 days
                            </option>

                            <option value={14}>
                              Last 14 days
                            </option>

                            <option value={31}>
                              Last 31 days
                            </option>

                          </select>

                        </label>


                        <label>

                          <span
                            className="
                              text-xs
                              font-medium
                              text-slate-600
                            "
                          >

                            Maximum notices

                          </span>


                          <select
                            value={
                              limit
                            }
                            onChange={
                              (
                                event,
                              ) =>
                                setLimit(
                                  Number(
                                    event.target.value,
                                  ),
                                )
                            }
                            className="
                              mt-2
                              h-11
                              w-full
                              rounded-xl
                              border
                              border-slate-200
                              bg-white
                              px-3
                              text-sm
                              text-slate-700
                              outline-none
                              focus:border-[#2f6fed]
                            "
                          >

                            <option value={10}>
                              10
                            </option>

                            <option value={25}>
                              25
                            </option>

                            <option value={50}>
                              50
                            </option>

                            <option value={100}>
                              100
                            </option>

                          </select>

                        </label>

                      </div>


                      <button
                        type="submit"
                        disabled={
                          !canSync
                          ||
                          syncing
                        }
                        className="
                          mt-5
                          inline-flex
                          h-11
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
                          transition
                          hover:bg-[#1b2d52]
                          disabled:cursor-not-allowed
                          disabled:opacity-45
                        "
                      >

                        {
                          syncing

                            ? (

                              <LoaderCircle
                                className="
                                  size-4
                                  animate-spin
                                "
                              />

                            )

                            : (

                              <Play
                                className="
                                  size-4
                                "
                              />

                            )
                        }

                        {
                          syncing

                            ? "Synchronizing TED..."

                            : "Run TED sync"
                        }

                      </button>


                      {
                        user?.role !==
                        "OWNER"

                          ? (

                            <p
                              className="
                                mt-3
                                text-xs
                                leading-5
                                text-slate-400
                              "
                            >

                              Manual ingestion is restricted to the
                              organization owner in the local portfolio
                              environment.

                            </p>

                          )

                          : null
                      }

                    </form>


                    <div
                      className="
                        rounded-2xl
                        border
                        border-slate-200
                        p-5
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

                          <h3
                            className="
                              text-sm
                              font-semibold
                              text-slate-900
                            "
                          >

                            Latest ingestion run

                          </h3>


                          <p
                            className="
                              mt-1
                              text-xs
                              text-slate-500
                            "
                          >

                            Persistent source-operation telemetry.

                          </p>

                        </div>


                        {
                          status.lastRun

                            ? (

                              <RunBadge
                                status={
                                  status.lastRun.status
                                }
                              />

                            )

                            : null
                        }

                      </div>


                      {
                        status.lastRun

                          ? (

                            <div
                              className="
                                mt-5
                                space-y-4
                              "
                            >

                              <div
                                className="
                                  grid
                                  gap-3
                                  sm:grid-cols-3
                                "
                              >

                                <CountBox
                                  label="Discovered"
                                  value={
                                    status.lastRun.discoveredCount
                                  }
                                />

                                <CountBox
                                  label="Processed"
                                  value={
                                    status.lastRun.processedCount
                                  }
                                />

                                <CountBox
                                  label="Failed"
                                  value={
                                    status.lastRun.failedCount
                                  }
                                />

                              </div>


                              <div
                                className="
                                  rounded-xl
                                  bg-slate-50
                                  p-4
                                "
                              >

                                <p
                                  className="
                                    text-[10px]
                                    font-semibold
                                    uppercase
                                    tracking-[0.12em]
                                    text-slate-400
                                  "
                                >

                                  Expert query

                                </p>


                                <code
                                  className="
                                    mt-2
                                    block
                                    break-all
                                    text-xs
                                    text-slate-700
                                  "
                                >

                                  {
                                    status.lastRun.query
                                  }

                                </code>

                              </div>


                              <div
                                className="
                                  grid
                                  gap-3
                                  text-xs
                                  text-slate-500
                                  sm:grid-cols-2
                                "
                              >

                                <span>

                                  Started:
                                  {" "}
                                  {
                                    formatDateTime(
                                      status.lastRun.startedAt,
                                    )
                                  }

                                </span>


                                <span>

                                  Completed:
                                  {" "}
                                  {
                                    formatDateTime(
                                      status.lastRun.completedAt,
                                    )
                                  }

                                </span>

                              </div>


                              {
                                status.lastRun.lastError

                                  ? (

                                    <div
                                      className="
                                        flex
                                        gap-3
                                        rounded-xl
                                        border
                                        border-amber-200
                                        bg-amber-50
                                        p-4
                                        text-xs
                                        leading-5
                                        text-amber-800
                                      "
                                    >

                                      <TriangleAlert
                                        className="
                                          mt-0.5
                                          size-4
                                          shrink-0
                                        "
                                      />

                                      {
                                        status.lastRun.lastError
                                      }

                                    </div>

                                  )

                                  : null
                              }

                            </div>

                          )

                          : (

                            <div
                              className="
                                mt-8
                                py-8
                                text-center
                              "
                            >

                              <Activity
                                className="
                                  mx-auto
                                  size-6
                                  text-slate-300
                                "
                              />


                              <p
                                className="
                                  mt-3
                                  text-sm
                                  text-slate-400
                                "
                              >

                                No TED ingestion run recorded yet.

                              </p>

                            </div>

                          )
                      }

                    </div>

                  </div>

                </section>


                {
                  syncResult

                    ? (

                      <section
                        className="
                          rounded-2xl
                          border
                          border-emerald-200
                          bg-emerald-50
                          p-5
                        "
                      >

                        <div
                          className="
                            flex
                            items-start
                            gap-3
                          "
                        >

                          <CheckCircle2
                            className="
                              mt-0.5
                              size-5
                              shrink-0
                              text-emerald-600
                            "
                          />


                          <div>

                            <p
                              className="
                                text-sm
                                font-semibold
                                text-emerald-900
                              "
                            >

                              TED synchronization finished

                            </p>


                            <p
                              className="
                                mt-1
                                text-sm
                                leading-6
                                text-emerald-800
                              "
                            >

                              Processed
                              {" "}
                              {
                                syncResult.processedCount
                              }
                              {" "}
                              of
                              {" "}
                              {
                                syncResult.discoveredCount
                              }
                              {" "}
                              returned notices with
                              {" "}
                              {
                                syncResult.failedCount
                              }
                              {" "}
                              failures.

                            </p>

                          </div>

                        </div>

                      </section>

                    )

                    : null
                }


                <section
                  className="
                    grid
                    gap-4
                    md:grid-cols-3
                  "
                >

                  <ArchitectureCard

                    icon={
                      Search
                    }

                    title="
                      Search
                    "

                    text="
                      TED expert-query dates and selected published-notice fields are retrieved through the official v3 Search API.
                    "

                  />


                  <ArchitectureCard

                    icon={
                      RefreshCw
                    }

                    title="
                      Normalize
                    "

                    text="
                      TED buyer, CPV, country, procedure, contract-nature, value and deadline signals are converted into canonical values.
                    "

                  />


                  <ArchitectureCard

                    icon={
                      Database
                    }

                    title="
                      Upsert
                    "

                    text="
                      Source publication numbers are idempotent, with TED lineage retained on the shared canonical tender.
                    "

                  />

                </section>

              </div>

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

  detail,

}: {

  icon:
    typeof Database;

  label:
    string;

  value:
    string;

  detail:
    string;

}) {

  return (

    <div
      className="
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
          truncate
          text-sm
          font-semibold
          text-slate-900
        "
      >

        {
          value
        }

      </p>


      <p
        className="
          mt-1
          truncate
          text-xs
          text-slate-400
        "
      >

        {
          detail
        }

      </p>

    </div>

  );

}


function RunBadge({

  status,

}: {

  status:
    TedSyncResult["status"];

}) {

  const className =

    status ===
    "COMPLETED"

      ? "bg-emerald-100 text-emerald-700"

      : status ===
        "FAILED"

        ? "bg-red-100 text-red-700"

        : status ===
          "PARTIAL"

          ? "bg-amber-100 text-amber-700"

          : "bg-blue-100 text-blue-700";


  return (

    <span
      className={[
        "rounded-full px-2.5 py-1 text-[11px] font-semibold",
        className,
      ].join(
        " ",
      )}
    >

      {
        status
      }

    </span>

  );

}


function CountBox({

  label,

  value,

}: {

  label:
    string;

  value:
    number;

}) {

  return (

    <div
      className="
        rounded-xl
        bg-slate-50
        p-4
      "
    >

      <p
        className="
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
          text-xl
          font-semibold
          text-slate-950
        "
      >

        {
          value
        }

      </p>

    </div>

  );

}


function ArchitectureCard({

  icon:
    Icon,

  title,

  text,

}: {

  icon:
    typeof Database;

  title:
    string;

  text:
    string;

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

      <div
        className="
          grid
          size-9
          place-items-center
          rounded-xl
          bg-[#edf4ff]
          text-[#2f6fed]
        "
      >

        <Icon
          className="
            size-4
          "
        />

      </div>


      <h3
        className="
          mt-4
          text-sm
          font-semibold
          text-slate-900
        "
      >

        {
          title
        }

      </h3>


      <p
        className="
          mt-2
          text-sm
          leading-6
          text-slate-500
        "
      >

        {
          text
        }

      </p>

    </article>

  );

}
