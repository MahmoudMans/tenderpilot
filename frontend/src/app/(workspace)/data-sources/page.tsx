"use client";

import {

  CheckCircle2,

  Database,

  LoaderCircle,

  ShieldCheck,

} from "lucide-react";

import {

  type FormEvent,

  useEffect,

  useState,

} from "react";

import {

  SourcePanel,

} from "@/components/data-source/source-panel";

import {

  useAuth,

} from "@/features/auth/auth-context";

import type {

  BoampDataSourceStatus,

  DataSourceSyncResult,

  DecpDataSourceStatus,

  TedDataSourceStatus,

} from "@/features/data-source/ted-types";


type SourceName =
  | "TED"
  | "BOAMP"
  | "DECP";


export default function DataSourcesPage() {

  const {

    api,

    user,

  } = useAuth();


  const [
    ted,
    setTed,
  ] = useState<TedDataSourceStatus | null>(
    null,
  );


  const [
    boamp,
    setBoamp,
  ] = useState<BoampDataSourceStatus | null>(
    null,
  );


  const [
    decp,
    setDecp,
  ] = useState<DecpDataSourceStatus | null>(
    null,
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
  ] = useState<SourceName | null>(
    null,
  );


  const [
    error,
    setError,
  ] = useState<string | null>(
    null,
  );


  const [
    result,
    setResult,
  ] = useState<{
    source: SourceName;
    run: DataSourceSyncResult;
  } | null>(
    null,
  );


  const [tedDays, setTedDays] =
    useState(7);

  const [tedLimit, setTedLimit] =
    useState(25);

  const [boampDays, setBoampDays] =
    useState(7);

  const [boampLimit, setBoampLimit] =
    useState(25);

  const [decpDays, setDecpDays] =
    useState(7);

  const [decpLimit, setDecpLimit] =
    useState(25);


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

        api<DecpDataSourceStatus>(
          "/api/v1/data-sources/decp",
        ),

      ])
        .then(
          (
            [
              tedResult,
              boampResult,
              decpResult,
            ],
          ) => {

            if (
              cancelled
            ) {
              return;
            }

            setTed(tedResult);
            setBoamp(boampResult);
            setDecp(decpResult);
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
                  : "Unable to load data sources.",
              );
            }
          },
        )
        .finally(
          () => {

            if (
              !cancelled
            ) {
              setLoading(false);
            }
          },
        );


      return () => {
        cancelled = true;
      };

    },
    [api],
  );


  async function refresh() {

    const [
      tedResult,
      boampResult,
      decpResult,
    ] =
      await Promise.all([

        api<TedDataSourceStatus>(
          "/api/v1/data-sources/ted",
        ),

        api<BoampDataSourceStatus>(
          "/api/v1/data-sources/boamp",
        ),

        api<DecpDataSourceStatus>(
          "/api/v1/data-sources/decp",
        ),

      ]);

    setTed(tedResult);
    setBoamp(boampResult);
    setDecp(decpResult);
  }


  async function synchronize(

    source:
      SourceName,

    days:
      number,

    limit:
      number,

  ) {

    setSyncing(source);
    setError(null);
    setResult(null);

    try {

      const syncResult =
        await api<DataSourceSyncResult>(
          `/api/v1/data-sources/${source.toLowerCase()}/sync?days=${days}&limit=${limit}`,
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

      await refresh();

    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : `${source} synchronization failed.`,
      );

    } finally {
      setSyncing(null);
    }
  }


  function submit(

    source:
      SourceName,

    days:
      number,

    limit:
      number,

  ) {

    return (
      event:
        FormEvent<HTMLFormElement>,
    ) => {

      event.preventDefault();

      void synchronize(
        source,
        days,
        limit,
      );
    };
  }


  const owner =
    user?.role ===
    "OWNER";


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
              text-4xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
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
            EU notices, French notices and French
            award-stage procurement intelligence
            normalized into one canonical platform.
          </p>

        </div>


        <div
          className="
            inline-flex
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
            className="size-4"
          />
          Free official open data
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
                title="Tenders Electronic Daily"
                description="Official EU published procurement notices normalized into the shared TenderPilot opportunity catalogue."
                apiVersion={ted.apiVersion}
                baseUrl={ted.baseUrl}
                endpoint={ted.searchPath}
                datasetLabel="European procurement notices"
                documentationUrl="https://docs.ted.europa.eu/api/latest/search.html"
                authenticationRequired={
                  ted.authenticationRequired
                }
                recordCount={
                  ted.canonicalNoticeCount
                }
                recordCountLabel="Canonical notices"
                lastSeenAt={
                  ted.lastNoticeSeenAt
                }
                lastRun={
                  ted.lastRun
                }
                days={tedDays}
                setDays={setTedDays}
                limit={tedLimit}
                setLimit={setTedLimit}
                canSync={
                  owner
                  &&
                  ted.manualSyncEnabled
                }
                syncing={
                  syncing ===
                  "TED"
                }
                onSubmit={
                  submit(
                    "TED",
                    tedDays,
                    tedLimit,
                  )
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
                title="Bulletin officiel des annonces des marchés publics"
                description="Official French procurement notices retrieved through the DILA public open-data service."
                apiVersion={boamp.apiVersion}
                baseUrl={boamp.baseUrl}
                endpoint={boamp.recordsPath}
                datasetLabel={
                  `Dataset: ${boamp.datasetId}`
                }
                documentationUrl="https://boamp-datadila.opendatasoft.com/explore/dataset/boamp/api/"
                authenticationRequired={
                  boamp.authenticationRequired
                }
                recordCount={
                  boamp.canonicalNoticeCount
                }
                recordCountLabel="Canonical notices"
                lastSeenAt={
                  boamp.lastNoticeSeenAt
                }
                lastRun={
                  boamp.lastRun
                }
                days={boampDays}
                setDays={setBoampDays}
                limit={boampLimit}
                setLimit={setBoampLimit}
                canSync={
                  owner
                  &&
                  boamp.manualSyncEnabled
                }
                syncing={
                  syncing ===
                  "BOAMP"
                }
                onSubmit={
                  submit(
                    "BOAMP",
                    boampDays,
                    boampLimit,
                  )
                }
              />
            )
            : null
        }


        {
          decp
            ? (
              <SourcePanel
                source="DECP"
                title="Données essentielles de la commande publique"
                description="Official French award-stage contract data. Awarded amounts and suppliers remain separate from procurement estimates."
                apiVersion={decp.apiVersion}
                baseUrl={decp.baseUrl}
                endpoint={decp.recordsPath}
                datasetLabel={
                  `Dataset: ${decp.datasetId}`
                }
                documentationUrl="https://data.economie.gouv.fr/explore/dataset/decp-2022-marches-valides/"
                authenticationRequired={
                  decp.authenticationRequired
                }
                recordCount={
                  decp.canonicalContractCount
                }
                recordCountLabel="Awarded contracts"
                lastSeenAt={
                  decp.lastContractSeenAt
                }
                lastRun={
                  decp.lastRun
                }
                days={decpDays}
                setDays={setDecpDays}
                limit={decpLimit}
                setLimit={setDecpLimit}
                canSync={
                  owner
                  &&
                  decp.manualSyncEnabled
                }
                syncing={
                  syncing ===
                  "DECP"
                }
                onSubmit={
                  submit(
                    "DECP",
                    decpDays,
                    decpLimit,
                  )
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
                        text-emerald-800
                      "
                    >
                      {
                        result.run.processedCount
                      }
                      {" "}
                      processed,
                      {" "}
                      {
                        result.run.failedCount
                      }
                      {" "}
                      failed.
                    </p>

                  </div>

                </div>

              </section>
            )
            : null
        }


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
              items-start
              gap-4
            "
          >

            <div
              className="
                grid
                size-11
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

              <h2
                className="
                  text-base
                  font-semibold
                  text-slate-950
                "
              >
                Tender and award semantics stay separate
              </h2>

              <p
                className="
                  mt-2
                  max-w-4xl
                  text-sm
                  leading-6
                  text-slate-500
                "
              >
                TED and BOAMP contribute procurement
                notice intelligence. DECP contributes
                awarded amounts, suppliers, duration and
                notification information without turning
                those values into tender estimates.
              </p>

            </div>

          </div>

        </section>

      </div>

    </div>

  );
}
