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

  type ComponentType,

  type FormEvent,

  useEffect,

  useState,

} from "react";


import {

  useAuth,

} from "@/features/auth/auth-context";


import type {

  BoampDataSourceStatus,

  DataSourceLastRun,

  DataSourceSyncResult,

  TedDataSourceStatus,

} from "@/features/data-source/ted-types";


type SourceName =
  "TED"
  |
  "BOAMP";


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

    ted,

    setTed,

  ] = useState<
    TedDataSourceStatus
    |
    null
  >(
    null,
  );


  const [

    boamp,

    setBoamp,

  ] = useState<
    BoampDataSourceStatus
    |
    null
  >(
    null,
  );


  const [

    tedDays,

    setTedDays,

  ] = useState(
    7,
  );


  const [

    tedLimit,

    setTedLimit,

  ] = useState(
    25,
  );


  const [

    boampDays,

    setBoampDays,

  ] = useState(
    7,
  );


  const [

    boampLimit,

    setBoampLimit,

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

  ] = useState<
    SourceName
    |
    null
  >(
    null,
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

    result,

    setResult,

  ] = useState<{

    source:
      SourceName;

    run:
      DataSourceSyncResult;

  } | null>(
    null,
  );


  useEffect(
    () => {

      let cancelled =
        false;


      Promise.all([

        api<TedDataSourceStatus>(
          "/api/v1/data-sources/ted",
        ),

        api<BoampDataSourceStatus>(
          "/api/v1/data-sources/boamp",
        ),

      ])
        .then(
          (
            [
              tedResult,
              boampResult,
            ],
          ) => {

            if (
              cancelled
            ) {

              return;

            }


            setTed(
              tedResult,
            );


            setBoamp(
              boampResult,
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


  async function refreshStatuses() {

    const [
      tedResult,
      boampResult,
    ] =

      await Promise.all([

        api<TedDataSourceStatus>(
          "/api/v1/data-sources/ted",
        ),

        api<BoampDataSourceStatus>(
          "/api/v1/data-sources/boamp",
        ),

      ]);


    setTed(
      tedResult,
    );


    setBoamp(
      boampResult,
    );

  }


  async function syncSource(

    source:
      SourceName,

    days:
      number,

    limit:
      number,

  ) {

    setSyncing(
      source,
    );


    setError(
      null,
    );


    setResult(
      null,
    );


    try {

      const path =

        source ===
        "TED"

          ? `/api/v1/data-sources/ted/sync?days=${days}&limit=${limit}`

          : `/api/v1/data-sources/boamp/sync?days=${days}&limit=${limit}`;


      const syncResult =

        await api<DataSourceSyncResult>(

          path,

          {

            method:
              "POST",

          },

        );


      setResult({

        source,

        run:
          syncResult,

      });


      await refreshStatuses();


    } catch (
      cause
    ) {

      setError(

        cause instanceof Error

          ? cause.message

          : `${source} synchronization failed.`,

      );


    } finally {

      setSyncing(
        null,
      );

    }

  }


  function submitTed(
    event:
      FormEvent<HTMLFormElement>,
  ) {

    event.preventDefault();


    void syncSource(

      "TED",

      tedDays,

      tedLimit,

    );

  }


  function submitBoamp(
    event:
      FormEvent<HTMLFormElement>,
  ) {

    event.preventDefault();


    void syncSource(

      "BOAMP",

      boampDays,

      boampLimit,

    );

  }


  const isOwner =
    user?.role ===
    "OWNER";


  return (

    <div
      className="
        mx-auto
        max-w-[1450px]
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

            Official European and French procurement feeds normalized
            into one source-neutral TenderPilot catalogue.

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

          Free official open-data integrations

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

          : (

            <div
              className="
                mt-7
                space-y-6
              "
            >

              {
                ted

                  ? (

                    <SourcePanel

                      source="TED"

                      title="
                        Tenders Electronic Daily
                      "

                      description="
                        Official EU procurement published-notice Search API normalized into TenderPilot's canonical tender model.
                      "

                      apiVersion={
                        ted.apiVersion
                      }

                      baseUrl={
                        ted.baseUrl
                      }

                      endpoint={
                        ted.searchPath
                      }

                      datasetLabel="
                        Published EU notices
                      "

                      documentationUrl="
                        https://docs.ted.europa.eu/api/latest/search.html
                      "

                      authenticationRequired={
                        ted.authenticationRequired
                      }

                      noticeCount={
                        ted.canonicalNoticeCount
                      }

                      lastNoticeSeenAt={
                        ted.lastNoticeSeenAt
                      }

                      lastRun={
                        ted.lastRun
                      }

                      days={
                        tedDays
                      }

                      setDays={
                        setTedDays
                      }

                      limit={
                        tedLimit
                      }

                      setLimit={
                        setTedLimit
                      }

                      canSync={
                        isOwner
                        &&
                        ted.manualSyncEnabled
                      }

                      syncing={
                        syncing ===
                        "TED"
                      }

                      onSubmit={
                        submitTed
                      }

                    />

                  )

                  : null
              }


              {
                boamp

                  ? (

                    <SourcePanel

                      source="BOAMP"

                      title="
                        Bulletin officiel des annonces des marchés publics
                      "

                      description="
                        Official French BOAMP/DILA open-data records retrieved through the public Opendatasoft Explore API.
                      "

                      apiVersion={
                        boamp.apiVersion
                      }

                      baseUrl={
                        boamp.baseUrl
                      }

                      endpoint={
                        boamp.recordsPath
                      }

                      datasetLabel={
                        `Dataset: ${boamp.datasetId}`
                      }

                      documentationUrl="
                        https://boamp-datadila.opendatasoft.com/explore/dataset/boamp/api/
                      "

                      authenticationRequired={
                        boamp.authenticationRequired
                      }

                      noticeCount={
                        boamp.canonicalNoticeCount
                      }

                      lastNoticeSeenAt={
                        boamp.lastNoticeSeenAt
                      }

                      lastRun={
                        boamp.lastRun
                      }

                      days={
                        boampDays
                      }

                      setDays={
                        setBoampDays
                      }

                      limit={
                        boampLimit
                      }

                      setLimit={
                        setBoampLimit
                      }

                      canSync={
                        isOwner
                        &&
                        boamp.manualSyncEnabled
                      }

                      syncing={
                        syncing ===
                        "BOAMP"
                      }

                      onSubmit={
                        submitBoamp
                      }

                    />

                  )

                  : null
              }


              {
                result

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

                            {
                              result.source
                            }
                            {" "}
                            synchronization finished

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
                              result.run.processedCount
                            }
                            {" "}
                            of
                            {" "}
                            {
                              result.run.discoveredCount
                            }
                            {" "}
                            returned notices with
                            {" "}
                            {
                              result.run.failedCount
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
                    Retrieve
                  "

                  text="
                    TED and BOAMP each keep their own official API client, query language and transport contracts.
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
                    Source-specific buyer, CPV, geography, procedure, deadline and notice signals are converted into canonical values.
                  "

                />


                <ArchitectureCard

                  icon={
                    Database
                  }

                  title="
                    Deduplicate
                  "

                  text="
                    Source plus external notice identifier provides idempotent lineage while the shared tender catalogue remains source-neutral.
                  "

                />

              </section>

            </div>

          )
      }

    </div>

  );

}


function SourcePanel({

  source,

  title,

  description,

  apiVersion,

  baseUrl,

  endpoint,

  datasetLabel,

  documentationUrl,

  authenticationRequired,

  noticeCount,

  lastNoticeSeenAt,

  lastRun,

  days,

  setDays,

  limit,

  setLimit,

  canSync,

  syncing,

  onSubmit,

}: {

  source:
    SourceName;

  title:
    string;

  description:
    string;

  apiVersion:
    string;

  baseUrl:
    string;

  endpoint:
    string;

  datasetLabel:
    string;

  documentationUrl:
    string;

  authenticationRequired:
    boolean;

  noticeCount:
    number;

  lastNoticeSeenAt:
    string
    |
    null;

  lastRun:
    DataSourceLastRun
    |
    null;

  days:
    number;

  setDays:
    (
      value:
        number,
    ) => void;

  limit:
    number;

  setLimit:
    (
      value:
        number,
    ) => void;

  canSync:
    boolean;

  syncing:
    boolean;

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
            authenticationRequired

              ? "Required"

              : "No API key"
          }

          detail="
            Public open-data access
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
            noticeCount
              .toLocaleString()
          }

          detail={
            `${source} lineage records`
          }

        />


        <Metric

          icon={
            Clock3
          }

          label="
            Last source touch
          "

          value={
            formatDateTime(
              lastNoticeSeenAt,
            )
          }

          detail="
            Most recently observed notice
          "

        />


        <Metric

          icon={
            Server
          }

          label="
            Source contract
          "

          value={
            endpoint
          }

          detail={
            datasetLabel
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
            onSubmit
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

                Recent-publication sync

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
                value={
                  days
                }
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

                ? `Synchronizing ${source}...`

                : `Run ${source} sync`
            }

          </button>

        </form>


        <RunPanel
          run={
            lastRun
          }
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

            Persistent ingestion telemetry.

          </p>

        </div>


        {
          run

            ? (

              <RunBadge
                status={
                  run.status
                }
              />

            )

            : null
        }

      </div>


      {
        run

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
                    run.discoveredCount
                  }
                />

                <CountBox
                  label="Processed"
                  value={
                    run.processedCount
                  }
                />

                <CountBox
                  label="Failed"
                  value={
                    run.failedCount
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

                  Source query

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
                    run.query
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
                      run.startedAt,
                    )
                  }

                </span>


                <span>

                  Completed:
                  {" "}
                  {
                    formatDateTime(
                      run.completedAt,
                    )
                  }

                </span>

              </div>


              {
                run.lastError

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
                        run.lastError
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

                No ingestion run recorded yet.

              </p>

            </div>

          )
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
    DataSourceLastRun["status"];

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
    ComponentType<{
      className?:
        string;
    }>;

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
