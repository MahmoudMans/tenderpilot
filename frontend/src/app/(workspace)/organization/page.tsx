"use client";


import {

  Building2,

  CalendarDays,

  Crown,

  Fingerprint,

  LoaderCircle,

  ShieldCheck,

  UsersRound,

} from "lucide-react";


import {

  useEffect,

  useState,

} from "react";


import {

  ApiHttpError,

} from "@/features/auth/auth-api";


import {

  useAuth,

} from "@/features/auth/auth-context";


import type {

  MembershipRole,

} from "@/features/auth/auth-types";


type Organization = {

  id:
    string;

  name:
    string;

  slug:
    string;

  createdAt:
    string;

};


type OrganizationMember = {

  userId:
    string;

  fullName:
    string;

  email:
    string;

  role:
    MembershipRole;

  joinedAt:
    string;

};


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


export default function OrganizationPage() {

  const {

    api,

    user,

  } = useAuth();


  const [

    organization,

    setOrganization,

  ] = useState<
    Organization
    |
    null
  >(
    null,
  );


  const [

    members,

    setMembers,

  ] = useState<
    OrganizationMember[]
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

      let cancelled =
        false;


      Promise.all([

        api<Organization>(
          "/api/v1/organizations/current",
        ),

        api<OrganizationMember[]>(
          "/api/v1/organizations/current/members",
        ),

      ])
        .then(
          (
            [
              organizationResult,
              membersResult,
            ],
          ) => {

            if (
              cancelled
            ) {

              return;

            }


            setOrganization(
              organizationResult,
            );


            setMembers(
              membersResult,
            );

          },
        )
        .catch(
          (
            cause,
          ) => {

            if (
              cancelled
            ) {

              return;

            }


            if (
              cause instanceof ApiHttpError
              &&
              cause.status ===
              403
            ) {

              setOrganization({

                id:
                  user?.organization.id
                  ??
                  "",

                name:
                  user?.organization.name
                  ??
                  "",

                slug:
                  user?.organization.slug
                  ??
                  "",

                createdAt:
                  "",

              });


              setMembers(
                null,
              );


              setError(
                "Your role can access this organization but cannot list organization members.",
              );


              return;

            }


            setError(

              cause instanceof Error

                ? cause.message

                : "Unable to load organization information.",

            );

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
      user,
    ],

  );


  if (
    loading
  ) {

    return (

      <div
        className="
          grid
          min-h-[60vh]
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

    );

  }


  return (

    <div
      className="
        mx-auto
        max-w-6xl
      "
    >

      <p
        className="
          text-xs
          font-semibold
          uppercase
          tracking-[0.18em]
          text-[#2f6fed]
        "
      >

        Identity & tenancy

      </p>


      <div
        className="
          mt-3
          flex
          flex-col
          gap-4
          md:flex-row
          md:items-end
          md:justify-between
        "
      >

        <div>

          <h1
            className="
              text-3xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
            "
          >

            {
              organization?.name
              ??
              user?.organization.name
              ??
              "Organization"
            }

          </h1>


          <p
            className="
              mt-2
              max-w-2xl
              text-sm
              leading-6
              text-slate-500
            "
          >

            TenderPilot scopes private application data to the active
            organization contained in the authenticated access token.

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
            px-3.5
            py-2
            text-xs
            font-semibold
            text-emerald-700
          "
        >

          <ShieldCheck
            className="
              size-4
            "
          />

          Tenant isolation active

        </div>

      </div>


      {
        error

          ? (

            <div
              className="
                mt-6
                rounded-2xl
                border
                border-amber-200
                bg-amber-50
                p-4
                text-sm
                text-amber-800
              "
            >

              {
                error
              }

            </div>

          )

          : null
      }


      <div
        className="
          mt-7
          grid
          gap-4
          md:grid-cols-2
          lg:grid-cols-4
        "
      >

        <InfoCard

          icon={
            Building2
          }

          label="Organization"

          value={
            organization?.name
            ??
            user?.organization.name
            ??
            "—"
          }

        />


        <InfoCard

          icon={
            Fingerprint
          }

          label="Tenant ID"

          value={
            organization?.id
              ? `${organization.id.slice(0, 8)}…`
              : "—"
          }

        />


        <InfoCard

          icon={
            Crown
          }

          label="Your role"

          value={
            user
              ? roleLabel(
                  user.role,
                )
              : "—"
          }

        />


        <InfoCard

          icon={
            CalendarDays
          }

          label="Created"

          value={
            organization?.createdAt

              ? new Intl.DateTimeFormat(
                  "en",
                  {
                    dateStyle:
                      "medium",
                  },
                ).format(
                  new Date(
                    organization.createdAt,
                  ),
                )

              : "Current workspace"
          }

        />

      </div>


      <section
        className="
          mt-6
          overflow-hidden
          rounded-3xl
          border
          border-slate-200
          bg-white
          shadow-[0_12px_40px_rgba(15,23,42,0.05)]
        "
      >

        <div
          className="
            flex
            items-center
            justify-between
            gap-4
            border-b
            border-slate-100
            px-6
            py-5
          "
        >

          <div>

            <div
              className="
                flex
                items-center
                gap-2
              "
            >

              <UsersRound
                className="
                  size-4
                  text-[#2f6fed]
                "
              />


              <h2
                className="
                  text-base
                  font-semibold
                  text-slate-950
                "
              >

                Organization members

              </h2>

            </div>


            <p
              className="
                mt-1
                text-xs
                text-slate-500
              "
            >

              Member administration is restricted to OWNER and BID_MANAGER roles.

            </p>

          </div>


          {
            members

              ? (

                <span
                  className="
                    rounded-full
                    bg-slate-100
                    px-3
                    py-1.5
                    text-xs
                    font-semibold
                    text-slate-600
                  "
                >

                  {
                    members.length
                  }
                  {" "}
                  member
                  {
                    members.length ===
                    1

                      ? ""

                      : "s"
                  }

                </span>

              )

              : null
          }

        </div>


        {
          members

            ? (

              <div
                className="
                  divide-y
                  divide-slate-100
                "
              >

                {
                  members.map(
                    (
                      member,
                    ) => (

                      <div

                        key={
                          member.userId
                        }

                        className="
                          flex
                          items-center
                          justify-between
                          gap-5
                          px-6
                          py-4
                        "

                      >

                        <div
                          className="
                            min-w-0
                          "
                        >

                          <p
                            className="
                              truncate
                              text-sm
                              font-semibold
                              text-slate-900
                            "
                          >

                            {
                              member.fullName
                            }

                          </p>


                          <p
                            className="
                              mt-1
                              truncate
                              text-xs
                              text-slate-500
                            "
                          >

                            {
                              member.email
                            }

                          </p>

                        </div>


                        <span
                          className="
                            shrink-0
                            rounded-full
                            bg-[#edf4ff]
                            px-3
                            py-1.5
                            text-[11px]
                            font-semibold
                            text-[#245fcd]
                          "
                        >

                          {
                            roleLabel(
                              member.role,
                            )
                          }

                        </span>

                      </div>

                    ),
                  )
                }

              </div>

            )

            : (

              <div
                className="
                  px-6
                  py-10
                  text-center
                  text-sm
                  text-slate-500
                "
              >

                Member listing is not available for your current role.

              </div>

            )
        }

      </section>

    </div>

  );

}


function InfoCard({

  icon:
    Icon,

  label,

  value,

}: {

  icon:
    typeof Building2;

  label:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        rounded-2xl
        border
        border-slate-200
        bg-white
        p-5
        shadow-[0_8px_30px_rgba(15,23,42,0.04)]
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


      <p
        className="
          mt-4
          text-[10px]
          font-semibold
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
          mt-1
          truncate
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
