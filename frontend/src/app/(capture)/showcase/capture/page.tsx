import {

  AlertOctagon,

  ArrowLeft,

  BarChart3,

  BellRing,

  Building2,

  CheckCircle2,

  Database,

  Factory,

  FileCheck2,

  LockKeyhole,

  Scale,

  ShieldCheck,

  Target,

  TriangleAlert,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import Link from "next/link";


import type {

  ReactNode,

} from "react";


import {

  CAPTURE_DATASET_ID,

  CAPTURE_HEIGHT,

  CAPTURE_WIDTH,

  captureManifest,

} from "@/features/showcase/capture-manifest";


import {

  SHOWCASE_DATASET_KIND,

  showcaseData,

} from "@/features/showcase/showcase-data";


export default function PortfolioCapturePage() {

  return (

    <main
      data-capture-mode="portfolio"
      data-synthetic-data="true"
      className="
        min-h-screen
        bg-slate-100
        px-3
        py-6
        md:px-6
        print:bg-white
        print:p-0
      "
    >

      <div
        className="
          mx-auto
          mb-6
          flex
          max-w-[1440px]
          flex-col
          gap-4
          rounded-2xl
          border
          border-slate-200
          bg-white
          p-5
          md:flex-row
          md:items-center
          md:justify-between
          print:hidden
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
                uppercase
                tracking-[0.08em]
                text-blue-700
              "
            >
              Portfolio Capture Mode
            </span>


            <span
              className="
                rounded-full
                bg-amber-50
                px-2.5
                py-1
                text-[10px]
                font-semibold
                uppercase
                tracking-[0.08em]
                text-amber-700
              "
            >
              Synthetic data
            </span>

          </div>


          <p
            className="
              mt-3
              text-sm
              font-semibold
              text-slate-900
            "
          >
            Six presentation frames ·
            {" "}
            {
              CAPTURE_WIDTH
            }
            ×
            {
              CAPTURE_HEIGHT
            }
          </p>


          <p
            className="
              mt-1
              text-xs
              text-slate-500
            "
          >
            Each frame is deterministic and designed for
            portfolio screenshots.
          </p>

        </div>


        <div
          className="
            flex
            flex-wrap
            gap-2
          "
        >

          <Link
            href="/showcase"
            className="
              inline-flex
              h-9
              items-center
              gap-2
              rounded-xl
              border
              border-slate-200
              px-3
              text-xs
              font-semibold
              text-slate-700
            "
          >
            <ArrowLeft
              className="size-3.5"
            />

            Interactive showcase
          </Link>


          {
            captureManifest.map(
              (
                capture,
              ) => (

                <a
                  key={
                    capture.id
                  }
                  href={
                    `#${capture.anchor}`
                  }
                  className="
                    inline-flex
                    h-9
                    items-center
                    rounded-xl
                    bg-slate-100
                    px-3
                    text-xs
                    font-semibold
                    text-slate-600
                  "
                >
                  {
                    capture.order
                  }
                </a>

              ),
            )
          }

        </div>

      </div>


      <div
        className="
          space-y-8
          print:space-y-0
        "
      >

        <CaptureFrame
          anchor={
            captureManifest[0].anchor
          }
          captureId={
            captureManifest[0].id
          }
          order={
            captureManifest[0].order
          }
          title={
            captureManifest[0].title
          }
          subtitle={
            captureManifest[0].subtitle
          }
          filename={
            captureManifest[0].recommendedFilename
          }
          icon={Target}
        >

          <ExecutiveCapture />

        </CaptureFrame>


        <CaptureFrame
          anchor={
            captureManifest[1].anchor
          }
          captureId={
            captureManifest[1].id
          }
          order={
            captureManifest[1].order
          }
          title={
            captureManifest[1].title
          }
          subtitle={
            captureManifest[1].subtitle
          }
          filename={
            captureManifest[1].recommendedFilename
          }
          icon={FileCheck2}
        >

          <PipelineCapture />

        </CaptureFrame>


        <CaptureFrame
          anchor={
            captureManifest[2].anchor
          }
          captureId={
            captureManifest[2].id
          }
          order={
            captureManifest[2].order
          }
          title={
            captureManifest[2].title
          }
          subtitle={
            captureManifest[2].subtitle
          }
          filename={
            captureManifest[2].recommendedFilename
          }
          icon={BarChart3}
        >

          <MarketCapture />

        </CaptureFrame>


        <CaptureFrame
          anchor={
            captureManifest[3].anchor
          }
          captureId={
            captureManifest[3].id
          }
          order={
            captureManifest[3].order
          }
          title={
            captureManifest[3].title
          }
          subtitle={
            captureManifest[3].subtitle
          }
          filename={
            captureManifest[3].recommendedFilename
          }
          icon={BellRing}
        >

          <AlertsCapture />

        </CaptureFrame>


        <CaptureFrame
          anchor={
            captureManifest[4].anchor
          }
          captureId={
            captureManifest[4].id
          }
          order={
            captureManifest[4].order
          }
          title={
            captureManifest[4].title
          }
          subtitle={
            captureManifest[4].subtitle
          }
          filename={
            captureManifest[4].recommendedFilename
          }
          icon={Database}
        >

          <OperationsCapture />

        </CaptureFrame>


        <CaptureFrame
          anchor={
            captureManifest[5].anchor
          }
          captureId={
            captureManifest[5].id
          }
          order={
            captureManifest[5].order
          }
          title={
            captureManifest[5].title
          }
          subtitle={
            captureManifest[5].subtitle
          }
          filename={
            captureManifest[5].recommendedFilename
          }
          icon={LockKeyhole}
        >

          <ArchitectureCapture />

        </CaptureFrame>

      </div>

    </main>

  );

}


function CaptureFrame({

  anchor,

  captureId,

  order,

  title,

  subtitle,

  filename,

  icon:
    Icon,

  children,

}: {

  anchor:
    string;

  captureId:
    string;

  order:
    number;

  title:
    string;

  subtitle:
    string;

  filename:
    string;

  icon:
    LucideIcon;

  children:
    ReactNode;

}) {

  return (

    <section
      id={
        anchor
      }
      data-capture-id={
        captureId
      }
      data-capture-width={
        CAPTURE_WIDTH
      }
      data-capture-height={
        CAPTURE_HEIGHT
      }
      className="
        mx-auto
        min-h-[900px]
        w-full
        max-w-[1440px]
        scroll-mt-6
        overflow-hidden
        rounded-[32px]
        border
        border-slate-200
        bg-[#f8fafc]
        shadow-xl
        shadow-slate-900/5
        print:min-h-screen
        print:max-w-none
        print:break-after-page
        print:rounded-none
        print:border-0
        print:shadow-none
      "
    >

      <div
        className="
          flex
          min-h-[900px]
          flex-col
        "
      >

        <header
          className="
            flex
            items-start
            justify-between
            gap-6
            border-b
            border-slate-200
            bg-white
            px-8
            py-6
            md:px-10
          "
        >

          <div
            className="
              flex
              min-w-0
              items-start
              gap-4
            "
          >

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
                    text-[10px]
                    font-semibold
                    uppercase
                    tracking-[0.12em]
                    text-[#2f6fed]
                  "
                >
                  TenderPilot · Capture
                  {" "}
                  0
                  {
                    order
                  }
                </span>


                <span
                  className="
                    rounded-full
                    bg-amber-50
                    px-2
                    py-0.5
                    text-[9px]
                    font-semibold
                    uppercase
                    tracking-[0.06em]
                    text-amber-700
                  "
                >
                  Synthetic
                </span>

              </div>


              <h1
                className="
                  mt-2
                  text-2xl
                  font-semibold
                  tracking-[-0.025em]
                  text-slate-950
                "
              >
                {
                  title
                }
              </h1>


              <p
                className="
                  mt-1
                  text-sm
                  text-slate-500
                "
              >
                {
                  subtitle
                }
              </p>

            </div>

          </div>


          <div
            className="
              hidden
              shrink-0
              text-right
              lg:block
            "
          >

            <p
              className="
                text-[9px]
                font-semibold
                uppercase
                tracking-[0.08em]
                text-slate-400
              "
            >
              Recommended filename
            </p>


            <p
              className="
                mt-1
                font-mono
                text-[10px]
                text-slate-500
              "
            >
              {
                filename
              }
            </p>

          </div>

        </header>


        <div
          className="
            flex-1
            p-8
            md:p-10
          "
        >
          {
            children
          }
        </div>


        <footer
          className="
            flex
            items-center
            justify-between
            gap-4
            border-t
            border-slate-200
            bg-white
            px-8
            py-3
            text-[9px]
            font-medium
            text-slate-400
            md:px-10
          "
        >

          <span>
            {
              CAPTURE_DATASET_ID
            }
          </span>


          <span>
            Synthetic non-production presentation data
          </span>

        </footer>

      </div>

    </section>

  );

}


function ExecutiveCapture() {

  return (

    <div>

      <div
        className="
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        <Metric
          label="High-potential matches"
          value={
            showcaseData.executive.highPotentialMatches
          }
          detail={
            `${showcaseData.executive.matchedOpportunities} total matches`
          }
          icon={Target}
        />


        <Metric
          label="GO portfolio"
          value={
            showcaseData.executive.goBids
          }
          detail={
            `${showcaseData.executive.holdBids} HOLD · ${showcaseData.executive.noGoBids} NO-GO`
          }
          icon={Scale}
        />


        <Metric
          label="Submission ready"
          value={
            showcaseData.executive.submissionReady
          }
          detail={
            `${showcaseData.executive.complianceReady} compliance ready`
          }
          icon={CheckCircle2}
        />


        <Metric
          label="Overdue work"
          value={
            showcaseData.executive.overdueTasks
          }
          detail={
            `${showcaseData.executive.criticalTasks} critical tasks`
          }
          icon={AlertOctagon}
        />

      </div>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-[1.15fr_0.85fr]
        "
      >

        <div
          className="
            rounded-3xl
            border
            border-slate-200
            bg-white
            p-6
          "
        >

          <p
            className="
              text-xs
              font-semibold
              uppercase
              tracking-[0.1em]
              text-[#2f6fed]
            "
          >
            Immediate portfolio priorities
          </p>


          <div
            className="
              mt-5
              space-y-3
            "
          >

            {
              showcaseData.alerts
                .slice(
                  0,
                  3,
                )
                .map(
                  (
                    alert,
                  ) => (

                    <div
                      key={
                        alert.id
                      }
                      className="
                        flex
                        items-start
                        gap-4
                        rounded-2xl
                        bg-slate-50
                        p-4
                      "
                    >

                      <AlertMarker
                        severity={
                          alert.severity
                        }
                      />


                      <div>

                        <p
                          className="
                            text-sm
                            font-semibold
                            text-slate-900
                          "
                        >
                          {
                            alert.title
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
                            alert.detail
                          }
                        </p>

                      </div>

                    </div>

                  ),
                )
            }

          </div>

        </div>


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
              tracking-[0.1em]
              text-blue-200
            "
          >
            30-day public market pulse
          </p>


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
            </p>

          </div>

        </div>

      </div>

    </div>

  );

}


function PipelineCapture() {

  return (

    <div
      className="
        overflow-hidden
        rounded-3xl
        border
        border-slate-200
        bg-white
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
              Task health
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

                  <td className="px-5 py-5">

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

                    <p
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
                    </p>


                    <p
                      className="
                        text-[9px]
                        font-semibold
                        text-blue-600
                      "
                    >
                      {
                        bid.grade
                      }
                    </p>

                  </td>


                  <td className="px-5 py-5">

                    <DecisionBadge
                      value={
                        bid.decision
                      }
                    />

                  </td>


                  <td className="px-5 py-5">

                    <p
                      className="
                        text-sm
                        font-semibold
                        text-slate-700
                      "
                    >
                      {
                        bid.compliance
                      }
                      %
                    </p>


                    <div
                      className="
                        mt-2
                        h-1.5
                        w-24
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

                          <p
                            className="
                              mt-1
                              font-semibold
                              text-red-600
                            "
                          >
                            {
                              bid.overdueTasks
                            }
                            {" "}
                            overdue
                          </p>

                        )

                        : null
                    }

                  </td>


                  <td className="px-5 py-5">

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
                      value={
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

  );

}


function MarketCapture() {

  return (

    <div
      className="
        grid
        gap-6
        xl:grid-cols-[1.05fr_0.95fr]
      "
    >

      <div
        className="
          rounded-3xl
          bg-[#14213d]
          p-7
          text-white
        "
      >

        <div
          className="
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
            label="Suppliers"
            value={
              showcaseData.market.observedSuppliers30Days
            }
          />

        </div>


        <div
          className="
            mt-7
            grid
            gap-5
            md:grid-cols-3
          "
        >

          <MarketSignal
            label="Buyer concentration"
            value={
              showcaseData.market.buyerConcentration
            }
          />


          <MarketSignal
            label="Supplier concentration"
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
            mt-8
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
              tracking-[0.08em]
              text-blue-200
            "
          >
            Leading CPV
          </p>


          <p
            className="
              mt-2
              text-2xl
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
            %
          </p>


          <div
            className="
              mt-6
              flex
              h-48
              items-end
              gap-3
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
                      `${index}-${value}`
                    }
                    className="
                      flex-1
                      rounded-t-lg
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

        <RankingCard
          title="Buyer intelligence"
          icon={Building2}
        >

          {
            showcaseData.buyers.map(
              (
                buyer,
              ) => (

                <RankingRow
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

        </RankingCard>


        <RankingCard
          title="Observed supplier landscape"
          icon={Factory}
        >

          {
            showcaseData.suppliers.map(
              (
                supplier,
              ) => (

                <RankingRow
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

        </RankingCard>

      </div>

    </div>

  );

}


function AlertsCapture() {

  return (

    <div
      className="
        grid
        gap-5
        md:grid-cols-2
      "
    >

      {
        showcaseData.alerts.map(
          (
            alert,
          ) => (

            <article
              key={
                alert.id
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
                  gap-4
                "
              >

                <AlertMarker
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
                      mt-2
                      text-base
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
                      text-sm
                      leading-6
                      text-slate-500
                    "
                  >
                    {
                      alert.detail
                    }
                  </p>

                </div>

              </div>

            </article>

          ),
        )
      }


      <div
        className="
          col-span-full
          rounded-3xl
          bg-[#14213d]
          p-7
          text-white
        "
      >

        <div
          className="
            flex
            items-start
            gap-4
          "
        >

          <ShieldCheck
            className="
              mt-1
              size-6
              shrink-0
              text-emerald-300
            "
          />


          <div>

            <h3
              className="
                text-lg
                font-semibold
              "
            >
              Human-controlled execution
            </h3>


            <p
              className="
                mt-2
                max-w-4xl
                text-sm
                leading-6
                text-slate-300
              "
            >
              Notifications surface deterministic risk and
              readiness signals. They never change GO/HOLD/NO-GO
              decisions, compliance verdicts, tasks or submission
              state.
            </p>

          </div>

        </div>

      </div>

    </div>

  );

}


function OperationsCapture() {

  return (

    <div>

      <div
        className="
          grid
          gap-5
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
                  p-7
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

                  <div>

                    <p
                      className="
                        text-2xl
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
                      Canonical procurement ingestion
                    </p>

                  </div>


                  <HealthBadge
                    value={
                      source.health
                    }
                  />

                </div>


                <div
                  className="
                    mt-7
                    grid
                    grid-cols-3
                    gap-3
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


      <div
        className="
          mt-6
          grid
          gap-4
          md:grid-cols-4
        "
      >

        <OperationalPrinciple
          title="Existing ingestion authority"
          detail="TED, BOAMP and DECP adapters remain the execution source of truth."
        />


        <OperationalPrinciple
          title="Shared corpus"
          detail="Source health describes shared public procurement ingestion."
        />


        <OperationalPrinciple
          title="Read-only monitoring"
          detail="Organization users cannot pause ingestion for other tenants."
        />


        <OperationalPrinciple
          title="Schema tolerant"
          detail="Operational monitoring adapts to available ingestion-run metadata."
        />

      </div>

    </div>

  );

}


function ArchitectureCapture() {

  return (

    <div>

      <div
        className="
          grid
          gap-5
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
                  p-7
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
                    text-lg
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
                    mt-3
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


      <div
        className="
          mt-6
          rounded-3xl
          bg-[#14213d]
          p-7
          text-white
        "
      >

        <div
          className="
            grid
            gap-5
            md:grid-cols-3
          "
        >

          <TrustBoundary
            title="PUBLIC"
            detail="Procurement corpus, buyer intelligence, supplier observations and market analytics."
          />


          <TrustBoundary
            title="PRIVATE"
            detail="Company profile, matching, decisions, documents, compliance, planning and notifications."
          />


          <TrustBoundary
            title="LOCAL AI"
            detail="Document analysis and RAG remain local and cannot make human bid decisions."
          />

        </div>

      </div>


      <div
        className="
          mt-6
          flex
          items-start
          gap-4
          rounded-3xl
          border
          border-blue-100
          bg-blue-50
          p-6
        "
      >

        <ShieldCheck
          className="
            mt-0.5
            size-5
            shrink-0
            text-blue-600
          "
        />


        <div>

          <p
            className="
              text-sm
              font-semibold
              text-blue-950
            "
          >
            Showcase trust guarantee
          </p>


          <p
            className="
              mt-1
              text-xs
              leading-5
              text-blue-800
            "
          >
            This capture route uses
            {" "}
            {
              SHOWCASE_DATASET_KIND
            }
            {" "}
            data only. It performs no TenderPilot API request,
            reads no live database and persists nothing.
          </p>

        </div>

      </div>

    </div>

  );

}


function Metric({

  label,

  value,

  detail,

  icon:
    Icon,

}: {

  label:
    string;

  value:
    number;

  detail:
    string;

  icon:
    LucideIcon;

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
        "
      >
        {
          value.toLocaleString()
        }
      </p>

    </div>

  );

}


function DecisionBadge({

  value,

}: {

  value:
    string;

}) {

  const classes =
    value ===
    "GO"
      ? "bg-emerald-50 text-emerald-700"
      : value ===
        "HOLD"
        ? "bg-amber-50 text-amber-700"
        : value ===
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
          ${classes}
        `
      }
    >
      {
        value.replace(
          "_",
          "-",
        )
      }
    </span>

  );

}


function ReadinessBadge({

  value,

  label,

}: {

  value:
    string;

  label:
    string;

}) {

  const classes =
    value ===
    "READY"
      ? "bg-emerald-50 text-emerald-700"
      : value ===
        "AT_RISK"
        ? "bg-red-50 text-red-700"
        : value ===
          "REVIEW"
          ? "bg-amber-50 text-amber-700"
          : value ===
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
          ${classes}
        `
      }
    >
      {
        label
      }
    </span>

  );

}


function AlertMarker({

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
        "
      >
        {
          value
        }
      </p>

    </div>

  );

}


function RankingCard({

  title,

  icon:
    Icon,

  children,

}: {

  title:
    string;

  icon:
    LucideIcon;

  children:
    ReactNode;

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
            text-base
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


function RankingRow({

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


function HealthBadge({

  value,

}: {

  value:
    string;

}) {

  const classes =
    value ===
    "HEALTHY"
      ? "bg-emerald-50 text-emerald-700"
      : value ===
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
          ${classes}
        `
      }
    >
      {
        value
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


function OperationalPrinciple({

  title,

  detail,

}: {

  title:
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

      <p
        className="
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

    </div>

  );

}


function TrustBoundary({

  title,

  detail,

}: {

  title:
    string;

  detail:
    string;

}) {

  return (

    <div
      className="
        rounded-2xl
        bg-white/10
        p-5
      "
    >

      <p
        className="
          text-[10px]
          font-semibold
          tracking-[0.1em]
          text-blue-200
        "
      >
        {
          title
        }
      </p>


      <p
        className="
          mt-3
          text-sm
          leading-6
          text-slate-200
        "
      >
        {
          detail
        }
      </p>

    </div>

  );

}
