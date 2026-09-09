import {

  Activity,

  Clock3,

  Database,

  ExternalLink,

  KeyRound,

  LoaderCircle,

  Play,

  RefreshCw,

  Server,

  TriangleAlert,

} from "lucide-react";

import type {

  ComponentType,

  FormEvent,

} from "react";

import type {

  DataSourceLastRun,

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
    new Date(value),
  );
}


export function SourcePanel({

  source,

  title,

  description,

  apiVersion,

  baseUrl,

  endpoint,

  datasetLabel,

  documentationUrl,

  authenticationRequired,

  recordCount,

  recordCountLabel,

  lastSeenAt,

  lastRun,

  days,

  setDays,

  limit,

  setLimit,

  canSync,

  syncing,

  onSubmit,

}: {

  source: string;

  title: string;

  description: string;

  apiVersion: string;

  baseUrl: string;

  endpoint: string;

  datasetLabel: string;

  documentationUrl: string;

  authenticationRequired: boolean;

  recordCount: number;

  recordCountLabel: string;

  lastSeenAt:
    string
    |
    null;

  lastRun:
    DataSourceLastRun
    |
    null;

  days: number;

  setDays:
    (
      value: number,
    ) => void;

  limit: number;

  setLimit:
    (
      value: number,
    ) => void;

  canSync: boolean;

  syncing: boolean;

  onSubmit:
    (
      event:
        FormEvent<HTMLFormElement>,
    ) => void;

}) {

  return (

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
                className="size-5"
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
                  {
                    title
                  }
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
                    apiVersion
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
                {
                  description
                }
              </p>

            </div>

          </div>


          <a
            href={
              documentationUrl.trim()
            }
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
              hover:bg-slate-50
            "
          >

            Source

            <ExternalLink
              className="size-4"
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
          icon={KeyRound}
          label="Authentication"
          value={
            authenticationRequired
              ? "Required"
              : "No API key"
          }
          detail="Public open data"
        />

        <Metric
          icon={Database}
          label={recordCountLabel}
          value={
            recordCount.toLocaleString()
          }
          detail={
            `${source} canonical records`
          }
        />

        <Metric
          icon={Clock3}
          label="Last source touch"
          value={
            formatDateTime(
              lastSeenAt,
            )
          }
          detail="Latest observed record"
        />

        <Metric
          icon={Server}
          label="Source contract"
          value={endpoint}
          detail={datasetLabel}
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
          onSubmit={onSubmit}
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
                className="size-4"
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
                Recent-data sync
              </h3>

              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Bounded local development ingestion.
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
                value={days}
                onChange={
                  (
                    event,
                  ) =>
                    setDays(
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
                Maximum records
              </span>

              <select
                value={limit}
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
                    className="size-4"
                  />
                )
            }

            {
              syncing
                ? `Synchronizing ${source}...`
                : `Run ${source} sync`
            }

          </button>

        </form>


        <RunPanel
          run={lastRun}
        />

      </div>


      <div
        className="
          border-t
          border-slate-100
          px-6
          py-4
          text-[11px]
          text-slate-400
        "
      >
        {
          baseUrl
        }
      </div>

    </section>

  );
}


function RunPanel({

  run,

}: {

  run:
    DataSourceLastRun
    |
    null;

}) {

  if (
    run ===
    null
  ) {

    return (

      <div
        className="
          grid
          min-h-64
          place-items-center
          rounded-2xl
          border
          border-slate-200
          p-5
        "
      >

        <div
          className="text-center"
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
            No ingestion run recorded yet.
          </p>

        </div>

      </div>

    );
  }


  return (

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
        "
      >

        <h3
          className="
            text-sm
            font-semibold
            text-slate-900
          "
        >
          Latest ingestion run
        </h3>

        <span
          className="
            rounded-full
            bg-slate-100
            px-2.5
            py-1
            text-[11px]
            font-semibold
            text-slate-700
          "
        >
          {
            run.status
          }
        </span>

      </div>


      <div
        className="
          mt-5
          grid
          gap-3
          sm:grid-cols-3
        "
      >

        <Count
          label="Discovered"
          value={
            run.discoveredCount
          }
        />

        <Count
          label="Processed"
          value={
            run.processedCount
          }
        />

        <Count
          label="Failed"
          value={
            run.failedCount
          }
        />

      </div>


      <div
        className="
          mt-4
          rounded-xl
          bg-slate-50
          p-4
        "
      >

        <code
          className="
            break-all
            text-xs
            text-slate-700
          "
        >
          {
            run.query
          }
        </code>

      </div>


      {
        run.lastError
          ? (
            <div
              className="
                mt-4
                flex
                gap-3
                rounded-xl
                border
                border-amber-200
                bg-amber-50
                p-4
                text-xs
                text-amber-800
              "
            >
              <TriangleAlert
                className="
                  size-4
                  shrink-0
                "
              />
              {
                run.lastError
              }
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
    ComponentType<{
      className?:
        string;
    }>;

  label: string;

  value: string;

  detail: string;

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


function Count({

  label,

  value,

}: {

  label: string;

  value: number;

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
