"use client";

import { Bookmark, BookmarkPlus, Trash2 } from "lucide-react";
import { useRef, useState } from "react";

import { Button } from "@/components/ui/button";
import { inputClasses } from "@/components/ui/field";
import { useDismiss } from "@/components/ui/use-dismiss";
import { cn } from "@/lib/cn";

import type { SavedView } from "./use-saved-views";

type SavedViewsMenuProps<T> = {
  views: ReadonlyArray<SavedView<T>>;
  onApply: (value: T) => void;
  onSave: (name: string) => void;
  onRemove: (id: string) => void;
  /** Disables saving while the current filters equal the defaults. */
  canSave: boolean;
};

export function SavedViewsMenu<T>({ views, onApply, onSave, onRemove, canSave }: SavedViewsMenuProps<T>) {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const rootRef = useRef<HTMLDivElement>(null);
  useDismiss(rootRef, open, () => setOpen(false));

  function submit(event: React.FormEvent) {
    event.preventDefault();
    if (name.trim() === "" || !canSave) return;
    onSave(name);
    setName("");
  }

  return (
    <div ref={rootRef} className="relative">
      <Button
        variant="secondary"
        onClick={() => setOpen((value) => !value)}
        aria-haspopup="dialog"
        aria-expanded={open}
        leading={<Bookmark className="size-4" />}
      >
        Saved views
        {views.length > 0 ? (
          <span className="grid h-5 min-w-5 place-items-center rounded-full bg-brand-soft px-1.5 text-[10px] font-bold text-brand">
            {views.length}
          </span>
        ) : null}
      </Button>

      {open ? (
        <div
          role="dialog"
          aria-label="Saved views"
          className="absolute right-0 top-12 z-40 w-80 max-w-[calc(100vw-2rem)] animate-pop rounded-2xl border border-line bg-surface p-3 shadow-pop"
        >
          <form onSubmit={submit} className="flex gap-2">
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Name this view…"
              aria-label="Saved view name"
              maxLength={60}
              className={cn(inputClasses, "h-9")}
            />
            <Button
              type="submit"
              size="sm"
              variant="primary"
              disabled={name.trim() === "" || !canSave}
              leading={<BookmarkPlus className="size-3.5" />}
              className="h-9"
            >
              Save
            </Button>
          </form>
          {!canSave ? (
            <p className="mt-2 px-1 text-[11px] text-muted">Set at least one filter to save it as a view.</p>
          ) : null}

          <div className="my-3 h-px bg-line" />

          {views.length === 0 ? (
            <p className="px-1 py-4 text-center text-xs text-muted">
              No saved views yet. Save your favourite filter combinations for one-click access.
            </p>
          ) : (
            <ul className="max-h-64 space-y-0.5 overflow-y-auto">
              {views.map((view) => (
                <li key={view.id} className="group flex items-center gap-1 rounded-lg hover:bg-surface-3">
                  <button
                    type="button"
                    onClick={() => {
                      onApply(view.value);
                      setOpen(false);
                    }}
                    className="min-w-0 flex-1 truncate px-2.5 py-2 text-left text-sm text-ink-2 hover:text-ink"
                  >
                    {view.name}
                  </button>
                  <button
                    type="button"
                    onClick={() => onRemove(view.id)}
                    aria-label={`Delete saved view ${view.name}`}
                    className="mr-1 grid size-7 place-items-center rounded-md text-faint opacity-0 transition hover:text-danger focus:opacity-100 group-hover:opacity-100"
                  >
                    <Trash2 className="size-3.5" />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      ) : null}
    </div>
  );
}
