"use client";


import {

  ArrowRight,

  LoaderCircle,

  LockKeyhole,

  Mail,

} from "lucide-react";


import Link
  from "next/link";


import {

  useRouter,

} from "next/navigation";


import {

  type FormEvent,

  useState,

} from "react";


import {

  AuthShell,

} from "@/components/auth/auth-shell";


import {

  ApiHttpError,

} from "@/features/auth/auth-api";


import {

  useAuth,

} from "@/features/auth/auth-context";


export default function LoginPage() {

  const router =
    useRouter();


  const {

    login,

  } = useAuth();


  const [

    email,

    setEmail,

  ] = useState(
    "",
  );


  const [

    password,

    setPassword,

  ] = useState(
    "",
  );


  const [

    pending,

    setPending,

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


  async function submit(
    event:
      FormEvent<HTMLFormElement>,
  ) {

    event.preventDefault();


    setPending(
      true,
    );


    setError(
      null,
    );


    try {

      await login({

        email,

        password,

      });


      router.replace(
        "/dashboard",
      );


    } catch (
      cause
    ) {

      setError(

        cause instanceof ApiHttpError

          ? cause.message

          : "Unable to sign in. Please try again.",

      );


    } finally {

      setPending(
        false,
      );

    }

  }


  return (

    <AuthShell

      eyebrow="
        Welcome back
      "

      title="
        Sign in to TenderPilot
      "

      description="
        Access your organization's procurement intelligence workspace.
      "

    >

      <form
        onSubmit={
          submit
        }
        className="
          space-y-5
        "
      >

        {
          error

            ? (

              <div
                className="
                  rounded-xl
                  border
                  border-red-200
                  bg-red-50
                  px-4
                  py-3
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


        <label
          className="
            block
          "
        >

          <span
            className="
              text-sm
              font-medium
              text-slate-700
            "
          >

            Email address

          </span>


          <div
            className="
              relative
              mt-2
            "
          >

            <Mail
              className="
                absolute
                left-3.5
                top-1/2
                size-4
                -translate-y-1/2
                text-slate-400
              "
            />


            <input

              type="email"

              autoComplete="email"

              required

              value={
                email
              }

              onChange={
                (
                  event,
                ) =>
                  setEmail(
                    event.target.value,
                  )
              }

              placeholder="
                you@company.com
              "

              className="
                h-12
                w-full
                rounded-xl
                border
                border-slate-200
                bg-white
                pl-10
                pr-4
                text-sm
                text-slate-900
                outline-none
                transition
                placeholder:text-slate-400
                focus:border-[#2f6fed]
                focus:ring-4
                focus:ring-blue-500/10
              "

            />

          </div>

        </label>


        <label
          className="
            block
          "
        >

          <span
            className="
              text-sm
              font-medium
              text-slate-700
            "
          >

            Password

          </span>


          <div
            className="
              relative
              mt-2
            "
          >

            <LockKeyhole
              className="
                absolute
                left-3.5
                top-1/2
                size-4
                -translate-y-1/2
                text-slate-400
              "
            />


            <input

              type="password"

              autoComplete="current-password"

              required

              value={
                password
              }

              onChange={
                (
                  event,
                ) =>
                  setPassword(
                    event.target.value,
                  )
              }

              placeholder="
                Enter your password
              "

              className="
                h-12
                w-full
                rounded-xl
                border
                border-slate-200
                bg-white
                pl-10
                pr-4
                text-sm
                text-slate-900
                outline-none
                transition
                placeholder:text-slate-400
                focus:border-[#2f6fed]
                focus:ring-4
                focus:ring-blue-500/10
              "

            />

          </div>

        </label>


        <button

          type="submit"

          disabled={
            pending
          }

          className="
            flex
            h-12
            w-full
            items-center
            justify-center
            gap-2
            rounded-xl
            bg-[#14213d]
            px-5
            text-sm
            font-semibold
            text-white
            shadow-sm
            transition
            hover:bg-[#1b2d52]
            disabled:cursor-not-allowed
            disabled:opacity-60
          "

        >

          {
            pending

              ? (

                <LoaderCircle
                  className="
                    size-4
                    animate-spin
                  "
                />

              )

              : (

                <ArrowRight
                  className="
                    size-4
                  "
                />

              )
          }


          {
            pending

              ? "Signing in..."

              : "Sign in"
          }

        </button>


        <p
          className="
            text-center
            text-sm
            text-slate-500
          "
        >

          New to TenderPilot?
          {" "}

          <Link
            href="/register"
            className="
              font-semibold
              text-[#2f6fed]
              hover:underline
            "
          >

            Create your workspace

          </Link>

        </p>

      </form>

    </AuthShell>

  );

}
