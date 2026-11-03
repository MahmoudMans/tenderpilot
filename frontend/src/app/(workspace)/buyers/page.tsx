"use client";


import {

  Building2,

  CalendarClock,

  ChevronRight,

  CircleDollarSign,

  LoaderCircle,

  Repeat2,

  Search,

  ShieldCheck,

  Users,

} from "lucide-react";


import type { LucideIcon } from "lucide-react";


import Link from "next/link";
import { useSearchParams } from "next/navigation";


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

} from "@/features/procurement/procurement-format";


import type { BuyerIntelligenceSummary } from "@/features/buyer-intelligence/buyer-intelligence-types";
import { readText } from "@/features/navigation/url-filters";


/** The list request for a given search text and country. */
function buyersPath(query: string, country: string): string {
  const params = new URLSearchParams();
  if (query !== "") params.set("q", query);
  if (country !== "") params.set("country", country);
  params.set("limit", "60");
  return `/api/v1/buyers?${params.toString()}`;
}

export default function BuyersPage() {

  const {

    api,

  } = useAuth();


  const [

    buyers,

    setBuyers,

  ] = useState<
    BuyerIntelligenceSummary[]
  >(
    [],
  );


  const searchParams = useSearchParams();
  // Links such as /buyers?q=ministry&country=FR open the list already filtered.
  const [start] = useState(() => ({ query: readText(searchParams, "q"), country: readText(searchParams, "country", 8) }));
  const [query, setQuery] = useState(start.query);


  const [country, setCountry] = useState(start.country);


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


        params.set(
          "limit",
          "60",
        );


        try {

          const result =
            await api<BuyerIntelligenceSummary[]>(
              `/api/v1/buyers?${params.toString()}`,
            );


          setBuyers(
            result,
          );


        } catch (
          cause
        ) {

          setError(
            cause instanceof Error
              ? cause.message
              : "Unable to load buyer intelligence.",
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
        query,
      ],
    );


  useEffect(
    () => {

      let cancelled =
        false;


      api<BuyerIntelligenceSummary[]>(buyersPath(start.query, start.country))
        .then(
          (
            result,
          ) => {

            if (
              cancelled
            ) {
              return;
            }


            setBuyers(
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
      start,
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
          Buyer intelligence
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
          Understand public buyers through their canonical
          procurement history: notice volume, recurring CPVs,
          response windows, award activity and supplier
          concentration.
        </p>

      </div>


      <section
        className="
          mt-7
          grid
          gap-3
          rounded-2xl
          border
          border-slate-200
          bg-white
          p-4
          md:grid-cols-[1fr_180px_auto]
        "
      >

        <div
          className="
            relative
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
            aria-label="Search buyers by name"
            placeholder="Search buyer name"
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
          aria-label="Buyer country code"
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


        <button
          type="button"
          onClick={
            () => {
              void load();
            }
          }
          className="
            h-11
            rounded-xl
            bg-[#14213d]
            px-5
            text-sm
            font-semibold
            text-white
          "
        >
          Search
        </button>

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
                min-h-[40vh]
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
                grid
                gap-4
                xl:grid-cols-2
              "
            >

              {
                buyers.map(
                  (
                    buyer,
                  ) => (

                    <Link
                      key={
                        buyer.buyerId
                      }
                      href={
                        `/buyers/${buyer.buyerId}`
                      }
                      className="
                        group
                        rounded-3xl
                        border
                        border-slate-200
                        bg-white
                        p-6
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

                        <div
                          className="
                            min-w-0
                          "
                        >

                          <Building2
                            className="
                              size-5
                              text-[#2f6fed]
                            "
                          />


                          <h2
                            className="
                              mt-4
                              text-lg
                              font-semibold
                              text-slate-950
                            "
                          >
                            {
                              buyer.name
                            }
                          </h2>


                          <p
                            className="
                              mt-2
                              text-xs
                              text-slate-400
                            "
                          >
                            {
                              buyer.category
                                ? formatEnum(
                                    buyer.category,
                                  )
                                : "Buyer"
                            }
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


                        <ChevronRight
                          className="
                            size-5
                            shrink-0
                            text-slate-300
                            transition
                            group-hover:translate-x-0.5
                          "
                        />

                      </div>


                      <div
                        className="
                          mt-6
                          grid
                          grid-cols-2
                          gap-3
                          md:grid-cols-4
                        "
                      >

                        <MiniMetric
                          icon={Building2}
                          label="Notices"
                          value={
                            buyer.noticeCount
                              .toLocaleString()
                          }
                        />


                        <MiniMetric
                          icon={CircleDollarSign}
                          label="Awards"
                          value={
                            buyer.awardCount
                              .toLocaleString()
                          }
                        />


                        <MiniMetric
                          icon={CalendarClock}
                          label="Median window"
                          value={
                            buyer.medianResponseDays ===
                            null
                              ? "—"
                              : `${buyer.medianResponseDays}d`
                          }
                        />


                        <MiniMetric
                          icon={Users}
                          label="Supplier share"
                          value={
                            buyer.awardCount ===
                            0
                              ? "—"
                              : `${buyer.topSupplierSharePercentage}%`
                          }
                        />

                      </div>


                      <div
                        className="
                          mt-5
                          flex
                          flex-wrap
                          gap-2
                        "
                      >

                        <Badge
                          value={
                            formatEnum(
                              buyer.supplierConcentration,
                            )
                          }
                        />


                        <Badge
                          value={
                            formatEnum(
                              buyer.deadlineTempo,
                            )
                          }
                        />


                        {
                          buyer.recurringProcurement

                            ? (

                              <span
                                className="
                                  inline-flex
                                  items-center
                                  gap-1.5
                                  rounded-full
                                  bg-blue-50
                                  px-2.5
                                  py-1
                                  text-[10px]
                                  font-semibold
                                  text-blue-700
                                "
                              >

                                <Repeat2
                                  className="size-3"
                                />

                                Recurring procurement

                              </span>

                            )

                            : null
                        }

                      </div>


                      <div
                        className="
                          mt-5
                          flex
                          flex-wrap
                          gap-x-5
                          gap-y-2
                          text-xs
                          text-slate-400
                        "
                      >

                        <span>
                          Active:
                          {" "}
                          {
                            buyer.activeNoticeCount
                          }
                        </span>


                        <span>
                          Top CPV share:
                          {" "}
                          {
                            buyer.topCpvSharePercentage
                          }
                          %
                        </span>


                        {
                          buyer.lastPublicationDate

                            ? (

                              <span>
                                Last publication:
                                {" "}
                                {
                                  formatDate(
                                    buyer.lastPublicationDate,
                                  )
                                }
                              </span>

                            )

                            : null
                        }


                        {
                          buyer.totalAwardedValue !==
                            null
                          &&
                          buyer.awardCurrency
                          &&
                          buyer.awardCurrency !==
                            "MIXED"

                            ? (

                              <span>
                                Awarded:
                                {" "}
                                {
                                  formatMoney(
                                    buyer.totalAwardedValue,
                                    buyer.awardCurrency,
                                  )
                                }
                              </span>

                            )

                            : null
                        }

                      </div>

                    </Link>

                  ),
                )
              }


              {
                buyers.length ===
                0

                  ? (

                    <div
                      className="
                        col-span-full
                        rounded-3xl
                        border
                        border-dashed
                        border-slate-300
                        bg-white
                        py-16
                        text-center
                      "
                    >

                      <Building2
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
                        No buyers match these filters
                      </p>

                    </div>

                  )

                  : null
              }

            </div>

          )
      }


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

        Buyer intelligence is derived only from TenderPilot&apos;s
        shared canonical public-procurement corpus. Organization
        profiles, bid documents, compliance reviews, private RAG
        history and bid tasks are never used here.

      </div>

    </div>

  );

}


function MiniMetric({

  icon:
    Icon,

  label,

  value,

}: {

  icon:
    LucideIcon;

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

      <Icon
        className="
          size-3.5
          text-slate-400
        "
      />


      <p
        className="
          mt-2
          text-[9px]
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
        text-slate-600
      "
    >
      {
        value
      }
    </span>

  );

}
