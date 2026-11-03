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


/**
 * The server could not be reached at all: the network is down, the server is stopped or the request was
 * blocked. This is different from the server answering with an error.
 */
export class NetworkError extends Error {
  constructor() {
    super(
      typeof navigator !== "undefined" && navigator.onLine === false
        ? "You are offline. Reconnect to the internet and try again."
        : "TenderPilot could not be reached. Check your connection or try again in a moment.",
    );
    this.name = "NetworkError";
  }
}

/** fetch that reports an unreachable server as a NetworkError. A request cancelled on purpose stays an AbortError. */
export async function requestOrNetworkError(input: string, init?: RequestInit): Promise<Response> {
  try {
    return await fetch(input, init);
  } catch (cause) {
    if (cause instanceof Error && cause.name === "AbortError") throw cause;
    throw new NetworkError();
  }
}

/**
 * Whether a failed refresh means the sign-in is really over. A connection or server problem does not:
 * the refresh cookie is still valid and the next try may work.
 */
export function endsSession(cause: unknown): boolean {
  return cause instanceof ApiHttpError && (cause.status === 401 || cause.status === 403);
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

    await requestOrNetworkError(

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

    await requestOrNetworkError(

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

    await requestOrNetworkError(

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
