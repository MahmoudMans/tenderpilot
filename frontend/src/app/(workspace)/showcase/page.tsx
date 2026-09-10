import {

  AlertOctagon,

  ArrowRight,

  BarChart3,

  BellRing,

  Building2,

  CheckCircle2,

  CircleGauge,

  ClipboardCheck,

  Database,

  Factory,

  FileSearch,

  Gauge,

  Layers3,

  LockKeyhole,

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

  SHOWCASE_DATASET_ID,

  SHOWCASE_DATASET_KIND,

  showcaseData,

} from "@/features/showcase/showcase-data";


export default function ShowcasePage() {

  return (

    <div
      className="
        mx-auto
        max-w-[1550px]
      "
    >

      <section
        className="
          overflow-hidden
          rounded-[34px]
          bg-[#14213d]
          p-8
          text-white
          md:p-10
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
              inline-flex
              items-center
              gap-2
              rounded-full
              bg-blue-400/15
              px-3
              py-1.5
              text-[10px]
              font-semibold
              uppercase
              tracking-[0.12em]
              text-blue-200
            "
          >
            <Sparkles
              className="size-3"
            />

            Synthetic portfolio showcase
          </span>


          <span
            className="
              rounded-full
              border
              border-white/15
              px-3
              py-1.5
              text-[10px]
              font-semibold
              text-slate-300
            "
          >
            {
              SHOWCASE_DATASET_KIND
            }
          </span>

        </div>


        <div
          className="
            mt-7
            grid
            gap-8
            xl:grid-cols-[1.25fr_0.75fr]
            xl:items-end
          "
        >

          <div>

            <p
              className="
                text-xs
                font-semibold
                uppercase
                tracking-[0.18em]
                text-blue-200
              "
            >
              TenderPilot · Portfolio presentation
            </p>


            <h1
              className="
                mt-4
                max-w-4xl
                text-4xl
                font-semibold
                tracking-[-0.04em]
                md:text-6xl
              "
            >
              Public procurement intelligence from discovery to controlled submission.
            </h1>


            <p
              className="
                mt-6
                max-w-3xl
                text-sm
                leading-7
                text-slate-300
              "
            >
              A screenshot-ready demonstration of TenderPilot&apos;s
              procurement intelligence, company matching, bid
              decision, compliance, execution, alerting and data
              operations capabilities.
            </p>

          </div>


          <div
            className="
              rounded-3xl
              border
              border-white/10
              bg-white/5
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

              <ShieldCheck
                className="
                  mt-0.5
                  size-5
                  shrink-0
                  text-emerald-300
                "
              />


              <div>

                <p
                  className="
                    text-sm
                    font-semibold
                    text-white
                  "
                >
                  No live customer or procurement data
                </p>


                <p
                  className="
                    mt-2
                    text-xs
                    leading-5
                    text-slate-300
                  "
                >
                  {
                    showcaseData.metadata.disclaimer
                  }
                </p>


                <p
                  className="
                    mt-3
                    font-mono
                    text-[9px]
                    text-slate-500
                  "
                >
                  {
                    SHOWCASE_DATASET_ID
                  }
                </p>

              </div>

            </div>

          </div>

        </div>


        <div
          className="
            mt-8
            flex
            flex-wrap
            gap-2
          "
        >

          <CaptureLink
            href="#executive"
            label="Executive"
          />


          <CaptureLink
            href="#pipeline"
            label="Bid pipeline"
          />


          <CaptureLink
            href="#market"
            label="Market"
          />


          <CaptureLink
            href="#alerts"
            label="Alerts"
          />


          <CaptureLink
            href="#operations"
            label="Operations"
          />


          <CaptureLink
            href="#architecture"
            label="Architecture"
          />

        </div>

      </section>


      <section
        id="executive"
        className="
          scroll-mt-8
          pt-8
        "
      >

        <SectionHeader
          eyebrow="Capture 01"
          title="Executive command center"
          detail="Portfolio-level signals designed to answer what needs attention now."
          icon={Gauge}
        />


        <div
          className="
            mt-5
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
              showcaseData.executive.highPotentialMatches
            }
            detail={
              `${showcaseData.executive.matchedOpportunities} matched opportunities`
            }
          />


          <Metric
            icon={CheckCircle2}
            label="GO portfolio"
            value={
              showcaseData.executive.goBids
            }
            detail={
              `${showcaseData.executive.holdBids} HOLD · ${showcaseData.executive.noGoBids} NO-GO`
            }
          />


          <Metric
            icon={ClipboardCheck}
            label="Submission ready"
            value={
              showcaseData.executive.submissionReady
            }
            detail={
              `${showcaseData.executive.complianceReady} compliance-ready bids`
            }
          />


          <Metric
            icon={AlertOctagon}
            label="Overdue tasks"
            value={
              showcaseData.executive.overdueTasks
            }
            detail={
              `${showcaseData.executive.criticalTasks} critical open tasks`
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
              showcaseData.executive.pendingDecisions
            }
            detail="Opportunities awaiting human GO / HOLD / NO-GO"
          />


          <Metric
            icon={FileSearch}
            label="Open bid tasks"
            value={
              showcaseData.executive.openTasks
            }
            detail="Execution work across the active portfolio"
          />


          <Metric
            icon={BellRing}
            label="Deadlines ≤ 7 days"
            value={
              showcaseData.executive.deadlines7Days
            }
            detail="Immediate submission pressure"
          />


          <Metric
            icon={CheckCircle2}
            label="Submitted"
            value={
              showcaseData.executive.submitted
            }
            detail="Frozen after controlled human submission"
          />

        </div>

      </section>


      <section
        id="pipeline"
        className="
          scroll-mt-8
          pt-10
        "
      >

        <SectionHeader
          eyebrow="Capture 02"
          title="Bid portfolio pipeline"
          detail="Fit, decision, compliance, task health and submission readiness in one view."
          icon={Layers3}
        />


        <div
          className="
            mt-5
            overflow-hidden
            rounded-3xl
            border
            border-slate-200
            bg-white
          "
        >

          <div
            className="
              overflow-x-auto
            "
          >

            <table
              className="
                w-full
                min-w-[1100px]
                text-left
              "
            >

              <thead
                className="
                  bg-slate-50
                "
              >

                <tr
                  className="
                    text-[10px]
                    font-semibold
                    uppercase
                    tracking-[0.08em]
                    text-slate-400
                  "
                >

                  <th className="px-5 py-4">
                    Opportunity
                  </th>

                  <th className="px-5 py-4">
                    Match
                  </th>

                  <th className="px-5 py-4">
                    Decision
                  </th>

                  <th className="px-5 py-4">
                    Compliance
                  </th>

                  <th className="px-5 py-4">
                    Tasks
                  </th>

                  <th className="px-5 py-4">
                    Deadline
                  </th>

                  <th className="px-5 py-4">
                    Readiness
                  </th>

                </tr>

              </thead>


              <tbody>

                {
                  showcaseData.bids.map(
                    (
                      bid,
                    ) => (

                      <tr
                        key={
                          bid.id
                        }
                        className="
                          border-t
                          border-slate-100
                        "
                      >

                        <td
                          className="
                            px-5
                            py-5
                          "
                        >

                          <p
                            className="
                              max-w-sm
                              text-sm
                              font-semibold
                              text-slate-950
                            "
                          >
                            {
                              bid.title
                            }
                          </p>


                          <p
                            className="
                              mt-1
                              text-[10px]
                              text-slate-400
                            "
                          >
                            {
                              bid.buyer
                            }
                          </p>

                        </td>


                        <td className="px-5 py-5">

                          <div
                            className="
                              flex
                              items-center
                              gap-2
                            "
                          >

                            <span
                              className="
                                text-lg
                                font-semibold
                                text-slate-950
                              "
                            >
                              {
                                bid.matchScore
                              }
                              %
                            </span>


                            <span
                              className="
                                rounded-full
                                bg-blue-50
                                px-2
                                py-1
                                text-[9px]
                                font-semibold
                                text-blue-700
                              "
                            >
                              {
                                bid.grade
                              }
                            </span>

                          </div>

                        </td>


                        <td className="px-5 py-5">

                          <DecisionBadge
                            decision={
                              bid.decision
                            }
                          />

                        </td>


                        <td className="px-5 py-5">

                          <div
                            className="
                              w-28
                            "
                          >

                            <div
                              className="
                                flex
                                justify-between
                                text-[10px]
                              "
                            >

                              <span
                                className="
                                  text-slate-400
                                "
                              >
                                Ready
                              </span>


                              <span
                                className="
                                  font-semibold
                                  text-slate-700
                                "
                              >
                                {
                                  bid.compliance
                                }
                                %
                              </span>

                            </div>


                            <div
                              className="
                                mt-2
                                h-1.5
                                overflow-hidden
                                rounded-full
                                bg-slate-100
                              "
                            >

                              <div
                                className="
                                  h-full
                                  rounded-full
                                  bg-slate-600
                                "
                                style={{
                                  width:
                                    `${bid.compliance}%`,
                                }}
                              />

                            </div>

                          </div>

                        </td>


                        <td
                          className="
                            px-5
                            py-5
                            text-xs
                            text-slate-600
                          "
                        >
                          {
                            bid.openTasks
                          }
                          {" "}
                          open

                          {
                            bid.overdueTasks >
                            0

                              ? (

                                <span
                                  className="
                                    ml-2
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

                        </td>


                        <td
                          className="
                            px-5
                            py-5
                          "
                        >

                          <p
                            className="
                              text-xs
                              font-semibold
                              text-slate-700
                            "
                          >
                            {
                              bid.deadline
                            }
                          </p>


                          <p
                            className="
                              mt-1
                              text-[10px]
                              text-slate-400
                            "
                          >
                            {
                              bid.deadlineSignal
                            }
                          </p>

                        </td>


                        <td className="px-5 py-5">

                          <ReadinessBadge
                            readiness={
                              bid.readiness
                            }
                            label={
                              bid.readinessLabel
                            }
                          />

                        </td>

                      </tr>

                    ),
                  )
                }

              </tbody>

            </table>

          </div>

        </div>

      </section>


      <section
        id="market"
        className="
          scroll-mt-8
          pt-10
        "
      >

        <SectionHeader
          eyebrow="Capture 03"
          title="Procurement market intelligence"
          detail="Evidence-aware public-market context remains separated from private bid data."
          icon={BarChart3}
        />


        <div
          className="
            mt-5
            grid
            gap-5
            xl:grid-cols-[1.1fr_0.9fr]
          "
        >

          <div
            className="
              rounded-3xl
              bg-[#14213d]
              p-6
              text-white
            "
          >

            <p
              className="
                text-xs
                font-semibold
                uppercase
                tracking-[0.12em]
                text-blue-200
              "
            >
              Synthetic 30-day market pulse
            </p>


            <div
              className="
                mt-5
                grid
                grid-cols-2
                gap-3
                md:grid-cols-4
              "
            >

              <DarkMetric
                label="Notices"
                value={
                  showcaseData.market.notices30Days
                }
              />


              <DarkMetric
                label="Active"
                value={
                  showcaseData.market.activeNotices30Days
                }
              />


              <DarkMetric
                label="Buyers"
                value={
                  showcaseData.market.uniqueBuyers30Days
                }
              />


              <DarkMetric
                label="Observed suppliers"
                value={
                  showcaseData.market.observedSuppliers30Days
                }
              />

            </div>


            <div
              className="
                mt-6
                grid
                gap-5
                md:grid-cols-3
              "
            >

              <MarketSignal
                label="Buyer market"
                value={
                  showcaseData.market.buyerConcentration
                }
              />


              <MarketSignal
                label="Supplier market"
                value={
                  showcaseData.market.supplierConcentration
                }
              />


              <MarketSignal
                label="Award coverage"
                value={
                  `${showcaseData.market.awardCoverage}%`
                }
              />

            </div>


            <div
              className="
                mt-7
                rounded-2xl
                bg-white/10
                p-5
              "
            >

              <p
                className="
                  text-[10px]
                  font-semibold
                  uppercase
                  tracking-[0.1em]
                  text-blue-200
                "
              >
                Leading CPV
              </p>


              <p
                className="
                  mt-2
                  text-xl
                  font-semibold
                "
              >
                {
                  showcaseData.market.leadingCpv
                }
              </p>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-300
                "
              >
                {
                  showcaseData.market.leadingCpvLabel
                }
                {" · "}
                {
                  showcaseData.market.leadingCpvShare
                }
                % share
              </p>


              <div
                className="
                  mt-5
                  flex
                  h-24
                  items-end
                  gap-2
                "
              >

                {
                  showcaseData.market.trend.map(
                    (
                      value,
                      index,
                    ) => (

                      <div
                        key={
                          `${value}-${index}`
                        }
                        className="
                          flex-1
                          rounded-t
                          bg-blue-300/70
                        "
                        style={{
                          height:
                            `${value}%`,
                        }}
                      />

                    ),
                  )
                }

              </div>

            </div>

          </div>


          <div
            className="
              grid
              gap-5
            "
          >

            <IntelligenceCard
              icon={Building2}
              title="Buyer intelligence"
            >

              {
                showcaseData.buyers.map(
                  (
                    buyer,
                  ) => (

                    <Ranking
                      key={
                        buyer.name
                      }
                      name={
                        buyer.name
                      }
                      detail={
                        `${buyer.awards} observed awards · ${buyer.cadence}`
                      }
                      value={
                        `${buyer.noticeShare}%`
                      }
                    />

                  ),
                )
              }

            </IntelligenceCard>


            <IntelligenceCard
              icon={Factory}
              title="Observed supplier landscape"
            >

              {
                showcaseData.suppliers.map(
                  (
                    supplier,
                  ) => (

                    <Ranking
                      key={
                        supplier.name
                      }
                      name={
                        supplier.name
                      }
                      detail={
                        supplier.specialization
                      }
                      value={
                        `${supplier.awardShare}%`
                      }
                    />

                  ),
                )
              }

            </IntelligenceCard>

          </div>

        </div>

      </section>


      <section
        id="alerts"
        className="
          scroll-mt-8
          pt-10
        "
      >

        <SectionHeader
          eyebrow="Capture 04"
          title="Operational alert center"
          detail="Deterministic signals convert portfolio state into clear human actions."
          icon={BellRing}
        />


        <div
          className="
            mt-5
            grid
            gap-4
            lg:grid-cols-2
          "
        >

          {
            showcaseData.alerts.map(
              (
                alert,
              ) => (

                <div
                  key={
                    alert.id
                  }
                  className="
                    rounded-3xl
                    border
                    border-slate-200
                    bg-white
                    p-5
                  "
                >

                  <div
                    className="
                      flex
                      items-start
                      gap-4
                    "
                  >

                    <AlertIcon
                      severity={
                        alert.severity
                      }
                    />


                    <div>

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
                          alert.severity
                        }
                      </span>


                      <h3
                        className="
                          mt-1
                          text-sm
                          font-semibold
                          text-slate-950
                        "
                      >
                        {
                          alert.title
                        }
                      </h3>


                      <p
                        className="
                          mt-2
                          text-xs
                          leading-5
                          text-slate-500
                        "
                      >
                        {
                          alert.detail
                        }
                      </p>

                    </div>

                  </div>

                </div>

              ),
            )
          }

        </div>

      </section>


      <section
        id="operations"
        className="
          scroll-mt-8
          pt-10
        "
      >

        <SectionHeader
          eyebrow="Capture 05"
          title="Procurement data operations"
          detail="Source-health visibility for the shared canonical procurement corpus."
          icon={Database}
        />


        <div
          className="
            mt-5
            grid
            gap-4
            md:grid-cols-3
          "
        >

          {
            showcaseData.sourceHealth.map(
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
                      gap-3
                    "
                  >

                    <div>

                      <p
                        className="
                          text-xl
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
                        Canonical ingestion
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
                      grid-cols-3
                      gap-2
                    "
                  >

                    <MiniMetric
                      label="Reliability"
                      value={
                        `${source.reliability}%`
                      }
                    />


                    <MiniMetric
                      label="7d runs"
                      value={
                        `${source.runs7d}`
                      }
                    />


                    <MiniMetric
                      label="Records"
                      value={
                        source.records.toLocaleString()
                      }
                    />

                  </div>

                </article>

              ),
            )
          }

        </div>

      </section>


      <section
        id="architecture"
        className="
          scroll-mt-8
          pt-10
        "
      >

        <SectionHeader
          eyebrow="Capture 06"
          title="Architecture & trust boundaries"
          detail="A core differentiator: public procurement intelligence and private bid intelligence remain separated."
          icon={LockKeyhole}
        />


        <div
          className="
            mt-5
            grid
            gap-4
            md:grid-cols-2
          "
        >

          {
            showcaseData.architecture.map(
              (
                item,
                index,
              ) => (

                <article
                  key={
                    item.title
                  }
                  className="
                    rounded-3xl
                    border
                    border-slate-200
                    bg-white
                    p-6
                  "
                >

                  <span
                    className="
                      text-[10px]
                      font-semibold
                      text-[#2f6fed]
                    "
                  >
                    0
                    {
                      index + 1
                    }
                  </span>


                  <h3
                    className="
                      mt-4
                      text-base
                      font-semibold
                      text-slate-950
                    "
                  >
                    {
                      item.title
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
                      item.detail
                    }
                  </p>

                </article>

              ),
            )
          }

        </div>

      </section>


      <section
        className="
          mt-10
          rounded-3xl
          border
          border-slate-200
          bg-slate-50
          p-7
        "
      >

        <div
          className="
            flex
            items-center
            gap-3
          "
        >

          <CircleGauge
            className="
              size-5
              text-[#2f6fed]
            "
          />


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
              Portfolio capture checklist
            </p>


            <h2
              className="
                mt-1
                text-xl
                font-semibold
                text-slate-950
              "
            >
              Six screenshots tell the complete product story
            </h2>

          </div>

        </div>


        <div
          className="
            mt-6
            grid
            gap-3
            md:grid-cols-2
            xl:grid-cols-3
          "
        >

          {
            showcaseData.captureTargets.map(
              (
                target,
              ) => (

                <div
                  key={
                    target.number
                  }
                  className="
                    rounded-2xl
                    bg-white
                    p-5
                  "
                >

                  <span
                    className="
                      text-[10px]
                      font-semibold
                      text-[#2f6fed]
                    "
                  >
                    {
                      target.number
                    }
                  </span>


                  <p
                    className="
                      mt-3
                      text-sm
                      font-semibold
                      text-slate-900
                    "
                  >
                    {
                      target.title
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
                      target.detail
                    }
                  </p>

                </div>

              ),
            )
          }

        </div>


        <div
          className="
            mt-6
            flex
            flex-wrap
            gap-2
          "
        >

          <Link
            href="/workspace"
            className="
              inline-flex
              h-10
              items-center
              gap-2
              rounded-xl
              bg-[#14213d]
              px-4
              text-sm
              font-semibold
              text-white
            "
          >
            Open product map

            <ArrowRight
              className="size-4"
            />
          </Link>


          <Link
            href="/dashboard"
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
            "
          >
            Open live dashboard
          </Link>

        </div>

      </section>


      <div
        className="
          mt-6
          flex
          items-start
          gap-3
          rounded-2xl
          border
          border-blue-100
          bg-blue-50
          p-4
          text-xs
          leading-5
          text-blue-800
        "
      >

        <ShieldCheck
          className="
            mt-0.5
            size-4
            shrink-0
          "
        />

        This showcase does not call TenderPilot APIs, does not
        read the live database, and does not persist changes.
        It exists exclusively as a deterministic synthetic
        presentation surface for demonstrations and portfolio
        screenshots.

      </div>

    </div>

  );

}


function SectionHeader({

  eyebrow,

  title,

  detail,

  icon:
    Icon,

}: {

  eyebrow:
    string;

  title:
    string;

  detail:
    string;

  icon:
    LucideIcon;

}) {

  return (

    <div
      className="
        flex
        flex-col
        gap-4
        md:flex-row
        md:items-end
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
          {
            eyebrow
          }
        </p>


        <h2
          className="
            mt-2
            text-2xl
            font-semibold
            tracking-[-0.02em]
            text-slate-950
          "
        >
          {
            title
          }
        </h2>


        <p
          className="
            mt-2
            max-w-3xl
            text-sm
            text-slate-500
          "
        >
          {
            detail
          }
        </p>

      </div>


      <div
        className="
          grid
          size-11
          shrink-0
          place-items-center
          rounded-2xl
          bg-blue-50
          text-[#2f6fed]
        "
      >
        <Icon
          className="size-5"
        />
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
    number;

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
          text-2xl
          font-semibold
          text-slate-950
        "
      >
        {
          value.toLocaleString()
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


function DecisionBadge({

  decision,

}: {

  decision:
    string;

}) {

  const className =
    decision ===
    "GO"
      ? "bg-emerald-50 text-emerald-700"
      : decision ===
        "HOLD"
        ? "bg-amber-50 text-amber-700"
        : decision ===
          "NO_GO"
          ? "bg-red-50 text-red-700"
          : "bg-blue-50 text-blue-700";


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
        decision.replace(
          "_",
          "-",
        )
      }
    </span>

  );

}


function ReadinessBadge({

  readiness,

  label,

}: {

  readiness:
    string;

  label:
    string;

}) {

  const className =
    readiness ===
    "READY"
      ? "bg-emerald-50 text-emerald-700"
      : readiness ===
        "AT_RISK"
        ? "bg-red-50 text-red-700"
        : readiness ===
          "REVIEW"
          ? "bg-amber-50 text-amber-700"
          : readiness ===
            "DECISION"
            ? "bg-blue-50 text-blue-700"
            : "bg-slate-100 text-slate-600";


  return (

    <span
      className={
        `
          whitespace-nowrap
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
        label
      }
    </span>

  );

}


function CaptureLink({

  href,

  label,

}: {

  href:
    string;

  label:
    string;

}) {

  return (

    <a
      href={
        href
      }
      className="
        rounded-xl
        border
        border-white/15
        bg-white/5
        px-3
        py-2
        text-xs
        font-semibold
        text-slate-200
        transition
        hover:bg-white/10
      "
    >
      {
        label
      }
    </a>

  );

}


function DarkMetric({

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
          tracking-[0.08em]
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
          value.toLocaleString()
        }
      </p>

    </div>

  );

}


function MarketSignal({

  label,

  value,

}: {

  label:
    string;

  value:
    string;

}) {

  return (

    <div>

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


function IntelligenceCard({

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

    <div
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
          gap-3
        "
      >

        <Icon
          className="
            size-5
            text-[#2f6fed]
          "
        />


        <h3
          className="
            font-semibold
            text-slate-950
          "
        >
          {
            title
          }
        </h3>

      </div>


      <div
        className="
          mt-5
          divide-y
          divide-slate-100
        "
      >
        {
          children
        }
      </div>

    </div>

  );

}


function Ranking({

  name,

  detail,

  value,

}: {

  name:
    string;

  detail:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        flex
        items-start
        justify-between
        gap-4
        py-4
        first:pt-0
        last:pb-0
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
            name
          }
        </p>


        <p
          className="
            mt-1
            text-[10px]
            text-slate-400
          "
        >
          {
            detail
          }
        </p>

      </div>


      <span
        className="
          shrink-0
          text-sm
          font-semibold
          text-[#2f6fed]
        "
      >
        {
          value
        }
      </span>

    </div>

  );

}


function AlertIcon({

  severity,

}: {

  severity:
    string;

}) {

  if (
    severity ===
    "CRITICAL"
  ) {

    return (

      <div
        className="
          grid
          size-10
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
    severity ===
    "HIGH"
  ) {

    return (

      <div
        className="
          grid
          size-10
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
        size-10
        shrink-0
        place-items-center
        rounded-xl
        bg-blue-50
        text-blue-600
      "
    >
      <BellRing
        className="size-4"
      />
    </div>

  );

}


function HealthBadge({

  health,

}: {

  health:
    string;

}) {

  const className =
    health ===
    "HEALTHY"
      ? "bg-emerald-50 text-emerald-700"
      : health ===
        "DEGRADED"
        ? "bg-amber-50 text-amber-700"
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
        health
      }
    </span>

  );

}


function MiniMetric({

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
          tracking-[0.06em]
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
