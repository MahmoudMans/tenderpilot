"use client";


import {

  AlertTriangle,

  ArrowLeft,
  Download,

  FileCheck2,

  FileText,

  FolderOpen,

  Gauge,

  LoaderCircle,

  Pencil,

  Plus,

  Save,

  ShieldAlert,

  Trash2,

  Upload,

} from "lucide-react";


import Link from "next/link";


import {

  useCallback,

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

  formatDate,

  formatEnum,

  formatMoney,

  formatValueRange,

} from "@/features/procurement/procurement-format";


import type {

  BidDocumentCategory,

  BidDocumentContent,

  BidRequirementCategory,

  BidRequirementStatus,

  BidWorkspace,
} from "@/features/bid-workspace/bid-workspace-types";

import { ConfirmDialog } from "@/components/ui/confirm-dialog";

import {
  RequirementEditDialog,
  type RequirementPatch,
} from "@/features/bid-workspace/requirement-edit-dialog";

import {
  NO_REQUIREMENT_FILTERS,
  RequirementFilterBar,
  filterRequirements,
  type RequirementFilters,
} from "@/features/bid-workspace/requirement-filters";

import { ownerAliases } from "@/lib/owner";


const DOCUMENT_CATEGORIES: BidDocumentCategory[] = [

  "TENDER_NOTICE",

  "SPECIFICATIONS",

  "ADMINISTRATIVE",

  "TECHNICAL",

  "PRICING",

  "FORMS",

  "ANNEX",

  "INTERNAL_DRAFT",

  "EVIDENCE",

  "OTHER",

];


const REQUIREMENT_CATEGORIES: BidRequirementCategory[] = [

  "ADMINISTRATIVE",

  "TECHNICAL",

  "COMMERCIAL",

  "FINANCIAL",

  "LEGAL",

  "SUBMISSION",

  "OTHER",

];


const REQUIREMENT_STATUSES: BidRequirementStatus[] = [

  "NOT_REVIEWED",

  "IN_PROGRESS",

  "SATISFIED",

  "GAP",

  "NOT_APPLICABLE",

];


export default function BidWorkspacePage() {

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

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    busy,

    setBusy,

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


  const [

    selectedFile,

    setSelectedFile,

  ] = useState<
    File
    |
    null
  >(
    null,
  );


  const [

    documentCategory,

    setDocumentCategory,

  ] = useState<BidDocumentCategory>(
    "SPECIFICATIONS",
  );


  const [

    documentNote,

    setDocumentNote,

  ] = useState(
    "",
  );


  const [

    requirementTitle,

    setRequirementTitle,

  ] = useState(
    "",
  );


  const [

    requirementDescription,

    setRequirementDescription,

  ] = useState(
    "",
  );


  const [

    requirementCategory,

    setRequirementCategory,

  ] = useState<BidRequirementCategory>(
    "TECHNICAL",
  );


  const [

    requirementMandatory,

    setRequirementMandatory,

  ] = useState(
    true,
  );


  const [

    requirementSourceDocument,

    setRequirementSourceDocument,

  ] = useState(
    "",
  );


  const [

    requirementSourceReference,

    setRequirementSourceReference,

  ] = useState(
    "",
  );


  const [
    pendingDelete,
    setPendingDelete,
  ] = useState<{
    kind: "document" | "requirement";
    id: string;
    label: string;
  } | null>(
    null,
  );

  const [
    editingRequirement,
    setEditingRequirement,
  ] = useState<BidWorkspace["requirements"][number] | null>(
    null,
  );

  const [
    requirementFilters,
    setRequirementFilters,
  ] = useState<RequirementFilters>(
    NO_REQUIREMENT_FILTERS,
  );

  const fetchWorkspace =
    useCallback(
      () =>
        api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}`,
        ),
      [
        api,
        tenderId,
      ],
    );


  useEffect(
    () => {

      let cancelled =
        false;


      fetchWorkspace()
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
                  : "Unable to load bid workspace.",
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
      fetchWorkspace,
    ],
  );


  async function uploadDocument() {

    if (
      selectedFile ===
      null
    ) {

      setError(
        "Choose a document first.",
      );

      return;
    }


    if (
      selectedFile.size >
      15 * 1024 * 1024
    ) {

      setError(
        "The maximum document size is 15 MiB.",
      );

      return;
    }


    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const contentBase64 =
        await fileToBase64(
          selectedFile,
        );


      const updated =
        await api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}/documents`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                originalFilename:
                  selectedFile.name,

                contentType:
                  selectedFile.type
                  ||
                  "application/octet-stream",

                contentBase64,

                category:
                  documentCategory,

                note:
                  documentNote,
              }),
          },
        );


      setWorkspace(
        updated,
      );


      setSelectedFile(
        null,
      );


      setDocumentNote(
        "",
      );


      const input =
        document.getElementById(
          "bid-document-file",
        ) as HTMLInputElement | null;


      if (
        input
      ) {

        input.value =
          "";

      }


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to upload bid document.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function downloadDocument(
    documentId:
      string,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const result =
        await api<BidDocumentContent>(
          `/api/v1/bid-workspaces/${tenderId}/documents/${documentId}/content`,
        );


      const binary =
        window.atob(
          result.contentBase64,
        );


      const bytes =
        new Uint8Array(
          binary.length,
        );


      for (
        let index = 0;
        index < binary.length;
        index += 1
      ) {

        bytes[index] =
          binary.charCodeAt(
            index,
          );

      }


      const blob =
        new Blob(
          [
            bytes,
          ],
          {
            type:
              result.contentType
              ??
              "application/octet-stream",
          },
        );


      const url =
        URL.createObjectURL(
          blob,
        );


      const link =
        document.createElement(
          "a",
        );


      link.href =
        url;

      link.download =
        result.originalFilename;


      document.body.appendChild(
        link,
      );


      link.click();

      link.remove();


      URL.revokeObjectURL(
        url,
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to download document.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function deleteDocument(
    documentId:
      string,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}/documents/${documentId}`,
          {
            method:
              "DELETE",
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
          : "Unable to delete document.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function createRequirement() {

    if (
      requirementTitle.trim() ===
      ""
    ) {

      setError(
        "Requirement title is required.",
      );

      return;
    }


    setBusy(
      true,
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
                  requirementTitle,

                description:
                  requirementDescription,

                category:
                  requirementCategory,

                mandatory:
                  requirementMandatory,

                sourceDocumentId:
                  requirementSourceDocument
                  ||
                  null,

                sourceReference:
                  requirementSourceReference,

                dueDate:
                  null,

                assignedTo:
                  null,

                evidenceNotes:
                  null,
              }),
          },
        );


      setWorkspace(
        updated,
      );


      setRequirementTitle(
        "",
      );


      setRequirementDescription(
        "",
      );


      setRequirementSourceDocument(
        "",
      );


      setRequirementSourceReference(
        "",
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to create requirement.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function updateRequirementStatus(
    requirementId:
      string,
    status:
      BidRequirementStatus,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}/requirements/${requirementId}`,
          {
            method:
              "PUT",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                status,
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
          : "Unable to update requirement.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function saveRequirement(
    requirementId: string,
    patch: RequirementPatch,
  ): Promise<string | null> {

    setBusy(
      true,
    );


    try {

      const updated =
        await api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}/requirements/${requirementId}`,
          {
            method:
              "PUT",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify(
                patch,
              ),
          },
        );


      setWorkspace(
        updated,
      );

      return null;


    } catch (
      cause
    ) {

      return cause instanceof Error
        ? cause.message
        : "Unable to save the requirement.";


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function deleteRequirement(
    requirementId:
      string,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidWorkspace>(
          `/api/v1/bid-workspaces/${tenderId}/requirements/${requirementId}`,
          {
            method:
              "DELETE",
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
          : "Unable to delete requirement.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  const myNames = ownerAliases(user);

  const visibleRequirements = filterRequirements(workspace?.requirements ?? [], requirementFilters, myNames);


  const canWrite =
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
      ||
      user?.role ===
        "CONTRIBUTOR"
    );


  const canDelete =
    workspace?.writable ===
      true
    &&
    (
      user?.role ===
        "OWNER"
      ||
      user?.role ===
        "BID_MANAGER"
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

          Bid decisions

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
            "Bid workspace is unavailable."
          }
        </div>

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

      <ConfirmDialog
        open={pendingDelete !== null}
        title={
          pendingDelete?.kind === "document"
            ? "Delete this document?"
            : "Delete this requirement?"
        }
        description={
          pendingDelete !== null
            ? `"${pendingDelete.label}" will be permanently removed from this bid workspace. This cannot be undone.`
            : ""
        }
        confirmLabel={
          pendingDelete?.kind === "document"
            ? "Delete document"
            : "Delete requirement"
        }
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => {
          if (pendingDelete !== null) {
            const target = pendingDelete;
            setPendingDelete(null);
            if (target.kind === "document") {
              void deleteDocument(target.id);
            } else {
              void deleteRequirement(target.id);
            }
          }
        }}
      />


      <RequirementEditDialog
        key={editingRequirement?.id ?? "none"}
        requirement={editingRequirement}
        documents={workspace.documents}
        onClose={() => setEditingRequirement(null)}
        onSave={saveRequirement}
      />


      <Link
        href={
          `/bid-decisions/${tenderId}`
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

        Go / No-Go decision

      </Link>


      <div
        className="
          mt-4
        "
      >

        <Link
          href={
            `/bid-workspaces/${tenderId}/ai`
          }
          className="
            inline-flex
            h-10
            items-center
            rounded-xl
            border
            border-slate-200
            bg-white
            px-4
            text-sm
            font-semibold
            text-slate-700
            hover:bg-slate-50
          "
        >
          Analyze documents with local AI
        </Link>


        <Link
          href={
            `/bid-workspaces/${tenderId}/qa`
          }
          className="
            ml-2
            inline-flex
            h-10
            items-center
            rounded-xl
            border
            border-slate-200
            bg-white
            px-4
            text-sm
            font-semibold
            text-slate-700
            hover:bg-slate-50
          "
        >
          Ask bid documents
        </Link>


        <Link
          href={
            `/bid-workspaces/${tenderId}/compliance`
          }
          className="
            ml-2
            inline-flex
            h-10
            items-center
            rounded-xl
            border
            border-slate-200
            bg-white
            px-4
            text-sm
            font-semibold
            text-slate-700
            hover:bg-slate-50
          "
        >
          Open compliance matrix
        </Link>


        <Link
          href={
            `/bid-workspaces/${tenderId}/planning`
          }
          className="
            ml-2
            inline-flex
            h-10
            items-center
            rounded-xl
            border
            border-slate-200
            bg-white
            px-4
            text-sm
            font-semibold
            text-slate-700
            hover:bg-slate-50
          "
        >
          Open bid plan
        </Link>

      </div>


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
            Bid preparation
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
            flex
            flex-wrap
            gap-2
          "
        >

          <Badge
            value={
              `Match ${workspace.matchScore}/100`
            }
          />


          <Badge
            value={
              formatEnum(
                workspace.matchGrade,
              )
            }
          />


          {
            workspace.decisionOutcome

              ? (

                <Badge
                  value={
                    `Decision ${formatEnum(workspace.decisionOutcome)}`
                  }
                />

              )

              : null
          }

        </div>

      </div>


      {
        !workspace.writable

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
                  Workspace locked
                </p>


                <p
                  className="mt-1"
                >
                  {
                    workspace.writeLockReason
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
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        <Metric
          icon={FolderOpen}
          label="Documents"
          value={
            workspace.statistics.documentCount
              .toLocaleString()
          }
          detail="Private bid files"
        />


        <Metric
          icon={FileCheck2}
          label="Requirements"
          value={
            workspace.statistics.requirementCount
              .toLocaleString()
          }
          detail={
            `${workspace.statistics.completedRequirementCount} completed`
          }
        />


        <Metric
          icon={AlertTriangle}
          label="Mandatory gaps"
          value={
            workspace.statistics.mandatoryGapCount
              .toLocaleString()
          }
          detail={
            `${workspace.statistics.gapRequirementCount} total gaps`
          }
        />


        <Metric
          icon={Gauge}
          label="Completion"
          value={
            `${workspace.statistics.completionPercentage}%`
          }
          detail="Satisfied or not applicable"
        />

      </div>


      <div
        className="
          mt-6
          grid
          gap-6
          xl:grid-cols-[0.9fr_1.1fr]
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

            <Upload
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
                Document library
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Stored locally and isolated by organization and tender.
              </p>

            </div>

          </div>


          {
            canWrite

              ? (

                <div
                  className="
                    mt-6
                    rounded-2xl
                    bg-slate-50
                    p-4
                  "
                >

                  <input
                    aria-label="Document file to upload"
                    id="bid-document-file"
                    type="file"
                    onChange={
                      (
                        event,
                      ) =>
                        setSelectedFile(
                          event.target.files?.[0]
                          ??
                          null,
                        )
                    }
                    className="
                      block
                      w-full
                      text-sm
                      text-slate-600
                    "
                  />


                  <div
                    className="
                      mt-4
                      grid
                      gap-3
                      md:grid-cols-2
                    "
                  >

                    <select
                      aria-label="Document category"
                      value={
                        documentCategory
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setDocumentCategory(
                            (event.target.value as BidDocumentCategory),
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

                      {
                        DOCUMENT_CATEGORIES.map(
                          (
                            category,
                          ) => (

                            <option
                              key={
                                category
                              }
                              value={
                                category
                              }
                            >
                              {
                                formatEnum(
                                  category,
                                )
                              }
                            </option>

                          ),
                        )
                      }

                    </select>


                    <input
                      value={
                        documentNote
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setDocumentNote(
                            event.target.value,
                          )
                      }
                      aria-label="Document note (optional)"
                      placeholder="Optional note"
                      className="
                        h-11
                        rounded-xl
                        border
                        border-slate-200
                        bg-white
                        px-3
                        text-sm
                      "
                    />

                  </div>


                  <button
                    type="button"
                    onClick={
                      () => {
                        void uploadDocument();
                      }
                    }
                    disabled={
                      busy
                      ||
                      selectedFile ===
                        null
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
                      text-sm
                      font-semibold
                      text-white
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

                          <Upload
                            className="size-4"
                          />

                        )
                    }

                    Upload document

                  </button>

                </div>

              )

              : null
          }


          <div
            className="
              mt-6
              space-y-3
            "
          >

            {
              workspace.documents.map(
                (
                  documentItem,
                ) => (

                  <article
                    key={
                      documentItem.id
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
                        items-start
                        justify-between
                        gap-4
                      "
                    >

                      <div
                        className="
                          min-w-0
                        "
                      >

                        <FileText
                          className="
                            size-5
                            text-[#2f6fed]
                          "
                        />


                        <p
                          className="
                            mt-3
                            truncate
                            text-sm
                            font-semibold
                            text-slate-900
                          "
                        >
                          {
                            documentItem.originalFilename
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
                            formatEnum(
                              documentItem.category,
                            )
                          }
                          {" · "}
                          {
                            formatBytes(
                              documentItem.sizeBytes,
                            )
                          }
                        </p>


                        {
                          documentItem.note

                            ? (

                              <p
                                className="
                                  mt-2
                                  text-xs
                                  leading-5
                                  text-slate-500
                                "
                              >
                                {
                                  documentItem.note
                                }
                              </p>

                            )

                            : null
                        }


                        <p
                          className="
                            mt-2
                            truncate
                            font-mono
                            text-[10px]
                            text-slate-400
                          "
                        >
                          SHA-256
                          {" "}
                          {
                            documentItem.sha256
                          }
                        </p>

                      </div>


                      <div
                        className="
                          flex
                          shrink-0
                          gap-2
                        "
                      >

                        <button
                          type="button"
                          onClick={
                            () => {
                              void downloadDocument(
                                documentItem.id,
                              );
                            }
                          }
                          disabled={
                            busy
                          }
                          title="Download"
                          className="
                            grid
                            size-9
                            place-items-center
                            rounded-xl
                            border
                            border-slate-200
                            text-slate-600
                          "
                        >
                          <Download
                            className="size-4"
                          />
                        </button>


                        {
                          canDelete

                            ? (

                              <button
                                type="button"
                                onClick={
                                  () => {
                                    setPendingDelete({
                                      kind: "document",
                                      id: documentItem.id,
                                      label: documentItem.originalFilename,
                                    });
                                  }
                                }
                                aria-label={`Delete document ${documentItem.originalFilename}`}
                                disabled={
                                  busy
                                }
                                title="Delete"
                                className="
                                  grid
                                  size-9
                                  place-items-center
                                  rounded-xl
                                  border
                                  border-red-200
                                  text-red-600
                                "
                              >
                                <Trash2
                                  className="size-4"
                                />
                              </button>

                            )

                            : null
                        }

                      </div>

                    </div>

                  </article>

                ),
              )
            }


            {
              workspace.documents.length ===
              0

                ? (

                  <div
                    className="
                      rounded-2xl
                      border
                      border-dashed
                      border-slate-300
                      p-8
                      text-center
                    "
                  >

                    <FolderOpen
                      className="
                        mx-auto
                        size-7
                        text-slate-300
                      "
                    />


                    <p
                      className="
                        mt-3
                        text-sm
                        text-slate-500
                      "
                    >
                      No bid documents uploaded yet.
                    </p>

                  </div>

                )

                : null
            }

          </div>

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

          <div
            className="
              flex
              items-center
              gap-3
            "
          >

            <FileCheck2
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
                Requirements workspace
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Track compliance, gaps and supporting evidence.
              </p>

            </div>

          </div>


          {
            canWrite

              ? (

                <div
                  className="
                    mt-6
                    rounded-2xl
                    bg-slate-50
                    p-4
                  "
                >

                  <input
                    value={
                      requirementTitle
                    }
                    onChange={
                      (
                        event,
                      ) =>
                        setRequirementTitle(
                          event.target.value,
                        )
                    }
                    aria-label="Requirement title"
                    placeholder="Requirement title"
                    className="
                      h-11
                      w-full
                      rounded-xl
                      border
                      border-slate-200
                      bg-white
                      px-3
                      text-sm
                    "
                  />


                  <textarea
                    value={
                      requirementDescription
                    }
                    onChange={
                      (
                        event,
                      ) =>
                        setRequirementDescription(
                          event.target.value,
                        )
                    }
                    aria-label="Requirement description"
                    placeholder="Requirement description"
                    rows={3}
                    className="
                      mt-3
                      w-full
                      rounded-xl
                      border
                      border-slate-200
                      bg-white
                      p-3
                      text-sm
                    "
                  />


                  <div
                    className="
                      mt-3
                      grid
                      gap-3
                      md:grid-cols-2
                    "
                  >

                    <select
                      aria-label="Requirement category"
                      value={
                        requirementCategory
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setRequirementCategory(
                            (event.target.value as BidRequirementCategory),
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

                      {
                        REQUIREMENT_CATEGORIES.map(
                          (
                            category,
                          ) => (

                            <option
                              key={
                                category
                              }
                              value={
                                category
                              }
                            >
                              {
                                formatEnum(
                                  category,
                                )
                              }
                            </option>

                          ),
                        )
                      }

                    </select>


                    <select
                      aria-label="Source document"
                      value={
                        requirementSourceDocument
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setRequirementSourceDocument(
                            event.target.value,
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
                      <option value="">
                        No source document
                      </option>

                      {
                        workspace.documents.map(
                          (
                            documentItem,
                          ) => (

                            <option
                              key={
                                documentItem.id
                              }
                              value={
                                documentItem.id
                              }
                            >
                              {
                                documentItem.originalFilename
                              }
                            </option>

                          ),
                        )
                      }
                    </select>


                    <input
                      value={
                        requirementSourceReference
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setRequirementSourceReference(
                            event.target.value,
                          )
                      }
                      aria-label="Page or clause reference"
                      placeholder="Page / clause reference"
                      className="
                        h-11
                        rounded-xl
                        border
                        border-slate-200
                        bg-white
                        px-3
                        text-sm
                      "
                    />


                    <label
                      className="
                        flex
                        h-11
                        items-center
                        gap-3
                        rounded-xl
                        border
                        border-slate-200
                        bg-white
                        px-3
                        text-sm
                        text-slate-600
                      "
                    >

                      <input
                        type="checkbox"
                        checked={
                          requirementMandatory
                        }
                        onChange={
                          (
                            event,
                          ) =>
                            setRequirementMandatory(
                              event.target.checked,
                            )
                        }
                      />

                      Mandatory requirement

                    </label>

                  </div>


                  <button
                    type="button"
                    onClick={
                      () => {
                        void createRequirement();
                      }
                    }
                    disabled={
                      busy
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
                      text-sm
                      font-semibold
                      text-white
                      disabled:opacity-50
                    "
                  >

                    <Plus
                      className="size-4"
                    />

                    Add requirement

                  </button>

                </div>

              )

              : null
          }


          <div
            className="
              mt-6
              space-y-3
            "
          >

            {
              workspace.requirements.length > 0

                ? (

                  <RequirementFilterBar
                    requirements={workspace.requirements}
                    visible={visibleRequirements}
                    filters={requirementFilters}
                    onChange={setRequirementFilters}
                    aliases={myNames}
                  />

                )

                : null
            }


            {
              visibleRequirements.map(
                (
                  requirement,
                ) => (

                  <article
                    key={
                      requirement.id
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
                        flex-col
                        gap-4
                        md:max-xl:flex-row
                        md:max-xl:items-start
                        md:max-xl:justify-between
                        2xl:flex-row
                        2xl:items-start
                        2xl:justify-between
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

                          <StatusBadge
                            status={
                              requirement.status
                            }
                          />


                          {
                            requirement.mandatory

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
                                requirement.category,
                              )
                            }
                          </span>

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
                            requirement.title
                          }
                        </p>


                        {
                          requirement.description

                            ? (

                              <p
                                className="
                                  mt-2
                                  text-sm
                                  leading-6
                                  text-slate-500
                                "
                              >
                                {
                                  requirement.description
                                }
                              </p>

                            )

                            : null
                        }


                        <div
                          className="
                            mt-3
                            flex
                            flex-wrap
                            gap-x-4
                            gap-y-1
                            text-xs
                            text-slate-400
                          "
                        >

                          {
                            requirement.sourceDocumentName

                              ? (

                                <span>
                                  Source:
                                  {" "}
                                  {
                                    requirement.sourceDocumentName
                                  }
                                </span>

                              )

                              : null
                          }


                          {
                            requirement.sourceReference

                              ? (

                                <span>
                                  Ref:
                                  {" "}
                                  {
                                    requirement.sourceReference
                                  }
                                </span>

                              )

                              : null
                          }


                          {
                            requirement.assignedTo

                              ? (

                                <span>
                                  Owner:
                                  {" "}
                                  {
                                    requirement.assignedTo
                                  }
                                </span>

                              )

                              : null
                          }


                          {
                            requirement.dueDate

                              ? (

                                <span>
                                  Due:
                                  {" "}
                                  {
                                    formatDate(
                                      requirement.dueDate,
                                    )
                                  }
                                </span>

                              )

                              : null
                          }

                        </div>


                        {
                          requirement.evidenceNotes

                            ? (

                              <p
                                className="
                                  mt-2
                                  text-xs
                                  leading-5
                                  text-slate-500
                                "
                              >
                                <span
                                  className="
                                    font-semibold
                                    text-slate-600
                                  "
                                >
                                  Evidence:
                                </span>
                                {" "}
                                {
                                  requirement.evidenceNotes
                                }
                              </p>

                            )

                            : null
                        }

                      </div>


                      <div
                        className="
                          flex
                          shrink-0
                          items-center
                          gap-2
                        "
                      >

                        {
                          canWrite

                            ? (

                              <select
                                aria-label={`Status of requirement ${requirement.title}`}
                                value={
                                  requirement.status
                                }
                                onChange={
                                  (
                                    event,
                                  ) => {
                                    void updateRequirementStatus(
                                      requirement.id,
                                      (event.target.value as BidRequirementStatus),
                                    );
                                  }
                                }
                                disabled={
                                  busy
                                }
                                className="
                                  h-9
                                  rounded-xl
                                  border
                                  border-slate-200
                                  bg-white
                                  px-2
                                  text-xs
                                "
                              >

                                {
                                  REQUIREMENT_STATUSES.map(
                                    (
                                      status,
                                    ) => (

                                      <option
                                        key={
                                          status
                                        }
                                        value={
                                          status
                                        }
                                      >
                                        {
                                          formatEnum(
                                            status,
                                          )
                                        }
                                      </option>

                                    ),
                                  )
                                }

                              </select>

                            )

                            : null
                        }


                        {
                          canWrite

                            ? (

                              <button
                                type="button"
                                onClick={
                                  () => {
                                    setEditingRequirement(
                                      requirement,
                                    );
                                  }
                                }
                                aria-label={`Edit requirement ${requirement.title}`}
                                title="Edit requirement"
                                disabled={
                                  busy
                                }
                                className="
                                  grid
                                  size-9
                                  place-items-center
                                  rounded-xl
                                  border
                                  border-slate-200
                                  text-slate-600
                                  disabled:opacity-50
                                "
                              >
                                <Pencil
                                  className="size-4"
                                />
                              </button>

                            )

                            : null
                        }


                        {
                          canDelete

                            ? (

                              <button
                                type="button"
                                onClick={
                                  () => {
                                    setPendingDelete({
                                      kind: "requirement",
                                      id: requirement.id,
                                      label: requirement.title,
                                    });
                                  }
                                }
                                aria-label={`Delete requirement ${requirement.title}`}
                                disabled={
                                  busy
                                }
                                className="
                                  grid
                                  size-9
                                  place-items-center
                                  rounded-xl
                                  border
                                  border-red-200
                                  text-red-600
                                "
                              >
                                <Trash2
                                  className="size-4"
                                />
                              </button>

                            )

                            : null
                        }

                      </div>

                    </div>

                  </article>

                ),
              )
            }


            {
              visibleRequirements.length ===
              0

                ? (

                  <div
                    className="
                      rounded-2xl
                      border
                      border-dashed
                      border-slate-300
                      p-8
                      text-center
                    "
                  >

                    <FileCheck2
                      className="
                        mx-auto
                        size-7
                        text-slate-300
                      "
                    />


                    <p
                      className="
                        mt-3
                        text-sm
                        text-slate-500
                      "
                    >
                      {
                        workspace.requirements.length === 0
                          ? "No requirements recorded yet."
                          : "No requirements match these filters."
                      }
                    </p>

                  </div>

                )

                : null
            }

          </div>

        </section>

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

          <Save
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
              Procurement context
            </h2>


            <p
              className="
                mt-1
                text-xs
                text-slate-500
              "
            >
              Shared procurement data is referenced here but never copied into private document storage.
            </p>

          </div>

        </div>


        <div
          className="
            mt-5
            grid
            gap-4
            md:grid-cols-3
          "
        >

          <SmallMetric
            label="Estimated / awarded value"
            value={
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
          />


          <SmallMetric
            label="Data quality"
            value={
              workspace.opportunity.dataQualityScore ===
              null

                ? "Not assessed"

                : `${workspace.opportunity.dataQualityScore}/100`
            }
          />


          <SmallMetric
            label="Deadline"
            value={
              workspace.opportunity.submissionDeadline

                ? formatDate(
                    workspace.opportunity.submissionDeadline,
                  )

                : "Not disclosed"
            }
          />

        </div>

      </section>

    </div>

  );

}


function fileToBase64(
  file:
    File,
) {

  return new Promise<string>(
    (
      resolve,
      reject,
    ) => {

      const reader =
        new FileReader();


      reader.onload =
        () => {

          if (
            typeof reader.result !==
            "string"
          ) {

            reject(
              new Error(
                "Unable to encode document.",
              ),
            );

            return;
          }


          const comma =
            reader.result.indexOf(
              ",",
            );


          resolve(
            comma >= 0
              ? reader.result.slice(
                  comma + 1,
                )
              : reader.result,
          );

        };


      reader.onerror =
        () => {

          reject(
            new Error(
              "Unable to read document.",
            ),
          );

        };


      reader.readAsDataURL(
        file,
      );

    },
  );

}


function formatBytes(
  bytes:
    number,
) {

  if (
    bytes <
    1024
  ) {

    return `${bytes} B`;

  }


  if (
    bytes <
    1024 * 1024
  ) {

    return `${(bytes / 1024).toFixed(1)} KiB`;

  }


  return `${(bytes / (1024 * 1024)).toFixed(1)} MiB`;

}


function Metric({

  icon:
    Icon,

  label,

  value,

  detail,

}: {

  icon:
    typeof FolderOpen;

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


function SmallMetric({

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
        rounded-xl
        bg-slate-50
        p-4
      "
    >

      <p
        className="
          text-[10px]
          uppercase
          tracking-[0.1em]
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


function StatusBadge({

  status,

}: {

  status:
    BidRequirementStatus;

}) {

  return (

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
          status,
        )
      }
    </span>

  );

}
