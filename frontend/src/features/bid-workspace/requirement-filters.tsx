"use client";

import { Download } from "lucide-react";

import { Button } from "@/components/ui/button";
import { SearchInput, SelectField } from "@/components/ui/field";
import { FilterSummary } from "@/components/ui/filter-summary";
import { useToast } from "@/components/ui/toast";
import { formatEnum } from "@/features/procurement/procurement-format";
import { exportCsv, type CsvColumn } from "@/lib/csv";
import { OWNER_ANY, matchesOwner, ownerOptions } from "@/lib/owner";

import type { BidRequirementCategory, BidRequirementStatus, BidWorkspace } from "./bid-workspace-types";

type WorkspaceRequirement = BidWorkspace["requirements"][number];

export type RequirementFilters = {
  query: string;
  /** OWNER_ANY, OWNER_ME, OWNER_NONE or a name chosen from the menu (see lib/owner.ts). */
  owner: string;
  /** "OPEN" means still to do: not reviewed, in progress or a gap. */
  status: "ALL" | "OPEN" | BidRequirementStatus;
  category: "ALL" | BidRequirementCategory;
  mandatoryOnly: boolean;
};

export const NO_REQUIREMENT_FILTERS: RequirementFilters = {
  query: "",
  owner: OWNER_ANY,
  status: "ALL",
  category: "ALL",
  mandatoryOnly: false,
};

const STATUSES: BidRequirementStatus[] = ["NOT_REVIEWED", "IN_PROGRESS", "SATISFIED", "GAP", "NOT_APPLICABLE"];
const CATEGORIES: BidRequirementCategory[] = [
  "ADMINISTRATIVE",
  "TECHNICAL",
  "COMMERCIAL",
  "FINANCIAL",
  "LEGAL",
  "SUBMISSION",
  "OTHER",
];

export function hasRequirementFilters(filters: RequirementFilters): boolean {
  return (
    filters.query.trim() !== "" ||
    filters.owner !== OWNER_ANY ||
    filters.status !== "ALL" ||
    filters.category !== "ALL" ||
    filters.mandatoryOnly
  );
}

function passesStatus(status: BidRequirementStatus, filter: RequirementFilters["status"]): boolean {
  if (filter === "ALL") return true;
  if (filter === "OPEN") return status === "NOT_REVIEWED" || status === "IN_PROGRESS" || status === "GAP";
  return status === filter;
}

export function filterRequirements(
  requirements: readonly WorkspaceRequirement[],
  filters: RequirementFilters,
  aliases: readonly string[],
): WorkspaceRequirement[] {
  const needle = filters.query.trim().toLowerCase();
  return requirements.filter((requirement) => {
    if (filters.mandatoryOnly && !requirement.mandatory) return false;
    if (filters.category !== "ALL" && requirement.category !== filters.category) return false;
    if (!passesStatus(requirement.status, filters.status)) return false;
    if (!matchesOwner(requirement.assignedTo, filters.owner, aliases)) return false;
    if (needle === "") return true;
    return [
      requirement.title,
      requirement.description,
      requirement.sourceReference,
      requirement.sourceDocumentName,
      requirement.assignedTo,
      requirement.evidenceNotes,
    ].some((text) => (text ?? "").toLowerCase().includes(needle));
  });
}

const columns: CsvColumn<WorkspaceRequirement>[] = [
  { header: "Title", value: (requirement) => requirement.title },
  { header: "Status", value: (requirement) => formatEnum(requirement.status) },
  { header: "Category", value: (requirement) => formatEnum(requirement.category) },
  { header: "Mandatory", value: (requirement) => requirement.mandatory },
  { header: "Source document", value: (requirement) => requirement.sourceDocumentName },
  { header: "Reference", value: (requirement) => requirement.sourceReference },
  { header: "Due date", value: (requirement) => requirement.dueDate },
  { header: "Owner", value: (requirement) => requirement.assignedTo },
  { header: "Description", value: (requirement) => requirement.description },
  { header: "Evidence notes", value: (requirement) => requirement.evidenceNotes },
];

type RequirementFilterBarProps = {
  /** Every requirement of the bid, for the owner menu and the totals. */
  requirements: readonly WorkspaceRequirement[];
  /** The requirements that pass the filters. These are what the page shows and what is exported. */
  visible: readonly WorkspaceRequirement[];
  filters: RequirementFilters;
  onChange: (filters: RequirementFilters) => void;
  /** See ownerAliases in lib/owner.ts. */
  aliases: readonly string[];
};

export function RequirementFilterBar({ requirements, visible, filters, onChange, aliases }: RequirementFilterBarProps) {
  const { toast } = useToast();
  const active = hasRequirementFilters(filters);
  const owners = ownerOptions(
    requirements.map((requirement) => requirement.assignedTo),
    aliases,
    filters.owner,
  );

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap items-center gap-2">
        <SearchInput
          className="w-full"
          value={filters.query}
          onValueChange={(query) => onChange({ ...filters, query })}
          placeholder="Search requirements"
          aria-label="Search requirements"
        />
        <div className="min-w-[10rem] flex-1">
          <SelectField
            label="Filter by status"
            value={filters.status}
            onChange={(event) => onChange({ ...filters, status: event.target.value as RequirementFilters["status"] })}
          >
            <option value="ALL">Any status</option>
            <option value="OPEN">Still open</option>
            {STATUSES.map((status) => (
              <option key={status} value={status}>
                {formatEnum(status)}
              </option>
            ))}
          </SelectField>
        </div>
        <div className="min-w-[10rem] flex-1">
          <SelectField
            label="Filter by category"
            value={filters.category}
            onChange={(event) => onChange({ ...filters, category: event.target.value as RequirementFilters["category"] })}
          >
            <option value="ALL">Any category</option>
            {CATEGORIES.map((category) => (
              <option key={category} value={category}>
                {formatEnum(category)}
              </option>
            ))}
          </SelectField>
        </div>
        <div className="min-w-[10rem] flex-1">
          <SelectField
            label="Filter by owner"
            value={filters.owner}
            onChange={(event) => onChange({ ...filters, owner: event.target.value })}
          >
            {owners.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </SelectField>
        </div>
        <label className="inline-flex cursor-pointer items-center gap-2 text-xs font-semibold text-ink-2">
          <input
            type="checkbox"
            checked={filters.mandatoryOnly}
            onChange={(event) => onChange({ ...filters, mandatoryOnly: event.target.checked })}
            className="size-4 rounded border-line-strong accent-[var(--brand)]"
          />
          Mandatory only
        </label>
        <Button
          size="sm"
          className="ml-auto"
          disabled={visible.length === 0}
          leading={<Download className="size-4" />}
          onClick={() => {
            exportCsv("tenderpilot-bid-requirements", visible, columns);
            toast({
              tone: "success",
              title: "CSV exported",
              description: `${visible.length} ${visible.length === 1 ? "requirement" : "requirements"}.`,
            });
          }}
        >
          Export CSV
        </Button>
      </div>
      <FilterSummary
        shown={visible.length}
        total={requirements.length}
        noun="requirement"
        active={active}
        onClear={() => onChange(NO_REQUIREMENT_FILTERS)}
      />
    </div>
  );
}
