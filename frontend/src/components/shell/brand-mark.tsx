import { cn } from "@/lib/cn";

export function BrandMark({ className }: { className?: string }) {
  return (
    <div
      aria-hidden="true"
      className={cn(
        "tp-dark-surface grid size-10 shrink-0 place-items-center rounded-xl bg-[#14213d] text-sm font-bold tracking-tight text-white shadow-sm",
        className,
      )}
    >
      TP
    </div>
  );
}
