"use client";


import {

  BarChart3,

  Building2,

  Database,

  FileSearch,

  LayoutDashboard,

} from "lucide-react";


import Link
  from "next/link";


import {
  usePathname,
} from "next/navigation";


const navigation = [

  {

    label:
      "Dashboard",

    href:
      "/dashboard",

    icon:
      LayoutDashboard,

  },


  {

    label:
      "Opportunities",

    href:
      "/opportunities",

    icon:
      FileSearch,

  },


  {

    label:
      "Intelligence",

    href:
      "/intelligence",

    icon:
      BarChart3,

  },


  {

    label:
      "Company",

    href:
      "/company",

    icon:
      Building2,

  },


  {

    label:
      "Data Sources",

    href:
      "/data-sources",

    icon:
      Database,

  },

];


export function Sidebar() {

  const pathname =
    usePathname();


  return (

    <aside
      className="
        hidden
        min-h-screen
        w-72
        shrink-0
        border-r
        border-slate-200/80
        bg-white/95
        px-5
        py-6
        lg:flex
        lg:flex-col
      "
    >


      <div
        className="
          flex
          items-center
          gap-3
          px-2
        "
      >

        <div
          className="
            grid
            size-10
            place-items-center
            rounded-xl
            bg-[#14213d]
            text-sm
            font-bold
            tracking-tight
            text-white
            shadow-sm
          "
        >

          TP

        </div>


        <div>

          <p
            className="
              text-lg
              font-semibold
              tracking-tight
              text-slate-950
            "
          >

            TenderPilot

          </p>


          <p
            className="
              text-xs
              text-slate-500
            "
          >

            Procurement intelligence

          </p>

        </div>

      </div>


      <nav
        className="
          mt-10
          space-y-1.5
        "
      >

        {navigation.map(
          (
            item,
          ) => {

            const Icon =
              item.icon;


            const active =

              pathname ===
              item.href

              ||

              pathname.startsWith(
                `${item.href}/`,
              );


            return (

              <Link

                key={
                  item.href
                }

                href={
                  item.href
                }

                className={[

                  "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition",

                  active

                    ? "bg-[#edf4ff] text-[#245fcd]"

                    : "text-slate-600 hover:bg-slate-50 hover:text-slate-950",

                ].join(
                  " ",
                )}

              >

                <Icon
                  className="
                    size-[18px]
                  "
                  strokeWidth={
                    1.9
                  }
                />

                {
                  item.label
                }

              </Link>

            );

          },
        )}

      </nav>


      <div
        className="
          mt-auto
          rounded-2xl
          border
          border-slate-200
          bg-slate-50
          p-4
        "
      >

        <p
          className="
            text-xs
            font-semibold
            uppercase
            tracking-[0.16em]
            text-slate-400
          "
        >

          Development mode

        </p>


        <p
          className="
            mt-2
            text-sm
            font-medium
            text-slate-800
          "
        >

          Zero paid services

        </p>


        <p
          className="
            mt-1
            text-xs
            leading-5
            text-slate-500
          "
        >

          Local database, search,
          AI and document processing.

        </p>

      </div>

    </aside>

  );

}
