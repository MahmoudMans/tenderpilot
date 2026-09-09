import {

  ArrowRight,

  Construction,

} from "lucide-react";


type ModulePlaceholderProps = {

  eyebrow:
    string;

  title:
    string;

  description:
    string;

  phase:
    string;

  capabilities:
    string[];

};


export function ModulePlaceholder({

  eyebrow,

  title,

  description,

  phase,

  capabilities,

}: ModulePlaceholderProps) {

  return (

    <div
      className="
        mx-auto
        max-w-6xl
      "
    >

      <div
        className="
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-8
          shadow-[0_16px_50px_rgba(15,23,42,0.06)]
          md:p-12
        "
      >

        <div
          className="
            flex
            size-12
            items-center
            justify-center
            rounded-2xl
            bg-[#edf4ff]
            text-[#2f6fed]
          "
        >

          <Construction
            className="
              size-5
            "
          />

        </div>


        <p
          className="
            mt-7
            text-xs
            font-semibold
            uppercase
            tracking-[0.2em]
            text-[#2f6fed]
          "
        >

          {
            eyebrow
          }

        </p>


        <h1
          className="
            mt-3
            max-w-3xl
            text-3xl
            font-semibold
            tracking-tight
            text-slate-950
            md:text-4xl
          "
        >

          {
            title
          }

        </h1>


        <p
          className="
            mt-4
            max-w-3xl
            text-base
            leading-7
            text-slate-600
          "
        >

          {
            description
          }

        </p>


        <div
          className="
            mt-9
            grid
            gap-3
            md:grid-cols-2
          "
        >

          {
            capabilities.map(
              (
                capability,
              ) => (

                <div

                  key={
                    capability
                  }

                  className="
                    flex
                    items-center
                    gap-3
                    rounded-2xl
                    border
                    border-slate-200
                    bg-slate-50
                    px-4
                    py-3.5
                  "

                >

                  <ArrowRight
                    className="
                      size-4
                      shrink-0
                      text-[#2f6fed]
                    "
                  />


                  <span
                    className="
                      text-sm
                      font-medium
                      text-slate-700
                    "
                  >

                    {
                      capability
                    }

                  </span>

                </div>

              ),
            )
          }

        </div>


        <div
          className="
            mt-9
            inline-flex
            rounded-full
            bg-slate-950
            px-4
            py-2
            text-xs
            font-semibold
            text-white
          "
        >

          Scheduled development phase:
          {" "}
          {
            phase
          }

        </div>

      </div>

    </div>

  );

}
