"use client";


import {

  Check,

  LoaderCircle,

  Save,

  X,

} from "lucide-react";


import {

  type Dispatch,

  type FormEvent,

  type SetStateAction,

} from "react";


import type {

  AnnualRevenueBand,

  BuyerType,

  EmployeeBand,

} from "@/features/company/company-types";


import type {

  CompanyFormState,

} from "@/features/company/company-form";


const EMPLOYEE_OPTIONS:
Array<{

  value:
    EmployeeBand;

  label:
    string;

}> = [

  {
    value:
      "NOT_DISCLOSED",
    label:
      "Not disclosed",
  },

  {
    value:
      "ONE_TO_NINE",
    label:
      "1–9 employees",
  },

  {
    value:
      "TEN_TO_FORTY_NINE",
    label:
      "10–49 employees",
  },

  {
    value:
      "FIFTY_TO_TWO_FORTY_NINE",
    label:
      "50–249 employees",
  },

  {
    value:
      "TWO_FIFTY_TO_NINE_NINETY_NINE",
    label:
      "250–999 employees",
  },

  {
    value:
      "THOUSAND_PLUS",
    label:
      "1,000+ employees",
  },

];


const REVENUE_OPTIONS:
Array<{

  value:
    AnnualRevenueBand;

  label:
    string;

}> = [

  {
    value:
      "NOT_DISCLOSED",
    label:
      "Not disclosed",
  },

  {
    value:
      "UNDER_500K",
    label:
      "Under €500K",
  },

  {
    value:
      "FROM_500K_TO_2M",
    label:
      "€500K–€2M",
  },

  {
    value:
      "FROM_2M_TO_10M",
    label:
      "€2M–€10M",
  },

  {
    value:
      "FROM_10M_TO_50M",
    label:
      "€10M–€50M",
  },

  {
    value:
      "FROM_50M_TO_250M",
    label:
      "€50M–€250M",
  },

  {
    value:
      "ABOVE_250M",
    label:
      "Above €250M",
  },

];


const BUYER_TYPES:
Array<{

  value:
    BuyerType;

  label:
    string;

}> = [

  {
    value:
      "CENTRAL_GOVERNMENT",
    label:
      "Central government",
  },

  {
    value:
      "LOCAL_AUTHORITY",
    label:
      "Local authorities",
  },

  {
    value:
      "PUBLIC_AGENCY",
    label:
      "Public agencies",
  },

  {
    value:
      "EDUCATION",
    label:
      "Education",
  },

  {
    value:
      "HEALTHCARE",
    label:
      "Healthcare",
  },

  {
    value:
      "SOCIAL_HOUSING",
    label:
      "Social housing",
  },

  {
    value:
      "UTILITIES",
    label:
      "Utilities",
  },

  {
    value:
      "TRANSPORT",
    label:
      "Transport",
  },

  {
    value:
      "OTHER",
    label:
      "Other",
  },

];


export function CompanyProfileEditor({

  form,

  setForm,

  pending,

  error,

  onSubmit,

  onCancel,

}: {

  form:
    CompanyFormState;

  setForm:
    Dispatch<
      SetStateAction<CompanyFormState>
    >;

  pending:
    boolean;

  error:
    string
    |
    null;

  onSubmit:
    (
      event:
        FormEvent<HTMLFormElement>,
    ) => void;

  onCancel:
    () => void;

}) {

  function update<K extends keyof CompanyFormState>(

    key:
      K,

    value:
      CompanyFormState[K],

  ) {

    setForm(
      (
        current,
      ) => ({

        ...current,

        [key]:
          value,

      }),
    );

  }


  function toggleBuyerType(
    buyerType:
      BuyerType,
  ) {

    const next =

      form.preferredBuyerTypes.includes(
        buyerType,
      )

        ? form.preferredBuyerTypes.filter(
            (
              value,
            ) =>
              value !==
              buyerType,
          )

        : [
            ...form.preferredBuyerTypes,
            buyerType,
          ];


    update(
      "preferredBuyerTypes",
      next,
    );

  }


  return (

    <form
      onSubmit={
        onSubmit
      }
      className="
        mt-7
        space-y-6
      "
    >

      {
        error

          ? (

            <div
              className="
                rounded-2xl
                border
                border-red-200
                bg-red-50
                p-4
                text-sm
                text-red-700
              "
            >

              {
                error
              }

            </div>

          )

          : null
      }


      <FormSection

        title="
          Company identity
        "

        description="
          Core information used to understand what the organization does.
        "

      >

        <div
          className="
            grid
            gap-4
            md:grid-cols-2
          "
        >

          <Field
            label="Legal name"
            required
          >

            <input
              required
              value={
                form.legalName
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "legalName",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Trading name"
          >

            <input
              value={
                form.tradingName
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "tradingName",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Industry"
            required
          >

            <input
              required
              placeholder="Building renovation"
              value={
                form.industry
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "industry",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Website"
          >

            <input
              type="url"
              placeholder="https://example.com"
              value={
                form.websiteUrl
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "websiteUrl",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Country code"
            required
          >

            <input
              required
              maxLength={
                2
              }
              value={
                form.countryCode
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "countryCode",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Headquarters city"
            required
          >

            <input
              required
              value={
                form.headquartersCity
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "headquartersCity",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Employee band"
          >

            <select
              value={
                form.employeeBand
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "employeeBand",
                    (event.target.value as EmployeeBand),
                  )
              }
              className={
                inputClass()
              }
            >

              {
                EMPLOYEE_OPTIONS.map(
                  (
                    option,
                  ) => (

                    <option
                      key={
                        option.value
                      }
                      value={
                        option.value
                      }
                    >

                      {
                        option.label
                      }

                    </option>

                  ),
                )
              }

            </select>

          </Field>


          <Field
            label="Revenue band"
          >

            <select
              value={
                form.annualRevenueBand
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "annualRevenueBand",
                    (event.target.value as AnnualRevenueBand),
                  )
              }
              className={
                inputClass()
              }
            >

              {
                REVENUE_OPTIONS.map(
                  (
                    option,
                  ) => (

                    <option
                      key={
                        option.value
                      }
                      value={
                        option.value
                      }
                    >

                      {
                        option.label
                      }

                    </option>

                  ),
                )
              }

            </select>

          </Field>

        </div>


        <Field
          label="Company description"
          required
        >

          <textarea
            required
            rows={
              5
            }
            value={
              form.description
            }
            onChange={
              (
                event,
              ) =>
                update(
                  "description",
                  event.target.value,
                )
            }
            className={
              textareaClass()
            }
          />

        </Field>

      </FormSection>


      <FormSection

        title="
          Commercial strategy
        "

        description="
          Define the opportunity sizes and response windows the company is comfortable pursuing.
        "

      >

        <div
          className="
            grid
            gap-4
            md:grid-cols-2
            xl:grid-cols-5
          "
        >

          <Field
            label="Minimum contract"
          >

            <input
              type="number"
              min="0"
              step="0.01"
              value={
                form.preferredMinContractValue
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "preferredMinContractValue",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Maximum contract"
          >

            <input
              type="number"
              min="0"
              step="0.01"
              value={
                form.preferredMaxContractValue
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "preferredMaxContractValue",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Currency"
          >

            <input
              maxLength={
                3
              }
              value={
                form.preferredCurrency
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "preferredCurrency",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Years experience"
          >

            <input
              type="number"
              min="0"
              value={
                form.yearsExperience
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "yearsExperience",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>


          <Field
            label="Minimum notice days"
          >

            <input
              type="number"
              min="0"
              value={
                form.minimumNoticeDays
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "minimumNoticeDays",
                    event.target.value,
                  )
              }
              className={
                inputClass()
              }
            />

          </Field>

        </div>

      </FormSection>


      <FormSection

        title="
          Matching signals
        "

        description="
          These structured signals will become direct inputs into TenderPilot's deterministic matching engine.
        "

      >

        <div
          className="
            grid
            gap-5
            xl:grid-cols-3
          "
        >

          <Field
            label="Capabilities"
            help="One capability per line."
          >

            <textarea
              rows={
                8
              }
              placeholder={"Energy renovation\nInsulation\nHVAC\nRoofing"}
              value={
                form.capabilities
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "capabilities",
                    event.target.value,
                  )
              }
              className={
                textareaClass()
              }
            />

          </Field>


          <Field
            label="Operating regions"
            help="Country|Region code|Region name"
          >

            <textarea
              rows={
                8
              }
              placeholder={"FR|IDF|Île-de-France\nFR|HDF|Hauts-de-France"}
              value={
                form.operatingRegions
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "operatingRegions",
                    event.target.value,
                  )
              }
              className={
                textareaClass()
              }
            />

          </Field>


          <Field
            label="Target CPV codes"
            help="8-digit code|Label"
          >

            <textarea
              rows={
                8
              }
              placeholder={"45000000|Construction work\n45453000|Overhaul and refurbishment work"}
              value={
                form.targetCpvs
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "targetCpvs",
                    event.target.value,
                  )
              }
              className={
                textareaClass()
              }
            />

          </Field>

        </div>

      </FormSection>


      <FormSection

        title="
          Certifications & experience
        "

        description="
          Capture evidence signals that future compliance and reference-matching modules will use.
        "

      >

        <div
          className="
            grid
            gap-5
            xl:grid-cols-2
          "
        >

          <Field
            label="Certifications"
            help="Name|Issuer|Number|Valid from|Expires on"
          >

            <textarea
              rows={
                8
              }
              placeholder={"RGE|Qualibat|DEMO-RGE-001|2025-01-01|2028-12-31"}
              value={
                form.certifications
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "certifications",
                    event.target.value,
                  )
              }
              className={
                textareaClass()
              }
            />

          </Field>


          <Field
            label="Reference projects"
            help="Project|Client|Country|Region|Value|Currency|Start|End|Description"
          >

            <textarea
              rows={
                8
              }
              placeholder={"School Renovation|Demo Municipality|FR|Île-de-France|1800000|EUR|2024-02-01|2025-01-31|Occupied-school energy renovation"}
              value={
                form.referenceProjects
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "referenceProjects",
                    event.target.value,
                  )
              }
              className={
                textareaClass()
              }
            />

          </Field>

        </div>

      </FormSection>


      <FormSection

        title="
          Strategic preferences
        "

        description="
          Focus recommendations on preferred public buyers and exclude clearly irrelevant opportunity themes.
        "

      >

        <div
          className="
            grid
            gap-6
            xl:grid-cols-[1.4fr_0.6fr]
          "
        >

          <div>

            <p
              className="
                text-sm
                font-medium
                text-slate-700
              "
            >

              Preferred buyer types

            </p>


            <div
              className="
                mt-3
                flex
                flex-wrap
                gap-2
              "
            >

              {
                BUYER_TYPES.map(
                  (
                    option,
                  ) => {

                    const selected =

                      form.preferredBuyerTypes.includes(
                        option.value,
                      );


                    return (

                      <button

                        key={
                          option.value
                        }

                        type="button"

                        onClick={
                          () =>
                            toggleBuyerType(
                              option.value,
                            )
                        }

                        className={[

                          "inline-flex items-center gap-2 rounded-full border px-3 py-2 text-xs font-semibold transition",

                          selected

                            ? "border-[#2f6fed] bg-[#edf4ff] text-[#245fcd]"

                            : "border-slate-200 bg-white text-slate-600 hover:bg-slate-50",

                        ].join(
                          " ",
                        )}

                      >

                        {
                          selected

                            ? (

                              <Check
                                className="
                                  size-3
                                "
                              />

                            )

                            : null
                        }

                        {
                          option.label
                        }

                      </button>

                    );

                  },
                )
              }

            </div>

          </div>


          <Field
            label="Excluded keywords"
            help="One keyword or phrase per line."
          >

            <textarea
              rows={
                7
              }
              value={
                form.excludedKeywords
              }
              onChange={
                (
                  event,
                ) =>
                  update(
                    "excludedKeywords",
                    event.target.value,
                  )
              }
              className={
                textareaClass()
              }
            />

          </Field>

        </div>

      </FormSection>


      <div
        className="
          sticky
          bottom-5
          flex
          justify-end
          gap-3
          rounded-2xl
          border
          border-slate-200
          bg-white/95
          p-4
          shadow-xl
          backdrop-blur
        "
      >

        <button
          type="button"
          onClick={
            onCancel
          }
          disabled={
            pending
          }
          className="
            inline-flex
            h-11
            items-center
            gap-2
            rounded-xl
            border
            border-slate-200
            px-4
            text-sm
            font-semibold
            text-slate-600
            transition
            hover:bg-slate-50
            disabled:opacity-50
          "
        >

          <X
            className="
              size-4
            "
          />

          Cancel

        </button>


        <button
          type="submit"
          disabled={
            pending
          }
          className="
            inline-flex
            h-11
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
            disabled:opacity-50
          "
        >

          {
            pending

              ? (

                <LoaderCircle
                  className="
                    size-4
                    animate-spin
                  "
                />

              )

              : (

                <Save
                  className="
                    size-4
                  "
                />

              )
          }

          {
            pending

              ? "Saving..."

              : "Save company profile"
          }

        </button>

      </div>

    </form>

  );

}


function FormSection({

  title,

  description,

  children,

}: {

  title:
    string;

  description:
    string;

  children:
    React.ReactNode;

}) {

  return (

    <section
      className="
        rounded-3xl
        border
        border-slate-200
        bg-white
        p-6
        shadow-[0_10px_36px_rgba(15,23,42,0.045)]
      "
    >

      <h2
        className="
          text-lg
          font-semibold
          text-slate-950
        "
      >

        {
          title
        }

      </h2>


      <p
        className="
          mt-1
          text-sm
          leading-6
          text-slate-500
        "
      >

        {
          description
        }

      </p>


      <div
        className="
          mt-6
          space-y-5
        "
      >

        {
          children
        }

      </div>

    </section>

  );

}


function Field({

  label,

  help,

  required,

  children,

}: {

  label:
    string;

  help?:
    string;

  required?:
    boolean;

  children:
    React.ReactNode;

}) {

  return (

    <label
      className="
        block
      "
    >

      <span
        className="
          text-sm
          font-medium
          text-slate-700
        "
      >

        {
          label
        }

        {
          required

            ? (

              <span
                className="
                  ml-1
                  text-red-500
                "
              >

                *

              </span>

            )

            : null
        }

      </span>


      {
        help

          ? (

            <span
              className="
                mt-1
                block
                text-xs
                leading-5
                text-slate-400
              "
            >

              {
                help
              }

            </span>

          )

          : null
      }


      <div
        className="
          mt-2
        "
      >

        {
          children
        }

      </div>

    </label>

  );

}


function inputClass() {

  return `
    h-11
    w-full
    rounded-xl
    border
    border-slate-200
    bg-white
    px-3.5
    text-sm
    text-slate-900
    outline-none
    transition
    placeholder:text-slate-400
    focus:border-[#2f6fed]
    focus:ring-4
    focus:ring-blue-500/10
  `;

}


function textareaClass() {

  return `
    w-full
    rounded-xl
    border
    border-slate-200
    bg-white
    px-3.5
    py-3
    text-sm
    leading-6
    text-slate-900
    outline-none
    transition
    placeholder:text-slate-400
    focus:border-[#2f6fed]
    focus:ring-4
    focus:ring-blue-500/10
  `;

}
