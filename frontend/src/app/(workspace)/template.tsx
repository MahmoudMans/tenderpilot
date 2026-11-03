import type { ReactNode } from "react";

import { ProductExperienceLayer } from "@/features/navigation/product-experience-layer";

export default function WorkspaceExperienceTemplate({
  children,
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <>
      <ProductExperienceLayer />
      <div className="animate-rise">{children}</div>
    </>
  );
}
