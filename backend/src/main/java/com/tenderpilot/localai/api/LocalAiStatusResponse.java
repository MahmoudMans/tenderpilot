package com.tenderpilot.localai.api;

public record LocalAiStatusResponse(

        boolean enabled,

        boolean reachable,

        boolean modelAvailable,

        String provider,

        String model,

        String baseUrl,

        String mode,

        String dataBoundary

) {
}
