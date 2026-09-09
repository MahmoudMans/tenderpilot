import {
  ModulePlaceholder,
} from "@/components/ui/module-placeholder";


export default function IntelligencePage() {

  return (

    <ModulePlaceholder

      eyebrow="
        Market intelligence
      "

      title="
        Turn historical public awards into buyer and supplier intelligence.
      "

      description="
        The intelligence module will use public award data to expose market trends, buyer profiles, supplier relationships and regional procurement activity without commercial data subscriptions.
      "

      phase="
        Historical DECP intelligence
      "

      capabilities={[

        "Buyer spending and category analysis",

        "Supplier and competitor award histories",

        "Procurement market trend explorer",

        "Buyer–supplier network visualizations",

      ]}

    />

  );

}
