package com.tenderpilot.bidworkspace.api;

import java.util.UUID;

public record BidDocumentContentResponse(

        UUID id,

        String originalFilename,

        String contentType,

        String contentBase64

) {
}
