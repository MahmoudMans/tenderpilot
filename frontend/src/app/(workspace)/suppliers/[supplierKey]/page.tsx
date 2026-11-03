"use client";

import { useRecordVisit } from "@/features/navigation/recent";


import {

  ArrowLeft,

  Building2,

  CircleDollarSign,

  Factory,

  LoaderCircle,

  Repeat2,

  ShieldCheck,

  Target,

  Users,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


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

} from "@/features/procurement/procurement-format";


import type {

  SupplierIntelligenceDetail,

} from "@/features/supplier-intelligence/supplier-intelligence-types";


export default function SupplierDetailPage() {

  const params =
    useParams<{
      supplierKey:
        string;
    }>();


  const {

    api,

  } = useAuth();


  const [

    detail,

    setDetail,

  ] = useState<
    SupplierIntelligenceDetail
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
          href: `/suppliers/${detail.summary.supplierKey}`,
          title: detail.summary.name,
          kind: "supplier",
        }
      : null,
  );

  useEffect(
    () => {

      let cancelled =
        false;


      api<SupplierIntelligenceDetail>(
        `/api/v1/suppliers/${params.supplierKey}`,
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
                  : "Unable to load supplier intelligence.",
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
      params.supplierKey,
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
          href="/suppliers"
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

          Supplier intelligence

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
            "Supplier intelligence is unavailable."
          }
        </div>

      </div>

    );

  }


  const summary =
    detail.summary;


  return (

    <div
      className="
        mx-auto
        max-w-[1450px]
      "
    >

      <Link
        href="/suppliers"
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

        All suppliers

      </Link>


      <div
        className="
          mt-6
        "
      >

        <Factory
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
          Public award-history intelligence
        </p>

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
          icon={CircleDollarSign}
          label="Observed awards"
          value={
            summary.awardCount
              .toLocaleString()
          }
          detail={
            `${summary.uniqueBuyerCount} public buyers`
          }
        />


        <Metric
          icon={Building2}
          label="Buyer concentration"
          value={
            formatEnum(
              summary.buyerConcentration,
            )
          }
          detail={
            `${summary.topBuyerSharePercentage}% top-buyer share`
          }
        />


        <Metric
          icon={Target}
          label="CPV profile"
          value={
            formatEnum(
              summary.cpvSpecialization,
            )
          }
          detail={
            `${summary.topCpvSharePercentage}% top-CPV share`
          }
        />


        <Metric
          icon={Users}
          label="Geographic footprint"
          value={
            summary.uniqueCountryCount
              .toLocaleString()
          }
          detail="Observed buyer countries"
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

          <Repeat2
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
            Award behavior
          </h2>


          <dl
            className="
              mt-5
              space-y-4
            "
          >

            <Row
              label="Repeat buyer signal"
              value={
                summary.repeatBuyerSignal
                  ? "Detected"
                  : "Not detected"
              }
            />


            <Row
              label="Top buyer share"
              value={
                `${summary.topBuyerSharePercentage}%`
              }
            />


            <Row
              label="Buyer HHI"
              value={
                summary.buyerHhi
                  .toLocaleString()
              }
            />


            <Row
              label="CPV specialization"
              value={
                formatEnum(
                  summary.cpvSpecialization,
                )
              }
            />


            <Row
              label="Latest observed publication"
              value={
                summary.lastObservedPublicationDate
                  ? formatDate(
                      summary.lastObservedPublicationDate,
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
            Associated award value
          </h2>


          <p
            className="
              mt-5
              text-3xl
              font-semibold
              text-slate-950
            "
          >
            {
              summary.associatedAwardValue !==
                null
              &&
              summary.awardCurrency
              &&
              summary.awardCurrency !==
                "MIXED"
                ? formatMoney(
                    summary.associatedAwardValue,
                    summary.awardCurrency,
                  )
                : "Mixed / unavailable"
            }
          </p>


          {
            summary.averageAssociatedAwardValue !==
              null
            &&
            summary.awardCurrency
            &&
            summary.awardCurrency !==
              "MIXED"

              ? (

                <p
                  className="
                    mt-2
                    text-sm
                    text-slate-500
                  "
                >
                  Average:
                  {" "}
                  {
                    formatMoney(
                      summary.averageAssociatedAwardValue,
                      summary.awardCurrency,
                    )
                  }
                </p>

              )

              : null
          }


          <div
            className="
              mt-5
              rounded-2xl
              bg-amber-50
              p-4
              text-xs
              leading-5
              text-amber-800
            "
          >
            {
              detail.valueInterpretation
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
            Frequent public buyers
          </h2>


          <div
            className="
              mt-5
              space-y-3
            "
          >

            {
              detail.topBuyers.map(
                (
                  buyer,
                ) => (

                  <div
                    key={
                      buyer.buyerId
                      ??
                      buyer.name
                      ??
                      `${buyer.countryCode}-${buyer.city}`
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
                          text-slate-900
                        "
                      >
                        {
                          buyer.name
                          ??
                          "Unknown buyer"
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
                          buyer.sharePercentage
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
                        buyer.awardCount
                      }
                      {" "}
                      observed awards
                      {
                        buyer.countryCode
                          ? ` · ${buyer.countryCode}`
                          : ""
                      }
                      {
                        buyer.city
                          ? ` · ${buyer.city}`
                          : ""
                      }
                    </p>

                  </div>

                ),
              )
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
            CPV specialization
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
          Co-award relationships
        </h2>


        <p
          className="
            mt-2
            text-xs
            leading-5
            text-slate-500
          "
        >
          Other suppliers appearing on the same public
          award records.
        </p>


        <div
          className="
            mt-5
            grid
            gap-3
            md:grid-cols-2
          "
        >

          {
            detail.coAwardSuppliers.map(
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
                      supplier.name
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
                      supplier.sharedAwardCount
                    }
                    {" "}
                    shared awards
                    {" · "}
                    {
                      supplier.sharePercentage
                    }
                    %
                  </p>

                </Link>

              ),
            )
          }


          {
            detail.coAwardSuppliers.length ===
            0

              ? (

                <p
                  className="
                    col-span-full
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
                  No co-award supplier relationships
                  were observed.
                </p>

              )

              : null
          }

        </div>


        <div
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
            detail.competitiveLandscapeInterpretation
          }
        </div>

      </section>


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
          Recent observed awards
        </h2>


        <div
          className="
            mt-5
            space-y-3
          "
        >

          {
            detail.recentAwards.map(
              (
                award,
              ) => (

                <Link
                  key={
                    award.awardId
                    ??
                    award.tenderId
                    ??
                    `${award.buyerName ?? "buyer"}-${award.publicationDate ?? "date"}-${award.tenderTitle ?? "award"}`
                  }
                  href={
                    award.tenderId
                      ? `/opportunities/${award.tenderId}`
                      : "/opportunities"
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
                      gap-3
                      md:flex-row
                      md:items-start
                      md:justify-between
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
                          award.tenderTitle
                          ??
                          "Public award"
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
                          award.buyerName
                          ??
                          "Unknown buyer"
                        }
                        {
                          award.buyerCountryCode
                            ? ` · ${award.buyerCountryCode}`
                            : ""
                        }
                        {
                          award.publicationDate
                            ? ` · ${formatDate(
                                award.publicationDate,
                              )}`
                            : ""
                        }
                      </p>

                    </div>


                    <div
                      className="
                        text-left
                        md:text-right
                      "
                    >

                      {
                        award.associatedAwardValue !==
                          null
                        &&
                        award.awardCurrency

                          ? (

                            <p
                              className="
                                text-sm
                                font-semibold
                                text-slate-900
                              "
                            >
                              {
                                formatMoney(
                                  award.associatedAwardValue,
                                  award.awardCurrency,
                                )
                              }
                            </p>

                          )

                          : null
                      }


                      {
                        award.jointAward

                          ? (

                            <span
                              className="
                                mt-2
                                inline-block
                                rounded-full
                                bg-blue-50
                                px-2.5
                                py-1
                                text-[10px]
                                font-semibold
                                text-blue-700
                              "
                            >
                              Joint award record
                            </span>

                          )

                          : null
                      }

                    </div>

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

        TenderPilot reports observed public award history.
        A supplier appearing here does not imply that it
        competed in every related procurement, and the
        absence of a supplier does not imply it never
        competed.

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
