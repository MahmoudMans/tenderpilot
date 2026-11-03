import Link from "next/link";
import { cn } from "@/lib/cn";
import { supplierKeyFor } from "./supplier-key";

/** A supplier name that opens the supplier profile. Shows plain text if the name cannot be turned into a key. */
export function SupplierLink({ name, className }: { name: string; className?: string }) {
  const key = supplierKeyFor(name);
  if (key === null) return <>{name}</>;
  return (
    <Link
      href={`/suppliers/${key}`}
      title="Open supplier profile"
      className={cn("hover:text-brand hover:underline", className)}
    >
      {name}
    </Link>
  );
}
