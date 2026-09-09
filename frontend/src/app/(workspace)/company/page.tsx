import {
  ModulePlaceholder,
} from "@/components/ui/module-placeholder";


export default function CompanyPage() {

  return (

    <ModulePlaceholder

      eyebrow="
        Company knowledge
      "

      title="
        Model the organization that TenderPilot will match against tenders.
      "

      description="
        Capabilities, target CPV codes, operating regions, certifications and reference projects will form the explainable company profile used by the matching and compliance engines.
      "

      phase="
        Identity + company profile
      "

      capabilities={[

        "Organization profile and operating preferences",

        "Capabilities and target procurement classifications",

        "Certification and document register",

        "Structured reference-project knowledge base",

      ]}

    />

  );

}
