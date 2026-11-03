"use client";

import { useRecordVisit } from "@/features/navigation/recent";
import { WatchButton } from "@/features/notifications/watch-button";
import { SupplierLink } from "@/features/supplier-intelligence/supplier-link";


import {

  ArrowLeft,

  Building2,

  CalendarClock,

  CircleDollarSign,

  Clock3,

  FileText,

  LoaderCircle,

  Repeat2,

  ShieldCheck,

  Users,

} from "lucide-react";


import type { LucideIcon } from "lucide-react";


import Link from "next/link";


import {

  useEffect,

  useState,

} from "react";


import {

  useParams,

} from "next/navigation";


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

  BuyerIntelligenceDetail,

} from "@/features/buyer-intelligence/buyer-intelligence-types";


const MONTHS = [

  "Jan",

  "Feb",

  "Mar",

  "Apr",

  "May",

  "Jun",

  "Jul",

  "Aug",

  "Sep",

  "Oct",

  "Nov",

  "Dec",

];


export default function BuyerDetailPage() {

  const params =
    useParams<{
      buyerId:
        string;
    }>();


  const {

    api,

  } = useAuth();


  const [

    detail,

    setDetail,

  ] = useState<
    BuyerIntelligenceDetail
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


  useRecordVisit(
    detail
      ? {
          href: `/buyers/${detail.summary.buyerId}`,
          title: detail.summary.name,
          kind: "buyer",
        }
      : null,
  );

  useEffect(
    () => {

      let cancelled =
        false;


      api<BuyerIntelligenceDetail>(
        `/api/v1/buyers/${params.buyerId}`,
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


            setDetail(
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
                  : "Unable to load buyer intelligence.",
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
      params.buyerId,
    ],
  );


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


  if (
    detail ===
    null
  ) {

    return (

      <div>

        <Link
          href="/buyers"
          className="
            inline-flex
            items-center
            gap-2
            text-sm
            font-semibold
            text-slate-600
          "
        >

          <ArrowLeft
            className="size-4"
          />

          Buyer intelligence

        </Link>


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
            "Buyer intelligence is unavailable."
          }
        </div>

      </div>

    );

  }


  const summary =
    detail.summary;


  const maximumMonth =
    Math.max(
      1,
      ...detail.monthlyActivity.map(
        (
          item,
        ) =>
          item.noticeCount,
      ),
    );


  return (

    <div
      className="
        mx-auto
        max-w-[1450px]
      "
    >

      <Link
        href="/buyers"
        className="
          inline-flex
          items-center
          gap-2
          text-sm
          font-semibold
          text-slate-600
        "
      >

        <ArrowLeft
          className="size-4"
        />

        All buyers

      </Link>


      <div
        className="
          mt-6
        "
      >

        <Building2
          className="
            size-6
            text-[#2f6fed]
          "
        />


        <h1
          className="
            mt-4
            text-4xl
            font-semibold
            tracking-[-0.03em]
            text-slate-950
          "
        >
          {
            summary.name
          }
        </h1>


        <p
          className="
            mt-3
            text-sm
            text-slate-500
          "
        >
          {
            summary.category
              ? formatEnum(
                  summary.category,
                )
              : "Public buyer"
          }
          {
            summary.countryCode
              ? ` · ${summary.countryCode}`
              : ""
          }
          {
            summary.city
              ? ` · ${summary.city}`
                            : ""
          }
        </p>
        <div
          data-print-hide
          className="
            mt-4
          "
        >
          <WatchButton
            type="BUYER"
            targetKey={
              summary.buyerId
            }
            label={
              summary.name
            }
            size="md"
          />
        </div>
      </div>
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
          icon={FileText}
          label="Procurement notices"
          value={
            summary.noticeCount
              .toLocaleString()
          }
          detail={
            `${summary.activeNoticeCount} currently active`
          }
        />


        <Metric
          icon={CircleDollarSign}
          label="Recorded awards"
          value={
            summary.awardCount
              .toLocaleString()
          }
          detail={
            summary.totalAwardedValue !==
              null
            &&
            summary.awardCurrency
            &&
            summary.awardCurrency !==
              "MIXED"
              ? formatMoney(
                  summary.totalAwardedValue,
                  summary.awardCurrency,
                )
              : "Award corpus dependent"
          }
        />


        <Metric
          icon={Clock3}
          label="Median response"
          value={
            summary.medianResponseDays ===
            null
              ? "—"
              : `${summary.medianResponseDays} days`
          }
          detail={
            formatEnum(
              summary.deadlineTempo,
            )
          }
        />


        <Metric
          icon={Users}
          label="Supplier concentration"
          value={
            formatEnum(
              summary.supplierConcentration,
            )
          }
          detail={
            summary.awardCount ===
            0
              ? "No supplier history"
              : `${summary.topSupplierSharePercentage}% top supplier share`
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

          <div
            className="
              flex
              items-center
              gap-3
            "
          >

            <Repeat2
              className="
                size-5
                text-[#2f6fed]
              "
            />


            <div>

              <h2
                className="
                  font-semibold
                  text-slate-950
                "
              >
                Procurement behavior
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Deterministic signals derived from public history.
              </p>

            </div>

          </div>


          <dl
            className="
              mt-6
              space-y-4
            "
          >

            <Row
              label="Recurring procurement"
              value={
                summary.recurringProcurement
                  ? "Detected"
                  : "Not detected"
              }
            />


            <Row
              label="Top CPV share"
              value={
                `${summary.topCpvSharePercentage}%`
              }
            />


            <Row
              label="Top supplier share"
              value={
                `${summary.topSupplierSharePercentage}%`
              }
            />


            <Row
              label="Supplier HHI"
              value={
                summary.supplierHhi
                  .toLocaleString()
              }
            />


            <Row
              label="Average estimated value"
              value={
                summary.averageEstimatedValue !==
                  null
                &&
                summary.estimatedCurrency
                &&
                summary.estimatedCurrency !==
                  "MIXED"
                  ? formatMoney(
                      summary.averageEstimatedValue,
                      summary.estimatedCurrency,
                    )
                  : "Mixed / unavailable"
              }
            />


            <Row
              label="Last publication"
              value={
                summary.lastPublicationDate
                  ? formatDate(
                      summary.lastPublicationDate,
                    )
                  : "Unavailable"
              }
            />

          </dl>

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

          <div
            className="
              flex
              items-center
              gap-3
            "
          >

            <CalendarClock
              className="
                size-5
                text-[#2f6fed]
              "
            />


            <div>

              <h2
                className="
                  font-semibold
                  text-slate-950
                "
              >
                Publication cadence
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Historical notice activity by calendar month.
              </p>

            </div>

          </div>


          <div
            className="
              mt-6
              space-y-3
            "
          >

            {
              detail.monthlyActivity.map(
                (
                  item,
                ) => (

                  <div
                    key={
                      item.month
                    }
                    className="
                      grid
                      grid-cols-[38px_1fr_30px]
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
                        MONTHS[
                          item.month - 1
                        ]
                      }
                    </span>


                    <div
                      className="
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
                            `${(
                              item.noticeCount
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
                        item.noticeCount
                      }
                    </span>

                  </div>

                ),
              )
            }

          </div>

        </section>

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

          <h2
            className="
              font-semibold
              text-slate-950
            "
          >
            Frequent CPVs
          </h2>


          <div
            className="
              mt-5
              space-y-3
            "
          >

            {
              detail.topCpvs.map(
                (
                  cpv,
                ) => (

                  <div
                    key={
                      cpv.code
                    }
                    className="
                      rounded-2xl
                      bg-slate-50
                      p-4
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

                      <p
                        className="
                          font-mono
                          text-xs
                          font-semibold
                          text-slate-900
                        "
                      >
                        {
                          cpv.code
                        }
                      </p>


                      <span
                        className="
                          text-xs
                          font-semibold
                          text-[#2f6fed]
                        "
                      >
                        {
                          cpv.sharePercentage
                        }
                        %
                      </span>

                    </div>


                    {
                      cpv.description

                        ? (

                          <p
                            className="
                              mt-2
                              text-xs
                              leading-5
                              text-slate-500
                            "
                          >
                            {
                              cpv.description
                            }
                          </p>

                        )

                        : null
                    }


                    <p
                      className="
                        mt-2
                        text-[10px]
                        text-slate-400
                      "
                    >
                      {
                        cpv.occurrenceCount
                      }
                      {" "}
                      occurrences
                    </p>

                  </div>

                ),
              )
            }


            {
              detail.topCpvs.length ===
              0

                ? (

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
                    No CPV history available.
                  </p>

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
            Frequent suppliers
          </h2>


          <div
            className="
              mt-5
              space-y-3
            "
          >

            {
              detail.topSuppliers.map(
                (
                  supplier,
                ) => (

                  <div
                    key={
                      supplier.name
                    }
                    className="
                      rounded-2xl
                      bg-slate-50
                      p-4
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

                      <p
                        className="
                          text-sm
                          font-semibold
                          text-slate-900 " > <SupplierLink name={supplier.name} /> </p>


                      <span
                        className="
                          text-xs
                          font-semibold
                          text-[#2f6fed]
                        "
                      >
                        {
                          supplier.sharePercentage
                        }
                        %
                      </span>

                    </div>


                    <p
                      className="
                        mt-2
                        text-xs
                        text-slate-400
                      "
                    >
                      {
                        supplier.awardCount
                      }
                      {" "}
                      recorded awards
                    </p>

                  </div>

                ),
              )
            }


            {
              detail.topSuppliers.length ===
              0

                ? (

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
                    No supplier award history is available
                    for this buyer in the current corpus.
                  </p>

                )

                : null
            }

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

        <h2
          className="
            font-semibold
            text-slate-950
          "
        >
          Recent procurement
        </h2>


        <div
          className="
            mt-5
            space-y-3
          "
        >

          {
            detail.recentTenders.map(
              (
                tender,
              ) => (

                <Link
                  key={
                    tender.tenderId
                  }
                  href={
                    `/opportunities/${tender.tenderId}`
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

                  <p
                    className="
                      text-sm
                      font-semibold
                      text-slate-900
                    "
                  >
                    {
                      tender.title
                    }
                  </p>


                  <div
                    className="
                      mt-3
                      flex
                      flex-wrap
                      gap-x-5
                      gap-y-2
                      text-xs
                      text-slate-400
                    "
                  >

                    {
                      tender.status

                        ? (

                          <span>
                            {
                              formatEnum(
                                tender.status,
                              )
                            }
                          </span>

                        )

                        : null
                    }


                    {
                      tender.publicationDate

                        ? (

                          <span>
                            Published
                            {" "}
                            {
                              formatDate(
                                tender.publicationDate,
                              )
                            }
                          </span>

                        )

                        : null
                    }


                    <span>
                      {
                        formatValueRange(
                          tender.estimatedMinValue,
                          tender.estimatedMaxValue,
                          tender.currency,
                        )
                      }
                    </span>

                  </div>

                </Link>

              ),
            )
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

        Buyer intelligence is descriptive, not predictive
        certainty. Signals depend on the coverage and quality
        of the normalized public procurement corpus.

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
          text-lg
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


function Row({

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
        border-slate-100
        pb-3
        last:border-0
        last:pb-0
      "
    >

      <dt
        className="
          text-sm
          text-slate-500
        "
      >
        {
          label
        }
      </dt>


      <dd
        className="
          text-right
          text-sm
          font-semibold
          text-slate-900
        "
      >
        {
          value
        }
      </dd>

    </div>

  );

}
