"use client";

/** Last-resort boundary: replaces the root layout, so it cannot rely on app styles or providers. */
export default function GlobalError({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <html lang="en">
      <body
        style={{
          margin: 0,
          minHeight: "100vh",
          display: "grid",
          placeItems: "center",
          fontFamily: "system-ui, sans-serif",
          background: "#f5f7fb",
          color: "#0f1b34",
        }}
      >
        <div style={{ maxWidth: 420, padding: 24, textAlign: "center" }}>
          <h1 style={{ fontSize: 24, margin: "0 0 12px" }}>TenderPilot could not load</h1>
          <p style={{ fontSize: 14, lineHeight: 1.6, color: "#667085", margin: "0 0 24px" }}>
            A critical error occurred. Reloading usually fixes it.
          </p>
          <button
            type="button"
            onClick={reset}
            style={{
              height: 40,
              padding: "0 20px",
              borderRadius: 12,
              border: 0,
              background: "#14213d",
              color: "#fff",
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            Reload
          </button>
        </div>
      </body>
    </html>
  );
}
