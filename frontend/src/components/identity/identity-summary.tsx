"use client";


import {

  Building2,

  ShieldCheck,

  UserRound,

} from "lucide-react";


import {

  useAuth,

} from "@/features/auth/auth-context";


function roleLabel(
  role:
    string,
) {

  return role
    .replaceAll(
      "_",
      " ",
    )
    .toLowerCase()
    .replace(
      /\b\w/g,
      (
        character,
      ) =>
        character.toUpperCase(),
    );

}


export function IdentitySummary() {

  const {

    user,

  } = useAuth();


  if (
    user === null
  ) {

    return null;

  }


  const items = [

    {

      icon:
        UserRound,

      label:
        "Signed in as",

      value:
        user.fullName,

      detail:
        user.email,

    },

    {

      icon:
        Building2,

      label:
        "Active organization",

      value:
        user.organization.name,

      detail:
        user.organization.slug,

    },

    {

      icon:
        ShieldCheck,

      label:
        "Workspace role",

      value:
        roleLabel(
          user.role,
        ),

      detail:
        "Tenant-scoped access",

    },

  ];


  return (

    <section
      className="
        mt-6
        grid
        gap-4
        md:grid-cols-3
      "
    >

      {
        items.map(
          (
            item,
          ) => {

            const Icon =
              item.icon;


            return (

              <div

                key={
                  item.label
                }

                className="
                  rounded-2xl
                  border
                  border-slate-200
                  bg-white
                  p-4
                  shadow-[0_8px_30px_rgba(15,23,42,0.04)]
                "

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


                  <div
                    className="
                      min-w-0
                    "
                  >

                    <p
                      className="
                        text-[10px]
                        font-semibold
                        uppercase
                        tracking-[0.14em]
                        text-slate-400
                      "
                    >

                      {
                        item.label
                      }

                    </p>


                    <p
                      className="
                        mt-1
                        truncate
                        text-sm
                        font-semibold
                        text-slate-900
                      "
                    >

                      {
                        item.value
                      }

                    </p>

                  </div>

                </div>


                <p
                  className="
                    mt-3
                    truncate
                    text-xs
                    text-slate-500
                  "
                >

                  {
                    item.detail
                  }

                </p>

              </div>

            );

          },
        )
      }

    </section>

  );

}
