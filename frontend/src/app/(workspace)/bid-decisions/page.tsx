"use client";


import {

  ArrowRight,

  CheckCircle2,

  CirclePause,

  Gauge,

  LoaderCircle,

  Scale,

  ShieldAlert,

  Target,

} from "lucide-react";


import Link from "next/link";


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

  formatMoney,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  BidDecisionOverview,

  BidDecisionQueueItem,

} from "@/features/bid-decision/bid-decision-types";


export default function BidDecisionsPage() {

  const {

    api,

  } = useAuth();


  const [

    queue,

    setQueue,

  ] = useState<BidDecisionQueueItem[]>(
    [],
  );


  const [

    overview,

    setOverview,

  ] = useState<BidDecisionOverview | null>(
    null,
  );


  const [

    minimumMatchScore,

    setMinimumMatchScore,

  ] = useState(
    "55",
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

    error,

    setError,

  ] = useState<string | null>(
    null,
  );


  useEffect(
    () => {

      let cancelled =
        false;


      const params =
        new URLSearchParams();


      if (
        minimumMatchScore
      ) {

        params.set(
          "minMatchScore",
          minimumMatchScore,
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


      Promise.all([

        api<BidDecisionQueueItem[]>(
          `/api/v1/bid-decisions/queue?${params.toString()}`,
        ),

        api<BidDecisionOverview>(
          "/api/v1/bid-decisions/overview",
        ),

      ])
        .then(
          (
            [
              queueResult,
              overviewResult,
            ],
          ) => {

            if (
              cancelled
            ) {
              return;
            }


            setQueue(
              queueResult,
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
                  : "Unable to load bid-decision queue.",
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
      includeBlocked,
      minimumMatchScore,
    ],
  );


  return (

    <div
      className="
        mx-auto
        max-w-[1450px]
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
          Bid management
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
          Go / No-Go decisions
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
          Turn explainable opportunity matches into
          governed bid decisions using a structured
          commercial and delivery scorecard.
        </p>

      </div>


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
                label="Candidates"
                value={
                  overview.candidateCount
                    .toLocaleString()
                }
                detail={
                  `${overview.totalDecisions.toLocaleString()} reviewed`
                }
              />


              <Metric
                icon={CheckCircle2}
                label="GO"
                value={
                  overview.goCount
                    .toLocaleString()
                }
                detail="Approved pursuits"
              />


              <Metric
                icon={CirclePause}
                label="HOLD"
                value={
                  overview.holdCount
                    .toLocaleString()
                }
                detail={
                  `${overview.pendingCount.toLocaleString()} pending`
                }
              />


              <Metric
                icon={Gauge}
                label="Average decision"
                value={
                  `${overview.averageDecisionScore.toFixed(1)}/100`
                }
                detail={
                  `${overview.noGoCount.toLocaleString()} no-go decisions`
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


      <section
        className="
          mt-6
          grid
          gap-3
          rounded-2xl
          border
          border-slate-200
          bg-white
          p-4
          md:grid-cols-2
        "
      >

        <select
          value={
            minimumMatchScore
          }
          onChange={
            (
              event,
            ) =>
              setMinimumMatchScore(
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
            Any match score
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

          <option value="40">
            40+ Weak
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

          Include blocked matches

        </label>

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
                space-y-4
              "
            >

              {
                queue.map(
                  (
                    item,
                  ) => (

                    <article
                      key={
                        item.matchId
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
                          flex-col
                          gap-6
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
                              gap-2
                            "
                          >

                            <Badge
                              value={
                                formatEnum(
                                  item.matchGrade,
                                )
                              }
                            />


                            {
                              item.decision

                                ? (

                                  <Badge
                                    value={
                                      `Decision ${formatEnum(item.decision.outcome)}`
                                    }
                                  />

                                )

                                : (

                                  <Badge
                                    value="Not reviewed"
                                  />

                                )
                            }


                            {
                              item.blocked

                                ? (

                                  <Badge
                                    value="Match blocked"
                                  />

                                )

                                : null
                            }

                          </div>


                          <h2
                            className="
                              mt-4
                              text-xl
                              font-semibold
                              text-slate-950
                            "
                          >
                            {
                              item.opportunity.title
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
                              item.opportunity.buyerName
                            }
                            {" · "}
                            {
                              item.opportunity.primaryRegionName
                              ??
                              item.opportunity.countryCode
                            }
                          </p>


                          <div
                            className="
                              mt-5
                              grid
                              gap-3
                              sm:grid-cols-3
                            "
                          >

                            <SmallMetric
                              label="Match"
                              value={
                                `${item.matchScore}/100`
                              }
                            />


                            <SmallMetric
                              label="Value"
                              value={
                                item.opportunity.awardedAmount !==
                                null

                                  ? formatMoney(
                                      item.opportunity.awardedAmount,
                                      item.opportunity.awardedCurrency,
                                    )

                                  : formatValueRange(
                                      item.opportunity.estimatedMinValue,
                                      item.opportunity.estimatedMaxValue,
                                      item.opportunity.currency,
                                    )
                              }
                            />


                            <SmallMetric
                              label="Deadline"
                              value={
                                item.opportunity.submissionDeadline
                                  ? formatDate(
                                      item.opportunity.submissionDeadline,
                                    )
                                  : "Not disclosed"
                              }
                            />

                          </div>

                        </div>


                        <div
                          className="
                            w-full
                            rounded-2xl
                            bg-slate-50
                            p-5
                            xl:w-[310px]
                          "
                        >

                          <Scale
                            className="
                              size-5
                              text-[#2f6fed]
                            "
                          />


                          <p
                            className="
                              mt-4
                              text-xs
                              font-semibold
                              uppercase
                              tracking-[0.12em]
                              text-slate-400
                            "
                          >
                            Decision intelligence
                          </p>


                          {
                            item.decision

                              ? (

                                <>

                                  <p
                                    className="
                                      mt-2
                                      text-3xl
                                      font-semibold
                                      text-slate-950
                                    "
                                  >
                                    {
                                      item.decision.decisionScore
                                    }
                                    <span
                                      className="
                                        text-sm
                                        text-slate-400
                                      "
                                    >
                                      /100
                                    </span>
                                  </p>


                                  <p
                                    className="
                                      mt-2
                                      text-sm
                                      text-slate-500
                                    "
                                  >
                                    Recommendation:
                                    {" "}
                                    {
                                      formatEnum(
                                        item.decision.recommendedOutcome,
                                      )
                                    }
                                  </p>

                                </>

                              )

                              : (

                                <p
                                  className="
                                    mt-3
                                    text-sm
                                    leading-6
                                    text-slate-500
                                  "
                                >
                                  No commercial assessment has
                                  been recorded yet.
                                </p>

                              )
                          }


                          <Link
                            href={
                              `/bid-decisions/${item.tenderId}`
                            }
                            className="
                              mt-5
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
                            "
                          >

                            Review decision

                            <ArrowRight
                              className="size-4"
                            />

                          </Link>


                          <Link
                            href={
                              `/bid-workspaces/${item.tenderId}`
                            }
                            className="
                              mt-2
                              inline-flex
                              h-10
                              w-full
                              items-center
                              justify-center
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

                            Preparation workspace

                          </Link>




                        </div>

                      </div>

                    </article>

                  ),
                )
              }


              {
                queue.length ===
                0

                  ? (

                    <div
                      className="
                        rounded-3xl
                        border
                        border-dashed
                        border-slate-300
                        bg-white
                        py-16
                        text-center
                      "
                    >

                      <ShieldAlert
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
                        No opportunities in the decision queue
                      </p>


                      <p
                        className="
                          mt-2
                          text-sm
                          text-slate-500
                        "
                      >
                        Recalculate company matches or lower the
                        minimum match-score filter.
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


function Metric({

  icon:
    Icon,

  label,

  value,

  detail,

}: {

  icon:
    typeof Target;

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

    </div>

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
        p-4
      "
    >

      <p
        className="
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
        text-slate-700
      "
    >
      {
        value
      }
    </span>

  );

}
