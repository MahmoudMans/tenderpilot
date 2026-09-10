"use client";


import {

  Activity,

  AlertOctagon,

  CircleGauge,

  Clock3,

  Database,

  LoaderCircle,

  RefreshCcw,

  ServerCog,

  ShieldCheck,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import {

  useEffect,

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

  DataOperations,

  IngestionRunOutcome,

  SourceHealthStatus,

} from "@/features/data-operations/data-operations-types";


export default function DataOperationsPage() {

  const {

    api,

  } = useAuth();


  const [

    operations,

    setOperations,

  ] = useState<
    DataOperations
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

    error,

    setError,

  ] = useState<
    string
    |
    null
  >(
    null,
  );


  useEffect(
    () => {

      let cancelled =
        false;


      api<DataOperations>(
        "/api/v1/data-operations?historyLimit=60",
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


            setOperations(
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
                  : "Unable to load data operations.",
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


  async function refresh() {

    setRefreshing(
      true,
    );


    setError(
      null,
    );


    try {

      const result =
        await api<DataOperations>(
          "/api/v1/data-operations?historyLimit=60",
        );


      setOperations(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to refresh source health.",
      );


    } finally {

      setRefreshing(
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
          min-h-[65vh]
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


  if (
    operations ===
    null
  ) {

    return (

      <div
        className="
          mx-auto
          max-w-[1500px]
        "
      >

        <h1
          className="
            text-3xl
            font-semibold
            text-slate-950
          "
        >
          Data operations
        </h1>


        <div
          className="
            mt-6
            rounded-2xl
            border
            border-red-200
            bg-red-50
            p-5
            text-sm
            text-red-700
          "
        >
          {
            error
            ??
            "Data operations are unavailable."
          }
        </div>

      </div>

    );

  }


  const summary =
    operations.summary;


  return (

    <div
      className="
        mx-auto
        max-w-[1550px]
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
            Shared corpus operations
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
            Data operations & source health
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
            Monitor TED, BOAMP, DECP and any additional
            canonical ingestion sources through recent run
            history, reliability, failure streaks, stale-data
            detection and ingestion throughput.
          </p>

        </div>


        <button
          type="button"
          disabled={
            refreshing
          }
          onClick={
            () => {
              void refresh();
            }
          }
          className="
            inline-flex
            h-10
            items-center
            gap-2
            self-start
            rounded-xl
            bg-[#14213d]
            px-4
            text-sm
            font-semibold
            text-white
            disabled:opacity-50
            xl:self-auto
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

          Refresh health

        </button>

      </div>


      {
        error

          ? (

            <div
              className="
                mt-6
                rounded-2xl
                border
                border-amber-200
                bg-amber-50
                p-4
                text-sm
                text-amber-800
              "
            >
              {
                error
              }
            </div>

          )

          : null
      }


      <div
        className="
          mt-7
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        <Metric
          icon={ServerCog}
          label="Sources"
          value={
            summary.sourceCount
          }
          detail={
            `${summary.healthySourceCount} healthy`
          }
        />


        <Metric
          icon={Activity}
          label="Runs · 24h"
          value={
            summary.runs24Hours
          }
          detail={
            `${summary.failures24Hours} failed`
          }
        />


        <Metric
          icon={CircleGauge}
          label="7-day reliability"
          value={
            summary.successRate7Days ===
            null
              ? "—"
              : `${summary.successRate7Days}%`
          }
          detail={
            `${summary.runs7Days} observed runs`
          }
        />


        <Metric
          icon={AlertOctagon}
          label="Needs attention"
          value={
            summary.unhealthySourceCount
            +
            summary.degradedSourceCount
            +
            summary.staleSourceCount
          }
          detail={
            `${summary.runningSourceCount} currently running`
          }
        />

      </div>


      <section
        className="
          mt-6
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
              Ingestion health
            </p>


            <h2
              className="
                mt-2
                text-xl
                font-semibold
                text-slate-950
              "
            >
              Source health
            </h2>

          </div>


          <Database
            className="
              size-5
              text-slate-400
            "
          />

        </div>


        <div
          className="
            mt-5
            grid
            gap-4
            xl:grid-cols-3
          "
        >

          {
            operations.sources.map(
              (
                source,
              ) => (

                <article
                  key={
                    source.source
                  }
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
                      items-start
                      justify-between
                      gap-4
                    "
                  >

                    <div>

                      <p
                        className="
                          text-lg
                          font-semibold
                          text-slate-950
                        "
                      >
                        {
                          source.source
                        }
                      </p>


                      <p
                        className="
                          mt-1
                          text-xs
                          text-slate-400
                        "
                      >
                        Public procurement ingestion
                      </p>

                    </div>


                    <HealthBadge
                      health={
                        source.health
                      }
                    />

                  </div>


                  <div
                    className="
                      mt-6
                      grid
                      grid-cols-2
                      gap-3
                    "
                  >

                    <SmallMetric
                      label="7d runs"
                      value={
                        source.runCount7Days
                          .toLocaleString()
                      }
                    />


                    <SmallMetric
                      label="Success rate"
                      value={
                        source.successRate7Days ===
                        null
                          ? "—"
                          : `${source.successRate7Days}%`
                      }
                    />


                    <SmallMetric
                      label="Failures"
                      value={
                        source.failureCount7Days
                          .toLocaleString()
                      }
                    />


                    <SmallMetric
                      label="Failure streak"
                      value={
                        source.failureStreak
                          .toLocaleString()
                      }
                    />

                  </div>


                  {
                    (
                      source.importedCount7Days >
                      0
                      ||
                      source.rejectedCount7Days >
                      0
                    )

                      ? (

                        <div
                          className="
                            mt-4
                            rounded-2xl
                            bg-slate-50
                            p-4
                          "
                        >

                          <p
                            className="
                              text-[10px]
                              font-semibold
                              uppercase
                              tracking-[0.1em]
                              text-slate-400
                            "
                          >
                            7-day records
                          </p>


                          <div
                            className="
                              mt-2
                              flex
                              flex-wrap
                              gap-x-5
                              gap-y-1
                              text-xs
                              text-slate-600
                            "
                          >

                            <span>
                              Imported:
                              {" "}
                              {
                                source.importedCount7Days
                                  .toLocaleString()
                              }
                            </span>


                            <span>
                              Rejected:
                              {" "}
                              {
                                source.rejectedCount7Days
                                  .toLocaleString()
                              }
                            </span>

                          </div>

                        </div>

                      )

                      : null
                  }


                  <dl
                    className="
                      mt-5
                      space-y-3
                      text-xs
                    "
                  >

                    <InfoRow
                      label="Last started"
                      value={
                        source.lastStartedAt
                          ? formatDate(
                              source.lastStartedAt,
                            )
                          : "Never observed"
                      }
                    />


                    <InfoRow
                      label="Last success"
                      value={
                        source.lastSuccessAt
                          ? formatDate(
                              source.lastSuccessAt,
                            )
                          : "None observed"
                      }
                    />


                    <InfoRow
                      label="Last failure"
                      value={
                        source.lastFailureAt
                          ? formatDate(
                              source.lastFailureAt,
                            )
                          : "None observed"
                      }
                    />

                  </dl>

                </article>

              ),
            )
          }

        </div>

      </section>


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
              text-[#2f6fed]
            "
          />


          <div>

            <h2
              className="
                text-lg
                font-semibold
                text-slate-950
              "
            >
              Recent ingestion runs
            </h2>


            <p
              className="
                mt-1
                text-xs
                text-slate-500
              "
            >
              Most recent canonical ingestion activity across
              all discovered sources.
            </p>

          </div>

        </div>


        <div
          className="
            mt-5
            overflow-x-auto
          "
        >

          <table
            className="
              w-full
              min-w-[900px]
              text-left
            "
          >

            <thead>

              <tr
                className="
                  border-b
                  border-slate-200
                  text-[10px]
                  font-semibold
                  uppercase
                  tracking-[0.08em]
                  text-slate-400
                "
              >

                <th className="pb-3 pr-5">
                  Source
                </th>

                <th className="pb-3 pr-5">
                  Outcome
                </th>

                <th className="pb-3 pr-5">
                  Started
                </th>

                <th className="pb-3 pr-5">
                  Finished
                </th>

                <th className="pb-3 pr-5">
                  Duration
                </th>

                <th className="pb-3 pr-5">
                  Imported
                </th>

                <th className="pb-3">
                  Rejected
                </th>

              </tr>

            </thead>


            <tbody>

              {
                operations.recentRuns.map(
                  (
                    run,
                    index,
                  ) => (

                    <tr
                      key={
                        `${run.source}-${run.startedAt}-${index}`
                      }
                      className="
                        border-b
                        border-slate-100
                        text-xs
                        text-slate-600
                        last:border-0
                      "
                    >

                      <td
                        className="
                          py-4
                          pr-5
                          font-semibold
                          text-slate-900
                        "
                      >
                        {
                          run.source
                        }
                      </td>


                      <td
                        className="
                          py-4
                          pr-5
                        "
                      >
                        <OutcomeBadge
                          outcome={
                            run.outcome
                          }
                        />
                      </td>


                      <td
                        className="
                          py-4
                          pr-5
                        "
                      >
                        {
                          formatDate(
                            run.startedAt,
                          )
                        }
                      </td>


                      <td
                        className="
                          py-4
                          pr-5
                        "
                      >
                        {
                          run.finishedAt
                            ? formatDate(
                                run.finishedAt,
                              )
                            : "—"
                        }
                      </td>


                      <td
                        className="
                          py-4
                          pr-5
                        "
                      >
                        {
                          durationText(
                            run.durationSeconds,
                          )
                        }
                      </td>


                      <td
                        className="
                          py-4
                          pr-5
                        "
                      >
                        {
                          run.importedCount ===
                          null
                            ? "—"
                            : run.importedCount
                                .toLocaleString()
                        }
                      </td>


                      <td
                        className="
                          py-4
                        "
                      >
                        {
                          run.rejectedCount ===
                          null
                            ? "—"
                            : run.rejectedCount
                                .toLocaleString()
                        }
                      </td>

                    </tr>

                  ),
                )
              }

            </tbody>

          </table>


          {
            operations.recentRuns.length ===
            0

              ? (

                <div
                  className="
                    py-14
                    text-center
                  "
                >

                  <Database
                    className="
                      mx-auto
                      size-8
                      text-slate-300
                    "
                  />


                  <p
                    className="
                      mt-4
                      text-sm
                      font-semibold
                      text-slate-900
                    "
                  >
                    No ingestion runs observed yet
                  </p>


                  <p
                    className="
                      mt-1
                      text-xs
                      text-slate-400
                    "
                  >
                    Source health will populate as TED, BOAMP,
                    DECP or other canonical importers record runs.
                  </p>

                </div>

              )

              : null
          }

        </div>

      </section>


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

        Source health is shared operational metadata for the
        canonical public-procurement corpus. This screen is
        intentionally read-only: organization roles cannot pause
        or disrupt shared ingestion for other tenants. Existing
        source adapters remain the authoritative ingestion
        execution path.

      </div>

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
    LucideIcon;

  label:
    string;

  value:
    number
    |
    string;

  detail:
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
          typeof value ===
          "number"
            ? value.toLocaleString()
            : value
        }
      </p>


      <p
        className="
          mt-1
          text-xs
          text-slate-500
        "
      >
        {
          detail
        }
      </p>

    </article>

  );

}


function SmallMetric({

  label,

  value,

}: {

  label:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        rounded-xl
        bg-slate-50
        p-3
      "
    >

      <p
        className="
          text-[9px]
          font-semibold
          uppercase
          tracking-[0.08em]
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
          text-sm
          font-semibold
          text-slate-900
        "
      >
        {
          value
        }
      </p>

    </div>

  );

}


function HealthBadge({

  health,

}: {

  health:
    SourceHealthStatus;

}) {

  const className =
    health ===
    "HEALTHY"
      ? "bg-emerald-50 text-emerald-700"
      : health ===
        "RUNNING"
        ? "bg-blue-50 text-blue-700"
        : health ===
          "DEGRADED"
          ? "bg-amber-50 text-amber-700"
          : health ===
            "STALE"
            ? "bg-orange-50 text-orange-700"
            : health ===
              "UNHEALTHY"
              ? "bg-red-50 text-red-700"
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
        formatEnum(
          health,
        )
      }
    </span>

  );

}


function OutcomeBadge({

  outcome,

}: {

  outcome:
    IngestionRunOutcome;

}) {

  const className =
    outcome ===
    "SUCCESS"
      ? "bg-emerald-50 text-emerald-700"
      : outcome ===
        "FAILURE"
        ? "bg-red-50 text-red-700"
        : outcome ===
          "RUNNING"
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
        outcome
      }
    </span>

  );

}


function InfoRow({

  label,

  value,

}: {

  label:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        flex
        items-center
        justify-between
        gap-4
        border-b
        border-slate-100
        pb-3
        last:border-0
        last:pb-0
      "
    >

      <dt
        className="
          text-slate-400
        "
      >
        {
          label
        }
      </dt>


      <dd
        className="
          text-right
          font-semibold
          text-slate-700
        "
      >
        {
          value
        }
      </dd>

    </div>

  );

}


function durationText(
  seconds:
    number
    |
    null,
) {

  if (
    seconds ===
    null
  ) {

    return "—";

  }


  if (
    seconds <
    60
  ) {

    return `${seconds}s`;

  }


  const minutes =
    Math.floor(
      seconds /
      60,
    );


  const remainingSeconds =
    seconds %
    60;


  if (
    minutes <
    60
  ) {

    return `${minutes}m ${remainingSeconds}s`;

  }


  const hours =
    Math.floor(
      minutes /
      60,
    );


  const remainingMinutes =
    minutes %
    60;


  return `${hours}h ${remainingMinutes}m`;

}
