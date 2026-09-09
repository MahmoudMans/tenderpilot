"use client";


import {

  ArrowRight,

  Building2,

  LoaderCircle,

  LockKeyhole,

  Mail,

  UserRound,

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


function inputClassName() {

  return `
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
  `;

}


export default function RegisterPage() {

  const router =
    useRouter();


  const {

    register,

  } = useAuth();


  const [

    fullName,

    setFullName,

  ] = useState(
    "",
  );


  const [

    organizationName,

    setOrganizationName,

  ] = useState(
    "",
  );


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

      await register({

        fullName,

        organizationName,

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

          : "Unable to create your TenderPilot workspace.",

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
        Create workspace
      "

      title="
        Start your TenderPilot organization
      "

      description="
        Your first account becomes the organization owner. You can build the procurement profile in the next development phase.
      "

    >

      <form
        onSubmit={
          submit
        }
        className="
          space-y-4
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

            Your name

          </span>


          <div
            className="
              relative
              mt-2
            "
          >

            <UserRound
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

              required

              maxLength={
                160
              }

              value={
                fullName
              }

              onChange={
                (
                  event,
                ) =>
                  setFullName(
                    event.target.value,
                  )
              }

              placeholder="
                Julie Martin
              "

              className={
                inputClassName()
              }

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

            Organization

          </span>


          <div
            className="
              relative
              mt-2
            "
          >

            <Building2
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

              required

              maxLength={
                180
              }

              value={
                organizationName
              }

              onChange={
                (
                  event,
                ) =>
                  setOrganizationName(
                    event.target.value,
                  )
              }

              placeholder="
                Aurelia Rénovation
              "

              className={
                inputClassName()
              }

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

            Work email

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

              maxLength={
                320
              }

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
                julie@aurelia.fr
              "

              className={
                inputClassName()
              }

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

              autoComplete="new-password"

              required

              minLength={
                10
              }

              maxLength={
                100
              }

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
                At least 10 characters
              "

              className={
                inputClassName()
              }

            />

          </div>


          <p
            className="
              mt-2
              text-xs
              leading-5
              text-slate-400
            "
          >

            Use at least 10 characters. Passwords are stored only as adaptive one-way hashes.

          </p>

        </label>


        <button

          type="submit"

          disabled={
            pending
          }

          className="
            mt-2
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

              ? "Creating workspace..."

              : "Create workspace"
          }

        </button>


        <p
          className="
            text-center
            text-sm
            text-slate-500
          "
        >

          Already have an account?
          {" "}

          <Link
            href="/login"
            className="
              font-semibold
              text-[#2f6fed]
              hover:underline
            "
          >

            Sign in

          </Link>

        </p>

      </form>

    </AuthShell>

  );

}
