"use client";


import {

  ArrowLeft,

  CheckCircle2,

  CirclePause,

  History,

  LoaderCircle,

  Save,

  Scale,

  ShieldAlert,

  Sparkles,

  XCircle,

} from "lucide-react";


import Link from "next/link";


import {

  useEffect,

  useState,

} from "react";


import {

  useParams,

} from "next/navigation";


import {

  useAuth,

} from "@/features/auth/auth-context";


import {

  formatEnum,

  formatMoney,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  BidDecisionOutcome,

  BidDecisionWorkspace,

} from "@/features/bid-decision/bid-decision-types";


type AssessmentForm = {

  strategicFit:
    number;

  capabilityConfidence:
    number;

  commercialAttractiveness:
    number;

  deliveryFeasibility:
    number;

  resourceCapacity:
    number;

  riskConfidence:
    number;

  rationale:
    string;

  keyRisks:
    string;

  assumptions:
    string;

};


const INITIAL_FORM: AssessmentForm = {

  strategicFit:
    3,

  capabilityConfidence:
    3,

  commercialAttractiveness:
    3,

  deliveryFeasibility:
    3,

  resourceCapacity:
    3,

  riskConfidence:
    3,

  rationale:
    "",

  keyRisks:
    "",

  assumptions:
    "",

};


export default function BidDecisionWorkspacePage() {

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

    workspace,

    setWorkspace,

  ] = useState<
    BidDecisionWorkspace
    |
    null
  >(
    null,
  );


  const [

    form,

    setForm,

  ] = useState<AssessmentForm>(
    INITIAL_FORM,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    saving,

    setSaving,

  ] = useState(
    false,
  );


  const [

    deciding,

    setDeciding,

  ] = useState<
    BidDecisionOutcome
    |
    "REOPEN"
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


  useEffect(
    () => {

      let cancelled =
        false;


      api<BidDecisionWorkspace>(
        `/api/v1/bid-decisions/${tenderId}`,
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


            setWorkspace(
              result,
            );


            if (
              result.decision
            ) {

              setForm({
                strategicFit:
                  result.decision.strategicFit,

                capabilityConfidence:
                  result.decision.capabilityConfidence,

                commercialAttractiveness:
                  result.decision.commercialAttractiveness,

                deliveryFeasibility:
                  result.decision.deliveryFeasibility,

                resourceCapacity:
                  result.decision.resourceCapacity,

                riskConfidence:
                  result.decision.riskConfidence,

                rationale:
                  result.decision.rationale
                  ??
                  "",

                keyRisks:
                  result.decision.keyRisks
                  ??
                  "",

                assumptions:
                  result.decision.assumptions
                  ??
                  "",
              });

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
                  : "Unable to load bid-decision workspace.",
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


  async function saveAssessment() {

    setSaving(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidDecisionWorkspace>(
          `/api/v1/bid-decisions/${tenderId}/assessment`,
          {
            method:
              "PUT",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify(
                form,
              ),
          },
        );


      setWorkspace(
        updated,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to save bid assessment.",
      );


    } finally {

      setSaving(
        false,
      );

    }

  }


  async function setDecision(
    outcome:
      Exclude<
        BidDecisionOutcome,
        "PENDING"
      >,
  ) {

    setDeciding(
      outcome,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidDecisionWorkspace>(
          `/api/v1/bid-decisions/${tenderId}/decision`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                outcome,
                note:
                  form.rationale,
              }),
          },
        );


      setWorkspace(
        updated,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to record bid decision.",
      );


    } finally {

      setDeciding(
        null,
      );

    }

  }


  async function reopen() {

    setDeciding(
      "REOPEN",
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidDecisionWorkspace>(
          `/api/v1/bid-decisions/${tenderId}/reopen`,
          {
            method:
              "POST",
          },
        );


      setWorkspace(
        updated,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to reopen bid decision.",
      );


    } finally {

      setDeciding(
        null,
      );

    }

  }


  const canAssess =
    user?.role ===
      "OWNER"
    ||
    user?.role ===
      "BID_MANAGER"
    ||
    user?.role ===
      "ANALYST";


  const canDecide =
    user?.role ===
      "OWNER"
    ||
    user?.role ===
      "BID_MANAGER";


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
    workspace ===
    null
  ) {

    return (

      <div>

        <Link
          href="/bid-decisions"
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

          Decision queue

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
            "Bid-decision workspace is unavailable."
          }
        </div>

      </div>

    );

  }


  return (

    <div
      className="
        mx-auto
        max-w-[1350px]
      "
    >

      <Link
        href="/bid-decisions"
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

        Decision queue

      </Link>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-[1fr_340px]
        "
      >

        <div>

          <p
            className="
              text-xs
              font-semibold
              uppercase
              tracking-[0.16em]
              text-[#2f6fed]
            "
          >
            Bid decision workspace
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
              workspace.opportunity.title
            }
          </h1>


          <p
            className="
              mt-3
              text-sm
              text-slate-500
            "
          >
            {
              workspace.opportunity.buyerName
            }
            {" · "}
            {
              workspace.opportunity.primaryRegionName
              ??
              workspace.opportunity.countryCode
            }
          </p>

        </div>


        <div
          className="
            rounded-2xl
            bg-slate-50
            p-5
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
            Company match
          </p>


          <p
            className="
              mt-2
              text-4xl
              font-semibold
              text-slate-950
            "
          >
            {
              workspace.matchScore
            }
            <span
              className="
                text-sm
                text-slate-400
              "
            >
              /100
            </span>
          </p>


          <p
            className="
              mt-2
              text-sm
              text-slate-500
            "
          >
            {
              formatEnum(
                workspace.matchGrade,
              )
            }
          </p>

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


      <div
        className="
          mt-7
          grid
          gap-6
          xl:grid-cols-[1fr_390px]
        "
      >

        <section
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
              items-center
              gap-3
            "
          >

            <Scale
              className="
                size-5
                text-[#2f6fed]
              "
            />


            <div>

              <h2
                className="
                  font-semibold
                  text-slate-950
                "
              >
                Decision scorecard
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Rate each dimension from 0 to 5.
              </p>

            </div>

          </div>


          <div
            className="
              mt-6
              grid
              gap-4
              md:grid-cols-2
            "
          >

            <ScoreField
              label="Strategic fit"
              value={
                form.strategicFit
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      strategicFit:
                        value,
                    }),
                  )
              }
            />


            <ScoreField
              label="Capability confidence"
              value={
                form.capabilityConfidence
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      capabilityConfidence:
                        value,
                    }),
                  )
              }
            />


            <ScoreField
              label="Commercial attractiveness"
              value={
                form.commercialAttractiveness
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      commercialAttractiveness:
                        value,
                    }),
                  )
              }
            />


            <ScoreField
              label="Delivery feasibility"
              value={
                form.deliveryFeasibility
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      deliveryFeasibility:
                        value,
                    }),
                  )
              }
            />


            <ScoreField
              label="Resource capacity"
              value={
                form.resourceCapacity
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      resourceCapacity:
                        value,
                    }),
                  )
              }
            />


            <ScoreField
              label="Risk confidence"
              value={
                form.riskConfidence
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      riskConfidence:
                        value,
                    }),
                  )
              }
            />

          </div>


          <div
            className="
              mt-6
              space-y-4
            "
          >

            <TextArea
              label="Assessment rationale"
              value={
                form.rationale
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      rationale:
                        value,
                    }),
                  )
              }
            />


            <TextArea
              label="Key risks"
              value={
                form.keyRisks
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      keyRisks:
                        value,
                    }),
                  )
              }
            />


            <TextArea
              label="Assumptions"
              value={
                form.assumptions
              }
              onChange={
                (
                  value,
                ) =>
                  setForm(
                    (
                      current,
                    ) => ({
                      ...current,
                      assumptions:
                        value,
                    }),
                  )
              }
            />

          </div>


          {
            canAssess

              ? (

                <button
                  type="button"
                  onClick={
                    () => {
                      void saveAssessment();
                    }
                  }
                  disabled={
                    saving
                  }
                  className="
                    mt-6
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

                  Save assessment

                </button>

              )

              : null
          }

        </section>


        <div
          className="
            space-y-6
          "
        >

          <section
            className="
              rounded-3xl
              border
              border-slate-200
              bg-white
              p-6
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
              Decision
            </p>


            {
              workspace.decision

                ? (

                  <>

                    <p
                      className="
                        mt-3
                        text-4xl
                        font-semibold
                        text-slate-950
                      "
                    >
                      {
                        workspace.decision.decisionScore
                      }
                      <span
                        className="
                          text-sm
                          text-slate-400
                        "
                      >
                        /100
                      </span>
                    </p>


                    <div
                      className="
                        mt-5
                        rounded-2xl
                        bg-blue-50
                        p-4
                      "
                    >

                      <div
                        className="
                          flex
                          items-center
                          gap-2
                          text-sm
                          font-semibold
                          text-blue-900
                        "
                      >

                        <Sparkles
                          className="size-4"
                        />

                        Recommendation:
                        {" "}
                        {
                          formatEnum(
                            workspace.decision.recommendedOutcome,
                          )
                        }

                      </div>


                      <p
                        className="
                          mt-2
                          text-xs
                          leading-5
                          text-blue-800
                        "
                      >
                        {
                          workspace.decision.recommendationReason
                        }
                      </p>

                    </div>


                    <p
                      className="
                        mt-5
                        text-sm
                        text-slate-500
                      "
                    >
                      Current decision:
                      {" "}
                      <strong
                        className="
                          text-slate-900
                        "
                      >
                        {
                          formatEnum(
                            workspace.decision.outcome,
                          )
                        }
                      </strong>
                    </p>


                    {
                      canDecide

                        ? (

                          <div
                            className="
                              mt-5
                              grid
                              gap-2
                            "
                          >

                            <DecisionButton
                              label="GO"
                              icon={CheckCircle2}
                              busy={
                                deciding ===
                                "GO"
                              }
                              onClick={
                                () => {
                                  void setDecision(
                                    "GO",
                                  );
                                }
                              }
                            />


                            <DecisionButton
                              label="HOLD"
                              icon={CirclePause}
                              busy={
                                deciding ===
                                "HOLD"
                              }
                              onClick={
                                () => {
                                  void setDecision(
                                    "HOLD",
                                  );
                                }
                              }
                            />


                            <DecisionButton
                              label="NO-GO"
                              icon={XCircle}
                              busy={
                                deciding ===
                                "NO_GO"
                              }
                              onClick={
                                () => {
                                  void setDecision(
                                    "NO_GO",
                                  );
                                }
                              }
                            />


                            {
                              workspace.decision.outcome !==
                              "PENDING"

                                ? (

                                  <button
                                    type="button"
                                    onClick={
                                      () => {
                                        void reopen();
                                      }
                                    }
                                    disabled={
                                      deciding !==
                                      null
                                    }
                                    className="
                                      h-10
                                      rounded-xl
                                      border
                                      border-slate-200
                                      text-sm
                                      font-semibold
                                      text-slate-600
                                    "
                                  >
                                    Reopen decision
                                  </button>

                                )

                                : null
                            }

                          </div>

                        )

                        : null
                    }

                  </>

                )

                : (

                  <div
                    className="
                      mt-4
                      rounded-2xl
                      border
                      border-dashed
                      border-slate-300
                      p-5
                    "
                  >

                    <ShieldAlert
                      className="
                        size-5
                        text-slate-400
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
                      Assessment required
                    </p>


                    <p
                      className="
                        mt-2
                        text-xs
                        leading-5
                        text-slate-500
                      "
                    >
                      Complete the scorecard before a
                      governed GO, HOLD or NO-GO decision.
                    </p>

                  </div>

                )
            }

          </section>


          <section
            className="
              rounded-3xl
              border
              border-slate-200
              bg-white
              p-6
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
              Opportunity
            </p>


            <p
              className="
                mt-3
                text-sm
                font-semibold
                text-slate-900
              "
            >
              {
                workspace.opportunity.awardedAmount !==
                null

                  ? formatMoney(
                      workspace.opportunity.awardedAmount,
                      workspace.opportunity.awardedCurrency,
                    )

                  : formatValueRange(
                      workspace.opportunity.estimatedMinValue,
                      workspace.opportunity.estimatedMaxValue,
                      workspace.opportunity.currency,
                    )
              }
            </p>


            <Link
              href={
                `/opportunities/${tenderId}`
              }
              className="
                mt-4
                inline-flex
                text-sm
                font-semibold
                text-[#2f6fed]
              "
            >
              Open procurement record
            </Link>

          </section>

        </div>

      </div>


      <section
        className="
          mt-6
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
            items-center
            gap-3
          "
        >

          <History
            className="
              size-5
              text-[#2f6fed]
            "
          />


          <h2
            className="
              font-semibold
              text-slate-950
            "
          >
            Decision history
          </h2>

        </div>


        <div
          className="
            mt-5
            space-y-3
          "
        >

          {
            workspace.history.map(
              (
                event,
              ) => (

                <div
                  key={
                    event.id
                  }
                  className="
                    rounded-2xl
                    border
                    border-slate-200
                    p-4
                  "
                >

                  <div
                    className="
                      flex
                      flex-wrap
                      items-center
                      justify-between
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
                        formatEnum(
                          event.eventType,
                        )
                      }
                    </p>


                    <span
                      className="
                        text-xs
                        text-slate-400
                      "
                    >
                      {
                        new Intl.DateTimeFormat(
                          "en",
                          {
                            dateStyle:
                              "medium",

                            timeStyle:
                              "short",
                          },
                        ).format(
                          new Date(
                            event.createdAt,
                          ),
                        )
                      }
                    </span>

                  </div>


                  <p
                    className="
                      mt-2
                      text-xs
                      text-slate-500
                    "
                  >
                    {
                      event.previousOutcome
                        ? `${formatEnum(event.previousOutcome)} → `
                        : ""
                    }
                    {
                      formatEnum(
                        event.newOutcome,
                      )
                    }
                    {" · "}
                    Score
                    {" "}
                    {
                      event.decisionScore
                    }
                    /100
                  </p>


                  {
                    event.note

                      ? (

                        <p
                          className="
                            mt-3
                            text-sm
                            leading-6
                            text-slate-600
                          "
                        >
                          {
                            event.note
                          }
                        </p>

                      )

                      : null
                  }


                  <p
                    className="
                      mt-2
                      text-[11px]
                      text-slate-400
                    "
                  >
                    {
                      event.actor
                    }
                  </p>

                </div>

              ),
            )
          }


          {
            workspace.history.length ===
            0

              ? (

                <p
                  className="
                    text-sm
                    text-slate-400
                  "
                >
                  No decision activity recorded yet.
                </p>

              )

              : null
          }

        </div>

      </section>

    </div>

  );

}


function ScoreField({

  label,

  value,

  onChange,

}: {

  label:
    string;

  value:
    number;

  onChange:
    (
      value:
        number,
    ) => void;

}) {

  return (

    <label>

      <span
        className="
          text-xs
          font-semibold
          text-slate-600
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
        onChange={
          (
            event,
          ) =>
            onChange(
              Number(
                event.target.value,
              ),
            )
        }
        className="
          mt-2
          h-11
          w-full
          rounded-xl
          border
          border-slate-200
          bg-white
          px-3
          text-sm
        "
      >

        <option value={0}>
          0 — None
        </option>

        <option value={1}>
          1 — Very weak
        </option>

        <option value={2}>
          2 — Weak
        </option>

        <option value={3}>
          3 — Adequate
        </option>

        <option value={4}>
          4 — Strong
        </option>

        <option value={5}>
          5 — Excellent
        </option>

      </select>

    </label>

  );

}


function TextArea({

  label,

  value,

  onChange,

}: {

  label:
    string;

  value:
    string;

  onChange:
    (
      value:
        string,
    ) => void;

}) {

  return (

    <label
      className="
        block
      "
    >

      <span
        className="
          text-xs
          font-semibold
          text-slate-600
        "
      >
        {
          label
        }
      </span>


      <textarea
        value={
          value
        }
        onChange={
          (
            event,
          ) =>
            onChange(
              event.target.value,
            )
        }
        rows={4}
        className="
          mt-2
          w-full
          rounded-xl
          border
          border-slate-200
          p-3
          text-sm
          outline-none
          focus:border-[#2f6fed]
        "
      />

    </label>

  );

}


function DecisionButton({

  label,

  icon:
    Icon,

  busy,

  onClick,

}: {

  label:
    string;

  icon:
    typeof CheckCircle2;

  busy:
    boolean;

  onClick:
    () => void;

}) {

  return (

    <button
      type="button"
      onClick={
        onClick
      }
      disabled={
        busy
      }
      className="
        inline-flex
        h-10
        items-center
        justify-center
        gap-2
        rounded-xl
        border
        border-slate-200
        bg-white
        text-sm
        font-semibold
        text-slate-700
        hover:bg-slate-50
        disabled:opacity-50
      "
    >

      {
        busy

          ? (

            <LoaderCircle
              className="
                size-4
                animate-spin
              "
            />

          )

          : (

            <Icon
              className="size-4"
            />

          )
      }

      {
        label
      }

    </button>

  );

}
