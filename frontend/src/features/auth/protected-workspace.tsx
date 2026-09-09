"use client";


import {

  useEffect,

  type ReactNode,

} from "react";


import {

  LoaderCircle,

  ShieldCheck,

} from "lucide-react";


import {

  useRouter,

} from "next/navigation";


import {

  AppShell,

} from "@/components/shell/app-shell";


import {

  useAuth,

} from "./auth-context";


export function ProtectedWorkspace({

  children,

}: {

  children:
    ReactNode;

}) {

  const {

    status,

  } = useAuth();


  const router =
    useRouter();


  useEffect(
    () => {

      if (
        status ===
        "unauthenticated"
      ) {

        router.replace(
          "/login",
        );

      }

    },

    [
      status,
      router,
    ],

  );


  if (
    status !==
    "authenticated"
  ) {

    return (

      <div
        className="
          grid
          min-h-screen
          place-items-center
          bg-[#f5f7fb]
          px-6
        "
      >

        <div
          className="
            text-center
          "
        >

          <div
            className="
              mx-auto
              grid
              size-14
              place-items-center
              rounded-2xl
              bg-[#14213d]
              text-white
              shadow-lg
            "
          >

            {
              status ===
              "loading"

                ? (

                  <LoaderCircle
                    className="
                      size-5
                      animate-spin
                    "
                  />

                )

                : (

                  <ShieldCheck
                    className="
                      size-5
                    "
                  />

                )
            }

          </div>


          <p
            className="
              mt-5
              text-sm
              font-semibold
              text-slate-800
            "
          >

            {
              status ===
              "loading"

                ? "Restoring your TenderPilot session..."

                : "Redirecting to sign in..."
            }

          </p>

        </div>

      </div>

    );

  }


  return (

    <AppShell>

      {
        children
      }

    </AppShell>

  );

}
