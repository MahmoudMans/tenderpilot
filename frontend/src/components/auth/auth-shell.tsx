import { BarChart3, FileSearch, ShieldCheck, Sparkles } from "lucide-react";
import type { ReactNode } from "react";

import { ThemeToggle } from "@/components/shell/theme-toggle";

const capabilities = [
  {
    icon: FileSearch,
    title: "Opportunity intelligence",
    text: "Discover and qualify public procurement opportunities.",
  },
  {
    icon: BarChart3,
    title: "Market intelligence",
    text: "Understand buyers, suppliers and procurement trends.",
  },
  {
    icon: ShieldCheck,
    title: "Explainable decisions",
    text: "Keep tenant data isolated and every future recommendation traceable.",
  },
];

export function AuthShell({
  eyebrow,
  title,
  description,
  children,
}: {
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
}) {
  return (
    <main className="min-h-screen lg:grid lg:grid-cols-[1.05fr_0.95fr]">
      <section className="tp-dark-surface relative hidden overflow-hidden bg-[#14213d] p-12 text-white lg:flex lg:flex-col xl:p-16">
        <div aria-hidden="true" className="absolute -right-40 -top-40 size-[34rem] rounded-full bg-blue-500/10 blur-3xl" />
        <div aria-hidden="true" className="absolute -bottom-52 -left-32 size-[30rem] rounded-full bg-indigo-400/10 blur-3xl" />

        <div className="relative z-10 flex items-center gap-3">
          <div className="grid size-11 place-items-center rounded-xl bg-white text-sm font-bold text-[#14213d]">TP</div>
          <div>
            <p className="text-lg font-semibold">TenderPilot</p>
            <p className="text-xs text-slate-400">Procurement intelligence</p>
          </div>
        </div>

        <div className="relative z-10 my-auto max-w-xl">
          <div className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3.5 py-2 text-xs font-semibold text-blue-200">
            <Sparkles className="size-3.5" />
            Built for smarter bid decisions
          </div>
          <h2 className="mt-7 text-4xl font-semibold tracking-[-0.035em] xl:text-5xl">
            Turn public procurement data into actionable intelligence.
          </h2>
          <p className="mt-5 max-w-lg text-base leading-7 text-slate-300">
            TenderPilot combines structured procurement data, explainable qualification and local AI into one
            professional bid-intelligence workspace.
          </p>

          <div className="mt-10 space-y-3">
            {capabilities.map((capability) => {
              const Icon = capability.icon;
              return (
                <div
                  key={capability.title}
                  className="flex items-start gap-4 rounded-2xl border border-white/10 bg-white/[0.04] p-4"
                >
                  <div className="grid size-9 shrink-0 place-items-center rounded-xl bg-white/10 text-blue-200">
                    <Icon className="size-4" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold">{capability.title}</p>
                    <p className="mt-1 text-xs leading-5 text-slate-400">{capability.text}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <p className="relative z-10 text-xs text-slate-500">Local-first development · No paid API dependency</p>
      </section>

      <section className="relative flex min-h-screen items-center justify-center px-6 py-12 sm:px-10">
        <div className="absolute right-5 top-5">
          <ThemeToggle />
        </div>

        <div className="w-full max-w-md animate-rise">
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <div className="tp-dark-surface grid size-10 place-items-center rounded-xl bg-[#14213d] text-sm font-bold text-white">
              TP
            </div>
            <p className="text-lg font-semibold tracking-tight text-ink">TenderPilot</p>
          </div>

          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-brand">{eyebrow}</p>
          <h1 className="mt-3 text-3xl font-semibold tracking-[-0.03em] text-ink">{title}</h1>
          <p className="mt-3 text-sm leading-6 text-muted">{description}</p>

          <div className="mt-8">{children}</div>
        </div>
      </section>
    </main>
  );
}
