package com.tenderpilot.localai.api;

import com.tenderpilot.bidworkspace.domain.BidDocumentCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;

import com.tenderpilot.localai.domain.BidAiFindingType;
import com.tenderpilot.localai.domain.BidDocumentAnalysisStatus;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

public record BidDocumentAiAnalysisResponse(

        UUID documentId,

        String filename,

        BidDocumentCategory documentCategory,

        BidDocumentAnalysisStatus status,

        String provider,

        String model,

        String promptVersion,

        Integer confidence,

        String summary,

        String errorMessage,

        Integer extractedCharacterCount,

        Boolean extractionTruncated,

        String detectedMediaType,

        Instant analyzedAt,

        List<FindingResponse> findings

) {

    public record FindingResponse(

            BidAiFindingType type,

            String title,

            String detail,

            String evidenceSnippet,

            Boolean mandatory,

            BidRequirementCategory requirementCategory

    ) {
    }

}
