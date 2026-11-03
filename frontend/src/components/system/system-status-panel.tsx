"use client";


import {

  CheckCircle2,

  CircleAlert,

  RefreshCw,

  Server,

} from "lucide-react";


import {

  useEffect,

  useState,

} from "react";


import {

  fetchSystemStatus,

  type DependencyStatus,

  type SystemStatus,

} from "@/lib/api";


function isAbortError(

  cause:
    unknown,

): boolean {

  return (

    cause instanceof DOMException
    &&
    cause.name ===
      "AbortError"

  );

}


function describeError(

  cause:
    unknown,

): string {

  if (
    cause instanceof Error
  ) {

    return cause.message;

  }


  return "Unable to reach the TenderPilot backend";

}


function DependencyRow({

  dependency,

}: {

  dependency:
    DependencyStatus;

}) {

  const healthy =

    dependency.status ===
    "UP";


  return (

    <div
      className="
        flex
        items-start
        justify-between
        gap-5
        py-4
      "
    >

      <div
        className="
          flex
          min-w-0
          items-start
          gap-3
        "
      >

        <div
          className={[

            "mt-0.5 grid size-8 shrink-0 place-items-center rounded-lg",

            healthy

              ? "bg-emerald-50 text-emerald-700"

              : "bg-amber-50 text-amber-700",

          ].join(
            " ",
          )}
        >

          {
            healthy

              ? (

                <CheckCircle2
                  className="
                    size-4
                  "
                />

              )

              : (

                <CircleAlert
                  className="
                    size-4
                  "
                />

              )
          }

        </div>


        <div
          className="
            min-w-0
          "
        >

          <p
            className="
              text-sm
              font-semibold
              text-slate-800
            "
          >

            {
              dependency.name
            }

          </p>


          <p
            className="
              mt-0.5
              truncate
              text-xs
              text-slate-500
            "
          >

            {
              dependency.detail
            }

          </p>

        </div>

      </div>


      <span
        className={[

          "rounded-full px-2.5 py-1 text-[11px] font-bold",

          healthy

            ? "bg-emerald-50 text-emerald-700"

            : "bg-amber-50 text-amber-700",

        ].join(
          " ",
        )}
      >

        {
          dependency.status
        }

      </span>

    </div>

  );

}


export function SystemStatusPanel() {

  const [

    status,

    setStatus,

  ] = useState<
    SystemStatus
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


  useEffect(
    () => {

      const controller =

        new AbortController();


      fetchSystemStatus(
        controller.signal,
      )
        .then(
          (
            result,
          ) => {

            if (
              controller.signal.aborted
            ) {

              return;

            }


            setStatus(
              result,
            );


            setError(
              null,
            );

          },
        )
        .catch(
          (
            cause:
              unknown,
          ) => {

            if (
              controller.signal.aborted
              ||
              isAbortError(
                cause,
              )
            ) {

              return;

            }


            setStatus(
              null,
            );


            setError(
              describeError(
                cause,
              ),
            );

          },
        )
        .finally(
          () => {

            if (
              !controller.signal.aborted
            ) {

              setLoading(
                false,
              );

            }

          },
        );


      return () => {

        controller.abort();

      };

    },

    [],

  );


  async function refreshStatus() {

    setLoading(
      true,
    );


    setError(
      null,
    );


    try {

      const result =

        await fetchSystemStatus();


      setStatus(
        result,
      );


    } catch (
      cause:
        unknown
    ) {

      setStatus(
        null,
      );


      setError(
        describeError(
          cause,
        ),
      );


    } finally {

      setLoading(
        false,
      );

    }

  }


  return (

    <section
      className="
        rounded-3xl
        border
        border-slate-200
        bg-white
        p-6
        shadow-[0_12px_40px_rgba(15,23,42,0.05)]
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

        <div>

          <div
            className="
              flex
              items-center
              gap-2
              text-slate-500
            "
          >

            <Server
              className="
                size-4
              "
            />


            <p
              className="
                text-xs
                font-semibold
                uppercase
                tracking-[0.16em]
              "
            >

              Runtime health

            </p>

          </div>


          <h2
            className="
              mt-2
              text-lg
              font-semibold
              text-slate-950
            "
          >

            Local development services

          </h2>

        </div>


        <button

          type="button"

          onClick={
            () => {

              void refreshStatus();

            }
          }

          disabled={
            loading
          }

          className="
            grid
            size-9
            place-items-center
            rounded-xl
            border
            border-slate-200
            text-slate-500
            transition
            hover:bg-slate-50
            disabled:cursor-not-allowed
            disabled:opacity-50
          "

          aria-label="Refresh system status"

        >

          <RefreshCw
            className={

              loading

                ? "size-4 animate-spin"

                : "size-4"

            }
          />

        </button>

      </div>


      {
        loading
        &&
        !status

          ? (

            <div
              className="
                mt-5
                space-y-3
              "
            >

              {
                [
                  0,
                  1,
                  2,
                ].map(
                  (
                    item,
                  ) => (

                    <div

                      key={
                        item
                      }

                      className="
                        h-14
                        animate-pulse
                        rounded-xl
                        bg-slate-100
                      "

                    />

                  ),
                )
              }

            </div>

          )

          : null
      }


      {
        error

          ? (

            <div
              className="
                mt-5
                rounded-2xl
                border
                border-amber-200
                bg-amber-50
                p-4
              "
            >

              <p
                className="
                  text-sm
                  font-semibold
                  text-amber-900
                "
              >

                Backend is not running

              </p>


              <p
                className="
                  mt-1
                  text-xs
                  leading-5
                  text-amber-800
                "
              >

                {
                  error
                }

              </p>

            </div>

          )

          : null
      }


      {
        status

          ? (

            <>

              <div
                className="
                  mt-5
                  divide-y
                  divide-slate-100
                "
              >

                {
                  status.dependencies.map(
                    (
                      dependency,
                    ) => (

                      <DependencyRow

                        key={
                          dependency.name
                        }

                        dependency={
                          dependency
                        }

                      />

                    ),
                  )
                }

              </div>


              <div
                className="
                  mt-3
                  flex
                  items-center
                  justify-between
                  border-t
                  border-slate-100
                  pt-4
                  text-xs
                  text-slate-400
                "
              >

                <span>

                  {
                    status.application
                  }

                </span>


                <span>

                  {
                    status.version
                  }

                </span>

              </div>

            </>

          )

          : null
      }

    </section>

  );

}
