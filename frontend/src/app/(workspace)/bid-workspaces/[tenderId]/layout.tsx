import type { ReactNode } from "react";
import { BidWorkspaceNav } from "@/features/bid-workspace/bid-workspace-nav";

export default function BidWorkspaceLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <BidWorkspaceNav />
      {children}
    </>
  );
}
