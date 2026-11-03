"use client";

import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { inputClasses } from "@/components/ui/field";
import { Labelled } from "@/components/ui/labelled";
import { formatEnum } from "@/features/procurement/procurement-format";
import { cn } from "@/lib/cn";

import type { BidRequirementCategory, BidWorkspace } from "./bid-workspace-types";

type WorkspaceRequirement = BidWorkspace["requirements"][number];
type WorkspaceDocument = BidWorkspace["documents"][number];

/**
 * Only the fields that changed are sent. The server keeps any field that is left out, and an empty
 * description, reference, owner or evidence note clears it. A due date or a source document can be
 * changed but not removed.
 */
export type RequirementPatch = {
  title?: string;
  description?: string;
  category?: BidRequirementCategory;
  mandatory?: boolean;
  sourceDocumentId?: string;
  sourceReference?: string;
  dueDate?: string;
  assignedTo?: string;
  evidenceNotes?: string;
};

const CATEGORIES: BidRequirementCategory[] = [
  "ADMINISTRATIVE",
  "TECHNICAL",
  "COMMERCIAL",
  "FINANCIAL",
  "LEGAL",
  "SUBMISSION",
  "OTHER",
];

type RequirementEditDialogProps = {
  /** The requirement being edited, or null when the dialog is closed. Give the dialog a key that changes with it. */
  requirement: WorkspaceRequirement | null;
  /** The documents of this bid, for the source document menu. */
  documents: WorkspaceDocument[];
  onClose: () => void;
  /** Resolves to null on success, or to the message to show when the server refused the change. */
  onSave: (requirementId: string, patch: RequirementPatch) => Promise<string | null>;
};

export function RequirementEditDialog({ requirement, documents, onClose, onSave }: RequirementEditDialogProps) {
  const [title, setTitle] = useState(requirement?.title ?? "");
  const [description, setDescription] = useState(requirement?.description ?? "");
  const [category, setCategory] = useState<BidRequirementCategory>(requirement?.category ?? "OTHER");
  const [mandatory, setMandatory] = useState(requirement?.mandatory ?? false);
  const [sourceDocumentId, setSourceDocumentId] = useState(requirement?.sourceDocumentId ?? "");
  const [sourceReference, setSourceReference] = useState(requirement?.sourceReference ?? "");
  const [dueDate, setDueDate] = useState(requirement?.dueDate ?? "");
  const [owner, setOwner] = useState(requirement?.assignedTo ?? "");
  const [evidence, setEvidence] = useState(requirement?.evidenceNotes ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const patch: RequirementPatch = {};
  if (requirement !== null) {
    if (title.trim() !== requirement.title) patch.title = title.trim();
    if (description.trim() !== (requirement.description ?? "")) patch.description = description.trim();
    if (category !== requirement.category) patch.category = category;
    if (mandatory !== requirement.mandatory) patch.mandatory = mandatory;
    if (sourceDocumentId !== "" && sourceDocumentId !== requirement.sourceDocumentId) {
      patch.sourceDocumentId = sourceDocumentId;
    }
    if (sourceReference.trim() !== (requirement.sourceReference ?? "")) patch.sourceReference = sourceReference.trim();
    if (dueDate !== "" && dueDate !== requirement.dueDate) patch.dueDate = dueDate;
    if (owner.trim() !== (requirement.assignedTo ?? "")) patch.assignedTo = owner.trim();
    if (evidence.trim() !== (requirement.evidenceNotes ?? "")) patch.evidenceNotes = evidence.trim();
  }
  const changed = Object.keys(patch).length > 0;
  const hadDueDate = requirement !== null && requirement.dueDate !== null;
  const hadSource = requirement !== null && requirement.sourceDocumentId !== null;
  const valid = title.trim() !== "" && (!hadDueDate || dueDate !== "");

  async function save() {
    if (requirement === null || !changed || !valid || saving) return;
    setSaving(true);
    setError(null);
    const message = await onSave(requirement.id, patch);
    if (message === null) {
      onClose();
      return;
    }
    setError(message);
    setSaving(false);
  }

  return (
    <Dialog
      open={requirement !== null}
      onClose={onClose}
      title="Edit requirement"
      description="Its status is changed from the menu on the requirement."
      size="lg"
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
          <Labelled label="Category">
            <select
              value={category}
              onChange={(event) => setCategory(event.target.value as BidRequirementCategory)}
              className={cn(inputClasses, "cursor-pointer")}
            >
              {CATEGORIES.map((value) => (
                <option key={value} value={value}>
                  {formatEnum(value)}
                </option>
              ))}
            </select>
          </Labelled>
          <div className="flex items-end pb-2">
            <label className="inline-flex cursor-pointer items-center gap-2 text-sm font-medium text-ink-2">
              <input
                type="checkbox"
                checked={mandatory}
                onChange={(event) => setMandatory(event.target.checked)}
                className="size-4 rounded border-line-strong accent-[var(--brand)]"
              />
              Mandatory requirement
            </label>
          </div>
          {documents.length > 0 ? (
            <Labelled
              label="Source document"
              hint={hadSource ? "A source document can be changed, but not removed." : undefined}
            >
              <select
                value={sourceDocumentId}
                onChange={(event) => setSourceDocumentId(event.target.value)}
                className={cn(inputClasses, "cursor-pointer")}
              >
                {hadSource ? null : <option value="">No source document</option>}
                {documents.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.originalFilename}
                  </option>
                ))}
              </select>
            </Labelled>
          ) : null}
          <Labelled label="Reference in the document">
            <input
              value={sourceReference}
              onChange={(event) => setSourceReference(event.target.value)}
              maxLength={500}
              placeholder="Article or page"
              className={inputClasses}
            />
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
        <Labelled label="Evidence notes" hint="Where the proof is kept, or what is still missing.">
          <textarea
            value={evidence}
            onChange={(event) => setEvidence(event.target.value)}
            maxLength={3000}
            rows={2}
            className={cn(inputClasses, "h-auto resize-y py-2 leading-6")}
          />
        </Labelled>
        {error !== null ? (
          <p role="alert" className="rounded-xl border border-danger-line bg-danger-soft px-3 py-2 text-sm text-danger">
            {error}
          </p>
        ) : null}
      </form>
    </Dialog>
  );
}
