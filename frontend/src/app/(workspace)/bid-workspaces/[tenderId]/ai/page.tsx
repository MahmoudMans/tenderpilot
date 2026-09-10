"use client";


import {

  AlertTriangle,

  ArrowLeft,

  Bot,

  CheckCircle2,

  FileSearch,

  LoaderCircle,

  Plus,

  RefreshCw,

  Server,

  ShieldCheck,

  Sparkles,

  TriangleAlert,

} from "lucide-react";


import type { LucideIcon } from "lucide-react";


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


import type {

  BidWorkspace,

} from "@/features/bid-workspace/bid-workspace-types";


import type {

  BidDocumentAiAnalysis,

  LocalAiStatus,

} from "@/features/local-ai/local-ai-types";


import {

  formatEnum,

} from "@/features/procurement/procurement-format";


export default function LocalAiDocumentAnalysisPage() {

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
    BidWorkspace
    |
    null
  >(
    null,
  );


  const [

    localAi,

    setLocalAi,

  ] = useState<
    LocalAiStatus
    |
    null
  >(
    null,
  );


  const [

    analyses,

    setAnalyses,

  ] = useState<
    BidDocumentAiAnalysis[]
  >(
    [],
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    activeDocument,

    setActiveDocument,

  ] = useState<
    string
    |
    "ALL"
    |
    null
  >(
    null,
  );


  const [

    promoting,

    setPromoting,

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


  useEffect(
    () => {

      let cancelled =
        false;


      Promise.all([

        api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}`,
        ),

        api<LocalAiStatus>(
          "/api/v1/local-ai/status",
        ),

        api<BidDocumentAiAnalysis[]>(
          `/api/v1/local-ai/bid-workspaces/${tenderId}/analyses`,
        ),

      ])
        .then(
          (
            [
              workspaceResult,
              aiResult,
              analysisResult,
            ],
          ) => {

            if (
              cancelled
            ) {
              return;
            }


            setWorkspace(
              workspaceResult,
            );


            setLocalAi(
              aiResult,
            );


            setAnalyses(
              analysisResult,
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
                  : "Unable to load local AI analysis.",
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


  async function analyzeDocument(
    documentId:
      string,
  ) {

    setActiveDocument(
      documentId,
    );

    setError(
      null,
    );


    try {

      const result =
        await api<BidDocumentAiAnalysis>(
          `/api/v1/local-ai/bid-workspaces/${tenderId}/documents/${documentId}/analyze`,
          {
            method:
              "POST",
          },
        );


      setAnalyses(
        (
          current,
        ) =>
          current.map(
            (
              analysis,
            ) =>
              analysis.documentId ===
              documentId
                ? result
                : analysis,
          ),
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Local AI analysis failed.",
      );


    } finally {

      setActiveDocument(
        null,
      );

    }

  }


  async function analyzeAll() {

    setActiveDocument(
      "ALL",
    );

    setError(
      null,
    );


    try {

      const result =
        await api<BidDocumentAiAnalysis[]>(
          `/api/v1/local-ai/bid-workspaces/${tenderId}/analyze-all`,
          {
            method:
              "POST",
          },
        );


      setAnalyses(
        result,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to analyze all supported documents.",
      );


    } finally {

      setActiveDocument(
        null,
      );

    }

  }


  async function promoteFinding(
    analysis:
      BidDocumentAiAnalysis,
    findingIndex:
      number,
  ) {

    const finding =
      analysis.findings[
        findingIndex
      ];


    if (
      finding ===
      undefined
    ) {
      return;
    }


    const key =
      `${analysis.documentId}-${findingIndex}`;


    setPromoting(
      key,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}/requirements`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                title:
                  finding.title,

                description:
                  finding.detail,

                category:
                  finding.requirementCategory
                  ??
                  "OTHER",

                mandatory:
                  finding.mandatory
                  ??
                  false,

                sourceDocumentId:
                  analysis.documentId,

                sourceReference:
                  finding.evidenceSnippet,

                dueDate:
                  null,

                assignedTo:
                  null,

                evidenceNotes:
                  "Created from local AI document analysis. Human review required.",
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
          : "Unable to create requirement from AI finding.",
      );


    } finally {

      setPromoting(
        null,
      );

    }

  }


  const canAnalyze =
    workspace?.writable ===
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
        max-w-[1450px]
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
            Local AI
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
            Document analysis
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
            Extract procurement requirements, risks,
            deadlines, qualifications and deliverables
            locally with Ollama. Uploaded document content
            remains inside the TenderPilot environment.
          </p>

        </div>


        {
          canAnalyze

            ? (

              <button
                type="button"
                onClick={
                  () => {
                    void analyzeAll();
                  }
                }
                disabled={
                  activeDocument !==
                  null
                  ||
                  !localAi?.reachable
                  ||
                  !localAi.modelAvailable
                }
                className="
                  inline-flex
                  h-11
                  items-center
                  justify-center
                  gap-2
                  rounded-xl
                  bg-[#14213d]
                  px-5
                  text-sm
                  font-semibold
                  text-white
                  disabled:opacity-40
                "
              >

                {
                  activeDocument ===
                  "ALL"

                    ? (

                      <LoaderCircle
                        className="
                          size-4
                          animate-spin
                        "
                      />

                    )

                    : (

                      <Sparkles
                        className="size-4"
                      />

                    )
                }

                Analyze all documents

              </button>

            )

            : null
        }

      </div>


      {
        localAi

          ? (

            <div
              className="
                mt-7
                grid
                gap-4
                md:grid-cols-3
              "
            >

              <StatusCard
                icon={Server}
                label="Ollama"
                value={
                  localAi.reachable
                    ? "Reachable"
                    : "Offline"
                }
                detail={
                  localAi.baseUrl
                }
              />


              <StatusCard
                icon={Bot}
                label="Model"
                value={
                  localAi.model
                }
                detail={
                  localAi.modelAvailable
                    ? "Available locally"
                    : "Model not installed"
                }
              />


              <StatusCard
                icon={ShieldCheck}
                label="Data boundary"
                value="Local only"
                detail="Organization-private"
              />

            </div>

          )

          : null
      }


      {
        localAi
        &&
        localAi.reachable
        &&
        !localAi.modelAvailable

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
                text-amber-900
              "
            >

              <p
                className="font-semibold"
              >
                Local model is not installed.
              </p>


              <p
                className="mt-2"
              >
                Prepare it locally with:
              </p>


              <code
                className="
                  mt-2
                  block
                  rounded-lg
                  bg-white
                  px-3
                  py-2
                  font-mono
                  text-xs
                "
              >
                ./scripts/prepare-local-ai.sh
              </code>

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
                items-start
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
                  mt-0.5
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


      {
        workspace
        &&
        !workspace.writable

          ? (

            <div
              className="
                mt-6
                flex
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

              <AlertTriangle
                className="
                  size-5
                  shrink-0
                "
              />

              Local AI writes are locked because the
              bid decision is NO-GO. Existing analysis
              remains readable.

            </div>

          )

          : null
      }


      <div
        className="
          mt-7
          space-y-5
        "
      >

        {
          analyses.map(
            (
              analysis,
            ) => (

              <article
                key={
                  analysis.documentId
                }
                className="
                  overflow-hidden
                  rounded-3xl
                  border
                  border-slate-200
                  bg-white
                "
              >

                <div
                  className="
                    flex
                    flex-col
                    gap-5
                    p-6
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

                      <AnalysisStatus
                        status={
                          analysis.status
                        }
                      />


                      <span
                        className="
                          rounded-full
                          bg-slate-100
                          px-2.5
                          py-1
                          text-[10px]
                          font-semibold
                          text-slate-600
                        "
                      >
                        {
                          formatEnum(
                            analysis.documentCategory,
                          )
                        }
                      </span>


                      {
                        analysis.confidence !==
                        null

                          ? (

                            <span
                              className="
                                rounded-full
                                bg-violet-50
                                px-2.5
                                py-1
                                text-[10px]
                                font-semibold
                                text-violet-700
                              "
                            >
                              Confidence
                              {" "}
                              {
                                analysis.confidence
                              }
                              %
                            </span>

                          )

                          : null
                      }

                    </div>


                    <div
                      className="
                        mt-4
                        flex
                        items-center
                        gap-3
                      "
                    >

                      <FileSearch
                        className="
                          size-5
                          shrink-0
                          text-[#2f6fed]
                        "
                      />


                      <h2
                        className="
                          truncate
                          text-lg
                          font-semibold
                          text-slate-950
                        "
                      >
                        {
                          analysis.filename
                        }
                      </h2>

                    </div>


                    {
                      analysis.summary

                        ? (

                          <p
                            className="
                              mt-4
                              max-w-4xl
                              text-sm
                              leading-6
                              text-slate-600
                            "
                          >
                            {
                              analysis.summary
                            }
                          </p>

                        )

                        : null
                    }


                    {
                      analysis.errorMessage

                        ? (

                          <div
                            className="
                              mt-4
                              rounded-xl
                              bg-red-50
                              p-3
                              text-xs
                              leading-5
                              text-red-700
                            "
                          >
                            {
                              analysis.errorMessage
                            }
                          </div>

                        )

                        : null
                    }


                    {
                      analysis.extractedCharacterCount !==
                      null

                        ? (

                          <p
                            className="
                              mt-4
                              text-xs
                              text-slate-400
                            "
                          >
                            Extracted
                            {" "}
                            {
                              analysis.extractedCharacterCount
                                .toLocaleString()
                            }
                            {" "}
                            characters
                            {
                              analysis.extractionTruncated
                                ? " · extraction capped"
                                : ""
                            }
                            {
                              analysis.detectedMediaType
                                ? ` · ${analysis.detectedMediaType}`
                                : ""
                            }
                          </p>

                        )

                        : null
                    }

                  </div>


                  {
                    canAnalyze

                      ? (

                        <button
                          type="button"
                          onClick={
                            () => {
                              void analyzeDocument(
                                analysis.documentId,
                              );
                            }
                          }
                          disabled={
                            activeDocument !==
                            null
                            ||
                            !localAi?.reachable
                            ||
                            !localAi.modelAvailable
                          }
                          className="
                            inline-flex
                            h-10
                            shrink-0
                            items-center
                            justify-center
                            gap-2
                            rounded-xl
                            border
                            border-slate-200
                            bg-white
                            px-4
                            text-sm
                            font-semibold
                            text-slate-700
                            disabled:opacity-40
                          "
                        >

                          {
                            activeDocument ===
                            analysis.documentId

                              ? (

                                <LoaderCircle
                                  className="
                                    size-4
                                    animate-spin
                                  "
                                />

                              )

                              : (

                                <RefreshCw
                                  className="size-4"
                                />

                              )
                          }

                          {
                            analysis.status ===
                            "NOT_ANALYZED"
                              ? "Analyze"
                              : "Re-analyze"
                          }

                        </button>

                      )

                      : null
                  }

                </div>


                {
                  analysis.findings.length >
                  0

                    ? (

                      <div
                        className="
                          border-t
                          border-slate-100
                          bg-slate-50/50
                          p-6
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
                          Structured findings
                        </p>


                        <div
                          className="
                            mt-4
                            grid
                            gap-3
                            lg:grid-cols-2
                          "
                        >

                          {
                            analysis.findings.map(
                              (
                                finding,
                                index,
                              ) => {

                                const promotionKey =
                                  `${analysis.documentId}-${index}`;


                                return (

                                  <div
                                    key={
                                      promotionKey
                                    }
                                    className="
                                      rounded-2xl
                                      border
                                      border-slate-200
                                      bg-white
                                      p-4
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
                                          formatEnum(
                                            finding.type,
                                          )
                                        }
                                      </span>


                                      {
                                        finding.mandatory

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

                                    </div>


                                    <p
                                      className="
                                        mt-3
                                        text-sm
                                        font-semibold
                                        text-slate-900
                                      "
                                    >
                                      {
                                        finding.title
                                      }
                                    </p>


                                    <p
                                      className="
                                        mt-2
                                        text-sm
                                        leading-6
                                        text-slate-600
                                      "
                                    >
                                      {
                                        finding.detail
                                      }
                                    </p>


                                    {
                                      finding.evidenceSnippet

                                        ? (

                                          <div
                                            className="
                                              mt-3
                                              rounded-xl
                                              bg-slate-50
                                              p-3
                                              text-xs
                                              leading-5
                                              text-slate-500
                                            "
                                          >
                                            Evidence:
                                            {" "}
                                            {
                                              finding.evidenceSnippet
                                            }
                                          </div>

                                        )

                                        : null
                                    }


                                    {
                                      workspace?.writable
                                      &&
                                      (
                                        finding.type ===
                                        "REQUIREMENT"
                                        ||
                                        finding.type ===
                                        "DEADLINE"
                                        ||
                                        finding.type ===
                                        "DELIVERABLE"
                                        ||
                                        finding.type ===
                                        "QUALIFICATION"
                                      )

                                        ? (

                                          <button
                                            type="button"
                                            onClick={
                                              () => {
                                                void promoteFinding(
                                                  analysis,
                                                  index,
                                                );
                                              }
                                            }
                                            disabled={
                                              promoting ===
                                              promotionKey
                                            }
                                            className="
                                              mt-4
                                              inline-flex
                                              h-9
                                              items-center
                                              gap-2
                                              rounded-xl
                                              border
                                              border-slate-200
                                              px-3
                                              text-xs
                                              font-semibold
                                              text-slate-700
                                              disabled:opacity-50
                                            "
                                          >

                                            {
                                              promoting ===
                                              promotionKey

                                                ? (

                                                  <LoaderCircle
                                                    className="
                                                      size-3.5
                                                      animate-spin
                                                    "
                                                  />

                                                )

                                                : (

                                                  <Plus
                                                    className="size-3.5"
                                                  />

                                                )
                                            }

                                            Add to requirements

                                          </button>

                                        )

                                        : null
                                    }

                                  </div>

                                );

                              },
                            )
                          }

                        </div>

                      </div>

                    )

                    : null
                }

              </article>

            ),
          )
        }


        {
          analyses.length ===
          0

            ? (

              <div
                className="
                  rounded-3xl
                  border
                  border-dashed
                  border-slate-300
                  bg-white
                  px-6
                  py-16
                  text-center
                "
              >

                <FileSearch
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
                  No bid documents available
                </p>


                <p
                  className="
                    mt-2
                    text-sm
                    text-slate-500
                  "
                >
                  Upload bid documents in the preparation
                  workspace before running local AI analysis.
                </p>

              </div>

            )

            : null
        }

      </div>


      <div
        className="
          mt-6
          rounded-2xl
          border
          border-slate-200
          bg-slate-50
          p-4
          text-xs
          leading-5
          text-slate-500
        "
      >

        <ShieldCheck
          className="
            mb-2
            size-4
            text-slate-500
          "
        />

        AI findings are decision-support suggestions.
        TenderPilot does not automatically mark requirements
        satisfied or make GO/NO-GO decisions from model output.
        Human review remains authoritative.

      </div>

    </div>

  );

}


function StatusCard({

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

    <div
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
          truncate
          text-sm
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
          text-slate-400
        "
      >
        {
          detail
        }
      </p>

    </div>

  );

}


function AnalysisStatus({

  status,

}: {

  status:
    BidDocumentAiAnalysis["status"];

}) {

  const icon =
    status ===
      "COMPLETED"
      ? CheckCircle2
      : status ===
        "FAILED"
        ? AlertTriangle
        : FileSearch;


  const Icon =
    icon;


  return (

    <span
      className="
        inline-flex
        items-center
        gap-1.5
        rounded-full
        bg-slate-100
        px-2.5
        py-1
        text-[10px]
        font-semibold
        text-slate-700
      "
    >

      <Icon
        className="size-3"
      />

      {
        formatEnum(
          status,
        )
      }

    </span>

  );

}
