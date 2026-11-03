#!/usr/bin/env node
/**
 * TenderPilot demo API server (synthetic data).
 *
 * Serves scripts/mock-api.mjs over HTTP on http://localhost:8080 so the Next.js
 * frontend can run with no backend, database, search index or local AI.
 *
 *   node scripts/mock-api-server.mjs            # port 8080
 *   PORT=8081 node scripts/mock-api-server.mjs
 *   MOCK_FRESH=1 node scripts/mock-api-server.mjs   # empty account (onboarding)
 *
 * The browser session is signed in automatically (the mock accepts any
 * credentials). Writes are accepted but not persisted.
 */

import http from "node:http";
import { handle } from "./mock-api.mjs";

const PORT = Number(process.env.PORT ?? 8080);

function corsHeaders(request) {
  const origin = request.headers.origin;
  return {
    "Access-Control-Allow-Origin": origin ?? "http://localhost:3000",
    "Access-Control-Allow-Credentials": "true",
    "Access-Control-Allow-Headers": "Authorization, Content-Type",
    "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
    Vary: "Origin",
  };
}

function readBody(request) {
  return new Promise((resolve) => {
    const chunks = [];
    request.on("data", (chunk) => chunks.push(chunk));
    request.on("end", () => {
      const raw = Buffer.concat(chunks).toString("utf8");
      if (!raw) return resolve(null);
      try {
        resolve(JSON.parse(raw));
      } catch {
        resolve(null);
      }
    });
    request.on("error", () => resolve(null));
  });
}

const server = http.createServer(async (request, response) => {
  const headers = corsHeaders(request);

  if (request.method === "OPTIONS") {
    response.writeHead(204, headers);
    response.end();
    return;
  }

  try {
    const url = new URL(request.url ?? "/", `http://localhost:${PORT}`);
    const body = request.method === "GET" ? null : await readBody(request);
    const result = handle(request.method ?? "GET", url.pathname, url.search, body);

    if (result.status === 204 || result.json === undefined) {
      response.writeHead(result.status, headers);
      response.end();
      return;
    }

    response.writeHead(result.status, { ...headers, "Content-Type": "application/json; charset=utf-8" });
    response.end(JSON.stringify(result.json));
  } catch (error) {
    response.writeHead(500, { ...headers, "Content-Type": "application/json" });
    response.end(JSON.stringify({ message: error instanceof Error ? error.message : "Mock server error" }));
  }
});

server.listen(PORT, () => {
  console.log(`TenderPilot demo API (synthetic data) listening on http://localhost:${PORT}`);
});
