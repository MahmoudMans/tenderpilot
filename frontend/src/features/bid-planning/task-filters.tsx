"use client";

import { Download } from "lucide-react";

import { Button } from "@/components/ui/button";
import { SearchInput, SelectField } from "@/components/ui/field";
import { FilterSummary } from "@/components/ui/filter-summary";
import { useToast } from "@/components/ui/toast";
import { formatEnum } from "@/features/procurement/procurement-format";
import { exportCsv, type CsvColumn } from "@/lib/csv";
import { OWNER_ANY, matchesOwner, ownerOptions } from "@/lib/owner";

import type { BidPlanningWorkspace, BidTaskPriority } from "./bid-planning-types";

type PlanTask = BidPlanningWorkspace["tasks"][number];

export type TaskFilters = {
  query: string;
  /** OWNER_ANY, OWNER_ME, OWNER_NONE or a name chosen from the menu (see lib/owner.ts). */
  owner: string;
  priority: "ALL" | BidTaskPriority;
  openOnly: boolean;
};

export const NO_TASK_FILTERS: TaskFilters = { query: "", owner: OWNER_ANY, priority: "ALL", openOnly: false };

const PRIORITIES: BidTaskPriority[] = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];

export function hasTaskFilters(filters: TaskFilters): boolean {
  return (
    filters.query.trim() !== "" || filters.owner !== OWNER_ANY || filters.priority !== "ALL" || filters.openOnly
  );
}

export function filterTasks(tasks: readonly PlanTask[], filters: TaskFilters, aliases: readonly string[]): PlanTask[] {
  const needle = filters.query.trim().toLowerCase();
  return tasks.filter((task) => {
    if (filters.openOnly && (task.status === "DONE" || task.status === "CANCELLED")) return false;
    if (filters.priority !== "ALL" && task.priority !== filters.priority) return false;
    if (!matchesOwner(task.assignedTo, filters.owner, aliases)) return false;
    if (needle === "") return true;
    return [task.title, task.description, task.assignedTo, task.requirementTitle].some((text) =>
      (text ?? "").toLowerCase().includes(needle),
    );
  });
}

const columns: CsvColumn<PlanTask>[] = [
  { header: "Title", value: (task) => task.title },
  { header: "Status", value: (task) => formatEnum(task.status) },
  { header: "Priority", value: (task) => formatEnum(task.priority) },
  { header: "Milestone", value: (task) => formatEnum(task.milestone) },
  { header: "Owner", value: (task) => task.assignedTo },
  { header: "Due date", value: (task) => task.dueDate },
  { header: "Overdue", value: (task) => task.overdue },
  { header: "Requirement", value: (task) => task.requirementTitle },
  { header: "Description", value: (task) => task.description },
  { header: "Last updated", value: (task) => task.updatedAt },
];

type TaskFilterBarProps = {
  /** Every task of the bid, for the owner menu and the totals. */
  tasks: readonly PlanTask[];
  /** The tasks that pass the filters. These are what the page shows and what is exported. */
  visible: readonly PlanTask[];
  filters: TaskFilters;
  onChange: (filters: TaskFilters) => void;
  /** See ownerAliases in lib/owner.ts. */
  aliases: readonly string[];
};

export function TaskFilterBar({ tasks, visible, filters, onChange, aliases }: TaskFilterBarProps) {
  const { toast } = useToast();
  const active = hasTaskFilters(filters);
  const owners = ownerOptions(
    tasks.map((task) => task.assignedTo),
    aliases,
    filters.owner,
  );

  return (
    <div className="mt-6 space-y-2">
      <div className="flex flex-wrap items-center gap-2">
        <SearchInput
          className="w-full"
          value={filters.query}
          onValueChange={(query) => onChange({ ...filters, query })}
          placeholder="Search tasks"
          aria-label="Search tasks"
        />
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
        <div className="min-w-[10rem] flex-1">
          <SelectField
            label="Filter by priority"
            value={filters.priority}
            onChange={(event) => onChange({ ...filters, priority: event.target.value as TaskFilters["priority"] })}
          >
            <option value="ALL">Any priority</option>
            {PRIORITIES.map((priority) => (
              <option key={priority} value={priority}>
                {formatEnum(priority)}
              </option>
            ))}
          </SelectField>
        </div>
        <label className="inline-flex cursor-pointer items-center gap-2 text-xs font-semibold text-ink-2">
          <input
            type="checkbox"
            checked={filters.openOnly}
            onChange={(event) => onChange({ ...filters, openOnly: event.target.checked })}
            className="size-4 rounded border-line-strong accent-[var(--brand)]"
          />
          Open tasks only
        </label>
        <Button
          size="sm"
          className="ml-auto"
          disabled={visible.length === 0}
          leading={<Download className="size-4" />}
          onClick={() => {
            exportCsv("tenderpilot-bid-tasks", visible, columns);
            toast({
              tone: "success",
              title: "CSV exported",
              description: `${visible.length} ${visible.length === 1 ? "task" : "tasks"}.`,
            });
          }}
        >
          Export CSV
        </Button>
      </div>
      <FilterSummary
        shown={visible.length}
        total={tasks.length}
        noun="task"
        active={active}
        onClear={() => onChange(NO_TASK_FILTERS)}
      />
    </div>
  );
}
