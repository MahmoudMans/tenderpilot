"use client";

import { useRecordVisit } from "@/features/navigation/recent";
import { OpportunityQuickActions } from "@/features/procurement/opportunity-quick-actions";
import { ButtonLink } from "@/components/ui/button";
import { WatchButton } from "@/features/notifications/watch-button";
import { linkWithParams } from "@/features/navigation/url-filters";
import { SupplierLink } from "@/features/supplier-intelligence/supplier-link";


import {

  ArrowLeft,

  Building2,

  CalendarDays,

  CheckCircle2,

  Database,

  ExternalLink,

  FileText,

  Layers3,

  LoaderCircle,

  MapPinned,

  ShieldCheck,

  WalletCards,

} from "lucide-react";


import Link
  from "next/link";


import {

  useParams,

} from "next/navigation";


import {

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

  formatMoney,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  OpportunityDetail,

} from "@/features/procurement/procurement-types";


export default function OpportunityDetailPage() {

  const params =
    useParams<{
      id:
        string;
    }>();


  const {

    api,

  } = useAuth();


  const [

    opportunity,

    setOpportunity,

  ] = useState<
    OpportunityDetail
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


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  useRecordVisit(
    opportunity
      ? {
          href: `/opportunities/${opportunity.id}`,
          title: opportunity.title,
          subtitle: opportunity.buyer.legalName,
          kind: "opportunity",
        }
      : null,
  );

  useEffect(
    () => {

      let cancelled =
        false;


      api<OpportunityDetail>(

        `/api/v1/opportunities/${params.id}`,

      )
        .then(
          (
            result,
          ) => {

            if (
              !cancelled
            ) {

              setOpportunity(
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

                  : "Unable to load opportunity.",

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
      params.id,
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
    opportunity ===
    null
  ) {

    return (

      <div
        className="
          mx-auto
          max-w-5xl
        "
      >

        <Link
          href="/opportunities"
          className="
            inline-flex
            items-center
            gap-2
            text-sm
            font-semibold
            text-[#2f6fed]
          "
        >

          <ArrowLeft
            className="
              size-4
            "
          />

          Opportunities

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
            "Opportunity unavailable."
          }

        </div>

      </div>

    );

  }


  return (

    <div
      className="
        mx-auto
        max-w-[1400px]
      "
    >

      <Link
        href="/opportunities"
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

        <ArrowLeft
          className="
            size-4
          "
        />

        Back to opportunities

      </Link>


      <div
        className="
          mt-6
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
            max-w-4xl
          "
        >

          <div
            className="
              flex
              flex-wrap
              gap-2
            "
          >

            <Badge>

              {
                formatEnum(
                  opportunity.status,
                )
              }

            </Badge>


            <Badge>

              {
                formatEnum(
                  opportunity.contractNature,
                )
              }

            </Badge>


            <Badge>

              {
                formatEnum(
                  opportunity.procedureType,
                )
              }

            </Badge>

          </div>


          <h1
            className="
              mt-5
              text-3xl
              font-semibold
              leading-tight
              tracking-[-0.03em]
              text-slate-950
              md:text-4xl
            "
          >

            {
              opportunity.title
            }

          </h1>


          <p
            className="
              mt-4
              max-w-3xl
              text-sm
              leading-7
              text-slate-600
            "
          >

            {
              opportunity.description
            }

          </p>

          <OpportunityQuickActions
            id={opportunity.id}
            title={opportunity.title}
            buyerId={opportunity.buyer.id}
            buyerName={opportunity.buyer.legalName}
            deadline={opportunity.submissionDeadline}
          />

        </div>


        <div
          className="
            rounded-2xl
            border
            border-blue-200
            bg-blue-50
            px-4
            py-3
          "
        >

          <div
            className="
              flex
              items-center
              gap-2
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

            Canonical record

          </div>


          <p
            className="
              mt-1
              max-w-64
              break-all
              text-[10px]
              text-blue-600/70
            "
          >

            {
              opportunity.canonicalKey
            }

          </p>

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

          icon={
            Building2
          }

          label="Buyer"

          value={
            opportunity.buyer.legalName
          }

          detail={
            formatEnum(
              opportunity.buyer.category,
            )
          }

        />


        <Metric

          icon={
            WalletCards
          }

          label="Estimated value"

          value={
            formatValueRange(

              opportunity.estimatedMinValue,

              opportunity.estimatedMaxValue,

              opportunity.currency,

            )
          }

          detail={
            opportunity.currency
            ??
            "Not disclosed"
          }

        />


        <Metric

          icon={
            CalendarDays
          }

          label="Submission deadline"

          value={
            formatDateTime(
              opportunity.submissionDeadline,
            )
          }

          detail={
            `Published ${formatDate(opportunity.publicationDate)}`
          }

        />


        <Metric

          icon={
            MapPinned
          }

          label="Primary geography"

          value={
            opportunity.primaryRegionName
            ??
            opportunity.countryCode
          }

          detail={
            opportunity.primaryRegionCode
            ??
            opportunity.countryCode
          }

        />

      </div>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-[1fr_0.9fr]
        "
      >

        <Section

          icon={
            FileText
          }

          title="CPV classifications"

        >

          <div
            className="
              space-y-2
            "
          >

            {
              opportunity.cpvs.map(
                (
                  cpv,
                ) => (

                  <div
                    key={
                      cpv.code
                    }
                    className="
                      flex
                      items-start
                      justify-between
                      gap-3
                      rounded-xl
                      border
                      border-slate-100
                      px-4
                      py-3
                    "
                  >
                    <div
                      className="
                        min-w-0
                      "
                    >
                      <p
                        className="
                          text-xs
                          font-semibold
                          text-[#2f6fed]
                        "
                      >
                        {
                          cpv.code
                        }
                      </p>
                      <p
                        className="
                          mt-1
                          text-sm
                          text-slate-700
                        "
                      >
                        {
                          cpv.label
                        }
                      </p>
                    </div>
                    <span
                      data-print-hide
                      className="flex shrink-0 items-center gap-1"
                    >
                      <ButtonLink
                        href={linkWithParams("/search", { q: cpv.code })}
                        variant="ghost"
                        size="sm"
                        title="Find similar opportunities"
                      >
                        Similar
                      </ButtonLink>
                      <WatchButton
                        type="CPV"
                        targetKey={
                          cpv.code
                        }
                        label={`${cpv.code} ${cpv.label}`}
                        compact
                      />
                    </span>
                  </div>

                ),
              )
            }

          </div>

        </Section>


        <Section

          icon={
            Building2
          }

          title="Contracting authority"

        >

          <dl
            className="
              space-y-4
            "
          >

            <Definition
              label="Legal name"
              value={
                opportunity.buyer.legalName
              }
            />


            <Definition
              label="Category"
              value={
                formatEnum(
                  opportunity.buyer.category,
                )
              }
            />


            <Definition
              label="Location"
              value={
                [
                  opportunity.buyer.city,
                  opportunity.buyer.countryCode,
                ]
                  .filter(
                    Boolean,
                  )
                  .join(
                    ", ",
                  )
              }
            />


            <Definition
              label="Registration"
              value={
                opportunity.buyer.registrationNumber
                ??
                "Not provided"
              }
            />
          </dl>
          <div
            data-print-hide
            className="
              mt-5
              flex
              flex-wrap
              gap-2
            "
          >
            <ButtonLink
              href={`/buyers/${opportunity.buyer.id}`}
              size="sm"
            >
              Buyer intelligence
            </ButtonLink>
            <WatchButton
              type="BUYER"
              targetKey={
                opportunity.buyer.id
              }
              label={
                opportunity.buyer.legalName
              }
            />
          </div>

        </Section>

      </div>


      {
        opportunity.quality

          ? (

            <Section

              icon={
                ShieldCheck
              }

              title="Data quality assessment"

              className="
                mt-6
              "

            >

              <div
                className="
                  grid
                  gap-4
                  md:grid-cols-4
                "
              >

                <Definition
                  label="Quality score"
                  value={
                    `${opportunity.quality.score}/100`
                  }
                />

                <Definition
                  label="Grade"
                  value={
                    formatEnum(
                      opportunity.quality.grade,
                    )
                  }
                />

                <Definition
                  label="Critical issues"
                  value={
                    opportunity.quality
                      .criticalIssueCount
                      .toString()
                  }
                />

                <Definition
                  label="Warnings"
                  value={
                    opportunity.quality
                      .warningIssueCount
                      .toString()
                  }
                />

              </div>


              {
                opportunity.quality.issues.length >
                0

                  ? (

                    <div
                      className="
                        mt-6
                        space-y-3
                      "
                    >

                      {
                        opportunity.quality.issues.map(
                          (
                            issue,
                            index,
                          ) => (

                            <div

                              key={
                                `${issue.code}-${issue.fieldName}-${index}`
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
                                  flex-wrap
                                  items-center
                                  gap-2
                                "
                              >

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
                                      issue.severity,
                                    )
                                  }
                                </span>


                                <span
                                  className="
                                    text-xs
                                    font-semibold
                                    text-slate-800
                                  "
                                >
                                  {
                                    formatEnum(
                                      issue.code,
                                    )
                                  }
                                </span>

                              </div>


                              <p
                                className="
                                  mt-3
                                  text-sm
                                  leading-6
                                  text-slate-600
                                "
                              >
                                {
                                  issue.message
                                }
                              </p>


                              <p
                                className="
                                  mt-2
                                  text-[11px]
                                  text-slate-400
                                "
                              >
                                Field:
                                {" "}
                                {
                                  issue.fieldName
                                }
                              </p>

                            </div>

                          ),
                        )
                      }

                    </div>

                  )

                  : (

                    <div
                      className="
                        mt-6
                        rounded-2xl
                        border
                        border-emerald-200
                        bg-emerald-50
                        p-4
                        text-sm
                        font-medium
                        text-emerald-800
                      "
                    >
                      No data-quality issues detected.
                    </div>

                  )
              }

            </Section>

          )

          : null
      }


      {
        opportunity.award

          ? (

            <Section

              icon={
                CheckCircle2
              }

              title="Contract award"

              className="
                mt-6
              "

            >

              <div
                className="
                  grid
                  gap-4
                  md:grid-cols-2
                  xl:grid-cols-4
                "
              >

                <Definition
                  label="Awarded amount"
                  value={
                    opportunity.award.awardedAmount ===
                    null

                      ? "Not disclosed"

                      : formatMoney(
                          opportunity.award.awardedAmount,
                          opportunity.award.currency,
                        )
                  }
                />

                <Definition
                  label="Notification date"
                  value={
                    formatDate(
                      opportunity.award.notificationDate,
                    )
                  }
                />

                <Definition
                  label="Duration"
                  value={
                    opportunity.award.durationMonths ===
                    null

                      ? "Not disclosed"

                      : `${opportunity.award.durationMonths} months`
                  }
                />

                <Definition
                  label="Offers received"
                  value={
                    opportunity.award.offersReceived ===
                    null

                      ? "Not disclosed"

                      : opportunity.award.offersReceived.toString()
                  }
                />

              </div>


              {
                opportunity.award.suppliers.length >
                0

                  ? (

                    <div
                      className="
                        mt-7
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

                        Awarded suppliers

                      </p>


                      <div
                        className="
                          mt-3
                          grid
                          gap-3
                          md:grid-cols-2
                        "
                      >

                        {
                          opportunity.award.suppliers.map(
                            (
                              supplier,
                              index,
                            ) => (

                              <div

                                key={
                                  `${supplier.identifier}-${supplier.legalName}-${index}`
                                }

                                className="
                                  rounded-2xl
                                  border
                                  border-slate-200
                                  p-4
                                "

                              >

                                <p
                                  className="
                                    text-sm
                                    font-semibold
                                    text-slate-900
                                  "
                                >

                                  {supplier.legalName ? <SupplierLink name={supplier.legalName} /> : (supplier.identifier ?? "Awarded supplier")}

                                </p>


                                <p
                                  className="
                                    mt-1
                                    text-xs
                                    text-slate-500
                                  "
                                >

                                  {
                                    [
                                      supplier.identifierType,
                                      supplier.identifier,
                                    ]
                                      .filter(Boolean)
                                      .join(" · ")
                                  }

                                </p>

                              </div>

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
                  mt-7
                  grid
                  gap-4
                  md:grid-cols-2
                "
              >

                <Definition
                  label="Price form"
                  value={
                    opportunity.award.priceForm
                    ??
                    "Not disclosed"
                  }
                />

                <Definition
                  label="Framework agreement"
                  value={
                    opportunity.award.frameworkAgreementId
                    ??
                    "Not specified"
                  }
                />

                <Definition
                  label="Social considerations"
                  value={
                    opportunity.award.socialConsiderations
                    ??
                    "Not disclosed"
                  }
                />

                <Definition
                  label="Environmental considerations"
                  value={
                    opportunity.award.environmentalConsiderations
                    ??
                    "Not disclosed"
                  }
                />

              </div>

            </Section>

          )

          : null
      }


      <Section

        icon={
          MapPinned
        }

        title="Places of performance"

        className="
          mt-6
        "

      >

        {
          opportunity.locations.length ===
          0

            ? (

              <EmptyText>

                No structured place-of-performance data.

              </EmptyText>

            )

            : (

              <div
                className="
                  grid
                  gap-3
                  md:grid-cols-2
                  xl:grid-cols-3
                "
              >

                {
                  opportunity.locations.map(
                    (
                      location,
                      index,
                    ) => (

                      <div
                        key={
                          `${location.countryCode}-${location.regionCode}-${location.city}-${index}`
                        }
                        className="
                          rounded-2xl
                          bg-slate-50
                          p-4
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
                            location.city
                            ??
                            location.regionName
                            ??
                            location.countryCode
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
                            [
                              location.regionName,
                              location.regionCode,
                              location.countryCode,
                            ]
                              .filter(
                                Boolean,
                              )
                              .join(
                                " · ",
                              )
                          }

                        </p>

                      </div>

                    ),
                  )
                }

              </div>

            )
        }

      </Section>


      <Section

        icon={
          Layers3
        }

        title="Tender lots"

        className="
          mt-6
        "

      >

        {
          opportunity.lots.length ===
          0

            ? (

              <EmptyText>

                This notice is represented as a single contract.

              </EmptyText>

            )

            : (

              <div
                className="
                  grid
                  gap-4
                  xl:grid-cols-2
                "
              >

                {
                  opportunity.lots.map(
                    (
                      lot,
                    ) => (

                      <article
                        key={
                          lot.id
                        }
                        className="
                          rounded-2xl
                          border
                          border-slate-200
                          p-5
                        "
                      >

                        <p
                          className="
                            text-[10px]
                            font-semibold
                            uppercase
                            tracking-[0.14em]
                            text-[#2f6fed]
                          "
                        >

                          Lot
                          {" "}
                          {
                            lot.lotNumber
                          }

                        </p>


                        <h3
                          className="
                            mt-2
                            text-base
                            font-semibold
                            text-slate-950
                          "
                        >

                          {
                            lot.title
                          }

                        </h3>


                        <p
                          className="
                            mt-3
                            text-sm
                            leading-6
                            text-slate-600
                          "
                        >

                          {
                            lot.description
                          }

                        </p>


                        <p
                          className="
                            mt-4
                            text-sm
                            font-semibold
                            text-slate-800
                          "
                        >

                          {
                            formatValueRange(

                              lot.estimatedMinValue,

                              lot.estimatedMaxValue,

                              lot.currency,

                            )
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
                            lot.cpvs.map(
                              (
                                cpv,
                              ) => (

                                <span
                                  key={
                                    cpv.code
                                  }
                                  className="
                                    rounded-full
                                    bg-slate-100
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

                      </article>

                    ),
                  )
                }

              </div>

            )
        }

      </Section>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-2
        "
      >

        <Section

          icon={
            CheckCircle2
          }

          title="Procurement characteristics"

        >

          <div
            className="
              space-y-3
            "
          >

            <BooleanSignal
              label="Framework agreement"
              active={
                opportunity.frameworkAgreement
              }
            />

            <BooleanSignal
              label="Joint procurement"
              active={
                opportunity.jointProcurement
              }
            />

            <BooleanSignal
              label="Electronic submission"
              active={
                opportunity.electronicSubmission
              }
            />

          </div>

        </Section>


        <Section

          icon={
            ShieldCheck
          }

          title="Source lineage"

        >

          <div
            className="
              space-y-3
            "
          >

            {
              opportunity.sources.map(
                (
                  source,
                  index,
                ) => (

                  <div
                    key={
                      `${source.source}-${source.externalId}-${index}`
                    }
                    className="
                      rounded-2xl
                      border
                      border-slate-100
                      p-4
                    "
                  >

                    <div
                      className="
                        flex
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
                            source.source
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
                            source.externalId
                          }

                        </p>

                      </div>


                      {
                        source.sourceUrl

                          ? (

                            <a
                              href={
                                source.sourceUrl
                              }
                              target="_blank"
                              rel="noreferrer"
                              className="
                                grid
                                size-9
                                place-items-center
                                rounded-xl
                                border
                                border-slate-200
                                text-slate-500
                                hover:bg-slate-50
                              "
                              title="Open source notice"
                            >

                              <ExternalLink
                                className="
                                  size-4
                                "
                              />

                            </a>

                          )

                          : null
                      }

                    </div>


                    <div
                      className="
                        mt-4
                        grid
                        gap-2
                        text-[11px]
                        text-slate-500
                        sm:grid-cols-2
                      "
                    >

                      <span>

                        Version:
                        {" "}
                        {
                          source.externalVersion
                          ??
                          "—"
                        }

                      </span>


                      <span>

                        Notice:
                        {" "}
                        {
                          source.noticeType
                          ??
                          "—"
                        }

                      </span>


                      <span>

                        First seen:
                        {" "}
                        {
                          formatDateTime(
                            source.firstSeenAt,
                          )
                        }

                      </span>


                      <span>

                        Last seen:
                        {" "}
                        {
                          formatDateTime(
                            source.lastSeenAt,
                          )
                        }

                      </span>

                    </div>

                  </div>

                ),
              )
            }

          </div>

        </Section>

      </div>

    </div>

  );

}


function Badge({

  children,

}: {

  children:
    React.ReactNode;

}) {

  return (

    <span
      className="
        rounded-full
        border
        border-blue-100
        bg-blue-50
        px-3
        py-1.5
        text-xs
        font-semibold
        text-blue-700
      "
    >

      {
        children
      }

    </span>

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
    typeof Building2;

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
        shadow-[0_8px_30px_rgba(15,23,42,0.04)]
      "
    >

      <div
        className="
          grid
          size-9
          place-items-center
          rounded-xl
          bg-[#edf4ff]
          text-[#2f6fed]
        "
      >

        <Icon
          className="
            size-4
          "
        />

      </div>


      <p
        className="
          mt-4
          text-[10px]
          font-semibold
          uppercase
          tracking-[0.14em]
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
          leading-5
          text-slate-900
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


function Section({

  icon:
    Icon,

  title,

  children,

  className =
    "",

}: {

  icon:
    typeof Building2;

  title:
    string;

  children:
    React.ReactNode;

  className?:
    string;

}) {

  return (

    <section
      className={[
        "rounded-3xl border border-slate-200 bg-white p-6 shadow-[0_10px_36px_rgba(15,23,42,0.045)]",
        className,
      ].join(
        " ",
      )}
    >

      <div
        className="
          flex
          items-center
          gap-3
        "
      >

        <div
          className="
            grid
            size-9
            place-items-center
            rounded-xl
            bg-[#edf4ff]
            text-[#2f6fed]
          "
        >

          <Icon
            className="
              size-4
            "
          />

        </div>


        <h2
          className="
            text-base
            font-semibold
            text-slate-950
          "
        >

          {
            title
          }

        </h2>

      </div>


      <div
        className="
          mt-5
        "
      >

        {
          children
        }

      </div>

    </section>

  );

}


function Definition({

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

      <dt
        className="
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

      </dt>


      <dd
        className="
          mt-1
          text-sm
          font-medium
          text-slate-800
        "
      >

        {
          value
        }

      </dd>

    </div>

  );

}


function BooleanSignal({

  label,

  active,

}: {

  label:
    string;

  active:
    boolean;

}) {

  return (

    <div
      className="
        flex
        items-center
        justify-between
        rounded-xl
        bg-slate-50
        px-4
        py-3
      "
    >

      <span
        className="
          text-sm
          text-slate-700
        "
      >

        {
          label
        }

      </span>


      <span
        className={[
          "rounded-full px-2.5 py-1 text-[11px] font-semibold",
          active
            ? "bg-emerald-100 text-emerald-700"
            : "bg-slate-200 text-slate-500",
        ].join(
          " ",
        )}
      >

        {
          active
            ? "Yes"
            : "No"
        }

      </span>

    </div>

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
