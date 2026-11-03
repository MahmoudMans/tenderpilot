"use client";

import { Search, X } from "lucide-react";
import type { InputHTMLAttributes, SelectHTMLAttributes } from "react";

import { cn } from "@/lib/cn";

export const inputClasses =
  "h-10 w-full rounded-xl border border-line bg-surface px-3 text-sm text-ink outline-none transition placeholder:text-faint hover:border-line-strong focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:cursor-not-allowed disabled:opacity-60";

type SearchInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value" | "type"> & {
  value: string;
  onValueChange: (value: string) => void;
};

/** Text search field with a leading icon and a clear button. */
export function SearchInput({ value, onValueChange, className, placeholder, ...rest }: SearchInputProps) {
  return (
    <div className={cn("relative", className)}>
      <Search
        className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-faint"
        aria-hidden="true"
      />
      <input
        type="search"
        value={value}
        onChange={(event) => onValueChange(event.target.value)}
        placeholder={placeholder}
        className={cn(inputClasses, "pl-10 pr-9 [&::-webkit-search-cancel-button]:hidden")}
        {...rest}
      />
      {value !== "" ? (
        <button
          type="button"
          onClick={() => onValueChange("")}
          aria-label="Clear search"
          className="absolute right-2 top-1/2 grid size-6 -translate-y-1/2 place-items-center rounded-md text-faint transition hover:bg-surface-3 hover:text-ink"
        >
          <X className="size-3.5" />
        </button>
      ) : null}
    </div>
  );
}

type SelectFieldProps = SelectHTMLAttributes<HTMLSelectElement> & {
  label: string;
};

/** Compact select with a visually hidden label. */
export function SelectField({ label, className, children, ...rest }: SelectFieldProps) {
  return (
    <select aria-label={label} className={cn(inputClasses, "cursor-pointer pr-8", className)} {...rest}>
      {children}
    </select>
  );
}
