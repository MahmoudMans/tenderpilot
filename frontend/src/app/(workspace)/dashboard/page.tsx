"use client";


import {

  AlertOctagon,

  ArrowRight,

  BarChart3,

  CalendarClock,

  CheckCircle2,

  ClipboardCheck,

  Compass,

  FileCheck2,

  Gauge,

  LoaderCircle,

  Scale,

  ShieldCheck,

  Sparkles,

  Target,

  TriangleAlert,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import Link from "next/link";


import {

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

} from "@/features/procurement/procurement-format";


import type {

  DashboardActionPriority,

  DashboardCommandCenter,

} from "@/features/dashboard/dashboard-types";


export default function DashboardPage() {

  const {

    api,

  } = useAuth();


  const [

    dashboard,

    setDashboard,

  ] = useState<
    DashboardCommandCenter
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


      api<DashboardCommandCenter>(
        "/api/v1/dashboard/command-center",
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


            setDashboard(
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
                  : "Unable to load the TenderPilot command center.",
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


  const visiblePortfolio =
    useMemo(
      () =>
        dashboard?.portfolio.slice(
          0,
          10,
        )
        ??
        [],
      [
        dashboard,
      ],
    );


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
    dashboard ===
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
          Command center
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
            "Dashboard is unavailable."
          }
        </div>

      </div>

    );

  }


  const summary =
    dashboard.summary;


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
            Executive command center
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
            Bid portfolio dashboard
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
            See where your organization should focus now:
            high-potential opportunities, bid decisions,
            compliance health, execution risk, submission
            readiness and approaching deadlines.
          </p>

        </div>


        <div
          className="
            flex
            flex-wrap
            gap-2
          "
        >

          <QuickLink
            href="/opportunities"
            label="Opportunities"
          />


          <QuickLink
            href="/matches"
            label="Matches"
          />


          <QuickLink
            href="/bid-decisions"
            label="Bid decisions"
          />


          <QuickLink
            href="/market"
            label="Market explorer"
          />


          <QuickLink
            href="/notifications"
            label="Notifications"
          />


          <QuickLink
            href="/data-operations"
            label="Data operations"
          />


          <QuickLink
            href="/workspace"
            label="Product map"
          />

        </div>

      </div>


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
                border-amber-200
                bg-amber-50
                p-4
                text-sm
                text-amber-800
              "
            >

              <TriangleAlert
                className="
                  size-5
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
          label="High-potential matches"
          value={
            summary.highPotentialMatches
              .toLocaleString()
          }
          detail={
            `${summary.matchedOpportunities} total matched opportunities`
          }
        />


        <Metric
          icon={CheckCircle2}
          label="GO portfolio"
          value={
            summary.goBids
              .toLocaleString()
          }
          detail={
            `${summary.holdBids} HOLD · ${summary.noGoBids} NO-GO`
          }
        />


        <Metric
          icon={FileCheck2}
          label="Submission ready"
          value={
            summary.submissionReadyBids
              .toLocaleString()
          }
          detail={
            `${summary.submittedBids} already submitted`
          }
        />


        <Metric
          icon={AlertOctagon}
          label="Overdue work"
          value={
            summary.overdueTasks
              .toLocaleString()
          }
          detail={
            `${summary.criticalOpenTasks} critical · ${summary.blockedTasks} blocked`
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
          icon={Scale}
          label="Pending decisions"
          value={
            summary.pendingDecisionBids
              .toLocaleString()
          }
          detail="Matched opportunities awaiting a decision"
        />


        <Metric
          icon={ShieldCheck}
          label="Compliance ready"
          value={
            summary.complianceReadyBids
              .toLocaleString()
          }
          detail="Bid workspaces clearing compliance readiness"
        />


        <Metric
          icon={ClipboardCheck}
          label="Open bid tasks"
          value={
            summary.openTasks
              .toLocaleString()
          }
          detail="Execution work remaining across the portfolio"
        />


        <Metric
          icon={CalendarClock}
          label="Deadlines ≤ 7 days"
          value={
            summary.deadlinesNext7Days
              .toLocaleString()
          }
          detail={
            `${summary.deadlinesNext30Days} deadlines within 30 days`
          }
        />

      </div>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-[1.1fr_0.9fr]
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
                What needs attention
              </p>


              <h2
                className="
                  mt-2
                  text-xl
                  font-semibold
                  text-slate-950
                "
              >
                Operational priorities
              </h2>

            </div>


            <AlertOctagon
              className="
                size-5
                text-slate-400
              "
            />

          </div>


          <div
            className="
              mt-6
              space-y-3
            "
          >

            {
              dashboard.priorities.map(
                (
                  action,
                ) => (

                  <Link
                    key={
                      `${action.tenderId ?? "global"}-${action.type}`
                    }
                    href={
                      action.href
                    }
                    className="
                      group
                      block
                      rounded-2xl
                      border
                      border-slate-200
                      p-4
                      transition
                      hover:border-slate-300
                      hover:bg-slate-50
                    "
                  >

                    <div
                      className="
                        flex
                        items-start
                        gap-4
                      "
                    >

                      <PriorityIcon
                        priority={
                          action.priority
                        }
                      />


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

                          <PriorityBadge
                            priority={
                              action.priority
                            }
                          />


                          <span
                            className="
                              text-[10px]
                              font-semibold
                              uppercase
                              tracking-[0.08em]
                              text-slate-400
                            "
                          >
                            {
                              formatEnum(
                                action.type,
                              )
                            }
                          </span>

                        </div>


                        <p
                          className="
                            mt-2
                            text-sm
                            font-semibold
                            text-slate-900
                          "
                        >
                          {
                            action.title
                          }
                        </p>


                        <p
                          className="
                            mt-1
                            text-xs
                            leading-5
                            text-slate-500
                          "
                        >
                          {
                            action.detail
                          }
                        </p>

                      </div>


                      <ArrowRight
                        className="
                          mt-1
                          size-4
                          shrink-0
                          text-slate-300
                          transition
                          group-hover:translate-x-0.5
                        "
                      />

                    </div>

                  </Link>

                ),
              )
            }


            {
              dashboard.priorities.length ===
              0

                ? (

                  <div
                    className="
                      rounded-2xl
                      border
                      border-dashed
                      border-slate-300
                      p-10
                      text-center
                    "
                  >

                    <CheckCircle2
                      className="
                        mx-auto
                        size-7
                        text-emerald-500
                      "
                    />


                    <p
                      className="
                        mt-3
                        text-sm
                        font-semibold
                        text-slate-900
                      "
                    >
                      No immediate portfolio actions
                    </p>


                    <p
                      className="
                        mt-1
                        text-xs
                        text-slate-400
                      "
                    >
                      TenderPilot did not detect an urgent
                      action in the current matched portfolio.
                    </p>

                  </div>

                )

                : null
            }

          </div>

        </section>


        <section
          className="
            rounded-3xl
            border
            border-slate-200
            bg-[#14213d]
            p-6
            text-white
          "
        >

          <Compass
            className="
              size-5
              text-blue-300
            "
          />


          <p
            className="
              mt-5
              text-xs
              font-semibold
              uppercase
              tracking-[0.12em]
              text-blue-200
            "
          >
            Public market pulse
          </p>


          <h2
            className="
              mt-2
              text-xl
              font-semibold
            "
          >
            Last 30 days
          </h2>


          {
            dashboard.market.available

              ? (

                <>

                  <div
                    className="
                      mt-6
                      grid
                      grid-cols-2
                      gap-3
                    "
                  >

                    <DarkMetric
                      label="Notices"
                      value={
                        dashboard.market.noticeCount30Days
                          .toLocaleString()
                      }
                    />


                    <DarkMetric
                      label="Active"
                      value={
                        dashboard.market.activeNoticeCount30Days
                          .toLocaleString()
                      }
                    />


                    <DarkMetric
                      label="Buyers"
                      value={
                        dashboard.market.uniqueBuyerCount30Days
                          .toLocaleString()
                      }
                    />


                    <DarkMetric
                      label="Observed suppliers"
                      value={
                        dashboard.market.uniqueObservedSupplierCount30Days
                          .toLocaleString()
                      }
                    />

                  </div>


                  <dl
                    className="
                      mt-6
                      space-y-3
                      text-xs
                    "
                  >

                    <DarkRow
                      label="Buyer concentration"
                      value={
                        formatEnum(
                          dashboard.market.buyerConcentration,
                        )
                      }
                    />


                    <DarkRow
                      label="Supplier concentration"
                      value={
                        formatEnum(
                          dashboard.market.supplierConcentration,
                        )
                      }
                    />


                    <DarkRow
                      label="Award coverage"
                      value={
                        `${dashboard.market.awardCoveragePercentage30Days}%`
                      }
                    />


                    <DarkRow
                      label="Leading CPV"
                      value={
                        dashboard.market.leadingCpv
                          ? `${dashboard.market.leadingCpv}${
                              dashboard.market.leadingCpvSharePercentage ===
                              null
                                ? ""
                                : ` · ${dashboard.market.leadingCpvSharePercentage}%`
                            }`
                          : "Unavailable"
                      }
                    />

                  </dl>


                  <Link
                    href="/market"
                    className="
                      mt-6
                      inline-flex
                      h-10
                      items-center
                      gap-2
                      rounded-xl
                      bg-white
                      px-4
                      text-sm
                      font-semibold
                      text-slate-900
                    "
                  >
                    Open Market Explorer

                    <ArrowRight
                      className="size-4"
                    />
                  </Link>

                </>

              )

              : (

                <div
                  className="
                    mt-6
                    rounded-2xl
                    bg-white/10
                    p-4
                    text-sm
                    leading-6
                    text-slate-200
                  "
                >
                  Public market pulse is temporarily unavailable.
                  Your private bid portfolio remains fully usable.
                </div>

              )
          }

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

        <div
          className="
            flex
            flex-col
            gap-4
            md:flex-row
            md:items-center
            md:justify-between
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
              Active intelligence
            </p>


            <h2
              className="
                mt-2
                text-xl
                font-semibold
                text-slate-950
              "
            >
              Bid portfolio
            </h2>


            <p
              className="
                mt-1
                text-xs
                text-slate-500
              "
            >
              Ordered by operational urgency, then deadline
              and match strength.
            </p>

          </div>


          <Link
            href="/matches"
            className="
              inline-flex
              h-10
              items-center
              gap-2
              rounded-xl
              border
              border-slate-200
              px-4
              text-sm
              font-semibold
              text-slate-700
            "
          >
            View all matches

            <ArrowRight
              className="size-4"
            />
          </Link>

        </div>


        <div
          className="
            mt-6
            space-y-3
          "
        >

          {
            visiblePortfolio.map(
              (
                bid,
              ) => (

                <article
                  key={
                    bid.tenderId
                  }
                  className="
                    rounded-2xl
                    border
                    border-slate-200
                    p-4
                  "
                >

                  <div
                    className="
                      flex
                      flex-col
                      gap-4
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
                            `${bid.matchScore}% match`
                          }
                        />


                        <Badge
                          value={
                            formatEnum(
                              bid.matchGrade,
                            )
                          }
                        />


                        <DecisionBadge
                          outcome={
                            bid.decisionOutcome
                          }
                        />


                        {
                          bid.readyForSubmission

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
                                Submission ready
                              </span>

                            )

                            : null
                        }


                        {
                          bid.submitted

                            ? (

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
                                Submitted
                              </span>

                            )

                            : null
                        }

                      </div>


                      <h3
                        className="
                          mt-3
                          text-sm
                          font-semibold
                          text-slate-950
                        "
                      >
                        {
                          bid.title
                        }
                      </h3>


                      <p
                        className="
                          mt-1
                          text-xs
                          text-slate-400
                        "
                      >
                        {
                          bid.buyerName
                        }
                      </p>


                      <div
                        className="
                          mt-4
                          flex
                          flex-wrap
                          gap-x-5
                          gap-y-2
                          text-xs
                          text-slate-500
                        "
                      >

                        <span>
                          Compliance:
                          {" "}
                          {
                            bid.complianceReadinessScore
                          }
                          %
                        </span>


                        <span>
                          Open tasks:
                          {" "}
                          {
                            bid.openTasks
                          }
                        </span>


                        {
                          bid.overdueTasks >
                          0

                            ? (

                              <span
                                className="
                                  font-semibold
                                  text-red-600
                                "
                              >
                                {
                                  bid.overdueTasks
                                }
                                {" "}
                                overdue
                              </span>

                            )

                            : null
                        }


                        {
                          bid.submissionDeadline

                            ? (

                              <span>
                                Deadline:
                                {" "}
                                {
                                  formatDate(
                                    bid.submissionDeadline,
                                  )
                                }
                                {
                                  bid.daysToDeadline ===
                                  null
                                    ? ""
                                    : ` · ${deadlineText(
                                        bid.daysToDeadline,
                                      )}`
                                }
                              </span>

                            )

                            : null
                        }

                      </div>

                    </div>


                    <div
                      className="
                        flex
                        flex-wrap
                        gap-2
                      "
                    >

                      <Link
                        href={
                          `/bid-workspaces/${bid.tenderId}`
                        }
                        className="
                          inline-flex
                          h-9
                          items-center
                          rounded-xl
                          border
                          border-slate-200
                          px-3
                          text-xs
                          font-semibold
                          text-slate-700
                        "
                      >
                        Workspace
                      </Link>


                      <Link
                        href={
                          `/opportunities/${bid.tenderId}`
                        }
                        className="
                          inline-flex
                          h-9
                          items-center
                          rounded-xl
                          bg-[#14213d]
                          px-3
                          text-xs
                          font-semibold
                          text-white
                        "
                      >
                        Opportunity
                      </Link>

                    </div>

                  </div>

                </article>

              ),
            )
          }


          {
            visiblePortfolio.length ===
            0

              ? (

                <div
                  className="
                    rounded-2xl
                    border
                    border-dashed
                    border-slate-300
                    p-12
                    text-center
                  "
                >

                  <Sparkles
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
                    No matched opportunities yet
                  </p>


                  <p
                    className="
                      mt-2
                      text-xs
                      text-slate-400
                    "
                  >
                    Complete your company profile and calculate
                    matches to populate the command center.
                  </p>


                  <Link
                    href="/matches"
                    className="
                      mt-5
                      inline-flex
                      h-10
                      items-center
                      rounded-xl
                      bg-[#14213d]
                      px-4
                      text-sm
                      font-semibold
                      text-white
                    "
                  >
                    Open matching
                  </Link>

                </div>

              )

              : null
          }

        </div>

      </section>


      <div
        className="
          mt-6
          grid
          gap-4
          md:grid-cols-3
        "
      >

        <CommandCard
          icon={Gauge}
          title="Match intelligence"
          detail="Review fit scores, signals and blocked opportunities."
          href="/matches"
        />


        <CommandCard
          icon={Scale}
          title="Bid decisions"
          detail="Manage PENDING, GO, HOLD and NO-GO portfolio decisions."
          href="/bid-decisions"
        />


        <CommandCard
          icon={BarChart3}
          title="Market intelligence"
          detail="Explore buyer, supplier and procurement-market behavior."
          href="/market"
        />

      </div>


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

        Portfolio metrics are organization-private and tenant
        isolated. The public market pulse is derived separately
        from the shared canonical procurement corpus. TenderPilot
        never sends private dashboard, compliance, document, RAG,
        or planning data into the public market index.

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
          leading-5
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


function QuickLink({

  href,

  label,

}: {

  href:
    string;

  label:
    string;

}) {

  return (

    <Link
      href={
        href
      }
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
        text-slate-700
        hover:bg-slate-50
      "
    >
      {
        label
      }

      <ArrowRight
        className="size-3.5"
      />
    </Link>

  );

}


function PriorityIcon({

  priority,

}: {

  priority:
    DashboardActionPriority;

}) {

  if (
    priority ===
    "CRITICAL"
  ) {

    return (

      <div
        className="
          grid
          size-9
          shrink-0
          place-items-center
          rounded-xl
          bg-red-50
          text-red-600
        "
      >
        <AlertOctagon
          className="size-4"
        />
      </div>

    );

  }


  if (
    priority ===
    "HIGH"
  ) {

    return (

      <div
        className="
          grid
          size-9
          shrink-0
          place-items-center
          rounded-xl
          bg-amber-50
          text-amber-600
        "
      >
        <TriangleAlert
          className="size-4"
        />
      </div>

    );

  }


  return (

    <div
      className="
        grid
        size-9
        shrink-0
        place-items-center
        rounded-xl
        bg-blue-50
        text-blue-600
      "
    >
      <Compass
        className="size-4"
      />
    </div>

  );

}


function PriorityBadge({

  priority,

}: {

  priority:
    DashboardActionPriority;

}) {

  const className =
    priority ===
    "CRITICAL"
      ? "bg-red-50 text-red-700"
      : priority ===
        "HIGH"
        ? "bg-amber-50 text-amber-700"
        : priority ===
          "MEDIUM"
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
        priority
      }
    </span>

  );

}


function DecisionBadge({

  outcome,

}: {

  outcome:
    string
    |
    null;

}) {

  const value =
    outcome
    ??
    "PENDING";


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
        formatEnum(
          value,
        )
      }
    </span>

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


function DarkMetric({

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
        bg-white/10
        p-4
      "
    >

      <p
        className="
          text-[9px]
          font-semibold
          uppercase
          tracking-[0.1em]
          text-slate-300
        "
      >
        {
          label
        }
      </p>


      <p
        className="
          mt-2
          text-xl
          font-semibold
          text-white
        "
      >
        {
          value
        }
      </p>

    </div>

  );

}


function DarkRow({

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
        border-white/10
        pb-3
        last:border-0
        last:pb-0
      "
    >

      <dt
        className="
          text-slate-300
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
          text-white
        "
      >
        {
          value
        }
      </dd>

    </div>

  );

}


function CommandCard({

  icon:
    Icon,

  title,

  detail,

  href,

}: {

  icon:
    LucideIcon;

  title:
    string;

  detail:
    string;

  href:
    string;

}) {

  return (

    <Link
      href={
        href
      }
      className="
        group
        rounded-2xl
        border
        border-slate-200
        bg-white
        p-5
        transition
        hover:border-slate-300
        hover:shadow-sm
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

        <Icon
          className="
            size-5
            text-[#2f6fed]
          "
        />


        <ArrowRight
          className="
            size-4
            text-slate-300
            transition
            group-hover:translate-x-0.5
          "
        />

      </div>


      <p
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
      </p>


      <p
        className="
          mt-2
          text-xs
          leading-5
          text-slate-500
        "
      >
        {
          detail
        }
      </p>

    </Link>

  );

}


function deadlineText(
  days:
    number,
) {

  if (
    days <
    0
  ) {

    return `${Math.abs(
      days,
    )}d overdue`;

  }


  if (
    days ===
    0
  ) {

    return "due today";

  }


  return `${days}d remaining`;

}
