"use client";


import {

  Award,

  Building2,

  CheckCircle2,

  CircleDot,

  Edit3,

  FileText,

  Globe2,

  LoaderCircle,

  MapPinned,

  ShieldCheck,

  Target,

  WalletCards,

} from "lucide-react";


import {

  type FormEvent,

  useEffect,

  useState,

} from "react";


import {

  CompanyProfileEditor,

} from "@/components/company/company-profile-editor";


import {

  ApiHttpError,

} from "@/features/auth/auth-api";


import {

  useAuth,

} from "@/features/auth/auth-context";


import {

  EMPTY_COMPANY_FORM,

  formToInput,

  profileToForm,

  type CompanyFormState,

} from "@/features/company/company-form";


import type { CompanyProfile } from "@/features/company/company-types";
import { ProfileSavedNotice } from "@/features/company/profile-saved-notice";


export default function CompanyPage() {

  const {

    api,

    user,

  } = useAuth();


  const [

    profile,

    setProfile,

  ] = useState<
    CompanyProfile
    |
    null
  >(
    null,
  );


  const [

    form,

    setForm,

  ] = useState<CompanyFormState>(
    EMPTY_COMPANY_FORM,
  );


  const [

    editing,

    setEditing,

  ] = useState(
    false,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    pending,

    setPending,

  ] = useState(
    false,
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


  useEffect(
    () => {

      let cancelled =
        false;


      api<CompanyProfile>(
        "/api/v1/company/profile",
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


            setProfile(
              result,
            );


            setForm(
              profileToForm(
                result,
              ),
            );


            if (
              !result.exists
            ) {

              setEditing(
                true,
              );

            }

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

                  : "Unable to load company profile.",

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


  const [justSaved, setJustSaved] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {

    event.preventDefault();


    setPending(
      true,
    );


    setError(
      null,
    );


    try {

      const input =
        formToInput(
          form,
        );


      const result =

        await api<CompanyProfile>(

          "/api/v1/company/profile",

          {

            method:
              "PUT",

            headers: {

              "Content-Type":
                "application/json",

            },

            body:
              JSON.stringify(
                input,
              ),

          },

        );


      setProfile(
        result,
      );


      setForm(profileToForm(result));
      setEditing(false);
      setJustSaved(true);


    } catch (
      cause
    ) {

      setError(

        cause instanceof ApiHttpError

          ? cause.message

          : cause instanceof Error

            ? cause.message

            : "Unable to save the company profile.",

      );


    } finally {

      setPending(
        false,
      );

    }

  }


  function cancel() {

    if (
      profile
    ) {

      setForm(
        profileToForm(
          profile,
        ),
      );

    }


    if (
      profile?.exists
    ) {

      setEditing(
        false,
      );

    }


    setError(
      null,
    );

  }


  if (
    loading
  ) {

    return (

      <div
        className="
          grid
          min-h-[65vh]
          place-items-center
        "
      >

        <div
          className="
            text-center
          "
        >

          <LoaderCircle
            className="
              mx-auto
              size-6
              animate-spin
              text-[#2f6fed]
            "
          />


          <p
            className="
              mt-4
              text-sm
              text-slate-500
            "
          >

            Loading company intelligence...

          </p>

        </div>

      </div>

    );

  }


  if (
    editing
  ) {

    return (

      <div
        className="
          mx-auto
          max-w-[1400px]
        "
      >

        <div
          className="
            flex
            flex-col
            gap-4
            md:flex-row
            md:items-end
            md:justify-between
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

              Company intelligence

            </p>


            <h1
              className="
                mt-3
                text-3xl
                font-semibold
                tracking-[-0.03em]
                text-slate-950
              "
            >

              {
                profile?.exists

                  ? "Edit company profile"

                  : "Build your matching profile"
              }

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

              This information becomes the deterministic company-side
              input for opportunity matching, qualification and
              compliance analysis.

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
              border-blue-200
              bg-blue-50
              px-4
              py-2
              text-xs
              font-semibold
              text-blue-700
            "
          >

            <ShieldCheck
              className="
                size-4
              "
            />

            Tenant-owned company data

          </div>

        </div>


        <CompanyProfileEditor

          form={
            form
          }

          setForm={
            setForm
          }

          pending={
            pending
          }

          error={
            error
          }

          onSubmit={
            submit
          }

          onCancel={
            cancel
          }

        />

      </div>

    );

  }


  if (
    !profile
  ) {

    return (

      <div
        className="
          rounded-2xl
          border
          border-red-200
          bg-red-50
          p-6
          text-red-700
        "
      >

        {
          error
          ??
          "Company profile unavailable."
        }

      </div>

    );

  }


  const canEdit =

    user?.role ===
    "OWNER"

    ||

    user?.role ===
    "BID_MANAGER";
  return (
    <div
      className="
        mx-auto
        max-w-[1400px]
      "
    >
      {justSaved ? <ProfileSavedNotice onDismiss={() => setJustSaved(false)} /> : null}

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
              tracking-[0.18em]
              text-[#2f6fed]
            "
          >

            Company intelligence

          </p>


          <h1
            className="
              mt-3
              text-3xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
              md:text-4xl
            "
          >

            {
              profile.tradingName
              ??
              profile.legalName
            }

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

            {
              profile.description
            }

          </p>

        </div>


        <div
          className="
            flex
            items-center
            gap-3
          "
        >

          <div
            className="
              rounded-2xl
              border
              border-slate-200
              bg-white
              px-5
              py-3
              shadow-sm
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

              Profile readiness

            </p>


            <div
              className="
                mt-1
                flex
                items-baseline
                gap-1
              "
            >

              <span
                className="
                  text-2xl
                  font-semibold
                  text-slate-950
                "
              >

                {
                  profile.completeness.score
                }

              </span>

              <span
                className="
                  text-sm
                  text-slate-400
                "
              >

                /100

              </span>

            </div>

          </div>


          {
            canEdit

              ? (

                <button
                  type="button"
                  onClick={
                    () =>
                      setEditing(
                        true,
                      )
                  }
                  className="
                    inline-flex
                    h-12
                    items-center
                    gap-2
                    rounded-xl
                    bg-[#14213d]
                    px-5
                    text-sm
                    font-semibold
                    text-white
                    transition
                    hover:bg-[#1b2d52]
                  "
                >

                  <Edit3
                    className="
                      size-4
                    "
                  />

                  Edit profile

                </button>

              )

              : null
          }

        </div>

      </div>


      <div
        className="
          mt-7
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        <MetricCard

          icon={
            Building2
          }

          label="Industry"

          value={
            profile.industry
            ??
            "—"
          }

          detail={
            `${profile.headquartersCity ?? ""}, ${profile.countryCode ?? ""}`
          }

        />


        <MetricCard

          icon={
            WalletCards
          }

          label="Target contract range"

          value={
            formatRange(
              profile.preferredMinContractValue,
              profile.preferredMaxContractValue,
              profile.preferredCurrency,
            )
          }

          detail={
            profile.minimumNoticeDays !==
            null

              ? `Minimum ${profile.minimumNoticeDays} days notice`

              : "No notice threshold"
          }

        />


        <MetricCard

          icon={
            Target
          }

          label="Matching signals"

          value={
            `${profile.capabilities.length} capabilities`
          }

          detail={
            `${profile.targetCpvs.length} CPV categories`
          }

        />


        <MetricCard

          icon={
            Award
          }

          label="Experience"

          value={
            profile.yearsExperience !==
            null

              ? `${profile.yearsExperience} years`

              : "Not specified"
          }

          detail={
            `${profile.referenceProjects.length} reference projects`
          }

        />

      </div>


      <section
        className="
          mt-6
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-6
          shadow-[0_10px_36px_rgba(15,23,42,0.045)]
        "
      >

        <div
          className="
            flex
            flex-col
            gap-5
            lg:flex-row
            lg:items-center
            lg:justify-between
          "
        >

          <div>

            <h2
              className="
                text-lg
                font-semibold
                text-slate-950
              "
            >

              Matching profile completeness

            </h2>


            <p
              className="
                mt-1
                text-sm
                text-slate-500
              "
            >

              The score measures how much structured information is
              currently available to the opportunity-matching engine.

            </p>

          </div>


          <div
            className="
              flex
              items-center
              gap-2
              rounded-full
              bg-emerald-50
              px-4
              py-2
              text-xs
              font-semibold
              text-emerald-700
            "
          >

            <CheckCircle2
              className="
                size-4
              "
            />

            {
              profile.completeness.score >=
              60

                ? "Ready for matching"

                : "More information recommended"
            }

          </div>

        </div>


        <div
          className="
            mt-6
            grid
            gap-3
            md:grid-cols-2
            xl:grid-cols-7
          "
        >

          {
            profile.completeness.sections.map(
              (
                section,
              ) => (

                <div

                  key={
                    section.key
                  }

                  className="
                    rounded-2xl
                    bg-slate-50
                    p-4
                  "

                >

                  <p
                    className="
                      text-xs
                      font-medium
                      text-slate-500
                    "
                  >

                    {
                      section.label
                    }

                  </p>


                  <p
                    className="
                      mt-2
                      text-lg
                      font-semibold
                      text-slate-950
                    "
                  >

                    {
                      section.points
                    }

                    <span
                      className="
                        text-xs
                        font-normal
                        text-slate-400
                      "
                    >

                      /
                      {
                        section.maximumPoints
                      }

                    </span>

                  </p>

                </div>

              ),
            )
          }

        </div>

      </section>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-2
        "
      >

        <DataSection

          icon={
            Target
          }

          title="Capabilities"

          count={
            profile.capabilities.length
          }

        >

          <Pills
            values={
              profile.capabilities
            }
          />

        </DataSection>


        <DataSection

          icon={
            MapPinned
          }

          title="Operating regions"

          count={
            profile.operatingRegions.length
          }

        >

          <div
            className="
              space-y-2
            "
          >

            {
              profile.operatingRegions.map(
                (
                  region,
                ) => (

                  <Row

                    key={
                      `${region.countryCode}-${region.regionCode}`
                    }

                    title={
                      region.regionName
                    }

                    detail={
                      `${region.countryCode} · ${region.regionCode}`
                    }

                  />

                ),
              )
            }

          </div>

        </DataSection>


        <DataSection

          icon={
            Globe2
          }

          title="Target CPV categories"

          count={
            profile.targetCpvs.length
          }

        >

          <div
            className="
              space-y-2
            "
          >

            {
              profile.targetCpvs.map(
                (
                  cpv,
                ) => (

                  <Row

                    key={
                      cpv.code
                    }

                    title={
                      cpv.label
                    }

                    detail={
                      cpv.code
                    }

                  />

                ),
              )
            }

          </div>

        </DataSection>


        <DataSection

          icon={
            Award
          }

          title="Certifications"

          count={
            profile.certifications.length
          }

        >

          <div
            className="
              space-y-2
            "
          >

            {
              profile.certifications.map(
                (
                  certification,
                  index,
                ) => (

                  <Row

                    key={
                      `${certification.name}-${index}`
                    }

                    title={
                      certification.name
                    }

                    detail={
                      `${certification.issuer} · ${certification.validityStatus}`
                    }

                  />

                ),
              )
            }

          </div>

        </DataSection>

      </div>


      <DataSection

        icon={
          FileText
        }

        title="Reference projects"

        count={
          profile.referenceProjects.length
        }

        className="
          mt-6
        "

      >

        <div
          className="
            grid
            gap-4
            lg:grid-cols-2
            xl:grid-cols-3
          "
        >

          {
            profile.referenceProjects.map(
              (
                project,
                index,
              ) => (

                <article

                  key={
                    `${project.projectName}-${index}`
                  }

                  className="
                    rounded-2xl
                    border
                    border-slate-200
                    p-5
                  "

                >

                  <p
                    className="
                      text-sm
                      font-semibold
                      text-slate-950
                    "
                  >

                    {
                      project.projectName
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
                      project.clientName
                    }

                    {" · "}

                    {
                      project.regionName
                      ??
                      project.countryCode
                    }

                  </p>


                  <p
                    className="
                      mt-4
                      line-clamp-3
                      text-sm
                      leading-6
                      text-slate-600
                    "
                  >

                    {
                      project.description
                    }

                  </p>


                  <div
                    className="
                      mt-4
                      flex
                      items-center
                      justify-between
                      border-t
                      border-slate-100
                      pt-4
                      text-xs
                      text-slate-500
                    "
                  >

                    <span>

                      {
                        project.contractValue !==
                        null

                          ? formatMoney(
                              project.contractValue,
                              project.currency,
                            )

                          : "Value not disclosed"
                      }

                    </span>


                    <span>

                      {
                        project.completedOn
                        ??
                        "Ongoing"
                      }

                    </span>

                  </div>

                </article>

              ),
            )
          }

        </div>

      </DataSection>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-2
        "
      >

        <DataSection

          icon={
            ShieldCheck
          }

          title="Preferred buyer types"

          count={
            profile.preferredBuyerTypes.length
          }

        >

          <Pills
            values={
              profile.preferredBuyerTypes.map(
                formatEnum,
              )
            }
          />

        </DataSection>


        <DataSection

          icon={
            CircleDot
          }

          title="Excluded opportunity keywords"

          count={
            profile.excludedKeywords.length
          }

        >

          <Pills
            values={
              profile.excludedKeywords
            }
          />

        </DataSection>

      </div>

    </div>

  );

}


function MetricCard({

  icon:
    Icon,

  label,

  value,

  detail,

}: {

  icon:
    typeof Building2;

  label:
    string;

  value:
    string;

  detail:
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
          text-base
          font-semibold
          text-slate-950
        "
      >

        {
          value
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
          detail
        }

      </p>

    </div>

  );

}


function DataSection({

  icon:
    Icon,

  title,

  count,

  children,

  className =
    "",

}: {

  icon:
    typeof Building2;

  title:
    string;

  count:
    number;

  children:
    React.ReactNode;

  className?:
    string;

}) {

  return (

    <section
      className={[
        "rounded-3xl border border-slate-200 bg-white p-6 shadow-[0_10px_36px_rgba(15,23,42,0.045)]",
        className,
      ].join(
        " ",
      )}
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


          <h2
            className="
              text-base
              font-semibold
              text-slate-950
            "
          >

            {
              title
            }

          </h2>

        </div>


        <span
          className="
            rounded-full
            bg-slate-100
            px-2.5
            py-1
            text-xs
            font-semibold
            text-slate-500
          "
        >

          {
            count
          }

        </span>

      </div>


      <div
        className="
          mt-5
        "
      >

        {
          count ===
          0

            ? (

              <p
                className="
                  text-sm
                  text-slate-400
                "
              >

                No information configured yet.

              </p>

            )

            : children
        }

      </div>

    </section>

  );

}


function Pills({

  values,

}: {

  values:
    string[];

}) {

  return (

    <div
      className="
        flex
        flex-wrap
        gap-2
      "
    >

      {
        values.map(
          (
            value,
          ) => (

            <span

              key={
                value
              }

              className="
                rounded-full
                border
                border-blue-100
                bg-blue-50
                px-3
                py-1.5
                text-xs
                font-medium
                text-blue-700
              "

            >

              {
                value
              }

            </span>

          ),
        )
      }

    </div>

  );

}


function Row({

  title,

  detail,

}: {

  title:
    string;

  detail:
    string;

}) {

  return (

    <div
      className="
        flex
        items-center
        justify-between
        gap-4
        rounded-xl
        border
        border-slate-100
        px-4
        py-3
      "
    >

      <p
        className="
          min-w-0
          truncate
          text-sm
          font-medium
          text-slate-800
        "
      >

        {
          title
        }

      </p>


      <span
        className="
          shrink-0
          text-xs
          text-slate-400
        "
      >

        {
          detail
        }

      </span>

    </div>

  );

}


function formatEnum(
  value:
    string,
) {

  return value
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


function formatRange(

  minimum:
    number
    |
    null,

  maximum:
    number
    |
    null,

  currency:
    string
    |
    null,

) {

  if (
    minimum ===
    null

    &&
    maximum ===
    null
  ) {

    return "Not configured";

  }


  return `${formatMoney(minimum, currency)} – ${formatMoney(maximum, currency)}`;

}


function formatMoney(

  value:
    number
    |
    null,

  currency:
    string
    |
    null,

) {

  if (
    value ===
    null
  ) {

    return "—";

  }


  return new Intl.NumberFormat(

    "en",

    {

      style:
        "currency",

      currency:
        currency
        ??
        "EUR",

      maximumFractionDigits:
        0,

    },

  ).format(
    value,
  );

}
