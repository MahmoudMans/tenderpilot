"use client";

import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { inputClasses } from "@/components/ui/field";
import { Labelled } from "@/components/ui/labelled";
import { formatEnum } from "@/features/procurement/procurement-format";
import { cn } from "@/lib/cn";

import type {
  BidPlanningWorkspace,
  BidTaskMilestone,
  BidTaskPriority,
} from "./bid-planning-types";

type PlanTask = BidPlanningWorkspace["tasks"][number];

/**
 * Only the fields that changed are sent. The server keeps any field that is left out, and an empty
 * description or owner clears it. A due date can be moved but not removed.
 */
export type TaskPatch = {
  title?: string;
  description?: string;
  priority?: BidTaskPriority;
  milestone?: BidTaskMilestone;
  assignedTo?: string;
  dueDate?: string;
};

const PRIORITIES: BidTaskPriority[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
const MILESTONES: BidTaskMilestone[] = [
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

type TaskEditDialogProps = {
  /** The task being edited, or null when the dialog is closed. Give the dialog a key that changes with the task. */
  task: PlanTask | null;
  onClose: () => void;
  /** Resolves to null on success, or to the message to show when the server refused the change. */
  onSave: (taskId: string, patch: TaskPatch) => Promise<string | null>;
};

export function TaskEditDialog({ task, onClose, onSave }: TaskEditDialogProps) {
  const [title, setTitle] = useState(task?.title ?? "");
  const [description, setDescription] = useState(task?.description ?? "");
  const [priority, setPriority] = useState<BidTaskPriority>(task?.priority ?? "MEDIUM");
  const [milestone, setMilestone] = useState<BidTaskMilestone>(task?.milestone ?? "OTHER");
  const [owner, setOwner] = useState(task?.assignedTo ?? "");
  const [dueDate, setDueDate] = useState(task?.dueDate ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const patch: TaskPatch = {};
  if (task !== null) {
    if (title.trim() !== task.title) patch.title = title.trim();
    if (description.trim() !== (task.description ?? "")) patch.description = description.trim();
    if (priority !== task.priority) patch.priority = priority;
    if (milestone !== task.milestone) patch.milestone = milestone;
    if (owner.trim() !== (task.assignedTo ?? "")) patch.assignedTo = owner.trim();
    if (dueDate !== "" && dueDate !== task.dueDate) patch.dueDate = dueDate;
  }
  const changed = Object.keys(patch).length > 0;
  const hadDueDate = task !== null && task.dueDate !== null;
  const valid = title.trim() !== "" && (!hadDueDate || dueDate !== "");

  async function save() {
    if (task === null || !changed || !valid || saving) return;
    setSaving(true);
    setError(null);
    const message = await onSave(task.id, patch);
    if (message === null) {
      onClose();
      return;
    }
    setError(message);
    setSaving(false);
  }

  return (
    <Dialog
      open={task !== null}
      onClose={onClose}
      title="Edit task"
      description="Its status is changed from the list or the board."
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" loading={saving} disabled={!changed || !valid} onClick={() => void save()}>
            Save changes
          </Button>
        </>
      }
    >
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault();
          void save();
        }}
      >
        <Labelled label="Title">
          <input
            data-autofocus
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            maxLength={600}
            required
            className={inputClasses}
          />
        </Labelled>
        <Labelled label="Description">
          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            maxLength={3000}
            rows={2}
            className={cn(inputClasses, "h-auto resize-y py-2 leading-6")}
          />
        </Labelled>
        <div className="grid gap-4 sm:grid-cols-2">
          <Labelled label="Priority">
            <select
              value={priority}
              onChange={(event) => setPriority(event.target.value as BidTaskPriority)}
              className={cn(inputClasses, "cursor-pointer")}
            >
              {PRIORITIES.map((value) => (
                <option key={value} value={value}>
                  {formatEnum(value)}
                </option>
              ))}
            </select>
          </Labelled>
          <Labelled label="Milestone">
            <select
              value={milestone}
              onChange={(event) => setMilestone(event.target.value as BidTaskMilestone)}
              className={cn(inputClasses, "cursor-pointer")}
            >
              {MILESTONES.map((value) => (
                <option key={value} value={value}>
                  {formatEnum(value)}
                </option>
              ))}
            </select>
          </Labelled>
          <Labelled label="Owner">
            <input
              value={owner}
              onChange={(event) => setOwner(event.target.value)}
              maxLength={320}
              placeholder="Name or email"
              className={inputClasses}
            />
          </Labelled>
          <Labelled
            label="Due date"
            hint={hadDueDate ? "A due date can be moved, but not removed." : undefined}
          >
            <input
              type="date"
              value={dueDate}
              onChange={(event) => setDueDate(event.target.value)}
              required={hadDueDate}
              className={inputClasses}
            />
          </Labelled>
        </div>
        {error !== null ? (
          <p role="alert" className="rounded-xl border border-danger-line bg-danger-soft px-3 py-2 text-sm text-danger">
            {error}
          </p>
        ) : null}
      </form>
    </Dialog>
  );
}
