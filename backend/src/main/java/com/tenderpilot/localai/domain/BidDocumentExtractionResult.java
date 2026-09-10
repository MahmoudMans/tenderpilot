package com.tenderpilot.localai.domain;

public record BidDocumentExtractionResult(

        String text,

        int characterCount,

        boolean truncated,

        String detectedMediaType,

        String extractorVersion

) {
}
