import {

  Activity,

  ArrowRight,

  BarChart3,

  BellRing,

  Building2,

  Factory,

  FileSearch,

  Gauge,

  Grid2X2,

  LayoutDashboard,

  Scale,

  Target,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import Link from "next/link";


import {

  productNavigationGroups,

} from "@/features/navigation/product-navigation";


import type {

  ProductNavigationIcon,

} from "@/features/navigation/product-navigation";


const icons:
Record<
  ProductNavigationIcon,
  LucideIcon
> = {

  workspace:
    Grid2X2,

  dashboard:
    LayoutDashboard,

  opportunities:
    FileSearch,

  matches:
    Target,

  decisions:
    Scale,

  notifications:
    BellRing,

  market:
    BarChart3,

  buyers:
    Building2,

  suppliers:
    Factory,

  intelligence:
    Gauge,

  operations:
    Activity,

};


export default function WorkspaceMapPage() {

  return (

    <div
      className="
        mx-auto
        max-w-[1500px]
      "
    >

      <section
        className="
          rounded-[32px]
          bg-[#14213d]
          p-8
          text-white
          md:p-10
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
          TenderPilot product map
        </p>


        <h1
          className="
            mt-4
            max-w-4xl
            text-4xl
            font-semibold
            tracking-[-0.035em]
            md:text-5xl
          "
        >
          One workspace from opportunity discovery to submission.
        </h1>


        <p
          className="
            mt-5
            max-w-3xl
            text-sm
            leading-7
            text-slate-300
          "
        >
          TenderPilot connects public procurement intelligence,
          private company matching, controlled bid decisions,
          compliance, execution, notifications and data operations
          without mixing tenant-private information into the
          shared public corpus.
        </p>


        <div
          className="
            mt-7
            flex
            flex-wrap
            gap-3
          "
        >

          <Link
            href="/dashboard"
            className="
              inline-flex
              h-11
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
            Open command center

            <ArrowRight
              className="size-4"
            />
          </Link>


          <div
            className="
              inline-flex
              h-11
              items-center
              rounded-xl
              border
              border-white/15
              px-4
              text-xs
              font-semibold
              text-slate-200
            "
          >
            Press ⌘K anywhere to navigate
          </div>

        </div>

      </section>


      <div
        className="
          mt-8
          space-y-8
        "
      >

        {
          productNavigationGroups.map(
            (
              group,
            ) => (

              <section
                key={
                  group.title
                }
              >

                <div>

                  <h2
                    className="
                      text-xl
                      font-semibold
                      text-slate-950
                    "
                  >
                    {
                      group.title
                    }
                  </h2>


                  <p
                    className="
                      mt-1
                      text-sm
                      text-slate-500
                    "
                  >
                    {
                      group.description
                    }
                  </p>

                </div>


                <div
                  className="
                    mt-4
                    grid
                    gap-4
                    md:grid-cols-2
                    xl:grid-cols-3
                  "
                >

                  {
                    group.items
                      .filter(
                        (
                          item,
                        ) =>
                          item.href !==
                          "/workspace",
                      )
                      .map(
                        (
                          item,
                        ) => {

                          const Icon =
                            icons[
                              item.icon
                            ];


                          return (

                            <Link
                              key={
                                item.href
                              }
                              href={
                                item.href
                              }
                              className="
                                group
                                rounded-3xl
                                border
                                border-slate-200
                                bg-white
                                p-6
                                transition
                                hover:-translate-y-0.5
                                hover:border-slate-300
                                hover:shadow-lg
                                hover:shadow-slate-900/5
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
                                    size-11
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


                                <ArrowRight
                                  className="
                                    size-4
                                    text-slate-300
                                    transition
                                    group-hover:translate-x-1
                                    group-hover:text-[#2f6fed]
                                  "
                                />

                              </div>


                              <h3
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
                                  item.description
                                }
                              </p>

                            </Link>

                          );

                        },
                      )
                  }

                </div>

              </section>

            ),
          )
        }

      </div>


      <section
        className="
          mt-8
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-7
        "
      >

        <p
          className="
            text-xs
            font-semibold
            uppercase
            tracking-[0.12em]
            text-[#2f6fed]
          "
        >
          Product flow
        </p>


        <h2
          className="
            mt-2
            text-xl
            font-semibold
            text-slate-950
          "
        >
          From market signal to controlled submission
        </h2>


        <div
          className="
            mt-5
            grid
            gap-3
            md:grid-cols-5
          "
        >

          <FlowStep
            number="01"
            title="Discover"
            detail="Public opportunities"
          />


          <FlowStep
            number="02"
            title="Match"
            detail="Organization fit"
          />


          <FlowStep
            number="03"
            title="Decide"
            detail="GO / HOLD / NO-GO"
          />


          <FlowStep
            number="04"
            title="Execute"
            detail="Compliance & planning"
          />


          <FlowStep
            number="05"
            title="Submit"
            detail="Controlled readiness"
          />

        </div>

      </section>

    </div>

  );

}


function FlowStep({

  number,

  title,

  detail,

}: {

  number:
    string;

  title:
    string;

  detail:
    string;

}) {

  return (

    <div
      className="
        rounded-2xl
        bg-slate-50
        p-4
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
          number
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
          title
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
