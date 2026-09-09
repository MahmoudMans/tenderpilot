export type DependencyStatus = {

  name:
    string;

  status:
    | "UP"
    | "DOWN";

  detail:
    string;

};


export type SystemStatus = {

  application:
    string;

  version:
    string;

  environment:
    string;

  timestamp:
    string;

  dependencies:
    DependencyStatus[];

};


const API_BASE_URL =

  process.env.NEXT_PUBLIC_API_URL
  ??
  "http://localhost:8080";


export async function fetchSystemStatus(

  signal?: AbortSignal,

): Promise<SystemStatus> {

  const response = await fetch(

    `${API_BASE_URL}/api/v1/system/status`,

    {

      method:
        "GET",

      cache:
        "no-store",

      signal,

    },

  );


  if (!response.ok) {

    throw new Error(

      `Backend returned HTTP ${response.status}`,

    );

  }


  const body =

    await response.json();


  return body as SystemStatus;

}
