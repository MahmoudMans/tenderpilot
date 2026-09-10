"use client";


import {

  BarChart3,

  Building2,

  CalendarClock,

  CircleDollarSign,

  Compass,

  Factory,

  FileSearch,

  Gauge,

  LoaderCircle,

  MapPinned,

  Search,

  ShieldCheck,

  Target,

  Users,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import Link from "next/link";


import {

  useCallback,

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

  formatMoney,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  MarketExplorer,

} from "@/features/market-analytics/market-analytics-types";


export default function MarketExplorerPage() {

  const {

    api,

  } = useAuth();


  const [

    market,

    setMarket,

  ] = useState<
    MarketExplorer
    |
    null
  >(
    null,
  );


  const [

    query,

    setQuery,

  ] = useState(
    "",
  );


  const [

    country,

    setCountry,

  ] = useState(
    "",
  );


  const [

    region,

    setRegion,

  ] = useState(
    "",
  );


  const [

    cpv,

    setCpv,

  ] = useState(
    "",
  );


  const [

    status,

    setStatus,

  ] = useState(
    "",
  );


  const [

    dateFrom,

    setDateFrom,

  ] = useState(
    "",
  );


  const [

    dateTo,

    setDateTo,

  ] = useState(
    "",
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


  const load =
    useCallback(
      async () => {

        setLoading(
          true,
        );


        setError(
          null,
        );


        const params =
          new URLSearchParams();


        if (
          query.trim() !==
          ""
        ) {

          params.set(
            "q",
            query.trim(),
          );

        }


        if (
          country.trim() !==
          ""
        ) {

          params.set(
            "country",
            country.trim(),
          );

        }


        if (
          region.trim() !==
          ""
        ) {

          params.set(
            "region",
            region.trim(),
          );

        }


        if (
          cpv.trim() !==
          ""
        ) {

          params.set(
            "cpv",
            cpv.trim(),
          );

        }


        if (
          status.trim() !==
          ""
        ) {

          params.set(
            "status",
            status.trim(),
          );

        }


        if (
          dateFrom !==
          ""
        ) {

          params.set(
            "dateFrom",
            dateFrom,
          );

        }


        if (
          dateTo !==
          ""
        ) {

          params.set(
            "dateTo",
            dateTo,
          );

        }


        try {

          const result =
            await api<MarketExplorer>(
              `/api/v1/market/explorer?${params.toString()}`,
            );


          setMarket(
            result,
          );


        } catch (
          cause
        ) {

          setError(
            cause instanceof Error
              ? cause.message
              : "Unable to load procurement market analytics.",
          );


        } finally {

          setLoading(
            false,
          );

        }

      },
      [
        api,
        country,
        cpv,
        dateFrom,
        dateTo,
        query,
        region,
        status,
      ],
    );


  useEffect(
    () => {

      let cancelled =
        false;


      api<MarketExplorer>(
        "/api/v1/market/explorer",
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


            setMarket(
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
                  : "Unable to load procurement market analytics.",
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


  const maximumMonth =
    useMemo(
      () =>
        Math.max(
          1,
          ...(
            market?.monthlyTrend.map(
              (
                item,
              ) =>
                item.noticeCount,
            )
            ??
            []
          ),
        ),
      [
        market,
      ],
    );


  return (

    <div
      className="
        mx-auto
        max-w-[1500px]
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
          Shared procurement intelligence
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
          Procurement market explorer
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
          Explore market activity across the normalized public
          procurement corpus by geography, CPV, publication
          period and status. Compare buyer concentration,
          observed supplier concentration, award coverage and
          procurement mix.
        </p>

      </div>


      <section
        className="
          mt-7
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-5
        "
      >

        <div
          className="
            grid
            gap-3
            md:grid-cols-2
            xl:grid-cols-4
          "
        >

          <div
            className="
              relative
              xl:col-span-2
            "
          >

            <Search
              className="
                absolute
                left-3
                top-3.5
                size-4
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
              placeholder="Opportunity or buyer"
              className="
                h-11
                w-full
                rounded-xl
                border
                border-slate-200
                pl-10
                pr-3
                text-sm
              "
            />

          </div>


          <input
            value={
              country
            }
            onChange={
              (
                event,
              ) =>
                setCountry(
                  event.target.value
                    .toUpperCase(),
                )
            }
            maxLength={2}
            placeholder="Country e.g. FR"
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              px-3
              text-sm
              uppercase
            "
          />


          <input
            value={
              region
            }
            onChange={
              (
                event,
              ) =>
                setRegion(
                  event.target.value,
                )
            }
            placeholder="Region e.g. IDF"
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              px-3
              text-sm
            "
          />


          <input
            value={
              cpv
            }
            onChange={
              (
                event,
              ) =>
                setCpv(
                  event.target.value
                    .replace(
                      /\D/g,
                      "",
                    )
                    .slice(
                      0,
                      8,
                    ),
                )
            }
            placeholder="CPV prefix"
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              px-3
              text-sm
            "
          />


          <input
            value={
              status
            }
            onChange={
              (
                event,
              ) =>
                setStatus(
                  event.target.value
                    .toUpperCase(),
                )
            }
            placeholder="Status e.g. PUBLISHED"
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              px-3
              text-sm
              uppercase
            "
          />


          <input
            type="date"
            value={
              dateFrom
            }
            onChange={
              (
                event,
              ) =>
                setDateFrom(
                  event.target.value,
                )
            }
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              px-3
              text-sm
            "
          />


          <input
            type="date"
            value={
              dateTo
            }
            onChange={
              (
                event,
              ) =>
                setDateTo(
                  event.target.value,
                )
            }
            className="
              h-11
              rounded-xl
              border
              border-slate-200
              px-3
              text-sm
            "
          />

        </div>


        <div
          className="
            mt-4
            flex
            flex-wrap
            items-center
            gap-3
          "
        >

          <button
            type="button"
            onClick={
              () => {
                void load();
              }
            }
            className="
              h-10
              rounded-xl
              bg-[#14213d]
              px-5
              text-sm
              font-semibold
              text-white
            "
          >
            Analyze market
          </button>


          <button
            type="button"
            onClick={
              () => {

                setQuery(
                  "",
                );

                setCountry(
                  "",
                );

                setRegion(
                  "",
                );

                setCpv(
                  "",
                );

                setStatus(
                  "",
                );

                setDateFrom(
                  "",
                );

                setDateTo(
                  "",
                );

              }
            }
            className="
              h-10
              rounded-xl
              border
              border-slate-200
              px-4
              text-sm
              font-semibold
              text-slate-600
            "
          >
            Clear filters
          </button>

        </div>

      </section>


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
                min-h-[45vh]
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

          : market

            ? (

              <>

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
                    icon={FileSearch}
                    label="Notices"
                    value={
                      market.summary.noticeCount
                        .toLocaleString()
                    }
                    detail={
                      `${market.summary.activeNoticeCount} active`
                    }
                  />


                  <Metric
                    icon={Building2}
                    label="Public buyers"
                    value={
                      market.summary.uniqueBuyerCount
                        .toLocaleString()
                    }
                    detail={
                      `${market.summary.topBuyerSharePercentage}% top-buyer share`
                    }
                  />


                  <Metric
                    icon={Factory}
                    label="Observed suppliers"
                    value={
                      market.summary.uniqueObservedSupplierCount
                        .toLocaleString()
                    }
                    detail={
                      `${market.summary.topObservedSupplierSharePercentage}% top observed share`
                    }
                  />


                  <Metric
                    icon={CircleDollarSign}
                    label="Awarded notices"
                    value={
                      market.summary.awardedNoticeCount
                        .toLocaleString()
                    }
                    detail={
                      `${market.summary.awardCoveragePercentage}% of filtered notices`
                    }
                  />

                </div>


                <div
                  className="
                    mt-4
                    grid
                    gap-4
                    md:grid-cols-2
                    xl:grid-cols-4
                  "
                >

                  <Metric
                    icon={Gauge}
                    label="Buyer concentration"
                    value={
                      formatEnum(
                        market.summary.buyerConcentration,
                      )
                    }
                    detail={
                      `HHI ${market.summary.buyerHhi.toLocaleString()}`
                    }
                  />


                  <Metric
                    icon={Users}
                    label="Supplier concentration"
                    value={
                      formatEnum(
                        market.summary.supplierConcentration,
                      )
                    }
                    detail={
                      `HHI ${market.summary.supplierHhi.toLocaleString()}`
                    }
                  />


                  <Metric
                    icon={CalendarClock}
                    label="Median response"
                    value={
                      market.summary.medianResponseDays ===
                      null
                        ? "—"
                        : `${market.summary.medianResponseDays} days`
                    }
                    detail="Publication to deadline"
                  />


                  <Metric
                    icon={Target}
                    label="CPV coverage"
                    value={
                      `${market.summary.cpvCoveragePercentage}%`
                    }
                    detail={
                      `${market.summary.geographyCoveragePercentage}% geography coverage`
                    }
                  />

                </div>


                <div
                  className="
                    mt-6
                    grid
                    gap-6
                    xl:grid-cols-2
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

                    <CircleDollarSign
                      className="
                        size-5
                        text-[#2f6fed]
                      "
                    />


                    <h2
                      className="
                        mt-4
                        font-semibold
                        text-slate-950
                      "
                    >
                      Market value
                    </h2>


                    <div
                      className="
                        mt-5
                        grid
                        gap-4
                        sm:grid-cols-2
                      "
                    >

                      <ValueMetric
                        label="Average estimated opportunity"
                        value={
                          market.summary.averageEstimatedValue !==
                            null
                          &&
                          market.summary.estimatedCurrency
                          &&
                          market.summary.estimatedCurrency !==
                            "MIXED"
                            ? formatMoney(
                                market.summary.averageEstimatedValue,
                                market.summary.estimatedCurrency,
                              )
                            : "Mixed / unavailable"
                        }
                      />


                      <ValueMetric
                        label="Recorded awarded value"
                        value={
                          market.summary.totalAwardedValue !==
                            null
                          &&
                          market.summary.awardedCurrency
                          &&
                          market.summary.awardedCurrency !==
                            "MIXED"
                            ? formatMoney(
                                market.summary.totalAwardedValue,
                                market.summary.awardedCurrency,
                              )
                            : "Mixed / unavailable"
                        }
                      />

                    </div>


                    <p
                      className="
                        mt-5
                        rounded-2xl
                        bg-slate-50
                        p-4
                        text-xs
                        leading-5
                        text-slate-500
                      "
                    >
                      {
                        market.valueInterpretation
                      }
                    </p>

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

                    <BarChart3
                      className="
                        size-5
                        text-[#2f6fed]
                      "
                    />


                    <h2
                      className="
                        mt-4
                        font-semibold
                        text-slate-950
                      "
                    >
                      Data coverage
                    </h2>


                    <div
                      className="
                        mt-5
                        space-y-4
                      "
                    >

                      <Coverage
                        label="CPV"
                        value={
                          market.summary.cpvCoveragePercentage
                        }
                      />


                      <Coverage
                        label="Geography"
                        value={
                          market.summary.geographyCoveragePercentage
                        }
                      />


                      <Coverage
                        label="Awards"
                        value={
                          market.summary.awardCoveragePercentage
                        }
                      />


                      <Coverage
                        label="Suppliers on awards"
                        value={
                          market.summary.supplierCoveragePercentage
                        }
                      />

                    </div>

                  </section>

                </div>


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

                  <CalendarClock
                    className="
                      size-5
                      text-[#2f6fed]
                    "
                  />


                  <h2
                    className="
                      mt-4
                      font-semibold
                      text-slate-950
                    "
                  >
                    Market activity trend
                  </h2>


                  <p
                    className="
                      mt-2
                      text-xs
                      text-slate-500
                    "
                  >
                    Notice activity by publication month
                    inside the selected market.
                  </p>


                  <div
                    className="
                      mt-6
                      space-y-3
                    "
                  >

                    {
                      market.monthlyTrend.map(
                        (
                          point,
                        ) => (

                          <div
                            key={
                              point.month
                            }
                            className="
                              grid
                              grid-cols-[78px_1fr_55px]
                              items-center
                              gap-3
                            "
                          >

                            <span
                              className="
                                text-xs
                                font-semibold
                                text-slate-500
                              "
                            >
                              {
                                point.month
                              }
                            </span>


                            <div
                              className="
                                h-3
                                overflow-hidden
                                rounded-full
                                bg-slate-100
                              "
                            >

                              <div
                                className="
                                  h-full
                                  rounded-full
                                  bg-slate-500
                                "
                                style={{
                                  width:
                                    `${(
                                      point.noticeCount
                                      /
                                      maximumMonth
                                    ) * 100}%`,
                                }}
                              />

                            </div>


                            <span
                              className="
                                text-right
                                text-xs
                                text-slate-400
                              "
                            >
                              {
                                point.noticeCount
                              }
                            </span>

                          </div>

                        ),
                      )
                    }


                    {
                      market.monthlyTrend.length ===
                      0

                        ? (

                          <EmptyText>
                            No publication-date history for
                            the current market.
                          </EmptyText>

                        )

                        : null
                    }

                  </div>

                </section>


                <div
                  className="
                    mt-6
                    grid
                    gap-6
                    xl:grid-cols-3
                  "
                >

                  <RankingCard
                    icon={Target}
                    title="Top CPVs"
                  >

                    {
                      market.topCpvs.map(
                        (
                          item,
                        ) => (

                          <RankingRow
                            key={
                              item.code
                            }
                            title={
                              item.code
                            }
                            detail={
                              item.description
                            }
                            count={
                              item.occurrenceCount
                            }
                            share={
                              item.sharePercentage
                            }
                          />

                        ),
                      )
                    }

                  </RankingCard>


                  <RankingCard
                    icon={Building2}
                    title="Top buyers"
                  >

                    {
                      market.topBuyers.map(
                        (
                          buyer,
                        ) => (

                          buyer.buyerId

                            ? (

                              <Link
                                key={
                                  buyer.buyerId
                                }
                                href={
                                  `/buyers/${buyer.buyerId}`
                                }
                                className="
                                  block
                                  rounded-xl
                                  p-3
                                  hover:bg-slate-50
                                "
                              >

                                <RankingRowContent
                                  title={
                                    buyer.name
                                    ??
                                    "Unknown buyer"
                                  }
                                  detail={
                                    buyer.countryCode
                                  }
                                  count={
                                    buyer.noticeCount
                                  }
                                  share={
                                    buyer.sharePercentage
                                  }
                                />

                              </Link>

                            )

                            : (

                              <div
                                key={
                                  buyer.name
                                  ??
                                  "unknown-buyer"
                                }
                                className="p-3"
                              >

                                <RankingRowContent
                                  title={
                                    buyer.name
                                    ??
                                    "Unknown buyer"
                                  }
                                  detail={
                                    buyer.countryCode
                                  }
                                  count={
                                    buyer.noticeCount
                                  }
                                  share={
                                    buyer.sharePercentage
                                  }
                                />

                              </div>

                            )

                        ),
                      )
                    }

                  </RankingCard>


                  <RankingCard
                    icon={Factory}
                    title="Observed award suppliers"
                  >

                    {
                      market.topSuppliers.map(
                        (
                          supplier,
                        ) => (

                          <Link
                            key={
                              supplier.supplierKey
                            }
                            href={
                              `/suppliers/${supplier.supplierKey}`
                            }
                            className="
                              block
                              rounded-xl
                              p-3
                              hover:bg-slate-50
                            "
                          >

                            <RankingRowContent
                              title={
                                supplier.name
                              }
                              detail="Public award observations"
                              count={
                                supplier.observedAwardCount
                              }
                              share={
                                supplier.sharePercentage
                              }
                            />

                          </Link>

                        ),
                      )
                    }


                    <p
                      className="
                        mt-4
                        rounded-xl
                        bg-slate-50
                        p-3
                        text-[10px]
                        leading-4
                        text-slate-400
                      "
                    >
                      {
                        market.supplierInterpretation
                      }
                    </p>

                  </RankingCard>

                </div>


                <div
                  className="
                    mt-6
                    grid
                    gap-6
                    xl:grid-cols-3
                  "
                >

                  <RankingCard
                    icon={MapPinned}
                    title="Geographies"
                  >

                    {
                      market.geographies.map(
                        (
                          item,
                        ) => (

                          <RankingRow
                            key={
                              `${item.countryCode ?? ""}-${item.regionCode ?? ""}-${item.regionName ?? ""}`
                            }
                            title={
                              [
                                item.countryCode,
                                item.regionCode,
                              ]
                                .filter(
                                  Boolean,
                                )
                                .join(
                                  " · ",
                                )
                              ||
                              "Unknown geography"
                            }
                            detail={
                              item.regionName
                            }
                            count={
                              item.noticeCount
                            }
                            share={
                              item.sharePercentage
                            }
                          />

                        ),
                      )
                    }

                  </RankingCard>


                  <RankingCard
                    icon={Compass}
                    title="Procedures"
                  >

                    {
                      market.procedures.map(
                        (
                          item,
                        ) => (

                          <RankingRow
                            key={
                              item.value
                            }
                            title={
                              formatEnum(
                                item.value,
                              )
                            }
                            detail={null}
                            count={
                              item.noticeCount
                            }
                            share={
                              item.sharePercentage
                            }
                          />

                        ),
                      )
                    }

                  </RankingCard>


                  <RankingCard
                    icon={FileSearch}
                    title="Contract nature"
                  >

                    {
                      market.contractNatures.map(
                        (
                          item,
                        ) => (

                          <RankingRow
                            key={
                              item.value
                            }
                            title={
                              formatEnum(
                                item.value,
                              )
                            }
                            detail={null}
                            count={
                              item.noticeCount
                            }
                            share={
                              item.sharePercentage
                            }
                          />

                        ),
                      )
                    }

                  </RankingCard>

                </div>


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

                  <h2
                    className="
                      font-semibold
                      text-slate-950
                    "
                  >
                    Recent opportunities in this market
                  </h2>


                  <div
                    className="
                      mt-5
                      space-y-3
                    "
                  >

                    {
                      market.recentOpportunities.map(
                        (
                          opportunity,
                        ) => (

                          <Link
                            key={
                              opportunity.tenderId
                            }
                            href={
                              `/opportunities/${opportunity.tenderId}`
                            }
                            className="
                              block
                              rounded-2xl
                              border
                              border-slate-200
                              p-4
                              transition
                              hover:bg-slate-50
                            "
                          >

                            <div
                              className="
                                flex
                                flex-col
                                gap-4
                                lg:flex-row
                                lg:items-start
                                lg:justify-between
                              "
                            >

                              <div
                                className="
                                  min-w-0
                                "
                              >

                                <div
                                  className="
                                    flex
                                    flex-wrap
                                    gap-2
                                  "
                                >

                                  {
                                    opportunity.status

                                      ? (

                                        <Badge
                                          value={
                                            formatEnum(
                                              opportunity.status,
                                            )
                                          }
                                        />

                                      )

                                      : null
                                  }


                                  {
                                    opportunity.procedure

                                      ? (

                                        <Badge
                                          value={
                                            formatEnum(
                                              opportunity.procedure,
                                            )
                                          }
                                        />

                                      )

                                      : null
                                  }


                                  {
                                    opportunity.awarded

                                      ? (

                                        <span
                                          className="
                                            rounded-full
                                            bg-emerald-50
                                            px-2.5
                                            py-1
                                            text-[10px]
                                            font-semibold
                                            text-emerald-700
                                          "
                                        >
                                          Award record
                                        </span>

                                      )

                                      : null
                                  }

                                </div>


                                <p
                                  className="
                                    mt-3
                                    text-sm
                                    font-semibold
                                    text-slate-900
                                  "
                                >
                                  {
                                    opportunity.title
                                  }
                                </p>


                                <p
                                  className="
                                    mt-2
                                    text-xs
                                    text-slate-400
                                  "
                                >
                                  {
                                    opportunity.buyerName
                                    ??
                                    "Unknown buyer"
                                  }
                                  {
                                    opportunity.publicationDate
                                      ? ` · ${formatDate(
                                          opportunity.publicationDate,
                                        )}`
                                      : ""
                                  }
                                </p>

                              </div>


                              <div
                                className="
                                  shrink-0
                                  text-sm
                                  font-semibold
                                  text-slate-700
                                "
                              >
                                {
                                  formatValueRange(
                                    opportunity.estimatedMinValue,
                                    opportunity.estimatedMaxValue,
                                    opportunity.currency,
                                  )
                                }
                              </div>

                            </div>

                          </Link>

                        ),
                      )
                    }


                    {
                      market.recentOpportunities.length ===
                      0

                        ? (

                          <EmptyText>
                            No opportunities match the current
                            market filters.
                          </EmptyText>

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

                  Market Explorer uses only the shared canonical
                  public-procurement corpus. Company profiles,
                  bid decisions, documents, local AI analyses,
                  private RAG, compliance reviews and bid-planning
                  data are excluded from market analytics.

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

  detail,

}: {

  icon:
    LucideIcon;

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
          text-xl
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


function ValueMetric({

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
        {
          label
        }
      </p>


      <p
        className="
          mt-2
          text-lg
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


function Coverage({

  label,

  value,

}: {

  label:
    string;

  value:
    number;

}) {

  return (

    <div>

      <div
        className="
          flex
          items-center
          justify-between
          text-xs
        "
      >

        <span
          className="
            font-semibold
            text-slate-600
          "
        >
          {
            label
          }
        </span>


        <span
          className="
            text-slate-400
          "
        >
          {
            value
          }
          %
        </span>

      </div>


      <div
        className="
          mt-2
          h-2
          overflow-hidden
          rounded-full
          bg-slate-100
        "
      >

        <div
          className="
            h-full
            rounded-full
            bg-slate-500
          "
          style={{
            width:
              `${Math.min(
                100,
                Math.max(
                  0,
                  value,
                ),
              )}%`,
          }}
        />

      </div>

    </div>

  );

}


function RankingCard({

  icon:
    Icon,

  title,

  children,

}: {

  icon:
    LucideIcon;

  title:
    string;

  children:
    React.ReactNode;

}) {

  return (

    <section
      className="
        rounded-3xl
        border
        border-slate-200
        bg-white
        p-6
      "
    >

      <Icon
        className="
          size-5
          text-[#2f6fed]
        "
      />


      <h2
        className="
          mt-4
          font-semibold
          text-slate-950
        "
      >
        {
          title
        }
      </h2>


      <div
        className="
          mt-4
          divide-y
          divide-slate-100
        "
      >
        {
          children
        }
      </div>

    </section>

  );

}


function RankingRow({

  title,

  detail,

  count,

  share,

}: {

  title:
    string;

  detail:
    string
    |
    null;

  count:
    number;

  share:
    number;

}) {

  return (

    <div
      className="p-3"
    >

      <RankingRowContent
        title={
          title
        }
        detail={
          detail
        }
        count={
          count
        }
        share={
          share
        }
      />

    </div>

  );

}


function RankingRowContent({

  title,

  detail,

  count,

  share,

}: {

  title:
    string;

  detail:
    string
    |
    null;

  count:
    number;

  share:
    number;

}) {

  return (

    <div
      className="
        flex
        items-start
        justify-between
        gap-4
      "
    >

      <div
        className="
          min-w-0
        "
      >

        <p
          className="
            truncate
            text-sm
            font-semibold
            text-slate-900
          "
        >
          {
            title
          }
        </p>


        {
          detail

            ? (

              <p
                className="
                  mt-1
                  line-clamp-2
                  text-[10px]
                  leading-4
                  text-slate-400
                "
              >
                {
                  detail
                }
              </p>

            )

            : null
        }


        <p
          className="
            mt-1
            text-[10px]
            text-slate-400
          "
        >
          {
            count
          }
          {" "}
          observations
        </p>

      </div>


      <span
        className="
          shrink-0
          text-xs
          font-semibold
          text-[#2f6fed]
        "
      >
        {
          share
        }
        %
      </span>

    </div>

  );

}


function Badge({

  value,

}: {

  value:
    string;

}) {

  return (

    <span
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
        value
      }
    </span>

  );

}


function EmptyText({

  children,

}: {

  children:
    React.ReactNode;

}) {

  return (

    <p
      className="
        rounded-2xl
        border
        border-dashed
        border-slate-300
        p-8
        text-center
        text-sm
        text-slate-400
      "
    >
      {
        children
      }
    </p>

  );

}
