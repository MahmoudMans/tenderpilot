"use client";


import {

  AlertTriangle,

  CheckCircle2,

  Database,

  Gauge,

  Info,

  LoaderCircle,

  RefreshCw,

  ShieldCheck,

  Sparkles,

} from "lucide-react";


import {

  useEffect,

  useState,

} from "react";


import {

  useAuth,

} from "@/features/auth/auth-context";


import type {

  DataQualityOverview,

  DataQualityRecalculationResult,

} from "@/features/procurement/data-quality-types";


function formatDateTime(
  value:
    string
    |
    null,
) {

  if (
    value ===
    null
  ) {
    return "Never";
  }


  return new Intl.DateTimeFormat(
    "en",
    {
      dateStyle:
        "medium",

      timeStyle:
        "short",
    },
  ).format(
    new Date(
      value,
    ),
  );
}


export default function IntelligencePage() {

  const {

    api,

    user,

  } = useAuth();


  const [

    overview,

    setOverview,

  ] = useState<
    DataQualityOverview
    |
    null
  >(
    null,
  );


  const [

    loading,

    setLoading,

  ] = useState(
    true,
  );


  const [

    recalculating,

    setRecalculating,

  ] = useState(
    false,
  );


  const [

    error,

    setError,

  ] = useState<
    string
    |
    null
  >(
    null,
  );


  const [

    recalculatedCount,

    setRecalculatedCount,

  ] = useState<
    number
    |
    null
  >(
    null,
  );


  useEffect(
    () => {

      let cancelled =
        false;


      api<DataQualityOverview>(
        "/api/v1/data-quality/overview",
      )
        .then(
          (
            result,
          ) => {

            if (
              !cancelled
            ) {
              setOverview(result);
            }
          },
        )
        .catch(
          (
            cause,
          ) => {

            if (
              !cancelled
            ) {

              setError(
                cause instanceof Error
                  ? cause.message
                  : "Unable to load procurement quality intelligence.",
              );
            }
          },
        )
        .finally(
          () => {

            if (
              !cancelled
            ) {
              setLoading(false);
            }
          },
        );


      return () => {
        cancelled = true;
      };

    },
    [api],
  );


  async function recalculate() {

    setRecalculating(true);
    setError(null);
    setRecalculatedCount(null);


    try {

      const result =
        await api<DataQualityRecalculationResult>(
          "/api/v1/data-quality/recalculate",
          {
            method:
              "POST",
          },
        );


      setRecalculatedCount(
        result.recalculatedCount,
      );


      const refreshed =
        await api<DataQualityOverview>(
          "/api/v1/data-quality/overview",
        );


      setOverview(refreshed);

    } catch (
      cause
    ) {

      setError(
        cause instanceof Error
          ? cause.message
          : "Unable to recalculate procurement quality.",
      );

    } finally {

      setRecalculating(false);
    }
  }


  if (
    loading
  ) {

    return (
      <div
        className="
          grid
          min-h-[60vh]
          place-items-center
        "
      >
        <LoaderCircle
          className="
            size-6
            animate-spin
            text-[#2f6fed]
          "
        />
      </div>
    );
  }


  if (
    overview ===
    null
  ) {

    return (
      <div
        className="
          rounded-2xl
          border
          border-red-200
          bg-red-50
          p-5
          text-sm
          text-red-700
        "
      >
        {
          error
          ??
          "Data quality intelligence is unavailable."
        }
      </div>
    );
  }


  return (

    <div
      className="
        mx-auto
        max-w-[1450px]
      "
    >

      <div
        className="
          flex
          flex-col
          gap-5
          xl:flex-row
          xl:items-end
          xl:justify-between
        "
      >

        <div>

          <p
            className="
              text-xs
              font-semibold
              uppercase
              tracking-[0.18em]
              text-[#2f6fed]
            "
          >
            Procurement intelligence
          </p>


          <h1
            className="
              mt-3
              text-4xl
              font-semibold
              tracking-[-0.03em]
              text-slate-950
            "
          >
            Data quality
          </h1>


          <p
            className="
              mt-3
              max-w-3xl
              text-sm
              leading-6
              text-slate-500
            "
          >
            Source-neutral quality scoring detects missing
            classifications, geography, deadlines, provenance
            and inconsistent award-stage data before matching
            and analytics consume the procurement catalogue.
          </p>

        </div>


        <a
          href="/matches"
          className="
            inline-flex
            h-11
            items-center
            justify-center
            gap-2
            rounded-xl
            border
            border-slate-200
            bg-white
            px-5
            text-sm
            font-semibold
            text-slate-700
            hover:bg-slate-50
          "
        >
          Match opportunities
        </a>


        <a
          href="/search"
          className="
            inline-flex
            h-11
            items-center
            justify-center
            gap-2
            rounded-xl
            border
            border-slate-200
            bg-white
            px-5
            text-sm
            font-semibold
            text-slate-700
            hover:bg-slate-50
          "
        >
          Search catalogue
        </a>


        {
          user?.role ===
          "OWNER"

            ? (

              <button
                type="button"
                onClick={
                  () => {
                    void recalculate();
                  }
                }
                disabled={
                  recalculating
                }
                className="
                  inline-flex
                  h-11
                  items-center
                  justify-center
                  gap-2
                  rounded-xl
                  bg-[#14213d]
                  px-5
                  text-sm
                  font-semibold
                  text-white
                  disabled:opacity-50
                "
              >

                {
                  recalculating
                    ? (
                      <LoaderCircle
                        className="
                          size-4
                          animate-spin
                        "
                      />
                    )
                    : (
                      <RefreshCw
                        className="size-4"
                      />
                    )
                }

                {
                  recalculating
                    ? "Recalculating..."
                    : "Recalculate catalogue"
                }

              </button>

            )

            : null
        }

      </div>


      {
        error

          ? (

            <div
              className="
                mt-6
                rounded-2xl
                border
                border-red-200
                bg-red-50
                p-4
                text-sm
                text-red-700
              "
            >
              {
                error
              }
            </div>

          )

          : null
      }


      {
        recalculatedCount !==
        null

          ? (

            <div
              className="
                mt-6
                flex
                items-center
                gap-3
                rounded-2xl
                border
                border-emerald-200
                bg-emerald-50
                p-4
                text-sm
                text-emerald-800
              "
            >

              <CheckCircle2
                className="size-5"
              />

              Recalculated
              {" "}
              {
                recalculatedCount
              }
              {" "}
              canonical procurement records.

            </div>

          )

          : null
      }


      <div
        className="
          mt-7
          grid
          gap-4
          md:grid-cols-2
          xl:grid-cols-4
        "
      >

        <Metric
          icon={Database}
          label="Canonical tenders"
          value={
            overview.totalTenders
              .toLocaleString()
          }
          detail="Shared procurement catalogue"
        />


        <Metric
          icon={ShieldCheck}
          label="Assessed"
          value={
            overview.assessedTenders
              .toLocaleString()
          }
          detail={
            `${overview.unassessedTenders.toLocaleString()} unassessed`
          }
        />


        <Metric
          icon={Gauge}
          label="Average quality"
          value={
            `${overview.averageScore.toFixed(1)}/100`
          }
          detail="Across assessed records"
        />


        <Metric
          icon={Sparkles}
          label="Excellent"
          value={
            overview.excellentCount
              .toLocaleString()
          }
          detail={
            `Last audit ${formatDateTime(overview.lastAssessedAt)}`
          }
        />

      </div>


      <section
        className="
          mt-6
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-6
          shadow-[0_8px_30px_rgba(15,23,42,0.04)]
        "
      >

        <div
          className="
            flex
            items-center
            gap-3
          "
        >

          <div
            className="
              grid
              size-10
              place-items-center
              rounded-xl
              bg-[#edf4ff]
              text-[#2f6fed]
            "
          >
            <Gauge
              className="size-5"
            />
          </div>


          <div>

            <h2
              className="
                text-base
                font-semibold
                text-slate-950
              "
            >
              Quality distribution
            </h2>

            <p
              className="
                mt-1
                text-xs
                text-slate-500
              "
            >
              Deterministic 0–100 quality grades.
            </p>

          </div>

        </div>


        <div
          className="
            mt-6
            grid
            gap-3
            sm:grid-cols-2
            xl:grid-cols-5
          "
        >

          <GradeCard
            label="Excellent"
            range="90–100"
            value={overview.excellentCount}
          />

          <GradeCard
            label="Good"
            range="75–89"
            value={overview.goodCount}
          />

          <GradeCard
            label="Fair"
            range="60–74"
            value={overview.fairCount}
          />

          <GradeCard
            label="Poor"
            range="40–59"
            value={overview.poorCount}
          />

          <GradeCard
            label="Critical"
            range="0–39"
            value={overview.criticalCount}
          />

        </div>

      </section>


      <section
        className="
          mt-6
          rounded-3xl
          border
          border-slate-200
          bg-white
          p-6
        "
      >

        <h2
          className="
            text-base
            font-semibold
            text-slate-950
          "
        >
          Structured quality findings
        </h2>


        <p
          className="
            mt-2
            max-w-3xl
            text-sm
            leading-6
            text-slate-500
          "
        >
          Every deduction is represented as a structured,
          explainable issue rather than an opaque score.
        </p>


        <div
          className="
            mt-6
            grid
            gap-4
            md:grid-cols-3
          "
        >

          <IssueMetric
            icon={AlertTriangle}
            label="Critical issues"
            value={
              overview.criticalIssueCount
            }
            description="
              Invalid chronology, missing provenance or missing award records.
            "
          />


          <IssueMetric
            icon={ShieldCheck}
            label="Warnings"
            value={
              overview.warningIssueCount
            }
            description="
              Missing CPV, geography, deadline or award supplier information.
            "
          />


          <IssueMetric
            icon={Info}
            label="Information gaps"
            value={
              overview.infoIssueCount
            }
            description="
              Useful but non-blocking fields such as estimated value or source URL.
            "
          />

        </div>

      </section>

    </div>

  );
}


function Metric({

  icon:
    Icon,

  label,

  value,

  detail,

}: {

  icon:
    typeof Database;

  label:
    string;

  value:
    string;

  detail:
    string;

}) {

  return (

    <article
      className="
        rounded-2xl
        border
        border-slate-200
        bg-white
        p-5
      "
    >

      <Icon
        className="
          size-4
          text-[#2f6fed]
        "
      />


      <p
        className="
          mt-4
          text-[10px]
          font-semibold
          uppercase
          tracking-[0.12em]
          text-slate-400
        "
      >
        {
          label
        }
      </p>


      <p
        className="
          mt-1
          text-2xl
          font-semibold
          text-slate-950
        "
      >
        {
          value
        }
      </p>


      <p
        className="
          mt-1
          text-xs
          text-slate-500
        "
      >
        {
          detail
        }
      </p>

    </article>

  );
}


function GradeCard({

  label,

  range,

  value,

}: {

  label:
    string;

  range:
    string;

  value:
    number;

}) {

  return (

    <div
      className="
        rounded-2xl
        bg-slate-50
        p-4
      "
    >

      <p
        className="
          text-xs
          font-semibold
          text-slate-800
        "
      >
        {
          label
        }
      </p>


      <p
        className="
          mt-3
          text-2xl
          font-semibold
          text-slate-950
        "
      >
        {
          value
            .toLocaleString()
        }
      </p>


      <p
        className="
          mt-1
          text-[11px]
          text-slate-400
        "
      >
        {
          range
        }
      </p>

    </div>

  );
}


function IssueMetric({

  icon:
    Icon,

  label,

  value,

  description,

}: {

  icon:
    typeof AlertTriangle;

  label:
    string;

  value:
    number;

  description:
    string;

}) {

  return (

    <div
      className="
        rounded-2xl
        border
        border-slate-200
        p-5
      "
    >

      <Icon
        className="
          size-5
          text-slate-500
        "
      />


      <p
        className="
          mt-4
          text-sm
          font-semibold
          text-slate-900
        "
      >
        {
          label
        }
      </p>


      <p
        className="
          mt-2
          text-2xl
          font-semibold
          text-slate-950
        "
      >
        {
          value
            .toLocaleString()
        }
      </p>


      <p
        className="
          mt-2
          text-xs
          leading-5
          text-slate-500
        "
      >
        {
          description
        }
      </p>

    </div>

  );
}
