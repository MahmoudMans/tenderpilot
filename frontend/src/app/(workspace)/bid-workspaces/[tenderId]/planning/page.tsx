"use client";


import {

  AlertOctagon,

  ArrowLeft,

  CalendarClock,

  CheckCircle2,

  ClipboardList,

  Columns3,

  Clock3,

  Flag,

  Gauge,

  List,

  LoaderCircle,

  Pencil,

  Plus,

  Send,

  ShieldCheck,

  Trash2,

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


import {

  formatDate,

  formatEnum,

} from "@/features/procurement/procurement-format";


import type {

  BidPlanningWorkspace,

  BidTaskMilestone,

  BidTaskPriority,

  BidTaskStatus,

} from "@/features/bid-planning/bid-planning-types";


import { SegmentedControl } from "@/components/ui/segmented-control";

import { TaskBoard } from "@/features/bid-planning/task-board";

import { useStoredState } from "@/lib/storage";

import { STORAGE_KEYS } from "@/lib/storage-keys";

import { ConfirmDialog } from "@/components/ui/confirm-dialog";

import { TaskEditDialog, type TaskPatch } from "@/features/bid-planning/task-edit-dialog";

import {
  NO_TASK_FILTERS,
  TaskFilterBar,
  filterTasks,
  type TaskFilters,
} from "@/features/bid-planning/task-filters";

import { ownerAliases } from "@/lib/owner";


type TaskView = "list" | "board";

const TASK_VIEWS = [
  { value: "list", label: "List", icon: List },
  { value: "board", label: "Board", icon: Columns3 },
] as const;


const TASK_STATUSES: BidTaskStatus[] = [

  "TODO",

  "IN_PROGRESS",

  "BLOCKED",

  "DONE",

  "CANCELLED",

];


const TASK_PRIORITIES: BidTaskPriority[] = [

  "LOW",

  "MEDIUM",

  "HIGH",

  "CRITICAL",

];


const TASK_MILESTONES: BidTaskMilestone[] = [

  "KICKOFF",

  "DOCUMENT_REVIEW",

  "COMPLIANCE",

  "TECHNICAL_RESPONSE",

  "COMMERCIAL_RESPONSE",

  "INTERNAL_REVIEW",

  "APPROVAL",

  "SUBMISSION",

  "OTHER",

];


export default function BidPlanningPage() {

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
    BidPlanningWorkspace
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

    title,

    setTitle,

  ] = useState(
    "",
  );


  const [

    description,

    setDescription,

  ] = useState(
    "",
  );


  const [

    priority,

    setPriority,

  ] = useState<BidTaskPriority>(
    "MEDIUM",
  );


  const [

    milestone,

    setMilestone,

  ] = useState<BidTaskMilestone>(
    "OTHER",
  );


  const [

    assignedTo,

    setAssignedTo,

  ] = useState(
    "",
  );


  const [

    dueDate,

    setDueDate,

  ] = useState(
    "",
  );


  const [

    submissionReference,

    setSubmissionReference,

  ] = useState(
    "",
  );


  useEffect(
    () => {

      let cancelled =
        false;


      api<BidPlanningWorkspace>(
        `/api/v1/bid-planning/${tenderId}`,
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
                  : "Unable to load bid plan.",
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


  const [taskView, setTaskView] = useStoredState<TaskView>(STORAGE_KEYS.planningTasksView, "list");


  const [editingTask, setEditingTask] = useState<BidPlanningWorkspace["tasks"][number] | null>(null);

  const [pendingDelete, setPendingDelete] = useState<{ id: string; title: string } | null>(null);

  const [taskFilters, setTaskFilters] = useState<TaskFilters>(NO_TASK_FILTERS);


  const myNames = ownerAliases(user);

  const visibleTasks = filterTasks(workspace?.tasks ?? [], taskFilters, myNames);


  const canEditTasks =
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


  const canEditControl =
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


  const canAuthorize =
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


  const canDelete =
    canAuthorize;


  async function createTask() {

    if (
      title.trim() ===
      ""
    ) {

      setError(
        "Task title is required.",
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
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/tasks`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                title,

                description,

                priority,

                milestone,

                assignedTo:
                  assignedTo
                  ||
                  null,

                dueDate:
                  dueDate
                  ||
                  null,

                requirementId:
                  null,
              }),
          },
        );


      setWorkspace(
        updated,
      );


      setTitle(
        "",
      );


      setDescription(
        "",
      );


      setAssignedTo(
        "",
      );


      setDueDate(
        "",
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to create task.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function updateTaskStatus(
    taskId:
      string,
    status:
      BidTaskStatus,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/tasks/${taskId}`,
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
          : "Unable to update task.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function saveTask(
    taskId: string,
    patch: TaskPatch,
  ): Promise<string | null> {

    setBusy(
      true,
    );


    try {

      const updated =
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/tasks/${taskId}`,
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
        : "Unable to save the task.";


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function deleteTask(
    taskId:
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
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/tasks/${taskId}`,
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
          : "Unable to delete task.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function updateChecklist(
    key:
      | "portalReady"
      | "signaturesReady"
      | "technicalResponseReady"
      | "commercialResponseReady"
      | "administrativePackReady"
      | "finalReviewReady",
    value:
      boolean,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/submission-control`,
          {
            method:
              "PUT",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                [key]:
                  value,
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
          : "Unable to update submission checklist.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function authorizeSubmission(
    authorized:
      boolean,
  ) {

    setBusy(
      true,
    );

    setError(
      null,
    );


    try {

      const updated =
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/submission-control/authorization`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                authorized,
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
          : "Unable to change submission authorization.",
      );


    } finally {

      setBusy(
        false,
      );

    }

  }


  async function markSubmitted() {

    if (
      submissionReference.trim() ===
      ""
    ) {

      setError(
        "Enter the portal receipt or submission reference.",
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
        await api<BidPlanningWorkspace>(
          `/api/v1/bid-planning/${tenderId}/submission-control/mark-submitted`,
          {
            method:
              "POST",

            headers: {
              "Content-Type":
                "application/json",
            },

            body:
              JSON.stringify({
                submissionReference,
                notes:
                  "Submission confirmed from TenderPilot.",
              }),
          },
        );


      setWorkspace(
        updated,
      );


      setSubmissionReference(
        "",
      );


    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to mark bid submitted.",
      );


    } finally {

      setBusy(
        false,
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
    workspace ===
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
            "Bid planning workspace is unavailable."
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
        className="mt-6"
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
          Bid execution
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
          Planning & submission control
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
          Coordinate bid tasks, monitor deadlines and blockers,
          complete submission controls, authorize release, and
          preserve the final portal submission reference.
        </p>

      </div>


      {
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
              {
                workspace.writeLockReason
              }
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
          label="Submission readiness"
          value={
            `${workspace.statistics.readinessScore}%`
          }
          detail={
            workspace.statistics.readyForSubmission
              ? "Ready to submit"
              : "Controls remain"
          }
        />


        <Metric
          icon={ClipboardList}
          label="Tasks"
          value={
            `${workspace.statistics.completedTasks}/${workspace.statistics.totalTasks}`
          }
          detail={
            `${workspace.statistics.openTasks} open`
          }
        />


        <Metric
          icon={Clock3}
          label="Overdue"
          value={
            workspace.statistics.overdueTasks
              .toLocaleString()
          }
          detail={
            `${workspace.statistics.blockedTasks} blocked`
          }
        />


        <Metric
          icon={AlertOctagon}
          label="Critical open"
          value={
            workspace.statistics.criticalOpenTasks
              .toLocaleString()
          }
          detail={
            `${workspace.complianceReadinessScore}% compliance readiness`
          }
        />

      </div>


      <ConfirmDialog
        open={pendingDelete !== null}
        title="Delete this task?"
        description={
          pendingDelete !== null
            ? `"${pendingDelete.title}" will be removed from the plan. This cannot be undone.`
            : ""
        }
        confirmLabel="Delete task"
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => {
          if (pendingDelete !== null) {
            const taskId = pendingDelete.id;
            setPendingDelete(null);
            void deleteTask(taskId);
          }
        }}
      />


      <TaskEditDialog
        key={editingTask?.id ?? "none"}
        task={editingTask}
        onClose={() => setEditingTask(null)}
        onSave={saveTask}
      />


      <div
        className={
          taskView === "board"
            ? "mt-6 grid gap-6"
            : "mt-6 grid gap-6 xl:grid-cols-[1.15fr_0.85fr]"
        }
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
              flex-wrap
              items-center
              gap-3
            "
          >

            <ClipboardList
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
                Bid tasks
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Track ownership, milestones, deadlines and blockers.
              </p>

            </div>


            <div className="ml-auto">

              <SegmentedControl<TaskView>
                label="Task view"
                size="sm"
                value={taskView}
                onChange={setTaskView}
                options={TASK_VIEWS}
              />

            </div>

          </div>


          {
            canEditTasks

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
                      title
                    }
                    onChange={
                      (
                        event,
                      ) =>
                        setTitle(
                          event.target.value,
                        )
                    }
                    aria-label="Task title"
                    placeholder="Task title"
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
                      description
                    }
                    onChange={
                      (
                        event,
                      ) =>
                        setDescription(
                          event.target.value,
                        )
                    }
                    aria-label="Task description"
                    placeholder="Task description"
                    rows={2}
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
                      aria-label="Task priority"
                      value={
                        priority
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setPriority(
                            event.target.value as BidTaskPriority,
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
                        TASK_PRIORITIES.map(
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
                      aria-label="Task milestone"
                      value={
                        milestone
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setMilestone(
                            event.target.value as BidTaskMilestone,
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
                        TASK_MILESTONES.map(
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


                    <input
                      value={
                        assignedTo
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setAssignedTo(
                            event.target.value,
                          )
                      }
                      aria-label="Task owner"
                      placeholder="Owner / assignee"
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


                    <input
                      aria-label="Task due date"
                      type="date"
                      value={
                        dueDate
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setDueDate(
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
                    />

                  </div>


                  <button
                    type="button"
                    disabled={
                      busy
                    }
                    onClick={
                      () => {
                        void createTask();
                      }
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

                    Add task

                  </button>

                </div>

              )

              : null
          }


          {
            workspace.tasks.length > 0

              ? (

                <TaskFilterBar
                  tasks={workspace.tasks}
                  visible={visibleTasks}
                  filters={taskFilters}
                  onChange={setTaskFilters}
                  aliases={myNames}
                />

              )

              : null
          }


          {
            taskView === "board"

              ? (

                <TaskBoard
                  tasks={visibleTasks}
                  canEdit={canEditTasks}
                  busy={busy}
                  onEdit={setEditingTask}
                  onMove={
                    (taskId, status) => {
                      void updateTaskStatus(taskId, status);
                    }
                  }
                />

              )

              : (

          <div
            className="
              mt-6
              space-y-3
            "
          >

            {
              visibleTasks.map(
                (
                  task,
                ) => (

                  <article
                    key={
                      task.id
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
                        lg:flex-row
                        lg:items-start
                        lg:justify-between
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
                            gap-2
                          "
                        >

                          <Badge
                            value={
                              formatEnum(
                                task.priority,
                              )
                            }
                          />


                          <Badge
                            value={
                              formatEnum(
                                task.milestone,
                              )
                            }
                          />


                          {
                            task.overdue

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
                                  Overdue
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
                            task.title
                          }
                        </p>


                        {
                          task.description

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
                                  task.description
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
                            task.assignedTo

                              ? (

                                <span>
                                  Owner:
                                  {" "}
                                  {
                                    task.assignedTo
                                  }
                                </span>

                              )

                              : null
                          }


                          {
                            task.dueDate

                              ? (

                                <span>
                                  Due:
                                  {" "}
                                  {
                                    formatDate(
                                      task.dueDate,
                                    )
                                  }
                                </span>

                              )

                              : null
                          }


                          {
                            task.requirementTitle

                              ? (

                                <span>
                                  Requirement:
                                  {" "}
                                  {
                                    task.requirementTitle
                                  }
                                </span>

                              )

                              : null
                          }

                        </div>

                      </div>


                      <div
                        className="
                          flex
                          items-center
                          gap-2
                        "
                      >

                        {
                          canEditTasks

                            ? (

                              <select
                                aria-label={`Status of task ${task.title}`}
                                value={
                                  task.status
                                }
                                onChange={
                                  (
                                    event,
                                  ) => {
                                    void updateTaskStatus(
                                      task.id,
                                      event.target.value as BidTaskStatus,
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
                                  TASK_STATUSES.map(
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

                            )

                            : (

                              <Badge
                                value={
                                  formatEnum(
                                    task.status,
                                  )
                                }
                              />

                            )
                        }


                        {
                          canEditTasks
                            ? (
                              <button
                                type="button"
                                disabled={
                                  busy
                                }
                                aria-label={`Edit task ${task.title}`}
                                title="Edit task"
                                onClick={
                                  () => {
                                    setEditingTask(
                                      task,
                                    );
                                  }
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
                                disabled={
                                  busy
                                }
                                aria-label={`Delete task ${task.title}`}
                                title="Delete task"
                                onClick={
                                  () => {
                                    setPendingDelete({
                                      id: task.id,
                                      title: task.title,
                                    });
                                  }
                                }
                                className="
                                  grid
                                  size-9
                                  place-items-center
                                  rounded-xl
                                  border
                                  border-red-200
                                  text-red-600
                                  disabled:opacity-50
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
              visibleTasks.length ===
              0

                ? (

                  <div
                    className="
                      rounded-2xl
                      border
                      border-dashed
                      border-slate-300
                      p-10
                      text-center
                      text-sm
                      text-slate-400
                    "
                  >
                    {
                      workspace.tasks.length === 0
                        ? "No bid tasks have been created."
                        : "No tasks match these filters."
                    }
                  </div>

                )

                : null
            }

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

          <div
            className="
              flex
              items-center
              gap-3
            "
          >

            <Flag
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
                Submission control
              </h2>


              <p
                className="
                  mt-1
                  text-xs
                  text-slate-500
                "
              >
                Final human-controlled release checklist.
              </p>

            </div>

          </div>


          <div
            className="
              mt-6
              space-y-3
            "
          >

            <ChecklistItem
              label="Portal access & account ready"
              checked={
                workspace.submissionControl.portalReady
              }
              disabled={
                !canEditControl
                ||
                busy
              }
              onChange={
                (
                  checked,
                ) => {
                  void updateChecklist(
                    "portalReady",
                    checked,
                  );
                }
              }
            />


            <ChecklistItem
              label="Signatures / certificates ready"
              checked={
                workspace.submissionControl.signaturesReady
              }
              disabled={
                !canEditControl
                ||
                busy
              }
              onChange={
                (
                  checked,
                ) => {
                  void updateChecklist(
                    "signaturesReady",
                    checked,
                  );
                }
              }
            />


            <ChecklistItem
              label="Technical response finalized"
              checked={
                workspace.submissionControl.technicalResponseReady
              }
              disabled={
                !canEditControl
                ||
                busy
              }
              onChange={
                (
                  checked,
                ) => {
                  void updateChecklist(
                    "technicalResponseReady",
                    checked,
                  );
                }
              }
            />


            <ChecklistItem
              label="Commercial / pricing response finalized"
              checked={
                workspace.submissionControl.commercialResponseReady
              }
              disabled={
                !canEditControl
                ||
                busy
              }
              onChange={
                (
                  checked,
                ) => {
                  void updateChecklist(
                    "commercialResponseReady",
                    checked,
                  );
                }
              }
            />


            <ChecklistItem
              label="Administrative pack finalized"
              checked={
                workspace.submissionControl.administrativePackReady
              }
              disabled={
                !canEditControl
                ||
                busy
              }
              onChange={
                (
                  checked,
                ) => {
                  void updateChecklist(
                    "administrativePackReady",
                    checked,
                  );
                }
              }
            />


            <ChecklistItem
              label="Final internal review completed"
              checked={
                workspace.submissionControl.finalReviewReady
              }
              disabled={
                !canEditControl
                ||
                busy
              }
              onChange={
                (
                  checked,
                ) => {
                  void updateChecklist(
                    "finalReviewReady",
                    checked,
                  );
                }
              }
            />


            <div
              className="
                rounded-xl
                border
                border-slate-200
                p-3
              "
            >

              <div
                className="
                  flex
                  items-center
                  justify-between
                  gap-3
                "
              >

                <div>

                  <p
                    className="
                      text-sm
                      font-semibold
                      text-slate-900
                    "
                  >
                    Submission authorization
                  </p>


                  <p
                    className="
                      mt-1
                      text-xs
                      text-slate-400
                    "
                  >
                    Owner or Bid Manager approval required.
                  </p>

                </div>


                {
                  canAuthorize

                    ? (

                      <button
                        type="button"
                        disabled={
                          busy
                        }
                        onClick={
                          () => {
                            void authorizeSubmission(
                              !workspace.submissionControl.submissionAuthorized,
                            );
                          }
                        }
                        className="
                          rounded-xl
                          border
                          border-slate-200
                          px-3
                          py-2
                          text-xs
                          font-semibold
                          text-slate-700
                          disabled:opacity-50
                        "
                      >
                        {
                          workspace.submissionControl.submissionAuthorized
                            ? "Revoke"
                            : "Authorize"
                        }
                      </button>

                    )

                    : (

                      <Badge
                        value={
                          workspace.submissionControl.submissionAuthorized
                            ? "Authorized"
                            : "Not authorized"
                        }
                      />

                    )
                }

              </div>

            </div>

          </div>


          <div
            className="
              mt-6
              rounded-2xl
              bg-slate-50
              p-4
            "
          >

            <div
              className="
                flex
                items-center
                gap-2
              "
            >

              <Gauge
                className="
                  size-4
                  text-[#2f6fed]
                "
              />


              <p
                className="
                  text-sm
                  font-semibold
                  text-slate-900
                "
              >
                Final submission gate
              </p>

            </div>


            <dl
              className="
                mt-4
                space-y-3
                text-xs
              "
            >

              <GateRow
                label="Bid decision"
                ready={
                  workspace.decisionOutcome ===
                  "GO"
                }
                value={
                  workspace.decisionOutcome
                    ? formatEnum(
                        workspace.decisionOutcome,
                      )
                    : "Not decided"
                }
              />


              <GateRow
                label="Compliance"
                ready={
                  workspace.complianceReady
                }
                value={
                  `${workspace.complianceReadinessScore}%`
                }
              />


              <GateRow
                label="Tasks"
                ready={
                  workspace.statistics.openTasks ===
                  0
                }
                value={
                  `${workspace.statistics.openTasks} open`
                }
              />


              <GateRow
                label="Checklist"
                ready={
                  workspace.statistics.checklistCompletionPercentage ===
                  100
                }
                value={
                  `${workspace.statistics.checklistCompletionPercentage}%`
                }
              />

            </dl>

          </div>


          {
            workspace.submissionControl.submittedAt

              ? (

                <div
                  className="
                    mt-6
                    rounded-2xl
                    border
                    border-emerald-200
                    bg-emerald-50
                    p-4
                  "
                >

                  <CheckCircle2
                    className="
                      size-5
                      text-emerald-700
                    "
                  />


                  <p
                    className="
                      mt-3
                      text-sm
                      font-semibold
                      text-emerald-900
                    "
                  >
                    Bid submitted
                  </p>


                  <p
                    className="
                      mt-2
                      text-xs
                      text-emerald-800
                    "
                  >
                    Reference:
                    {" "}
                    {
                      workspace.submissionControl.submissionReference
                    }
                  </p>


                  <p
                    className="
                      mt-1
                      text-xs
                      text-emerald-700
                    "
                  >
                    Submitted by
                    {" "}
                    {
                      workspace.submissionControl.submittedBy
                    }
                  </p>

                </div>

              )

              : canAuthorize

                ? (

                  <div
                    className="
                      mt-6
                    "
                  >

                    <input
                      value={
                        submissionReference
                      }
                      onChange={
                        (
                          event,
                        ) =>
                          setSubmissionReference(
                            event.target.value,
                          )
                      }
                      aria-label="Portal receipt or submission reference"
                      placeholder="Portal receipt / submission reference"
                      className="
                        h-11
                        w-full
                        rounded-xl
                        border
                        border-slate-200
                        px-3
                        text-sm
                      "
                    />


                    <button
                      type="button"
                      disabled={
                        busy
                        ||
                        !workspace.statistics.readyForSubmission
                      }
                      onClick={
                        () => {
                          void markSubmitted();
                        }
                      }
                      className="
                        mt-3
                        inline-flex
                        h-11
                        w-full
                        items-center
                        justify-center
                        gap-2
                        rounded-xl
                        bg-[#14213d]
                        px-4
                        text-sm
                        font-semibold
                        text-white
                        disabled:opacity-40
                      "
                    >

                      <Send
                        className="size-4"
                      />

                      Mark bid submitted

                    </button>

                  </div>

                )

                : null
          }

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

          <CalendarClock
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
              Planning audit
            </h2>


            <p
              className="
                mt-1
                text-xs
                text-slate-500
              "
            >
              Latest task and submission-control activity.
            </p>

          </div>

        </div>


        <div
          className="
            mt-5
            space-y-3
          "
        >

          {
            workspace.events.map(
              (
                event,
              ) => (

                <div
                  key={
                    event.id
                  }
                  className="
                    rounded-xl
                    border
                    border-slate-100
                    p-3
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
                          event.eventType,
                        )
                      }
                    />


                    <span
                      className="
                        text-[10px]
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
                      text-sm
                      text-slate-700
                    "
                  >
                    {
                      event.detail
                    }
                  </p>


                  <p
                    className="
                      mt-1
                      text-xs
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
            workspace.events.length ===
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
                  No planning activity yet.
                </p>

              )

              : null
          }

        </div>

      </section>


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

        TenderPilot never submits to a procurement portal
        automatically. The final submission record is a
        human-confirmed control and audit record.

      </div>

    </div>

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


function ChecklistItem({

  label,

  checked,

  disabled,

  onChange,

}: {

  label:
    string;

  checked:
    boolean;

  disabled:
    boolean;

  onChange:
    (
      checked:
        boolean,
    ) => void;

}) {

  return (

    <label
      className="
        flex
        items-center
        gap-3
        rounded-xl
        border
        border-slate-200
        p-3
      "
    >

      <input
        type="checkbox"
        checked={
          checked
        }
        disabled={
          disabled
        }
        onChange={
          (
            event,
          ) =>
            onChange(
              event.target.checked,
            )
        }
      />


      <span
        className="
          text-sm
          text-slate-700
        "
      >
        {
          label
        }
      </span>

    </label>

  );

}


function GateRow({

  label,

  ready,

  value,

}: {

  label:
    string;

  ready:
    boolean;

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
          text-slate-500
        "
      >
        {
          label
        }
      </dt>


      <dd
        className="
          flex
          items-center
          gap-2
          font-semibold
          text-slate-700
        "
      >

        {
          ready

            ? (

              <CheckCircle2
                className="
                  size-3.5
                  text-emerald-600
                "
              />

            )

            : (

              <Clock3
                className="
                  size-3.5
                  text-amber-600
                "
              />

            )
        }

        {
          value
        }

      </dd>

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
