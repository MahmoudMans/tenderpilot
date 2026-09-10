"use client";


import {

  Building2,

  ChevronRight,

  CircleDollarSign,

  Factory,

  LoaderCircle,

  Repeat2,

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


import type {

  SupplierIntelligenceSummary,

} from "@/features/supplier-intelligence/supplier-intelligence-types";


export default function SuppliersPage() {

  const {

    api,

  } = useAuth();


  const [

    suppliers,

    setSuppliers,

  ] = useState<
    SupplierIntelligenceSummary[]
  >(
    [],
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
            await api<SupplierIntelligenceSummary[]>(
              `/api/v1/suppliers?${params.toString()}`,
            );


          setSuppliers(
            result,
          );


        } catch (
          cause
        ) {

          setError(
            cause instanceof Error
              ? cause.message
              : "Unable to load supplier intelligence.",
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


      api<SupplierIntelligenceSummary[]>(
        "/api/v1/suppliers?limit=60",
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


            setSuppliers(
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
          Shared public award intelligence
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
          Supplier intelligence
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
          Explore suppliers observed in the canonical public
          award corpus, their buyer relationships, CPV
          specialization, award-value footprint and co-award
          relationships.
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
            placeholder="Search supplier name"
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
          placeholder="Buyer country"
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
                suppliers.map(
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

                          <Factory
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
                              supplier.name
                            }
                          </h2>


                          <p
                            className="
                              mt-2
                              text-xs
                              text-slate-400
                            "
                          >
                            Observed in
                            {" "}
                            {
                              supplier.uniqueCountryCount
                            }
                            {" "}
                            buyer
                            {
                              supplier.uniqueCountryCount ===
                              1
                                ? " country"
                                : " countries"
                            }
                          </p>

                        </div>


                        <ChevronRight
                          className="
                            size-5
                            shrink-0
                            text-slate-300
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
                          icon={CircleDollarSign}
                          label="Awards"
                          value={
                            supplier.awardCount
                              .toLocaleString()
                          }
                        />


                        <MiniMetric
                          icon={Building2}
                          label="Buyers"
                          value={
                            supplier.uniqueBuyerCount
                              .toLocaleString()
                          }
                        />


                        <MiniMetric
                          icon={Users}
                          label="Top buyer"
                          value={
                            `${supplier.topBuyerSharePercentage}%`
                          }
                        />


                        <MiniMetric
                          icon={Target}
                          label="Top CPV"
                          value={
                            `${supplier.topCpvSharePercentage}%`
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
                              supplier.buyerConcentration,
                            )
                          }
                        />


                        <Badge
                          value={
                            formatEnum(
                              supplier.cpvSpecialization,
                            )
                          }
                        />


                        {
                          supplier.repeatBuyerSignal

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

                                Repeat buyer signal

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

                        {
                          supplier.lastObservedPublicationDate

                            ? (

                              <span>
                                Latest observed win:
                                {" "}
                                {
                                  formatDate(
                                    supplier.lastObservedPublicationDate,
                                  )
                                }
                              </span>

                            )

                            : null
                        }


                        {
                          supplier.associatedAwardValue !==
                            null
                          &&
                          supplier.awardCurrency
                          &&
                          supplier.awardCurrency !==
                            "MIXED"

                            ? (

                              <span>
                                Associated value:
                                {" "}
                                {
                                  formatMoney(
                                    supplier.associatedAwardValue,
                                    supplier.awardCurrency,
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
                suppliers.length ===
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

                      <Factory
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
                        No suppliers match these filters
                      </p>


                      <p
                        className="
                          mt-2
                          text-sm
                          text-slate-400
                        "
                      >
                        Supplier intelligence depends on
                        public award records containing
                        supplier information.
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

        Supplier intelligence uses only public canonical
        procurement and award records. TenderPilot does not
        use private company profiles, documents, RAG history,
        compliance reviews or bid-planning data in these
        analytics.

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
