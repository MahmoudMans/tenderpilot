import {

  ArrowUpRight,

  BrainCircuit,

  Database,

  FileSearch,

  Layers3,

  ShieldCheck,

} from "lucide-react";


import {
  SystemStatusPanel,
} from "@/components/system/system-status-panel";


const foundationItems = [

  {

    title:
      "Canonical backend",

    description:

      "Spring Boot modular foundation with configuration, diagnostics and database migrations.",

    icon:
      Layers3,

    status:
      "Ready",

  },


  {

    title:
      "Local data layer",

    description:

      "PostgreSQL and OpenSearch are isolated in a reproducible local Compose environment.",

    icon:
      Database,

    status:
      "Ready",

  },


  {

    title:
      "Procurement search",

    description:

      "TED and BOAMP ingestion, normalization and discovery will build on this foundation.",

    icon:
      FileSearch,

    status:
      "Next",

  },


  {

    title:
      "Local AI",

    description:

      "Ollama connectivity is reserved for document extraction and evidence-grounded RAG.",

    icon:
      BrainCircuit,

    status:
      "Prepared",

  },

];


export default function DashboardPage() {

  return (

    <div
      className="
        mx-auto
        max-w-[1500px]
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
              tracking-[0.2em]
              text-[#2f6fed]
            "
          >

            TenderPilot · Development foundation

          </p>


          <h1
            className="
              mt-3
              max-w-4xl
              text-3xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
              md:text-4xl
            "
          >

            Public procurement intelligence,
            built on a trustworthy engineering
            foundation.

          </h1>


          <p
            className="
              mt-4
              max-w-3xl
              text-sm
              leading-6
              text-slate-600
              md:text-base
            "
          >

            Step 1 establishes the local
            full-stack platform. Real
            procurement ingestion, opportunity
            matching, document intelligence
            and market analytics will be
            added on top without paid APIs
            or hosted services.

          </p>

        </div>


        <div
          className="
            inline-flex
            w-fit
            items-center
            gap-2
            rounded-full
            border
            border-emerald-200
            bg-emerald-50
            px-4
            py-2
            text-xs
            font-semibold
            text-emerald-800
          "
        >

          <ShieldCheck
            className="
              size-4
            "
          />

          Zero-paid-service architecture

        </div>

      </div>


      <div
        className="
          mt-8
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        {
          foundationItems.map(
            (
              item,
            ) => {

              const Icon =
                item.icon;


              return (

                <article

                  key={
                    item.title
                  }

                  className="
                    group
                    rounded-3xl
                    border
                    border-slate-200
                    bg-white
                    p-5
                    shadow-[0_12px_40px_rgba(15,23,42,0.045)]
                    transition
                    hover:-translate-y-0.5
                    hover:shadow-[0_16px_46px_rgba(15,23,42,0.075)]
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
                        grid
                        size-10
                        place-items-center
                        rounded-xl
                        bg-[#edf4ff]
                        text-[#2f6fed]
                      "
                    >

                      <Icon
                        className="
                          size-[18px]
                        "
                      />

                    </div>


                    <span
                      className={[

                        "rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-[0.12em]",

                        item.status ===
                        "Ready"

                          ? "bg-emerald-50 text-emerald-700"

                          : "bg-slate-100 text-slate-500",

                      ].join(
                        " ",
                      )}
                    >

                      {
                        item.status
                      }

                    </span>

                  </div>


                  <h2
                    className="
                      mt-5
                      text-base
                      font-semibold
                      text-slate-950
                    "
                  >

                    {
                      item.title
                    }

                  </h2>


                  <p
                    className="
                      mt-2
                      text-sm
                      leading-6
                      text-slate-500
                    "
                  >

                    {
                      item.description
                    }

                  </p>

                </article>

              );

            },
          )
        }

      </div>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-[1.15fr_0.85fr]
        "
      >


        <section
          className="
            overflow-hidden
            rounded-3xl
            border
            border-slate-200
            bg-[#14213d]
            p-7
            text-white
            shadow-[0_18px_50px_rgba(20,33,61,0.16)]
          "
        >

          <p
            className="
              text-xs
              font-semibold
              uppercase
              tracking-[0.18em]
              text-blue-200
            "
          >

            Architecture baseline

          </p>


          <h2
            className="
              mt-3
              max-w-2xl
              text-2xl
              font-semibold
              tracking-tight
            "
          >

            One modular product.
            Clear boundaries.
            Local-first dependencies.

          </h2>


          <div
            className="
              mt-7
              grid
              gap-3
              sm:grid-cols-2
            "
          >

            {
              [

                [
                  "Frontend",
                  "Next.js 16.3.3 + TypeScript",
                ],

                [
                  "Backend",
                  "Spring Boot 4.1.1 + Java 21",
                ],

                [
                  "Database",
                  "PostgreSQL",
                ],

                [
                  "Search",
                  "OpenSearch 3.8.0",
                ],

              ].map(
                (
                  [
                    label,
                    value,
                  ],
                ) => (

                  <div

                    key={
                      label
                    }

                    className="
                      rounded-2xl
                      border
                      border-white/10
                      bg-white/5
                      p-4
                    "

                  >

                    <p
                      className="
                        text-xs
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
                        mt-2
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

                ),
              )
            }

          </div>


          <div
            className="
              mt-6
              flex
              items-center
              gap-2
              text-sm
              font-medium
              text-blue-200
            "
          >

            Next engineering target

            <ArrowUpRight
              className="
                size-4
              "
            />

            Identity, organizations
            and tenant isolation

          </div>

        </section>


        <SystemStatusPanel />


      </div>

    </div>

  );

}
