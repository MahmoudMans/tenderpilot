import type {

  AuthSession,

  LoginInput,

  RegisterInput,

} from "./auth-types";


export const API_BASE_URL =

  process.env.NEXT_PUBLIC_API_URL
  ??
  "http://localhost:8080";


type ApiErrorBody = {

  message?:
    string;

  violations?: Array<{

    field:
      string;

    message:
      string;

  }>;

};


export class ApiHttpError
  extends Error {

  readonly status:
    number;


  readonly violations:
    ApiErrorBody["violations"];


  constructor(

    status:
      number,

    message:
      string,

    violations?:
      ApiErrorBody["violations"],

  ) {

    super(
      message,
    );


    this.name =
      "ApiHttpError";


    this.status =
      status;


    this.violations =
      violations;

  }

}


async function readJson<T>(

  response:
    Response,

): Promise<T> {

  const body:
    unknown =

    await response.json();


  return body as T;

}


async function throwApiError(

  response:
    Response,

): Promise<never> {

  let body:
    ApiErrorBody
    |
    null = null;


  try {

    body =

      await readJson<ApiErrorBody>(
        response,
      );


  } catch {

    body =
      null;

  }


  throw new ApiHttpError(

    response.status,

    body?.message
    ??
    `Request failed with HTTP ${response.status}`,

    body?.violations,

  );

}


async function sessionRequest(

  path:
    string,

  body:
    LoginInput
    |
    RegisterInput,

): Promise<AuthSession> {

  const response =

    await fetch(

      `${API_BASE_URL}${path}`,

      {

        method:
          "POST",

        credentials:
          "include",

        cache:
          "no-store",

        headers: {

          "Content-Type":
            "application/json",

        },

        body:
          JSON.stringify(
            body,
          ),

      },

    );


  if (
    !response.ok
  ) {

    return throwApiError(
      response,
    );

  }


  return readJson<AuthSession>(
    response,
  );

}


export function loginSession(

  input:
    LoginInput,

): Promise<AuthSession> {

  return sessionRequest(

    "/api/v1/auth/login",

    input,

  );

}


export function registerSession(

  input:
    RegisterInput,

): Promise<AuthSession> {

  return sessionRequest(

    "/api/v1/auth/register",

    input,

  );

}


export async function refreshSession(

  signal?:
    AbortSignal,

): Promise<AuthSession> {

  const response =

    await fetch(

      `${API_BASE_URL}/api/v1/auth/refresh`,

      {

        method:
          "POST",

        credentials:
          "include",

        cache:
          "no-store",

        signal,

      },

    );


  if (
    !response.ok
  ) {

    return throwApiError(
      response,
    );

  }


  return readJson<AuthSession>(
    response,
  );

}


export async function logoutSession():
Promise<void> {

  const response =

    await fetch(

      `${API_BASE_URL}/api/v1/auth/logout`,

      {

        method:
          "POST",

        credentials:
          "include",

        cache:
          "no-store",

      },

    );


  if (
    !response.ok
    &&
    response.status !==
      401
  ) {

    await throwApiError(
      response,
    );

  }

}


export async function parseAuthorizedResponse<T>(

  response:
    Response,

): Promise<T> {

  if (
    !response.ok
  ) {

    return throwApiError(
      response,
    );

  }


  if (
    response.status ===
    204
  ) {

    return undefined as T;

  }


  return readJson<T>(
    response,
  );

}
