"use client";


import {

  createContext,

  useCallback,

  useContext,

  useEffect,

  useMemo,

  useRef,

  useState,

  type ReactNode,

} from "react";


import {

  API_BASE_URL,
  endsSession,
  loginSession,

  logoutSession,

  parseAuthorizedResponse,

  refreshSession,

  registerSession,
  requestOrNetworkError,
} from "./auth-api";


import type {

  AuthSession,

  AuthUser,

  LoginInput,

  RegisterInput,

} from "./auth-types";


type AuthStatus =

  | "loading"
  | "authenticated"
  | "unauthenticated"
  /** The server could not be reached, so it is not known whether the session is still valid. */
  | "unreachable";


type AuthContextValue = {

  status:
    AuthStatus;

  user:
    AuthUser
    |
    null;

  login:
    (
      input:
        LoginInput,
    ) => Promise<void>;

  register:
    (
      input:
        RegisterInput,
    ) => Promise<void>;

  logout:
    () => Promise<void>;

  /** Tries to restore the session again after the server could not be reached. */
  retry:
    () => Promise<void>;

  api:
    <T>(
      path:
        string,
      init?:
        RequestInit,
    ) => Promise<T>;

};


const AuthContext =

  createContext<
    AuthContextValue
    |
    null
  >(
    null,
  );


export function AuthProvider({

  children,

}: {

  children:
    ReactNode;

}) {

  const [

    status,

    setStatus,

  ] = useState<AuthStatus>(
    "loading",
  );


  const [

    user,

    setUser,

  ] = useState<
    AuthUser
    |
    null
  >(
    null,
  );


  const accessTokenRef =

    useRef<
      string
      |
      null
    >(
      null,
    );


  const refreshInFlightRef =

    useRef<
      Promise<AuthSession>
      |
      null
    >(
      null,
    );


  const applySession =

    useCallback(
      (
        session:
          AuthSession,
      ) => {

        accessTokenRef.current =
          session.accessToken;


        setUser(
          session.user,
        );


        setStatus(
          "authenticated",
        );

      },

      [],

    );


  const clearSession =

    useCallback(
      () => {

        accessTokenRef.current =
          null;


        setUser(
          null,
        );


        setStatus(
          "unauthenticated",
        );

      },

      [],

    );


  const refreshAccessToken =

    useCallback(
      async () => {

        let inFlight =
          refreshInFlightRef.current;


        if (
          inFlight ===
          null
        ) {

          inFlight =

            refreshSession()
              .finally(
                () => {

                  refreshInFlightRef.current =
                    null;

                },
              );


          refreshInFlightRef.current =
            inFlight;

        }


        const session =

          await inFlight;


        applySession(
          session,
        );


        return session.accessToken;

      },

      [
        applySession,
      ],

    );


  useEffect(
    () => {

      const controller =

        new AbortController();


      refreshSession(
        controller.signal,
      )
        .then(
          (
            session,
          ) => {

            if (
              !controller.signal.aborted
            ) {

              applySession(
                session,
              );

            }

          },
        )
        .catch(
          (
            cause,
          ) => {

            if (
              !controller.signal.aborted
            ) {

              if (
                endsSession(
                  cause,
                )
              ) {

                clearSession();

              } else {

                setStatus(
                  "unreachable",
                );

              }

            }

          },
        );


      return () => {

        controller.abort();

      };

    },

    [
      applySession,
      clearSession,
    ],

  );


  const login =

    useCallback(
      async (
        input:
          LoginInput,
      ) => {

        const session =

          await loginSession(
            input,
          );


        applySession(
          session,
        );

      },

      [
        applySession,
      ],

    );


  const register =

    useCallback(
      async (
        input:
          RegisterInput,
      ) => {

        const session =

          await registerSession(
            input,
          );


        applySession(
          session,
        );

      },

      [
        applySession,
      ],

    );


  const logout =

    useCallback(
      async () => {

        try {

          await logoutSession();


        } finally {

          clearSession();

        }

      },

      [
        clearSession,
      ],

    );


  const retry =

    useCallback(
      async () => {

        try {

          applySession(
            await refreshSession(),
          );

        } catch (
          cause
        ) {

          if (
            endsSession(
              cause,
            )
          ) {

            clearSession();

          }

        }

      },

      [
        applySession,
        clearSession,
      ],

    );


  const api =

    useCallback(
      async <T,>(

        path:
          string,

        init?:
          RequestInit,

      ): Promise<T> => {

        let token =
          accessTokenRef.current;


        if (
          token === null
        ) {

          token =
            await refreshAccessToken();

        }


        const request =

          async (
            accessToken:
              string,
          ) => {

            const headers =

              new Headers(
                init?.headers,
              );


            headers.set(

              "Authorization",

              `Bearer ${accessToken}`,

            );


            return requestOrNetworkError(

              `${API_BASE_URL}${path}`,

              {

                ...init,

                credentials:
                  "include",

                cache:
                  "no-store",

                headers,

              },

            );

          };


        let response =

          await request(
            token,
          );


        if (
          response.status ===
          401
        ) {

          try {

            const refreshedToken =

              await refreshAccessToken();


            response =

              await request(
                refreshedToken,
              );


          } catch (
            cause
          ) {

            if (
              endsSession(
                cause,
              )
            ) {

              clearSession();

            }


            throw cause;

          }

        }


        return parseAuthorizedResponse<T>(
          response,
        );

      },

      [
        clearSession,
        refreshAccessToken,
      ],

    );


  const value =

    useMemo<AuthContextValue>(
      () => ({

        status,

        user,

        login,

        register,

        logout,

        retry,

        api,

      }),

      [
        status,
        user,
        login,
        register,
        logout,
        retry,
        api,
      ],

    );


  return (

    <AuthContext.Provider
      value={
        value
      }
    >

      {
        children
      }

    </AuthContext.Provider>

  );

}


export function useAuth() {

  const context =

    useContext(
      AuthContext,
    );


  if (
    context === null
  ) {

    throw new Error(
      "useAuth must be used inside AuthProvider",
    );

  }


  return context;

}
