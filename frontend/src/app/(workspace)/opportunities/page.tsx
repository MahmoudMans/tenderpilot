"use client";


import {

  ArrowRight,

  Building2,

  CalendarDays,

  Database,

  FileSearch,

  Filter,

  Layers3,

  LoaderCircle,

  MapPinned,

  RotateCcw,

  Search,

  WalletCards,

} from "lucide-react";


import Link
  from "next/link";


import {

  type FormEvent,

  useEffect,

  useState,

} from "react";


import {

  useAuth,

} from "@/features/auth/auth-context";


import {

  formatDate,

  formatDateTime,

  formatEnum,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  OpportunitySummary,

  ProcurementSource,

  ProcurementStatus,

} from "@/features/procurement/procurement-types";


type FilterState = {

  query:
    string;

  status:
    ""
    |
    ProcurementStatus;

  source:
    ""
    |
    ProcurementSource;

};


const EMPTY_FILTERS:
FilterState = {

  query:
    "",

  status:
    "",

  source:
    "",

};


const STATUS_OPTIONS:
ProcurementStatus[] = [

  "PUBLISHED",

  "UPDATED",

  "CLOSED",

  "AWARDED",

  "CANCELLED",

  "ARCHIVED",

];


const SOURCE_OPTIONS:
ProcurementSource[] = [

  "TED",

  "BOAMP",

  "DECP",

  "MANUAL_DEMO",

];


function pathFor(
  filters:
    FilterState,
) {

  const parameters =

    new URLSearchParams();


  if (
    filters.query.trim() !==
    ""
  ) {

    parameters.set(

      "q",

      filters.query.trim(),

    );

  }


  if (
    filters.status !==
    ""
  ) {

    parameters.set(

      "status",

      filters.status,

    );

  }


  if (
    filters.source !==
    ""
  ) {

    parameters.set(

      "source",

      filters.source,

    );

  }


  const query =
    parameters.toString();


  return query ===
    ""

    ? "/api/v1/opportunities"

    : `/api/v1/opportunities?${query}`;

}


export default function OpportunitiesPage() {

  const {

    api,

  } = useAuth();


  const [

    opportunities,

    setOpportunities,

  ] = useState<
    OpportunitySummary[]
  >(
    [],
  );


  const [

    filters,

    setFilters,

  ] = useState<FilterState>(
    EMPTY_FILTERS,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
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


      api<OpportunitySummary[]>(

        "/api/v1/opportunities",

      )
        .then(
          (
            result,
          ) => {

            if (
              !cancelled
            ) {

              setOpportunities(
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

                  : "Unable to load opportunities.",

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


  async function searchOpportunities(
    event:
      FormEvent<HTMLFormElement>,
  ) {

    event.preventDefault();


    setLoading(
      true,
    );


    setError(
      null,
    );


    try {

      const result =

        await api<OpportunitySummary[]>(

          pathFor(
            filters,
          ),

        );


      setOpportunities(
        result,
      );


    } catch (
      cause
    ) {

      setError(

        cause instanceof Error

          ? cause.message

          : "Unable to search opportunities.",

      );


    } finally {

      setLoading(
        false,
      );

    }

  }


  async function resetFilters() {

    setFilters(
      EMPTY_FILTERS,
    );


    setLoading(
      true,
    );


    setError(
      null,
    );


    try {

      const result =

        await api<OpportunitySummary[]>(

          "/api/v1/opportunities",

        );


      setOpportunities(
        result,
      );


    } catch (
      cause
    ) {

      setError(

        cause instanceof Error

          ? cause.message

          : "Unable to reload opportunities.",

      );


    } finally {

      setLoading(
        false,
      );

    }

  }


  return (

    <div
      className="
        mx-auto
        max-w-[1440px]
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

            Procurement intelligence

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

            Opportunities

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

            Source-neutral public procurement records normalized
            into TenderPilot&apos;s canonical tender model.

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
            border-blue-200
            bg-blue-50
            px-4
            py-2
            text-xs
            font-semibold
            text-blue-700
          "
        >

          <Database
            className="
              size-4
            "
          />

          Canonical procurement layer

        </div>

      </div>


      <form

        onSubmit={
          searchOpportunities
        }

        className="
          mt-7
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-5
          shadow-[0_10px_36px_rgba(15,23,42,0.045)]
        "

      >

        <div
          className="
            grid
            gap-3
            lg:grid-cols-[1fr_220px_180px_auto]
          "
        >

          <label
            className="
              relative
            "
          >

            <Search
              className="
                absolute
                left-3.5
                top-1/2
                size-4
                -translate-y-1/2
                text-slate-400
              "
            />


            <input

              value={
                filters.query
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

                      query:
                        event.target.value,

                    }),
                  )
              }

              placeholder="
                Search title, buyer or CPV...
              "

              className="
                h-11
                w-full
                rounded-xl
                border
                border-slate-200
                bg-white
                pl-10
                pr-4
                text-sm
                text-slate-900
                outline-none
                focus:border-[#2f6fed]
                focus:ring-4
                focus:ring-blue-500/10
              "

            />

          </label>


          <select

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
                      (event.target.value as FilterState["status"]),

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
              text-slate-700
              outline-none
              focus:border-[#2f6fed]
            "

          >

            <option value="">

              Any status

            </option>


            {
              STATUS_OPTIONS.map(
                (
                  status,
                ) => (

                  <option
                    key={
                      status
                    }
                    value={
                      status
                    }
                  >

                    {
                      formatEnum(
                        status,
                      )
                    }

                  </option>

                ),
              )
            }

          </select>


          <select

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
                      (event.target.value as FilterState["source"]),

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
              text-slate-700
              outline-none
              focus:border-[#2f6fed]
            "

          >

            <option value="">

              Any source

            </option>


            {
              SOURCE_OPTIONS.map(
                (
                  source,
                ) => (

                  <option
                    key={
                      source
                    }
                    value={
                      source
                    }
                  >

                    {
                      source
                    }

                  </option>

                ),
              )
            }

          </select>


          <div
            className="
              flex
              gap-2
            "
          >

            <button
              type="submit"
              disabled={
                loading
              }
              className="
                inline-flex
                h-11
                flex-1
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
                disabled:opacity-50
              "
            >

              <Filter
                className="
                  size-4
                "
              />

              Apply

            </button>


            <button
              type="button"
              onClick={
                () => {

                  void resetFilters();

                }
              }
              disabled={
                loading
              }
              className="
                grid
                size-11
                place-items-center
                rounded-xl
                border
                border-slate-200
                bg-white
                text-slate-500
                transition
                hover:bg-slate-50
                disabled:opacity-50
              "
              title="
                Reset filters
              "
            >

              <RotateCcw
                className="
                  size-4
                "
              />

            </button>

          </div>

        </div>

      </form>


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

              <div
                className="
                  text-center
                "
              >

                <LoaderCircle
                  className="
                    mx-auto
                    size-6
                    animate-spin
                    text-[#2f6fed]
                  "
                />


                <p
                  className="
                    mt-4
                    text-sm
                    text-slate-500
                  "
                >

                  Loading canonical opportunities...

                </p>

              </div>

            </div>

          )

          : opportunities.length ===
            0

            ? (

              <section
                className="
                  mt-6
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

                <div
                  className="
                    mx-auto
                    grid
                    size-14
                    place-items-center
                    rounded-2xl
                    bg-[#edf4ff]
                    text-[#2f6fed]
                  "
                >

                  <FileSearch
                    className="
                      size-6
                    "
                  />

                </div>


                <h2
                  className="
                    mt-5
                    text-lg
                    font-semibold
                    text-slate-950
                  "
                >

                  No canonical opportunities yet

                </h2>


                <p
                  className="
                    mx-auto
                    mt-2
                    max-w-xl
                    text-sm
                    leading-6
                    text-slate-500
                  "
                >

                  The canonical model is ready. Upcoming source-adapter
                  steps will ingest TED, BOAMP and DECP records into this
                  shared procurement layer.

                </p>

              </section>

            )

            : (

              <div
                className="
                  mt-6
                  space-y-4
                "
              >

                {
                  opportunities.map(
                    (
                      opportunity,
                    ) => (

                      <article

                        key={
                          opportunity.id
                        }

                        className="
                          rounded-3xl
                          border
                          border-slate-200
                          bg-white
                          p-6
                          shadow-[0_8px_30px_rgba(15,23,42,0.04)]
                          transition
                          hover:border-blue-200
                          hover:shadow-[0_12px_38px_rgba(15,23,42,0.07)]
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
                                  bg-emerald-50
                                  px-2.5
                                  py-1
                                  text-[11px]
                                  font-semibold
                                  text-emerald-700
                                "
                              >

                                {
                                  formatEnum(
                                    opportunity.status,
                                  )
                                }

                              </span>


                              <span
                                className="
                                  rounded-full
                                  bg-slate-100
                                  px-2.5
                                  py-1
                                  text-[11px]
                                  font-semibold
                                  text-slate-600
                                "
                              >

                                {
                                  formatEnum(
                                    opportunity.contractNature,
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
                                        border
                                        border-blue-100
                                        bg-blue-50
                                        px-2.5
                                        py-1
                                        text-[11px]
                                        font-semibold
                                        text-blue-700
                                      "

                                    >

                                      {
                                        source
                                      }

                                    </span>

                                  ),
                                )
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


                            <div
                              className="
                                mt-3
                                flex
                                flex-wrap
                                gap-x-5
                                gap-y-2
                                text-xs
                                text-slate-500
                              "
                            >

                              <span
                                className="
                                  inline-flex
                                  items-center
                                  gap-1.5
                                "
                              >

                                <Building2
                                  className="
                                    size-3.5
                                  "
                                />

                                {
                                  opportunity.buyerName
                                }

                              </span>


                              <span
                                className="
                                  inline-flex
                                  items-center
                                  gap-1.5
                                "
                              >

                                <MapPinned
                                  className="
                                    size-3.5
                                  "
                                />

                                {
                                  opportunity.primaryRegionName
                                  ??
                                  opportunity.countryCode
                                }

                              </span>


                              <span
                                className="
                                  inline-flex
                                  items-center
                                  gap-1.5
                                "
                              >

                                <CalendarDays
                                  className="
                                    size-3.5
                                  "
                                />

                                Published
                                {" "}
                                {
                                  formatDate(
                                    opportunity.publicationDate,
                                  )
                                }

                              </span>

                            </div>


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
                                        py-1.5
                                        text-[11px]
                                        text-slate-600
                                      "

                                    >

                                      <strong
                                        className="
                                          mr-1
                                          text-slate-800
                                        "
                                      >

                                        {
                                          cpv.code
                                        }

                                      </strong>

                                      {
                                        cpv.label
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
                              gap-3
                              sm:grid-cols-3
                              xl:w-[460px]
                            "
                          >

                            <SmallMetric

                              icon={
                                WalletCards
                              }

                              label="
                                Estimated value
                              "

                              value={
                                formatValueRange(

                                  opportunity.estimatedMinValue,

                                  opportunity.estimatedMaxValue,

                                  opportunity.currency,

                                )
                              }

                            />


                            <SmallMetric

                              icon={
                                CalendarDays
                              }

                              label="
                                Deadline
                              "

                              value={
                                formatDateTime(
                                  opportunity.submissionDeadline,
                                )
                              }

                            />


                            <SmallMetric

                              icon={
                                Layers3
                              }

                              label="
                                Lots
                              "

                              value={
                                opportunity.lotCount ===
                                0

                                  ? "Single contract"

                                  : `${opportunity.lotCount} lots`
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

                          <p
                            className="
                              text-xs
                              text-slate-400
                            "
                          >

                            {
                              formatEnum(
                                opportunity.procedureType,
                              )
                            }

                          </p>


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
                              hover:underline
                            "

                          >

                            Open opportunity

                            <ArrowRight
                              className="
                                size-4
                              "
                            />

                          </Link>

                        </div>

                      </article>

                    ),
                  )
                }

              </div>

            )
      }

    </div>

  );

}


function SmallMetric({

  icon:
    Icon,

  label,

  value,

}: {

  icon:
    typeof WalletCards;

  label:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        rounded-2xl
        bg-slate-50
        p-4
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
          mt-3
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
          text-xs
          font-semibold
          leading-5
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
