"use client";


import {

  AlertOctagon,

  ArrowLeft,

  CheckCircle2,

  ClipboardCheck,

  FileWarning,

  Gauge,

  LoaderCircle,

  Save,

  ShieldAlert,

  ShieldCheck,

  TriangleAlert,

} from "lucide-react";


import type { LucideIcon } from "lucide-react";


import Link from "next/link";


import {

  useEffect,

  useMemo,

  useState,

} from "react";


import {

  useParams,

} from "next/navigation";


import {

  useAuth,

} from "@/features/auth/auth-context";


import {

  formatDate,

  formatEnum,

} from "@/features/procurement/procurement-format";


import type {

  BidComplianceMatrix,

  ComplianceRiskLevel,

  ComplianceVerdict,

  EvidenceStrength,

} from "@/features/compliance/compliance-types";


const VERDICTS: ComplianceVerdict[] = [

  "UNASSESSED",

  "COMPLIANT",

  "PARTIAL",

  "NON_COMPLIANT",

  "NOT_APPLICABLE",

];


const RISKS: ComplianceRiskLevel[] = [

  "UNASSESSED",

  "LOW",

  "MEDIUM",

  "HIGH",

  "CRITICAL",

];


const EVIDENCE: EvidenceStrength[] = [

  "NONE",

  "WEAK",

  "MODERATE",

  "STRONG",

];


export default function ComplianceMatrixPage() {

  const params =
    useParams<{
      tenderId:
        string;
    }>();


  const tenderId =
    params.tenderId;


  const {

    api,

    user,

  } = useAuth();


  const [

    matrix,

    setMatrix,

  ] = useState<
    BidComplianceMatrix
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


  const [

    savingId,

    setSavingId,

  ] = useState<
    string
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

    verdictFilter,

    setVerdictFilter,

  ] = useState<
    ComplianceVerdict
    |
    "ALL"
  >(
    "ALL",
  );


  const [

    riskFilter,

    setRiskFilter,

  ] = useState<
    ComplianceRiskLevel
    |
    "ALL"
  >(
    "ALL",
  );


  const [

    mandatoryOnly,

    setMandatoryOnly,

  ] = useState(
    false,
  );


  useEffect(
    () => {

      let cancelled =
        false;


      api<BidComplianceMatrix>(
        `/api/v1/compliance/bid-workspaces/${tenderId}`,
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


            setMatrix(
              result,
            );


            setError(
              null,
            );

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
                  : "Unable to load compliance matrix.",
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
      tenderId,
    ],
  );


  const filteredRows =
    useMemo(
      () => {

        if (
          matrix ===
          null
        ) {

          return [];
        }


        return matrix.rows.filter(
          (
            row,
          ) => {

            if (
              verdictFilter !==
                "ALL"
              &&
              row.verdict !==
                verdictFilter
            ) {

              return false;
            }


            if (
              riskFilter !==
                "ALL"
              &&
              row.riskLevel !==
                riskFilter
            ) {

              return false;
            }


            if (
              mandatoryOnly
              &&
              !row.mandatory
            ) {

              return false;
            }


            return true;

          },
        );

      },
      [
        mandatoryOnly,
        matrix,
        riskFilter,
        verdictFilter,
      ],
    );


  const canReview =
    matrix?.writable ===
      true
    &&
    (
      user?.role ===
        "OWNER"
      ||
      user?.role ===
        "BID_MANAGER"
      ||
      user?.role ===
        "ANALYST"
    );


  async function saveReview(
    row:
      BidComplianceMatrix["rows"][number],
    values: {
      verdict:
        ComplianceVerdict;

      riskLevel:
        ComplianceRiskLevel;

      evidenceStrength:
        EvidenceStrength;

      reviewRationale:
        string;

      mitigationAction:
        string;
    },
  ) {

    setSavingId(
      row.requirementId,
    );


    setError(
      null,
    );


    try {

      const updated =
        await api<BidComplianceMatrix>(
          `/api/v1/compliance/bid-workspaces/${tenderId}/requirements/${row.requirementId}`,
          {
            method:
              "PUT",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify(
                values,
              ),
          },
        );


      setMatrix(
        updated,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to save compliance review.",
      );


    } finally {

      setSavingId(
        null,
      );

    }

  }


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


  if (
    matrix ===
    null
  ) {

    return (

      <div>

        <Link
          href={
            `/bid-workspaces/${tenderId}`
          }
          className="
            inline-flex
            items-center
            gap-2
            text-sm
            font-semibold
            text-slate-600
          "
        >

          <ArrowLeft
            className="size-4"
          />

          Preparation workspace

        </Link>


        <div
          className="
            mt-6
            rounded-2xl
            border
            border-red-200
            bg-red-50
            p-5
            text-sm
            text-red-700
          "
        >
          {
            error
            ??
            "Compliance matrix is unavailable."
          }
        </div>

      </div>

    );

  }


  return (

    <div
      className="
        mx-auto
        max-w-[1500px]
      "
    >

      <Link
        href={
          `/bid-workspaces/${tenderId}`
        }
        className="
          inline-flex
          items-center
          gap-2
          text-sm
          font-semibold
          text-slate-600
        "
      >

        <ArrowLeft
          className="size-4"
        />

        Preparation workspace

      </Link>


      <div
        className="
          mt-6
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
          Bid compliance
        </p>


        <h1
          className="
            mt-3
            text-4xl
            font-semibold
            tracking-[-0.03em]
            text-slate-950
          "
        >
          Compliance matrix
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
          Review every tender requirement, assess evidence
          strength and risk, identify mandatory blockers, and
          track submission readiness without allowing AI to make
          compliance decisions automatically.
        </p>

      </div>


      {
        !matrix.writable

          ? (

            <div
              className="
                mt-6
                flex
                items-start
                gap-3
                rounded-2xl
                border
                border-amber-200
                bg-amber-50
                p-4
                text-sm
                text-amber-800
              "
            >

              <ShieldAlert
                className="
                  mt-0.5
                  size-5
                  shrink-0
                "
              />


              <div>

                <p
                  className="font-semibold"
                >
                  Compliance matrix locked
                </p>


                <p
                  className="mt-1"
                >
                  {
                    matrix.writeLockReason
                  }
                </p>

              </div>

            </div>

          )

          : null
      }


      {
        error

          ? (

            <div
              className="
                mt-6
                flex
                gap-3
                rounded-2xl
                border
                border-red-200
                bg-red-50
                p-4
                text-sm
                text-red-700
              "
            >

              <TriangleAlert
                className="
                  size-5
                  shrink-0
                "
              />

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
          xl:grid-cols-4
        "
      >

        <Metric
          icon={Gauge}
          label="Readiness"
          value={
            `${matrix.statistics.readinessScore}%`
          }
          detail={
            matrix.statistics.readyForSubmission
              ? "Submission-ready"
              : "More work required"
          }
        />


        <Metric
          icon={ClipboardCheck}
          label="Reviewed"
          value={
            `${matrix.statistics.reviewedRequirements}/${matrix.statistics.totalRequirements}`
          }
          detail={
            `${matrix.statistics.unassessedRequirements} unassessed`
          }
        />


        <Metric
          icon={AlertOctagon}
          label="Mandatory blockers"
          value={
            matrix.statistics.mandatoryBlockerCount
              .toLocaleString()
          }
          detail={
            `${matrix.statistics.mandatoryIncompleteCount} mandatory incomplete`
          }
        />


        <Metric
          icon={FileWarning}
          label="Critical risks"
          value={
            matrix.statistics.criticalRiskCount
              .toLocaleString()
          }
          detail={
            `${matrix.statistics.nonCompliantRequirements} non-compliant`
          }
        />

      </div>


      <section
        className="
          mt-6
          grid
          gap-3
          rounded-2xl
          border
          border-slate-200
          bg-white
          p-4
          md:grid-cols-3
        "
      >

        <select
          value={
            verdictFilter
          }
          onChange={
            (
              event,
            ) =>
              setVerdictFilter(
                (event.target.value as ComplianceVerdict | "ALL"),
              )
          }
          className="
            h-11
            rounded-xl
            border
            border-slate-200
            bg-white
            px-3
            text-sm
          "
        >

          <option value="ALL">
            All verdicts
          </option>

          {
            VERDICTS.map(
              (
                value,
              ) => (

                <option
                  key={
                    value
                  }
                  value={
                    value
                  }
                >
                  {
                    formatEnum(
                      value,
                    )
                  }
                </option>

              ),
            )
          }

        </select>


        <select
          value={
            riskFilter
          }
          onChange={
            (
              event,
            ) =>
              setRiskFilter(
                (event.target.value as ComplianceRiskLevel | "ALL"),
              )
          }
          className="
            h-11
            rounded-xl
            border
            border-slate-200
            bg-white
            px-3
            text-sm
          "
        >

          <option value="ALL">
            All risks
          </option>

          {
            RISKS.map(
              (
                value,
              ) => (

                <option
                  key={
                    value
                  }
                  value={
                    value
                  }
                >
                  {
                    formatEnum(
                      value,
                    )
                  }
                </option>

              ),
            )
          }

        </select>


        <label
          className="
            flex
            h-11
            items-center
            gap-3
            rounded-xl
            border
            border-slate-200
            px-4
            text-sm
            text-slate-600
          "
        >

          <input
            type="checkbox"
            checked={
              mandatoryOnly
            }
            onChange={
              (
                event,
              ) =>
                setMandatoryOnly(
                  event.target.checked,
                )
            }
          />

          Mandatory requirements only

        </label>

      </section>


      <div
        className="
          mt-6
          space-y-4
        "
      >

        {
          filteredRows.map(
            (
              row,
            ) => (

              <ComplianceRow
                key={
                  row.requirementId
                }
                row={
                  row
                }
                canReview={
                  canReview
                }
                saving={
                  savingId ===
                  row.requirementId
                }
                onSave={
                  (
                    values,
                  ) => {
                    void saveReview(
                      row,
                      values,
                    );
                  }
                }
              />

            ),
          )
        }


        {
          filteredRows.length ===
          0

            ? (

              <div
                className="
                  rounded-3xl
                  border
                  border-dashed
                  border-slate-300
                  bg-white
                  py-16
                  text-center
                "
              >

                <ClipboardCheck
                  className="
                    mx-auto
                    size-8
                    text-slate-300
                  "
                />


                <p
                  className="
                    mt-4
                    text-sm
                    font-semibold
                    text-slate-900
                  "
                >
                  No requirements match these filters
                </p>

              </div>

            )

            : null
        }

      </div>


      <div
        className="
          mt-6
          flex
          items-start
          gap-3
          rounded-2xl
          bg-slate-50
          p-4
          text-xs
          leading-5
          text-slate-500
        "
      >

        <ShieldCheck
          className="
            mt-0.5
            size-4
            shrink-0
          "
        />

        Compliance verdicts are human-controlled. Local AI and
        private RAG may help surface evidence, but they cannot
        mark a requirement compliant, non-compliant, or
        submission-ready.

      </div>

    </div>

  );

}


function ComplianceRow({

  row,

  canReview,

  saving,

  onSave,

}: {

  row:
    BidComplianceMatrix["rows"][number];

  canReview:
    boolean;

  saving:
    boolean;

  onSave:
    (
      values: {
        verdict:
          ComplianceVerdict;

        riskLevel:
          ComplianceRiskLevel;

        evidenceStrength:
          EvidenceStrength;

        reviewRationale:
          string;

        mitigationAction:
          string;
      },
    ) => void;

}) {

  const [

    verdict,

    setVerdict,

  ] = useState<ComplianceVerdict>(
    row.verdict,
  );


  const [

    risk,

    setRisk,

  ] = useState<ComplianceRiskLevel>(
    row.riskLevel,
  );


  const [

    evidence,

    setEvidence,

  ] = useState<EvidenceStrength>(
    row.evidenceStrength,
  );


  const [

    rationale,

    setRationale,

  ] = useState(
    row.reviewRationale
    ??
    "",
  );


  const [

    mitigation,

    setMitigation,

  ] = useState(
    row.mitigationAction
    ??
    "",
  );


  return (

    <article
      className="
        rounded-3xl
        border
        border-slate-200
        bg-white
        p-6
      "
    >

      <div
        className="
          flex
          flex-col
          gap-5
          xl:flex-row
          xl:items-start
          xl:justify-between
        "
      >

        <div
          className="
            min-w-0
            flex-1
          "
        >

          <div
            className="
              flex
              flex-wrap
              items-center
              gap-2
            "
          >

            <Badge
              value={
                formatEnum(
                  row.priority,
                )
              }
            />


            <Badge
              value={
                formatEnum(
                  row.category,
                )
              }
            />


            {
              row.mandatory

                ? (

                  <span
                    className="
                      rounded-full
                      bg-red-50
                      px-2.5
                      py-1
                      text-[10px]
                      font-semibold
                      text-red-700
                    "
                  >
                    Mandatory
                  </span>

                )

                : null
            }


            <span
              className="
                rounded-full
                bg-blue-50
                px-2.5
                py-1
                text-[10px]
                font-semibold
                text-blue-700
              "
            >
              {
                row.readinessContribution
              }
              /100
            </span>

          </div>


          <h2
            className="
              mt-4
              text-lg
              font-semibold
              text-slate-950
            "
          >
            {
              row.title
            }
          </h2>


          {
            row.description

              ? (

                <p
                  className="
                    mt-2
                    max-w-4xl
                    text-sm
                    leading-6
                    text-slate-500
                  "
                >
                  {
                    row.description
                  }
                </p>

              )

              : null
          }


          <div
            className="
              mt-4
              flex
              flex-wrap
              gap-x-5
              gap-y-2
              text-xs
              text-slate-400
            "
          >

            <span>
              Requirement:
              {" "}
              {
                formatEnum(
                  row.requirementStatus,
                )
              }
            </span>


            {
              row.sourceDocumentName

                ? (

                  <span>
                    Source:
                    {" "}
                    {
                      row.sourceDocumentName
                    }
                  </span>

                )

                : null
            }


            {
              row.sourceReference

                ? (

                  <span>
                    Ref:
                    {" "}
                    {
                      row.sourceReference
                    }
                  </span>

                )

                : null
            }


            {
              row.dueDate

                ? (

                  <span>
                    Due:
                    {" "}
                    {
                      formatDate(
                        row.dueDate,
                      )
                    }
                  </span>

                )

                : null
            }


            {
              row.assignedTo

                ? (

                  <span>
                    Owner:
                    {" "}
                    {
                      row.assignedTo
                    }
                  </span>

                )

                : null
            }

          </div>


          <div
            className="
              mt-5
              rounded-2xl
              bg-slate-50
              p-4
            "
          >

            <p
              className="
                text-[10px]
                font-semibold
                uppercase
                tracking-[0.12em]
                text-slate-400
              "
            >
              Next action
            </p>


            <p
              className="
                mt-2
                text-sm
                leading-6
                text-slate-700
              "
            >
              {
                row.nextAction
              }
            </p>

          </div>

        </div>


        <div
          className="
            w-full
            rounded-2xl
            border
            border-slate-200
            p-4
            xl:w-[430px]
          "
        >

          <div
            className="
              grid
              gap-3
              sm:grid-cols-3
            "
          >

            <SelectField
              label="Verdict"
              value={
                verdict
              }
              values={
                VERDICTS
              }
              disabled={
                !canReview
              }
              onChange={
                (
                  value,
                ) =>
                  setVerdict(
                    (value as ComplianceVerdict),
                  )
              }
            />


            <SelectField
              label="Risk"
              value={
                risk
              }
              values={
                RISKS
              }
              disabled={
                !canReview
              }
              onChange={
                (
                  value,
                ) =>
                  setRisk(
                    (value as ComplianceRiskLevel),
                  )
              }
            />


            <SelectField
              label="Evidence"
              value={
                evidence
              }
              values={
                EVIDENCE
              }
              disabled={
                !canReview
              }
              onChange={
                (
                  value,
                ) =>
                  setEvidence(
                    (value as EvidenceStrength),
                  )
              }
            />

          </div>


          <label
            className="
              mt-4
              block
            "
          >

            <span
              className="
                text-[10px]
                font-semibold
                uppercase
                tracking-[0.1em]
                text-slate-400
              "
            >
              Review rationale
            </span>


            <textarea
              value={
                rationale
              }
              onChange={
                (
                  event,
                ) =>
                  setRationale(
                    event.target.value,
                  )
              }
              disabled={
                !canReview
              }
              rows={3}
              className="
                mt-2
                w-full
                rounded-xl
                border
                border-slate-200
                p-3
                text-xs
                disabled:bg-slate-50
              "
            />

          </label>


          <label
            className="
              mt-3
              block
            "
          >

            <span
              className="
                text-[10px]
                font-semibold
                uppercase
                tracking-[0.1em]
                text-slate-400
              "
            >
              Mitigation / action
            </span>


            <textarea
              value={
                mitigation
              }
              onChange={
                (
                  event,
                ) =>
                  setMitigation(
                    event.target.value,
                  )
              }
              disabled={
                !canReview
              }
              rows={3}
              className="
                mt-2
                w-full
                rounded-xl
                border
                border-slate-200
                p-3
                text-xs
                disabled:bg-slate-50
              "
            />

          </label>


          {
            canReview

              ? (

                <button
                  type="button"
                  onClick={
                    () =>
                      onSave({
                        verdict,
                        riskLevel:
                          risk,
                        evidenceStrength:
                          evidence,
                        reviewRationale:
                          rationale,
                        mitigationAction:
                          mitigation,
                      })
                  }
                  disabled={
                    saving
                  }
                  className="
                    mt-4
                    inline-flex
                    h-10
                    items-center
                    gap-2
                    rounded-xl
                    bg-[#14213d]
                    px-4
                    text-xs
                    font-semibold
                    text-white
                    disabled:opacity-50
                  "
                >

                  {
                    saving

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
                          className="size-4"
                        />

                      )
                  }

                  Save compliance review

                </button>

              )

              : null
          }


          {
            row.reviewedBy

              ? (

                <p
                  className="
                    mt-3
                    text-[10px]
                    text-slate-400
                  "
                >
                  Reviewed by
                  {" "}
                  {
                    row.reviewedBy
                  }
                  {
                    row.reviewedAt
                      ? ` · ${new Intl.DateTimeFormat(
                          "en",
                          {
                            dateStyle:
                              "medium",
                          },
                        ).format(
                          new Date(
                            row.reviewedAt,
                          ),
                        )}`
                      : ""
                  }
                </p>

              )

              : null
          }

        </div>

      </div>

    </article>

  );

}


function SelectField({

  label,

  value,

  values,

  disabled,

  onChange,

}: {

  label:
    string;

  value:
    string;

  values:
    string[];

  disabled:
    boolean;

  onChange:
    (
      value:
        string,
    ) => void;

}) {

  return (

    <label>

      <span
        className="
          text-[10px]
          font-semibold
          uppercase
          tracking-[0.1em]
          text-slate-400
        "
      >
        {
          label
        }
      </span>


      <select
        value={
          value
        }
        disabled={
          disabled
        }
        onChange={
          (
            event,
          ) =>
            onChange(
              event.target.value,
            )
        }
        className="
          mt-2
          h-10
          w-full
          rounded-xl
          border
          border-slate-200
          bg-white
          px-2
          text-xs
          disabled:bg-slate-50
        "
      >

        {
          values.map(
            (
              item,
            ) => (

              <option
                key={
                  item
                }
                value={
                  item
                }
              >
                {
                  formatEnum(
                    item,
                  )
                }
              </option>

            ),
          )
        }

      </select>

    </label>

  );

}


function Metric({

  icon:
    Icon,

  label,

  value,

  detail,

}: {

  icon:
    LucideIcon;

  label:
    string;

  value:
    string;

  detail:
    string;

}) {

  return (

    <article
      className="
        rounded-2xl
        border
        border-slate-200
        bg-white
        p-5
      "
    >

      <Icon
        className="
          size-4
          text-[#2f6fed]
        "
      />


      <p
        className="
          mt-4
          text-[10px]
          font-semibold
          uppercase
          tracking-[0.12em]
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
          text-2xl
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
          text-xs
          text-slate-500
        "
      >
        {
          detail
        }
      </p>

    </article>

  );

}


function Badge({

  value,

}: {

  value:
    string;

}) {

  return (

    <span
      className="
        rounded-full
        bg-slate-100
        px-2.5
        py-1
        text-[10px]
        font-semibold
        text-slate-700
      "
    >
      {
        value
      }
    </span>

  );

}
