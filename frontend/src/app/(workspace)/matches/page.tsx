"use client";


import {

  ArrowRight,

  Ban,

  BriefcaseBusiness,

  CheckCircle2,

  Clock3,

  Gauge,

  LoaderCircle,

  MapPinned,

  RefreshCw,

  SearchCheck,

  ShieldCheck,

  Sparkles,

  Target,

} from "lucide-react";


import Link from "next/link";


import {

  useCallback,

  useEffect,

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

  TenderMatch,

  TenderMatchGrade,

  TenderMatchOverview,

  TenderMatchRecalculation,

} from "@/features/matching/matching-types";


function parseGrade(
  value:
    string,
):
  TenderMatchGrade
  |
  "" {

  switch (
    value
  ) {

    case "EXCELLENT":
      return "EXCELLENT";

    case "STRONG":
      return "STRONG";

    case "GOOD":
      return "GOOD";

    case "WEAK":
      return "WEAK";

    case "POOR":
      return "POOR";

    case "BLOCKED":
      return "BLOCKED";

    default:
      return "";

  }

}


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


export default function MatchesPage() {

  const {

    api,

    user,

  } = useAuth();


  const [

    matches,

    setMatches,

  ] = useState<TenderMatch[]>(
    [],
  );


  const [

    overview,

    setOverview,

  ] = useState<
    TenderMatchOverview
    |
    null
  >(
    null,
  );


  const [

    grade,

    setGrade,

  ] = useState<
    TenderMatchGrade
    |
    ""
  >(
    "",
  );


  const [

    minimumScore,

    setMinimumScore,

  ] = useState(
    "",
  );


  const [

    includeBlocked,

    setIncludeBlocked,

  ] = useState(
    false,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    recalculating,

    setRecalculating,

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

    recalculated,

    setRecalculated,

  ] = useState<
    TenderMatchRecalculation
    |
    null
  >(
    null,
  );


  const fetchMatchData =
    useCallback(
      async () => {

        const params =
          new URLSearchParams();


        if (
          minimumScore
        ) {

          params.set(
            "minScore",
            minimumScore,
          );

        }


        if (
          grade
        ) {

          params.set(
            "grade",
            grade,
          );

        }


        if (
          includeBlocked
        ) {

          params.set(
            "includeBlocked",
            "true",
          );

        }


        const suffix =
          params.toString()
            ? `?${params.toString()}`
            : "";


        return Promise.all([

          api<TenderMatch[]>(
            `/api/v1/matches${suffix}`,
          ),

          api<TenderMatchOverview>(
            "/api/v1/matches/overview",
          ),

        ]);

      },
      [
        api,
        grade,
        includeBlocked,
        minimumScore,
      ],
    );


  useEffect(
    () => {

      let cancelled =
        false;


      fetchMatchData()
        .then(
          (
            [
              matchResult,
              overviewResult,
            ],
          ) => {

            if (
              cancelled
            ) {

              return;

            }


            setMatches(
              matchResult,
            );


            setOverview(
              overviewResult,
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
                  : "Unable to load opportunity matches.",
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
      fetchMatchData,
    ],
  );


  async function recalculate() {

    setRecalculating(
      true,
    );

    setError(
      null,
    );

    setRecalculated(
      null,
    );


    try {

      const result =
        await api<TenderMatchRecalculation>(
          "/api/v1/matches/recalculate",
          {
            method:
              "POST",
          },
        );


      setRecalculated(
        result,
      );


      const [
        matchResult,
        overviewResult,
      ] =
        await fetchMatchData();


      setMatches(
        matchResult,
      );


      setOverview(
        overviewResult,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to recalculate opportunity matches.",
      );


    } finally {

      setRecalculating(
        false,
      );

    }

  }


  const canRecalculate =
    user?.role ===
      "OWNER"
    ||
    user?.role ===
      "BID_MANAGER"
    ||
    user?.role ===
      "ANALYST";


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
            Bid intelligence
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
            Opportunity matches
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
            Explainable company-to-tender scoring across
            CPV fit, geography, contract value, buyer
            preference, capabilities, response time and
            procurement-data quality.
          </p>

        </div>


        {
          canRecalculate

            ? (

              <button
                type="button"
                onClick={
                  () => {
                    void recalculate();
                  }
                }
                disabled={
                  recalculating
                }
                className="
                  inline-flex
                  h-11
                  items-center
                  justify-center
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
                  recalculating

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
                  recalculating
                    ? "Recalculating..."
                    : "Recalculate matches"
                }

              </button>

            )

            : null
        }

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
        recalculated

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
                  Matching recalculated
                </p>


                <p
                  className="mt-1"
                >
                  {
                    recalculated.evaluatedCount
                  }
                  {" "}
                  opportunities evaluated:
                  {" "}
                  {
                    recalculated.excellentCount
                  }
                  {" "}
                  excellent,
                  {" "}
                  {
                    recalculated.strongCount
                  }
                  {" "}
                  strong and
                  {" "}
                  {
                    recalculated.blockedCount
                  }
                  {" "}
                  blocked.
                </p>

              </div>

            </div>

          )

          : null
      }


      {
        overview

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

              <Metric
                icon={Target}
                label="Actionable"
                value={
                  overview.actionableMatches
                    .toLocaleString()
                }
                detail={
                  `${overview.totalMatches.toLocaleString()} evaluated`
                }
              />


              <Metric
                icon={Sparkles}
                label="Excellent"
                value={
                  overview.excellentMatches
                    .toLocaleString()
                }
                detail="85–100 match score"
              />


              <Metric
                icon={Gauge}
                label="Average score"
                value={
                  `${overview.averageScore.toFixed(1)}/100`
                }
                detail={
                  `${overview.strongMatches.toLocaleString()} strong matches`
                }
              />


              <Metric
                icon={Ban}
                label="Blocked"
                value={
                  overview.blockedMatches
                    .toLocaleString()
                }
                detail={
                  `Updated ${formatDateTime(overview.lastEvaluatedAt)}`
                }
              />

            </div>

          )

          : null
      }


      <section
        className="
          mt-6
          rounded-2xl
          border
          border-slate-200
          bg-white
          p-4
        "
      >

        <div
          className="
            grid
            gap-3
            md:grid-cols-3
          "
        >

          <select
            value={
              grade
            }
            onChange={
              (
                event,
              ) =>
                setGrade(
                  parseGrade(
                    event.target.value,
                  ),
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
              All match grades
            </option>
            <option value="EXCELLENT">
              Excellent
            </option>
            <option value="STRONG">
              Strong
            </option>
            <option value="GOOD">
              Good
            </option>
            <option value="WEAK">
              Weak
            </option>
            <option value="POOR">
              Poor
            </option>
          </select>


          <select
            value={
              minimumScore
            }
            onChange={
              (
                event,
              ) =>
                setMinimumScore(
                  event.target.value,
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
              Any score
            </option>
            <option value="85">
              85+ Excellent
            </option>
            <option value="70">
              70+ Strong
            </option>
            <option value="55">
              55+ Good
            </option>
          </select>


          <label
            className="
              flex
              h-11
              items-center
              gap-3
              rounded-xl
              border
              border-slate-200
              px-4
              text-sm
              text-slate-600
            "
          >

            <input
              type="checkbox"
              checked={
                includeBlocked
              }
              onChange={
                (
                  event,
                ) =>
                  setIncludeBlocked(
                    event.target.checked,
                  )
              }
            />

            Include blocked opportunities

          </label>

        </div>

      </section>


      {
        loading

          ? (

            <div
              className="
                grid
                min-h-[350px]
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
                mt-6
                space-y-5
              "
            >

              {
                matches.map(
                  (
                    match,
                  ) => (

                    <MatchCard
                      key={
                        match.id
                      }
                      match={
                        match
                      }
                    />

                  ),
                )
              }


              {
                matches.length ===
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

                      <SearchCheck
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
                        No calculated matches yet
                      </p>


                      <p
                        className="
                          mx-auto
                          mt-2
                          max-w-lg
                          text-sm
                          leading-6
                          text-slate-500
                        "
                      >
                        Complete your company profile and run the
                        matching engine to rank the currently open
                        procurement opportunities.
                      </p>

                    </div>

                  )

                  : null
              }

            </div>

          )
      }

    </div>

  );

}


function MatchCard({

  match,

}: {

  match:
    TenderMatch;

}) {

  const opportunity =
    match.opportunity;


  return (

    <article
      className="
        overflow-hidden
        rounded-3xl
        border
        border-slate-200
        bg-white
        shadow-[0_8px_30px_rgba(15,23,42,0.04)]
      "
    >

      <div
        className="
          grid
          gap-6
          p-6
          xl:grid-cols-[1fr_360px]
        "
      >

        <div>

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
                  match.grade,
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
              match.blocked

                ? (

                  <span
                    className="
                      rounded-full
                      bg-red-100
                      px-2.5
                      py-1
                      text-[10px]
                      font-semibold
                      text-red-700
                    "
                  >
                    Blocked
                  </span>

                )

                : null
            }

          </div>


          <h2
            className="
              mt-4
              text-xl
              font-semibold
              leading-8
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
              mt-5
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


          <div
            className="
              mt-6
              grid
              gap-3
              sm:grid-cols-2
              lg:grid-cols-4
            "
          >

            <Detail
              icon={BriefcaseBusiness}
              label="Value"
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


            <Detail
              icon={Clock3}
              label="Deadline"
              value={
                opportunity.submissionDeadline
                  ? formatDate(
                      opportunity.submissionDeadline,
                    )
                  : "Not disclosed"
              }
            />


            <Detail
              icon={MapPinned}
              label="Region"
              value={
                opportunity.primaryRegionName
                ??
                opportunity.countryCode
              }
            />


            <Detail
              icon={ShieldCheck}
              label="Data quality"
              value={
                opportunity.dataQualityScore ===
                null

                  ? "Not assessed"

                  : `${opportunity.dataQualityScore}/100`
              }
            />

          </div>

        </div>


        <div
          className="
            rounded-2xl
            bg-slate-50
            p-5
          "
        >

          <p
            className="
              text-[10px]
              font-semibold
              uppercase
              tracking-[0.13em]
              text-slate-400
            "
          >
            Match score
          </p>


          <div
            className="
              mt-2
              flex
              items-end
              gap-1
            "
          >

            <span
              className="
                text-5xl
                font-semibold
                tracking-[-0.05em]
                text-slate-950
              "
            >
              {
                match.score
              }
            </span>

            <span
              className="
                pb-1
                text-sm
                text-slate-400
              "
            >
              /100
            </span>

          </div>


          <div
            className="
              mt-5
              grid
              grid-cols-2
              gap-2
            "
          >

            <Score
              label="CPV"
              value={match.cpvScore}
              maximum={25}
            />

            <Score
              label="Geography"
              value={match.geographyScore}
              maximum={15}
            />

            <Score
              label="Value"
              value={match.valueScore}
              maximum={15}
            />

            <Score
              label="Buyer"
              value={match.buyerScore}
              maximum={10}
            />

            <Score
              label="Capabilities"
              value={match.capabilityScore}
              maximum={15}
            />

            <Score
              label="Timing"
              value={match.timingScore}
              maximum={10}
            />

            <Score
              label="Quality"
              value={match.qualityScore}
              maximum={10}
            />

          </div>

        </div>

      </div>


      {
        match.signals.length >
        0

          ? (

            <div
              className="
                border-t
                border-slate-100
                px-6
                py-5
              "
            >

              <p
                className="
                  text-[10px]
                  font-semibold
                  uppercase
                  tracking-[0.13em]
                  text-slate-400
                "
              >
                Why this score?
              </p>


              <div
                className="
                  mt-3
                  flex
                  flex-wrap
                  gap-2
                "
              >

                {
                  match.signals.map(
                    (
                      signal,
                      index,
                    ) => (

                      <span
                        key={
                          `${signal.code}-${index}`
                        }
                        className="
                          rounded-xl
                          border
                          border-slate-200
                          bg-white
                          px-3
                          py-2
                          text-xs
                          text-slate-600
                        "
                        title={
                          signal.message
                        }
                      >
                        {
                          formatEnum(
                            signal.dimension,
                          )
                        }
                        {": "}
                        {
                          signal.earnedScore
                        }
                        /
                        {
                          signal.maximumScore
                        }
                      </span>

                    ),
                  )
                }

              </div>

            </div>

          )

          : null
      }


      <div
        className="
          flex
          items-center
          justify-between
          border-t
          border-slate-100
          px-6
          py-4
        "
      >

        <span
          className="
            text-xs
            text-slate-400
          "
        >
          Evaluated
          {" "}
          {
            formatDateTime(
              match.evaluatedAt,
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
          Review opportunity

          <ArrowRight
            className="size-4"
          />
        </Link>

      </div>

    </article>

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
    typeof Gauge;

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
          text-2xl
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


function Detail({

  icon:
    Icon,

  label,

  value,

}: {

  icon:
    typeof Clock3;

  label:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        rounded-xl
        border
        border-slate-200
        p-3
      "
    >

      <Icon
        className="
          size-4
          text-slate-400
        "
      />


      <p
        className="
          mt-2
          text-[10px]
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


function Score({

  label,

  value,

  maximum,

}: {

  label:
    string;

  value:
    number;

  maximum:
    number;

}) {

  return (

    <div
      className="
        rounded-xl
        bg-white
        p-3
      "
    >

      <p
        className="
          text-[10px]
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
        /
        {
          maximum
        }
      </p>

    </div>

  );

}
