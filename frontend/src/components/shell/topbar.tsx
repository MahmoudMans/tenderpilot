"use client";


import {

  Bell,

  LogOut,

  Search,

} from "lucide-react";


import {

  useRouter,

} from "next/navigation";


import {

  useState,

} from "react";


import {

  useAuth,

} from "@/features/auth/auth-context";


function initials(
  value:
    string,
) {

  return value
    .split(
      /\s+/,
    )
    .filter(
      Boolean,
    )
    .slice(
      0,
      2,
    )
    .map(
      (
        part,
      ) =>
        part.charAt(
          0,
        ),
    )
    .join(
      "",
    )
    .toUpperCase();

}


export function Topbar() {

  const router =
    useRouter();


  const {

    user,

    logout,

  } = useAuth();


  const [

    signingOut,

    setSigningOut,

  ] = useState(
    false,
  );


  async function signOut() {

    setSigningOut(
      true,
    );


    try {

      await logout();


      router.replace(
        "/login",
      );


    } finally {

      setSigningOut(
        false,
      );

    }

  }


  return (

    <header
      className="
        flex
        h-20
        items-center
        justify-between
        border-b
        border-slate-200/80
        bg-white/80
        px-5
        backdrop-blur
        md:px-8
      "
    >

      <div
        className="
          relative
          hidden
          w-full
          max-w-xl
          md:block
        "
      >

        <Search
          className="
            pointer-events-none
            absolute
            left-3.5
            top-1/2
            size-4
            -translate-y-1/2
            text-slate-400
          "
        />


        <input

          aria-label="
            Global search
          "

          disabled

          placeholder="
            Search tenders, buyers or suppliers...
          "

          className="
            h-11
            w-full
            rounded-xl
            border
            border-slate-200
            bg-slate-50
            pl-10
            pr-4
            text-sm
            text-slate-700
            outline-none
            placeholder:text-slate-400
            disabled:cursor-not-allowed
          "

          title="
            Global search will activate during the OpenSearch phase.
          "

        />

      </div>


      <div
        className="
          ml-auto
          flex
          items-center
          gap-3
        "
      >

        <button

          type="button"

          aria-label="
            Notifications
          "

          className="
            grid
            size-10
            place-items-center
            rounded-xl
            border
            border-slate-200
            bg-white
            text-slate-500
            shadow-sm
          "

        >

          <Bell
            className="
              size-4
            "
          />

        </button>


        <div
          className="
            flex
            items-center
            gap-3
            rounded-xl
            border
            border-slate-200
            bg-white
            px-3
            py-2
            shadow-sm
          "
        >

          <div
            className="
              grid
              size-8
              place-items-center
              rounded-lg
              bg-[#14213d]
              text-[11px]
              font-semibold
              text-white
            "
          >

            {
              user

                ? initials(
                    user.fullName,
                  )

                : "TP"
            }

          </div>


          <div
            className="
              hidden
              sm:block
            "
          >

            <p
              className="
                max-w-40
                truncate
                text-xs
                font-semibold
                text-slate-800
              "
            >

              {
                user?.fullName
                ??
                "TenderPilot"
              }

            </p>


            <p
              className="
                max-w-40
                truncate
                text-[11px]
                text-slate-500
              "
            >

              {
                user?.organization.name
                ??
                ""
              }

            </p>

          </div>


          <button

            type="button"

            disabled={
              signingOut
            }

            onClick={
              () => {

                void signOut();

              }
            }

            aria-label="
              Sign out
            "

            title="
              Sign out
            "

            className="
              ml-1
              grid
              size-8
              place-items-center
              rounded-lg
              text-slate-400
              transition
              hover:bg-slate-50
              hover:text-slate-700
              disabled:opacity-50
            "

          >

            <LogOut
              className="
                size-4
              "
            />

          </button>

        </div>

      </div>

    </header>

  );

}
