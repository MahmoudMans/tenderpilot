import {
  ModulePlaceholder,
} from "@/components/ui/module-placeholder";


export default function OpportunitiesPage() {

  return (

    <ModulePlaceholder

      eyebrow="
        Opportunity intelligence
      "

      title="
        Discover and qualify public procurement opportunities.
      "

      description="
        This module will become the operational center for normalized TED and BOAMP opportunities, advanced search, saved opportunities and explainable company matching.
      "

      phase="
        Procurement ingestion + OpenSearch
      "

      capabilities={[

        "Full-text tender discovery and faceted filters",

        "Saved and dismissed opportunity workflows",

        "Company-specific match and confidence scores",

        "Tender detail with lots, buyers, dates and classifications",

      ]}

    />

  );

}
