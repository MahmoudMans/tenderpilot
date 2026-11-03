"use client";


import {

  ArrowLeft,

  Bot,

  BookOpen,

  CheckCircle2,

  Database,

  FileSearch,

  LoaderCircle,

  MessageSquareText,

  RefreshCw,

  Search,

  Server,

  ShieldCheck,

  TriangleAlert,

} from "lucide-react";


import type {

  LucideIcon,

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


import type {

  BidWorkspace,

} from "@/features/bid-workspace/bid-workspace-types";


import type {

  BidRagAnswer,

  BidRagIndexStatus,

  BidRagRebuildResult,

  RagStatus,

} from "@/features/rag/rag-types";


export default function BidDocumentQaPage() {

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

    ragStatus,

    setRagStatus,

  ] = useState<
    RagStatus
    |
    null
  >(
    null,
  );


  const [

    indexStatus,

    setIndexStatus,

  ] = useState<
    BidRagIndexStatus
    |
    null
  >(
    null,
  );


  const [

    history,

    setHistory,

  ] = useState<
    BidRagAnswer[]
  >(
    [],
  );


  const [

    latestAnswer,

    setLatestAnswer,

  ] = useState<
    BidRagAnswer
    |
    null
  >(
    null,
  );


  const [

    question,

    setQuestion,

  ] = useState(
    "",
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    rebuilding,

    setRebuilding,

  ] = useState(
    false,
  );


  const [

    asking,

    setAsking,

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


      Promise.all([

        api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}`,
        ),

        api<RagStatus>(
          "/api/v1/rag/status",
        ),

        api<BidRagIndexStatus>(
          `/api/v1/rag/bid-workspaces/${tenderId}/index`,
        ),

        api<BidRagAnswer[]>(
          `/api/v1/rag/bid-workspaces/${tenderId}/history`,
        ),

      ])
        .then(
          (
            [
              workspaceResult,
              statusResult,
              indexResult,
              historyResult,
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


            setRagStatus(
              statusResult,
            );


            setIndexStatus(
              indexResult,
            );


            setHistory(
              historyResult,
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
                  : "Unable to load private document Q&A.",
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


  async function rebuildIndex() {

    setRebuilding(
      true,
    );

    setError(
      null,
    );


    try {

      const result =
        await api<BidRagRebuildResult>(
          `/api/v1/rag/bid-workspaces/${tenderId}/rebuild`,
          {
            method:
              "POST",
          },
        );


      setIndexStatus({
        documentCount:
          result.totalDocuments,

        indexedDocumentCount:
          result.indexedDocuments,

        chunkCount:
          result.chunks,

        ready:
          result.chunks >
          0,

        embeddingModel:
          result.embeddingModel,
      });


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to rebuild the private RAG index.",
      );


    } finally {

      setRebuilding(
        false,
      );

    }

  }


  async function askQuestion() {

    const cleanQuestion =
      question.trim();


    if (
      cleanQuestion.length <
      3
    ) {

      setError(
        "Enter a question about the bid documents.",
      );

      return;
    }


    setAsking(
      true,
    );

    setError(
      null,
    );


    try {

      const answer =
        await api<BidRagAnswer>(
          `/api/v1/rag/bid-workspaces/${tenderId}/ask`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                question:
                  cleanQuestion,

                topK:
                  5,
              }),
          },
        );


      setLatestAnswer(
        answer,
      );


      setHistory(
        (
          current,
        ) => [

          answer,

          ...current.filter(
            (
              item,
            ) =>
              item.id !==
              answer.id,
          ),

        ].slice(
          0,
          25,
        ),
      );


      setQuestion(
        "",
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to answer the question.",
      );


    } finally {

      setAsking(
        false,
      );

    }

  }


  const canRebuild =
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


  const canAsk =
    workspace?.writable ===
      true;


  const modelsReady =
    ragStatus?.enabled ===
      true
    &&
    ragStatus.reachable
    &&
    ragStatus.generationModelAvailable
    &&
    ragStatus.embeddingModelAvailable;


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
        max-w-[1400px]
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
            Private RAG
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
            Ask your bid documents
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
            Ask questions against the organization&apos;s
            private tender documents. Answers are generated
            only from retrieved document evidence and include
            exact source citations.
          </p>

        </div>


        {
          canRebuild

            ? (

              <button
                type="button"
                onClick={
                  () => {
                    void rebuildIndex();
                  }
                }
                disabled={
                  rebuilding
                  ||
                  !modelsReady
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
                  disabled:opacity-40
                "
              >

                {
                  rebuilding

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

                Rebuild document index

              </button>

            )

            : null
        }

      </div>


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


      <div
        className="
          mt-7
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        <StatusCard
          icon={Server}
          label="Ollama"
          value={
            ragStatus?.reachable
              ? "Reachable"
              : "Offline"
          }
          detail={
            ragStatus?.baseUrl
            ??
            "Local provider"
          }
        />


        <StatusCard
          icon={Bot}
          label="Generation"
          value={
            ragStatus?.generationModel
            ??
            "Unknown"
          }
          detail={
            ragStatus?.generationModelAvailable
              ? "Available"
              : "Model missing"
          }
        />


        <StatusCard
          icon={Search}
          label="Embeddings"
          value={
            ragStatus?.embeddingModel
            ??
            "Unknown"
          }
          detail={
            ragStatus?.embeddingModelAvailable
              ? "Available"
              : "Model missing"
          }
        />


        <StatusCard
          icon={Database}
          label="Private index"
          value={
            indexStatus
              ? `${indexStatus.chunkCount} chunks`
              : "Unavailable"
          }
          detail={
            indexStatus
              ? `${indexStatus.indexedDocumentCount}/${indexStatus.documentCount} documents`
              : "PostgreSQL"
          }
        />

      </div>


      {
        !modelsReady

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
                Local RAG models are not ready.
              </p>


              <p
                className="mt-2"
              >
                Prepare both local models with:
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
        workspace
        &&
        !workspace.writable

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
              The bid is currently NO-GO. Existing Q&A history
              remains readable, but new retrieval/generation is locked.
            </div>

          )

          : null
      }


      <div
        className="
          mt-7
          grid
          gap-6
          xl:grid-cols-[1fr_370px]
        "
      >

        <div>

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

              <MessageSquareText
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
                  Ask a grounded question
                </h2>


                <p
                  className="
                    mt-1
                    text-xs
                    text-slate-500
                  "
                >
                  Retrieval uses only documents in this private bid workspace.
                </p>

              </div>

            </div>


            <textarea
              value={
                question
              }
              onChange={
                (
                  event,
                ) =>
                  setQuestion(
                    event.target.value,
                  )
              }
              disabled={
                !canAsk
              }
              rows={4}
              aria-label="Your question about the tender documents"
              placeholder="Example: What technical references must the bidder provide?"
              className="
                mt-6
                w-full
                rounded-2xl
                border
                border-slate-200
                p-4
                text-sm
                outline-none
                focus:border-[#2f6fed]
                disabled:bg-slate-50
              "
            />


            <button
              type="button"
              onClick={
                () => {
                  void askQuestion();
                }
              }
              disabled={
                asking
                ||
                !canAsk
                ||
                !modelsReady
                ||
                !indexStatus?.ready
              }
              className="
                mt-4
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
                disabled:opacity-40
              "
            >

              {
                asking

                  ? (

                    <LoaderCircle
                      className="
                        size-4
                        animate-spin
                      "
                    />

                  )

                  : (

                    <FileSearch
                      className="size-4"
                    />

                  )
              }

              Ask documents

            </button>

          </section>


          {
            latestAnswer

              ? (

                <AnswerCard
                  answer={
                    latestAnswer
                  }
                  title="Latest answer"
                />

              )

              : null
          }


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

              <BookOpen
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
                  Q&A history
                </h2>


                <p
                  className="
                    mt-1
                    text-xs
                    text-slate-500
                  "
                >
                  Private history with frozen citation excerpts.
                </p>

              </div>

            </div>


            <div
              className="
                mt-5
                space-y-4
              "
            >

              {
                history.map(
                  (
                    answer,
                  ) => (

                    <AnswerCard
                      key={
                        answer.id
                      }
                      answer={
                        answer
                      }
                    />

                  ),
                )
              }


              {
                history.length ===
                0

                  ? (

                    <p
                      className="
                        rounded-2xl
                        border
                        border-dashed
                        border-slate-300
                        p-8
                        text-center
                        text-sm
                        text-slate-400
                      "
                    >
                      No document questions have been asked yet.
                    </p>

                  )

                  : null
              }

            </div>

          </section>

        </div>


        <aside
          className="
            space-y-5
          "
        >

          <div
            className="
              rounded-3xl
              border
              border-slate-200
              bg-white
              p-6
            "
          >

            <ShieldCheck
              className="
                size-5
                text-[#2f6fed]
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
              Evidence-only answers
            </p>


            <p
              className="
                mt-2
                text-xs
                leading-5
                text-slate-500
              "
            >
              The model receives only the top retrieved
              private document chunks. A supported answer
              must reference at least one valid source ID.
            </p>

          </div>


          <div
            className="
              rounded-3xl
              border
              border-slate-200
              bg-white
              p-6
            "
          >

            <Database
              className="
                size-5
                text-[#2f6fed]
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
              Retrieval index
            </p>


            <dl
              className="
                mt-4
                space-y-3
                text-xs
              "
            >

              <MetricRow
                label="Documents"
                value={
                  indexStatus?.documentCount
                    .toLocaleString()
                  ??
                  "0"
                }
              />


              <MetricRow
                label="Indexed"
                value={
                  indexStatus?.indexedDocumentCount
                    .toLocaleString()
                  ??
                  "0"
                }
              />


              <MetricRow
                label="Chunks"
                value={
                  indexStatus?.chunkCount
                    .toLocaleString()
                  ??
                  "0"
                }
              />


              <MetricRow
                label="Vector store"
                value="Private PostgreSQL"
              />

            </dl>

          </div>


          <div
            className="
              rounded-3xl
              bg-slate-50
              p-6
            "
          >

            <CheckCircle2
              className="
                size-5
                text-slate-500
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
              Human authority preserved
            </p>


            <p
              className="
                mt-2
                text-xs
                leading-5
                text-slate-500
              "
            >
              Document Q&A cannot change the bid decision,
              satisfy requirements, or modify procurement data.
            </p>

          </div>

        </aside>

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

    </article>

  );

}


function AnswerCard({

  answer,

  title,

}: {

  answer:
    BidRagAnswer;

  title?:
    string;

}) {

  return (

    <article
      className="
        mt-6
        rounded-3xl
        border
        border-slate-200
        bg-white
        p-6
      "
    >

      {
        title

          ? (

            <p
              className="
                text-[10px]
                font-semibold
                uppercase
                tracking-[0.13em]
                text-[#2f6fed]
              "
            >
              {
                title
              }
            </p>

          )

          : null
      }


      <p
        className="
          mt-3
          text-sm
          font-semibold
          text-slate-950
        "
      >
        {
          answer.question
        }
      </p>


      <div
        className="
          mt-4
          flex
          items-start
          gap-3
        "
      >

        <Bot
          className="
            mt-0.5
            size-5
            shrink-0
            text-[#2f6fed]
          "
        />


        <p
          className="
            text-sm
            leading-7
            text-slate-600
          "
        >
          {
            answer.answer
          }
        </p>

      </div>


      <div
        className="
          mt-4
          flex
          flex-wrap
          gap-2
        "
      >

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
            answer.supported
              ? "Evidence supported"
              : "Insufficient evidence"
          }
        </span>


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
            answer.citations.length
          }
          {" "}
          citations
        </span>

      </div>


      {
        answer.citations.length >
        0

          ? (

            <div
              className="
                mt-5
                space-y-3
              "
            >

              {
                answer.citations.map(
                  (
                    citation,
                  ) => (

                    <div
                      key={
                        `${answer.id}-${citation.sourceId}`
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
                          font-semibold
                          text-slate-900
                        "
                      >
                        [
                        {
                          citation.sourceId
                        }
                        ]
                        {" "}
                        {
                          citation.filename
                        }
                        {" · "}
                        chunk
                        {" "}
                        {
                          citation.chunkIndex
                        }
                      </p>


                      <p
                        className="
                          mt-2
                          text-xs
                          leading-5
                          text-slate-500
                        "
                      >
                        {
                          citation.excerpt
                        }
                      </p>


                      <p
                        className="
                          mt-2
                          text-[10px]
                          text-slate-400
                        "
                      >
                        Similarity:
                        {" "}
                        {
                          (
                            citation.similarity
                            *
                            100
                          ).toFixed(
                            1,
                          )
                        }
                        %
                      </p>

                    </div>

                  ),
                )
              }

            </div>

          )

          : null
      }

    </article>

  );

}


function MetricRow({

  label,

  value,

}: {

  label:
    string;

  value:
    string;

}) {

  return (

    <div
      className="
        flex
        items-center
        justify-between
        gap-4
      "
    >

      <dt
        className="
          text-slate-400
        "
      >
        {
          label
        }
      </dt>


      <dd
        className="
          text-right
          font-semibold
          text-slate-700
        "
      >
        {
          value
        }
      </dd>

    </div>

  );

}
