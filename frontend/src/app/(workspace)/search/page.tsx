"use client";


import {

  ArrowRight,

  CheckCircle2,

  Database,

  Gauge,

  LoaderCircle,

  RefreshCw,

  Search,

  Server,

  ShieldCheck,

  Sparkles,

  TriangleAlert,

} from "lucide-react";


import Link from "next/link";
import { useSearchParams } from "next/navigation";


import {

  type FormEvent,

  useEffect,

  useState,

} from "react";


import {
  useAuth,
} from "@/features/auth/auth-context";

import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { readOption, readText, type ParamReader } from "@/features/navigation/url-filters";

import { SavedViewsMenu } from "@/features/saved-views/saved-views-menu";

import { useSavedViews } from "@/features/saved-views/use-saved-views";


import {

  formatDate,

  formatEnum,

  formatMoney,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  ContractNature,

  ProcurementSource,

  ProcurementStatus,

} from "@/features/procurement/procurement-types";


import type {

  ProcurementSearchResponse,

  SearchProjectionStatus,

  SearchReindexResponse,

} from "@/features/search/search-types";


type SearchFilters = {

  status:
    ProcurementStatus
    |
    "";

  source:
    ProcurementSource
    |
    "";

  contractNature:
    ContractNature
    |
    "";

  minimumQualityScore:
    string;

};





/** Result window sizes. The backend never returns more than 100 hits per request. */
const SEARCH_SIZES = [25, 50, 100] as const;

type AppliedSearch = {
  query: string;
  filters: SearchFilters;
  size: number;
};

type SavedSearch = {
  query: string;
  filters: SearchFilters;
};

function buildSearchParams({ query, filters, size }: AppliedSearch): URLSearchParams {
  const params = new URLSearchParams();
  if (query.trim() !== "") params.set("q", query.trim());
  if (filters.status !== "") params.set("status", filters.status);
  if (filters.source !== "") params.set("source", filters.source);
  if (filters.contractNature !== "") params.set("contractNature", filters.contractNature);
  if (filters.minimumQualityScore !== "") params.set("minQualityScore", filters.minimumQualityScore);
  params.set("size", String(size));
  return params;
}

function hasAnyFilter(query: string, filters: SearchFilters): boolean {
  return (
    query.trim() !== "" ||
    filters.status !== "" ||
    filters.source !== "" ||
    filters.contractNature !== "" ||
    filters.minimumQualityScore !== ""
  );
}

const STATUS_VALUES: ProcurementStatus[] = ["PUBLISHED", "UPDATED", "AWARDED", "CLOSED"];
const SOURCE_VALUES: ProcurementSource[] = ["TED", "BOAMP", "DECP", "MANUAL_DEMO"];
const NATURE_VALUES: ContractNature[] = ["WORKS", "SERVICES", "SUPPLIES", "MIXED"];
const QUALITY_VALUES = ["90", "75", "60"];

/** Reads the starting search from the address, so links like /search?q=72000000 work. */
function startFromParams(params: ParamReader): SavedSearch {
  return {
    query: readText(params, "q"),
    filters: {
      status: readOption<SearchFilters["status"]>(params, "status", STATUS_VALUES, ""),
      source: readOption<SearchFilters["source"]>(params, "source", SOURCE_VALUES, ""),
      contractNature: readOption<SearchFilters["contractNature"]>(params, "contractNature", NATURE_VALUES, ""),
      minimumQualityScore: readOption<string>(params, "minQualityScore", QUALITY_VALUES, ""),
    },
  };
}

export default function ProcurementSearchPage() {

  const {

    api,

    user,

  } = useAuth();


  const searchParams = useSearchParams();
  const { toast } = useToast();
  const [start] = useState(() => startFromParams(searchParams));
  const [query, setQuery] = useState(start.query);
  const [applied, setApplied] = useState<AppliedSearch>(() => ({
    query: start.query,
    filters: start.filters,
    size: SEARCH_SIZES[0],
  }));

  const [loadingMore, setLoadingMore] = useState(false);

  const [exhausted, setExhausted] = useState(false);

  const { views, save, remove } = useSavedViews<SavedSearch>("search");


  const [filters, setFilters] = useState<SearchFilters>(start.filters);


  const [

    result,

    setResult,

  ] = useState<
    ProcurementSearchResponse
    |
    null
  >(
    null,
  );


  const [

    status,

    setStatus,

  ] = useState<
    SearchProjectionStatus
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

    searching,

    setSearching,

  ] = useState(
    false,
  );


  const [

    rebuilding,

    setRebuilding,

  ] = useState(
    false,
  );


  const [

    rebuildResult,

    setRebuildResult,

  ] = useState<
    SearchReindexResponse
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


  useEffect(
    () => {

      let cancelled =
        false;


      Promise.all([

        api<SearchProjectionStatus>(
          "/api/v1/search/status",
        ),

        api<ProcurementSearchResponse>(
          `/api/v1/search/opportunities?${buildSearchParams({ ...start, size: SEARCH_SIZES[0] }).toString()}`,
        ),

      ])
        .then(
          (
            [
              projectionStatus,
              initialResult,
            ],
          ) => {

            if (
              cancelled
            ) {
              return;
            }


            setStatus(
              projectionStatus,
            );

            setResult(
              initialResult,
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
                  : "Unable to initialize procurement search.",
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
      start,
    ],
  );

  /** Copies a link that opens this same search, so it can be shared with a colleague. */
  async function copyLink() {
    const params = buildSearchParams(applied);
    params.delete("size");
    try {
      await navigator.clipboard.writeText(`${window.location.origin}/search?${params.toString()}`);
      toast({ tone: "success", title: "Link copied", description: "Anyone in your organization can open this search." });
    } catch {
      toast({ tone: "error", title: "Could not copy the link", description: "Copy the address from the browser instead." });
    }
  }

  async function runSearch(next: AppliedSearch, more: boolean) {
    if (more) {
      setLoadingMore(true);
    } else {
      setSearching(true);
    }
    setError(null);

    try {
      const response = await api<ProcurementSearchResponse>(
        `/api/v1/search/opportunities?${buildSearchParams(next).toString()}`,
      );
      setResult(response);
      setApplied(next);
      setExhausted(more && response.hits.length <= (result?.hits.length ?? 0));

      if (!more) {
        setStatus(await api<SearchProjectionStatus>("/api/v1/search/status"));
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Procurement search failed.");
    } finally {
      setSearching(false);
      setLoadingMore(false);
    }
  }

  async function executeSearch(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();
    await runSearch({ query, filters, size: SEARCH_SIZES[0] }, false);
  }

  async function showMore() {
    const nextSize = SEARCH_SIZES.find((size) => size > applied.size);
    if (nextSize === undefined) return;
    await runSearch({ ...applied, size: nextSize }, true);
  }

  async function rebuildIndex() {

    setRebuilding(
      true,
    );

    setRebuildResult(
      null,
    );

    setError(
      null,
    );


    try {

      const response =
        await api<SearchReindexResponse>(
          "/api/v1/search/reindex",
          {
            method:
              "POST",
          },
        );


      setRebuildResult(
        response,
      );


      const projectionStatus =
        await api<SearchProjectionStatus>(
          "/api/v1/search/status",
        );


      setStatus(
        projectionStatus,
      );


      await executeSearch();


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to rebuild the search projection.",
      );

    } finally {

      setRebuilding(
        false,
      );

    }

  }


  const nextSearchSize = SEARCH_SIZES.find((size) => size > applied.size);

  // The PostgreSQL fallback reports only the hits it returned as the total, so a full page may mean more exist.
  const mayHaveMore =
    result !== null &&
    (result.hits.length < result.total || (result.fallback && result.hits.length >= applied.size));

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
            Procurement discovery
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
            Search intelligence
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
            Full-text and structured procurement search across
            titles, buyers, CPVs, lots and awarded suppliers,
            backed by a rebuildable OpenSearch projection.
          </p>

        </div>


        {
          user?.role ===
          "OWNER"

            ? (

              <button
                type="button"
                onClick={
                  () => {
                    void rebuildIndex();
                  }
                }
                disabled={
                  rebuilding
                }
                className="
                  inline-flex
                  h-11
                  items-center
                  gap-2
                  rounded-xl
                  bg-[#14213d]
                  px-5
                  text-sm
                  font-semibold
                  text-white
                  disabled:opacity-50
                "
              >

                {
                  rebuilding

                    ? (

                      <LoaderCircle
                        className="
                          size-4
                          animate-spin
                        "
                      />

                    )

                    : (

                      <RefreshCw
                        className="size-4"
                      />

                    )
                }

                {
                  rebuilding
                    ? "Rebuilding..."
                    : "Rebuild search index"
                }

              </button>

            )

            : null
        }

      </div>


      {
        status

          ? (

            <div
              className="
                mt-7
                grid
                gap-4
                md:grid-cols-2
                xl:grid-cols-4
              "
            >

              <StatusCard
                icon={Server}
                label="Search engine"
                value={
                  status.reachable
                    ? "OpenSearch"
                    : "PostgreSQL fallback"
                }
                detail={
                  status.reachable
                    ? "Projection reachable"
                    : "Core search remains available"
                }
              />


              <StatusCard
                icon={Database}
                label="Search documents"
                value={
                  status.indexedDocuments
                    .toLocaleString()
                }
                detail={
                  status.indexName
                }
              />


              <StatusCard
                icon={ShieldCheck}
                label="Source of truth"
                value={
                  status.authority
                }
                detail="Search projection is disposable"
              />


              <StatusCard
                icon={Sparkles}
                label="Projection state"
                value={
                  status.indexExists
                    ? "Indexed"
                    : "Not built"
                }
                detail={
                  status.enabled
                    ? "Search projection enabled"
                    : "Projection disabled"
                }
              />

            </div>

          )

          : null
      }


      {
        error

          ? (

            <div
              className="
                mt-6
                flex
                gap-3
                rounded-2xl
                border
                border-red-200
                bg-red-50
                p-4
                text-sm
                text-red-700
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
                error
              }

            </div>

          )

          : null
      }


      {
        rebuildResult

          ? (

            <div
              className="
                mt-6
                flex
                items-start
                gap-3
                rounded-2xl
                border
                border-emerald-200
                bg-emerald-50
                p-4
                text-sm
                text-emerald-800
              "
            >

              <CheckCircle2
                className="
                  mt-0.5
                  size-5
                  shrink-0
                "
              />


              <div>

                <p
                  className="font-semibold"
                >
                  Search projection rebuilt
                </p>


                <p
                  className="mt-1"
                >
                  {
                    rebuildResult.indexedCount
                  }
                  {" "}
                  of
                  {" "}
                  {
                    rebuildResult.discoveredCount
                  }
                  {" "}
                  procurement records indexed.
                  {" "}
                  {
                    rebuildResult.failedCount
                  }
                  {" "}
                  failed.
                </p>

              </div>

            </div>

          )

          : null
      }


      <form
        onSubmit={
          executeSearch
        }
        className="
          mt-6
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-5
          shadow-[0_8px_30px_rgba(15,23,42,0.04)]
        "
      >

        <div
          className="
            flex
            flex-col
            gap-3
            lg:flex-row
          "
        >

          <div
            className="
              relative
              flex-1
            "
          >

            <Search
              className="
                absolute
                left-4
                top-1/2
                size-4
                -translate-y-1/2
                text-slate-400
              "
            />


            <input
              value={
                query
              }
              onChange={
                (
                  event,
                ) =>
                  setQuery(
                    event.target.value,
                  )
              }
              aria-label="Search the tender catalogue"
              placeholder="Search renovation, cybersecurity, buyer, CPV, supplier..."
              className="
                h-12
                w-full
                rounded-xl
                border
                border-slate-200
                bg-slate-50
                pl-11
                pr-4
                text-sm
                outline-none
                focus:border-[#2f6fed]
                focus:bg-white
              "
            />

          </div>


          <button
            type="submit"
            disabled={
              searching
            }
            className="
              inline-flex
              h-12
              items-center
              justify-center
              gap-2
              rounded-xl
              bg-[#2f6fed]
              px-6
              text-sm
              font-semibold
              text-white
              disabled:opacity-50
            "
          >

            {
              searching

                ? (

                  <LoaderCircle
                    className="
                      size-4
                      animate-spin
                    "
                  />

                )

                : (

                  <Search
                    className="size-4"
                  />

                )
            }

            Search catalogue

          </button>

        </div>


        <div
          className="
            mt-4
            grid
            gap-3
            md:grid-cols-2
            xl:grid-cols-4
          "
        >

          <select
            aria-label="Status"
            value={
              filters.status
            }
            onChange={
              (
                event,
              ) =>
                setFilters(
                  (
                    current,
                  ) => ({
                    ...current,
                    status:
                      (event.target.value as SearchFilters["status"]),
                  }),
                )
            }
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              bg-white
              px-3
              text-sm
            "
          >

            <option value="">
              All statuses
            </option>

            <option value="PUBLISHED">
              Published
            </option>

            <option value="UPDATED">
              Updated
            </option>

            <option value="AWARDED">
              Awarded
            </option>

            <option value="CLOSED">
              Closed
            </option>

          </select>


          <select
            aria-label="Source"
            value={
              filters.source
            }
            onChange={
              (
                event,
              ) =>
                setFilters(
                  (
                    current,
                  ) => ({
                    ...current,
                    source:
                      (event.target.value as SearchFilters["source"]),
                  }),
                )
            }
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              bg-white
              px-3
              text-sm
            "
          >

            <option value="">
              All sources
            </option>

            <option value="TED">
              TED
            </option>

            <option value="BOAMP">
              BOAMP
            </option>

            <option value="DECP">
              DECP
            </option>

            <option value="MANUAL_DEMO">
              Demo
            </option>

          </select>


          <select
            aria-label="Contract type"
            value={
              filters.contractNature
            }
            onChange={
              (
                event,
              ) =>
                setFilters(
                  (
                    current,
                  ) => ({
                    ...current,
                    contractNature:
                      (event.target.value as SearchFilters["contractNature"]),
                  }),
                )
            }
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              bg-white
              px-3
              text-sm
            "
          >

            <option value="">
              All contract types
            </option>

            <option value="WORKS">
              Works
            </option>

            <option value="SERVICES">
              Services
            </option>

            <option value="SUPPLIES">
              Supplies
            </option>

            <option value="MIXED">
              Mixed
            </option>

          </select>


          <select
            aria-label="Minimum data quality"
            value={
              filters.minimumQualityScore
            }
            onChange={
              (
                event,
              ) =>
                setFilters(
                  (
                    current,
                  ) => ({
                    ...current,
                    minimumQualityScore:
                      event.target.value,
                  }),
                )
            }
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              bg-white
              px-3
              text-sm
            "
          >

            <option value="">
              Any quality
            </option>

            <option value="90">
              90+ Excellent
            </option>

            <option value="75">
              75+ Good
            </option>

            <option value="60">
              60+ Fair
            </option>

          </select>

        </div>

      </form>


      <div
        className="
          mt-6
          flex
          flex-wrap
          items-center
          justify-between
          gap-3
        "
      >

        <div>

          <p
            className="
              text-sm
              font-semibold
              text-slate-900
            "
          >
            {
              result
                ? `${result.total.toLocaleString()} results`
                : "Search results"
            }
          </p>


          {
            result

              ? (

                <p
                  className="
                    mt-1
                    text-xs
                    text-slate-500
                  "
                >
                  Engine:
                  {" "}
                  {
                    result.engine ===
                    "OPENSEARCH"
                      ? "OpenSearch relevance"
                      : "PostgreSQL fallback"
                  }
                </p>

              )

              : null
          }

        </div>


        <div
          className="
            flex
            flex-wrap
            items-center
            gap-2
          "
        >
          {
            result?.fallback
              ? (
                <span
                  className="
                    rounded-full
                    bg-amber-100
                    px-3
                    py-1.5
                    text-xs
                    font-semibold
                    text-amber-800
                  "
                >
                  Search fallback active
                </span>
              )
              : null
          }
          <Button
            variant="secondary"
            size="sm"
            disabled={!hasAnyFilter(applied.query, applied.filters)}
            onClick={() => {
              void copyLink();
            }}
          >
            Copy link
          </Button>
          <SavedViewsMenu<SavedSearch>
            views={views}
            canSave={hasAnyFilter(query, filters)}
            onApply={(value) => {
              setQuery(value.query);
              setFilters(value.filters);
              void runSearch({ query: value.query, filters: value.filters, size: SEARCH_SIZES[0] }, false);
            }}
            onSave={(name) => {
              save(name, { query: query.trim(), filters });
            }}
            onRemove={remove}
          />
        </div>

      </div>


      <div
        className="
          mt-4
          space-y-4
        "
      >

        {
          result?.hits.map(
            (
              hit,
            ) => {

              const opportunity =
                hit.opportunity;


              return (

                <article
                  key={
                    opportunity.id
                  }
                  className="
                    rounded-2xl
                    border
                    border-slate-200
                    bg-white
                    p-5
                    transition
                    hover:border-slate-300
                    hover:shadow-[0_8px_30px_rgba(15,23,42,0.04)]
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

                        <span
                          className="
                            rounded-full
                            bg-blue-50
                            px-2.5
                            py-1
                            text-[10px]
                            font-semibold
                            text-blue-700
                          "
                        >
                          {
                            formatEnum(
                              opportunity.status,
                            )
                          }
                        </span>


                        {
                          opportunity.sources.map(
                            (
                              source,
                            ) => (

                              <span
                                key={
                                  source
                                }
                                className="
                                  rounded-full
                                  bg-slate-100
                                  px-2.5
                                  py-1
                                  text-[10px]
                                  font-semibold
                                  text-slate-600
                                "
                              >
                                {
                                  source
                                }
                              </span>

                            ),
                          )
                        }


                        {
                          hit.score !==
                          null

                            ? (

                              <span
                                className="
                                  inline-flex
                                  items-center
                                  gap-1
                                  rounded-full
                                  bg-violet-50
                                  px-2.5
                                  py-1
                                  text-[10px]
                                  font-semibold
                                  text-violet-700
                                "
                              >

                                <Sparkles
                                  className="
                                    size-3
                                  "
                                />

                                Relevance
                                {" "}
                                {
                                  hit.score.toFixed(
                                    2,
                                  )
                                }

                              </span>

                            )

                            : null
                        }

                      </div>


                      <h2
                        className="
                          mt-4
                          text-lg
                          font-semibold
                          leading-7
                          text-slate-950
                        "
                      >
                        {
                          opportunity.title
                        }
                      </h2>


                      <p
                        className="
                          mt-2
                          text-sm
                          text-slate-500
                        "
                      >
                        {
                          opportunity.buyerName
                        }
                        {" · "}
                        {
                          opportunity.primaryRegionName
                          ??
                          opportunity.countryCode
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

                        {
                          opportunity.cpvs.map(
                            (
                              cpv,
                            ) => (

                              <span
                                key={
                                  cpv.code
                                }
                                className="
                                  rounded-lg
                                  border
                                  border-slate-200
                                  bg-slate-50
                                  px-2.5
                                  py-1
                                  text-[11px]
                                  text-slate-600
                                "
                              >
                                {
                                  cpv.code
                                }
                              </span>

                            ),
                          )
                        }

                      </div>

                    </div>


                    <div
                      className="
                        grid
                        shrink-0
                        gap-4
                        sm:grid-cols-3
                        xl:w-[470px]
                      "
                    >

                      <ResultMetric
                        icon={Database}
                        label={
                          opportunity.awardedAmount !==
                          null
                            ? "Awarded"
                            : "Estimated"
                        }
                        value={
                          opportunity.awardedAmount !==
                          null
                            ? formatMoney(
                                opportunity.awardedAmount,
                                opportunity.awardedCurrency,
                              )
                            : formatValueRange(
                                opportunity.estimatedMinValue,
                                opportunity.estimatedMaxValue,
                                opportunity.currency,
                              )
                        }
                      />


                      <ResultMetric
                        icon={Gauge}
                        label="Quality"
                        value={
                          opportunity.dataQualityScore ===
                          null
                            ? "Not assessed"
                            : `${opportunity.dataQualityScore}/100`
                        }
                      />


                      <ResultMetric
                        icon={ShieldCheck}
                        label="Deadline"
                        value={
                          opportunity.submissionDeadline
                            ? formatDate(
                                opportunity.submissionDeadline,
                              )
                            : opportunity.status ===
                              "AWARDED"
                              ? "Awarded"
                              : "Not disclosed"
                        }
                      />

                    </div>

                  </div>


                  <div
                    className="
                      mt-5
                      flex
                      items-center
                      justify-between
                      border-t
                      border-slate-100
                      pt-4
                    "
                  >

                    <span
                      className="
                        text-xs
                        text-slate-400
                      "
                    >
                      Published
                      {" "}
                      {
                        formatDate(
                          opportunity.publicationDate,
                        )
                      }
                    </span>


                    <Link
                      href={
                        `/opportunities/${opportunity.id}`
                      }
                      className="
                        inline-flex
                        items-center
                        gap-2
                        text-sm
                        font-semibold
                        text-[#2f6fed]
                      "
                    >
                      Open opportunity

                      <ArrowRight
                        className="size-4"
                      />
                    </Link>

                  </div>

                </article>

              );

            },
          )
        }


        {
          result
          &&
          result.hits.length > 0
            ? (
              <div
                className="
                  flex
                  flex-col
                  items-center
                  gap-3
                  pt-2
                "
              >
                <p
                  className="
                    text-xs
                    text-slate-500
                  "
                >
                  Showing {result.hits.length.toLocaleString()}{result.fallback ? " results" : ` of ${result.total.toLocaleString()}`}
                </p>
                {
                  mayHaveMore
                    ? (
                      nextSearchSize !== undefined && !exhausted
                        ? (
                          <Button
                            loading={loadingMore}
                            onClick={() => void showMore()}
                          >
                            Show more results
                          </Button>
                        )
                        : (
                          <p
                            className="
                              max-w-md
                              text-center
                              text-xs
                              leading-5
                              text-slate-500
                            "
                          >
                            No more results can be loaded for this search.
                            Add a filter or a more specific term to narrow the list.
                          </p>
                        )
                    )
                    : null
                }
              </div>
            )
            : null
        }

        {
          result
          &&
          result.hits.length ===
          0
            ? (

              <div
                className="
                  rounded-3xl
                  border
                  border-dashed
                  border-slate-300
                  bg-white
                  px-6
                  py-16
                  text-center
                "
              >

                <Search
                  className="
                    mx-auto
                    size-7
                    text-slate-300
                  "
                />


                <p
                  className="
                    mt-4
                    text-sm
                    font-semibold
                    text-slate-800
                  "
                >
                  No matching opportunities
                </p>


                <p
                  className="
                    mt-2
                    text-sm
                    text-slate-500
                  "
                >
                  Try a broader term or remove one of the structured filters.
                </p>

              </div>

            )

            : null
        }

      </div>

    </div>

  );

}


function StatusCard({

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
          truncate
          text-sm
          font-semibold
          text-slate-950
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

    </article>

  );

}


function ResultMetric({

  icon:
    Icon,

  label,

  value,

}: {

  icon:
    typeof Database;

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
        p-4
      "
    >

      <Icon
        className="
          size-4
          text-slate-500
        "
      />


      <p
        className="
          mt-3
          text-[10px]
          font-semibold
          uppercase
          tracking-[0.1em]
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
          text-xs
          font-semibold
          text-slate-800
        "
      >
        {
          value
        }
      </p>

    </div>

  );

}
