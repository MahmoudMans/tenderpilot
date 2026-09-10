"use client";


import {

  Activity,

  ArrowRight,

  BarChart3,

  BellRing,

  Building2,

  Command,

  Factory,

  FileSearch,

  Gauge,

  Grid2X2,

  LayoutDashboard,

  Scale,

  Search,

  Target,

  X,

} from "lucide-react";


import type {

  LucideIcon,

} from "lucide-react";


import {

  usePathname,

  useRouter,

} from "next/navigation";


import {

  useEffect,

  useMemo,

  useRef,

  useState,

} from "react";


import {

  productContextForPath,

  productNavigationItems,

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


export function ProductExperienceLayer() {

  const pathname =
    usePathname();


  const router =
    useRouter();


  const inputRef =
    useRef<HTMLInputElement>(
      null,
    );


  const [

    open,

    setOpen,

  ] = useState(
    false,
  );


  const [

    query,

    setQuery,

  ] = useState(
    "",
  );


  useEffect(
    () => {

      function onKeyDown(
        event:
          KeyboardEvent,
      ) {

        if (
          (
            event.metaKey
            ||
            event.ctrlKey
          )
          &&
          event.key
            .toLowerCase() ===
            "k"
        ) {

          event.preventDefault();


          setOpen(
            (
              current,
            ) =>
              !current,
          );

        }


        if (
          event.key ===
          "Escape"
        ) {

          setOpen(
            false,
          );

        }

      }


      document.addEventListener(
        "keydown",
        onKeyDown,
      );


      return () => {

        document.removeEventListener(
          "keydown",
          onKeyDown,
        );

      };

    },
    [],
  );


  useEffect(
    () => {

      if (
        !open
      ) {
        return;
      }


      const frame =
        window.requestAnimationFrame(
          () => {

            inputRef.current?.focus();

          },
        );


      return () => {

        window.cancelAnimationFrame(
          frame,
        );

      };

    },
    [
      open,
    ],
  );


  const context =
    productContextForPath(
      pathname,
    );


  const results =
    useMemo(
      () => {

        const normalized =
          query
            .trim()
            .toLowerCase();


        if (
          normalized ===
          ""
        ) {

          return productNavigationItems;

        }


        return productNavigationItems.filter(
          (
            item,
          ) => {

            const searchable =
              [
                item.title,
                item.shortTitle,
                item.description,
                ...item.keywords,
              ]
                .join(
                  " ",
                )
                .toLowerCase();


            return searchable.includes(
              normalized,
            );

          },
        );

      },
      [
        query,
      ],
    );


  function navigate(
    href:
      string,
  ) {

    setOpen(
      false,
    );


    setQuery(
      "",
    );


    router.push(
      href,
    );

  }


  return (

    <>

      <button
        type="button"
        onClick={
          () =>
            setOpen(
              true,
            )
        }
        className="
          fixed
          bottom-5
          right-5
          z-40
          inline-flex
          h-11
          items-center
          gap-2
          rounded-2xl
          border
          border-slate-200
          bg-white
          px-4
          text-xs
          font-semibold
          text-slate-700
          shadow-lg
          shadow-slate-900/10
          transition
          hover:-translate-y-0.5
          hover:border-slate-300
          hover:shadow-xl
        "
        aria-label="Open TenderPilot navigation"
      >

        <Command
          className="
            size-4
            text-[#2f6fed]
          "
        />

        <span
          className="
            hidden
            sm:inline
          "
        >
          Navigate
        </span>


        <span
          className="
            rounded-md
            bg-slate-100
            px-1.5
            py-0.5
            font-mono
            text-[9px]
            text-slate-400
          "
        >
          ⌘K
        </span>

      </button>


      {
        open

          ? (

            <div
              className="
                fixed
                inset-0
                z-50
                flex
                items-start
                justify-center
                bg-slate-950/40
                px-4
                pt-[8vh]
                backdrop-blur-sm
              "
              role="dialog"
              aria-modal="true"
              aria-label="TenderPilot navigation"
            >

              <div
                className="
                  w-full
                  max-w-2xl
                  overflow-hidden
                  rounded-3xl
                  border
                  border-slate-200
                  bg-white
                  shadow-2xl
                "
              >

                <div
                  className="
                    border-b
                    border-slate-100
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

                    <div
                      className="
                        min-w-0
                      "
                    >

                      <p
                        className="
                          text-[9px]
                          font-semibold
                          uppercase
                          tracking-[0.12em]
                          text-slate-400
                        "
                      >
                        Current context
                      </p>


                      <p
                        className="
                          mt-1
                          truncate
                          text-xs
                          font-semibold
                          text-slate-700
                        "
                      >
                        TenderPilot
                        {" / "}
                        {
                          context.group
                        }
                        {" / "}
                        {
                          context.title
                        }
                      </p>

                    </div>


                    <button
                      type="button"
                      onClick={
                        () =>
                          setOpen(
                            false,
                          )
                      }
                      className="
                        grid
                        size-9
                        shrink-0
                        place-items-center
                        rounded-xl
                        text-slate-400
                        hover:bg-slate-100
                        hover:text-slate-700
                      "
                      aria-label="Close TenderPilot navigation"
                    >

                      <X
                        className="size-4"
                      />

                    </button>

                  </div>


                  <div
                    className="
                      relative
                      mt-4
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
                      ref={
                        inputRef
                      }
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
                      placeholder="Search TenderPilot…"
                      aria-label="Search TenderPilot sections"
                      className="
                        h-11
                        w-full
                        rounded-xl
                        border
                        border-slate-200
                        bg-slate-50
                        pl-10
                        pr-3
                        text-sm
                        outline-none
                        transition
                        focus:border-blue-300
                        focus:bg-white
                        focus:ring-4
                        focus:ring-blue-50
                      "
                    />

                  </div>

                </div>


                <div
                  className="
                    max-h-[62vh]
                    overflow-y-auto
                    p-3
                  "
                >

                  {
                    results.map(
                      (
                        item,
                      ) => {

                        const Icon =
                          icons[
                            item.icon
                          ];


                        const active =
                          pathname ===
                            item.href
                          ||
                          (
                            item.href !==
                              "/workspace"
                            &&
                            pathname.startsWith(
                              `${item.href}/`,
                            )
                          );


                        return (

                          <button
                            key={
                              item.href
                            }
                            type="button"
                            onClick={
                              () =>
                                navigate(
                                  item.href,
                                )
                            }
                            className={
                              `
                                group
                                flex
                                w-full
                                items-start
                                gap-4
                                rounded-2xl
                                p-4
                                text-left
                                transition
                                ${
                                  active
                                    ? "bg-blue-50"
                                    : "hover:bg-slate-50"
                                }
                              `
                            }
                          >

                            <div
                              className={
                                `
                                  grid
                                  size-10
                                  shrink-0
                                  place-items-center
                                  rounded-xl
                                  ${
                                    active
                                      ? "bg-white text-[#2f6fed]"
                                      : "bg-slate-100 text-slate-500"
                                  }
                                `
                              }
                            >

                              <Icon
                                className="size-4"
                              />

                            </div>


                            <div
                              className="
                                min-w-0
                                flex-1
                              "
                            >

                              <div
                                className="
                                  flex
                                  items-center
                                  gap-2
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
                                    item.title
                                  }
                                </p>


                                {
                                  active

                                    ? (

                                      <span
                                        className="
                                          rounded-full
                                          bg-white
                                          px-2
                                          py-0.5
                                          text-[9px]
                                          font-semibold
                                          text-[#2f6fed]
                                        "
                                      >
                                        Current
                                      </span>

                                    )

                                    : null
                                }

                              </div>


                              <p
                                className="
                                  mt-1
                                  text-xs
                                  leading-5
                                  text-slate-500
                                "
                              >
                                {
                                  item.description
                                }
                              </p>

                            </div>


                            <ArrowRight
                              className="
                                mt-3
                                size-4
                                shrink-0
                                text-slate-300
                                transition
                                group-hover:translate-x-0.5
                              "
                            />

                          </button>

                        );

                      },
                    )
                  }


                  {
                    results.length ===
                    0

                      ? (

                        <div
                          className="
                            py-14
                            text-center
                          "
                        >

                          <Search
                            className="
                              mx-auto
                              size-7
                              text-slate-300
                            "
                          />


                          <p
                            className="
                              mt-3
                              text-sm
                              font-semibold
                              text-slate-800
                            "
                          >
                            No matching TenderPilot section
                          </p>


                          <p
                            className="
                              mt-1
                              text-xs
                              text-slate-400
                            "
                          >
                            Try bids, market, buyers, alerts,
                            opportunities or operations.
                          </p>

                        </div>

                      )

                      : null
                  }

                </div>


                <div
                  className="
                    flex
                    items-center
                    justify-between
                    gap-4
                    border-t
                    border-slate-100
                    bg-slate-50
                    px-4
                    py-3
                    text-[10px]
                    text-slate-400
                  "
                >

                  <span>
                    ⌘K / Ctrl+K to toggle
                  </span>


                  <span>
                    Esc to close
                  </span>

                </div>

              </div>

            </div>

          )

          : null
      }

    </>

  );

}
