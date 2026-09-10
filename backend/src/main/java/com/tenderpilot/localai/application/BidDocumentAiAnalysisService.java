package com.tenderpilot.localai.application;

import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.application.BidDocumentStorageGateway;

import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;

import com.tenderpilot.bidworkspace.infrastructure.BidDocumentRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.localai.api.BidDocumentAiAnalysisResponse;

import com.tenderpilot.localai.domain.BidAiFindingValue;
import com.tenderpilot.localai.domain.BidDocumentAnalysisEntity;
import com.tenderpilot.localai.domain.BidDocumentAnalysisStatus;
import com.tenderpilot.localai.domain.BidDocumentExtractionResult;
import com.tenderpilot.localai.domain.BidDocumentTextEntity;
import com.tenderpilot.localai.domain.OllamaBidAnalysisResult;

import com.tenderpilot.localai.infrastructure.BidDocumentAnalysisRepository;
import com.tenderpilot.localai.infrastructure.BidDocumentTextRepository;
import com.tenderpilot.localai.infrastructure.LocalAiSettings;

import com.tenderpilot.matching.domain.TenderMatchEntity;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BidDocumentAiAnalysisService {

    private final TenderMatchRepository
            matchRepository;


    private final BidDecisionRepository
            decisionRepository;


    private final BidDocumentRepository
            documentRepository;


    private final BidDocumentTextRepository
            textRepository;


    private final BidDocumentAnalysisRepository
            analysisRepository;


    private final BidDocumentStorageGateway
            storageGateway;


    private final BidDocumentTextExtractor
            textExtractor;


    private final OllamaGateway
            ollamaGateway;


    private final LocalAiSettings
            settings;


    public BidDocumentAiAnalysisService(
            TenderMatchRepository matchRepository,
            BidDecisionRepository decisionRepository,
            BidDocumentRepository documentRepository,
            BidDocumentTextRepository textRepository,
            BidDocumentAnalysisRepository analysisRepository,
            BidDocumentStorageGateway storageGateway,
            BidDocumentTextExtractor textExtractor,
            OllamaGateway ollamaGateway,
            LocalAiSettings settings
    ) {

        this.matchRepository =
                matchRepository;

        this.decisionRepository =
                decisionRepository;

        this.documentRepository =
                documentRepository;

        this.textRepository =
                textRepository;

        this.analysisRepository =
                analysisRepository;

        this.storageGateway =
                storageGateway;

        this.textExtractor =
                textExtractor;

        this.ollamaGateway =
                ollamaGateway;

        this.settings =
                settings;
    }


    @Transactional(readOnly = true)
    public List<BidDocumentAiAnalysisResponse> analyses(
            UUID organizationId,
            UUID tenderId
    ) {

        requireMatch(
                organizationId,
                tenderId
        );


        return documentRepository
                .findByOrganizationIdAndTender_IdOrderByUploadedAtDesc(
                        organizationId,
                        tenderId
                )
                .stream()
                .map(
                        document ->
                                response(
                                        organizationId,
                                        tenderId,
                                        document
                                )
                )
                .toList();
    }


    @Transactional
    public BidDocumentAiAnalysisResponse analyze(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        BidDocumentEntity document =
                requireDocument(
                        organizationId,
                        tenderId,
                        documentId
                );


        BidDocumentAnalysisEntity analysis =
                analysisRepository
                        .findByOrganizationIdAndTender_IdAndDocument_Id(
                                organizationId,
                                tenderId,
                                documentId
                        )
                        .orElseGet(
                                () ->
                                        BidDocumentAnalysisEntity
                                                .create(
                                                        organizationId,
                                                        match.getTender(),
                                                        document,
                                                        settings.provider(),
                                                        settings.model(),
                                                        settings.promptVersion()
                                                )
                        );


        String textSha =
                null;


        try {

            BidDocumentTextEntity extracted =
                    extractOrReuse(
                            organizationId,
                            tenderId,
                            document
                    );


            textSha =
                    extracted.getTextSha256();


            String aiInput =
                    aiInputWindow(
                            extracted.getExtractedText()
                    );


            if (
                    aiInput.length()
                            <
                            20
            ) {

                throw new InvalidRequestException(
                        "Document does not contain enough readable text for AI analysis"
                );
            }


            OllamaBidAnalysisResult result =
                    ollamaGateway
                            .analyze(
                                    document.getOriginalFilename(),
                                    aiInput
                            );


            validateResult(
                    result
            );


            List<BidAiFindingValue> findings =
                    result
                            .findings()
                            .stream()
                            .limit(
                                    50
                            )
                            .map(
                                    this::finding
                            )
                            .toList();


            analysis.complete(
                    settings.provider(),
                    settings.model(),
                    settings.promptVersion(),
                    textSha,
                    cleanRequired(
                            result.summary(),
                            "AI summary is missing"
                    ),
                    result.confidence(),
                    findings
            );


        } catch (
                RuntimeException exception
        ) {

            analysis.fail(
                    settings.provider(),
                    settings.model(),
                    settings.promptVersion(),
                    textSha,
                    safeError(
                            exception
                    )
            );
        }


        analysisRepository
                .saveAndFlush(
                        analysis
                );


        return response(
                organizationId,
                tenderId,
                document
        );
    }


    @Transactional
    public List<BidDocumentAiAnalysisResponse> analyzeAll(
            UUID organizationId,
            UUID tenderId
    ) {

        requireWritableMatch(
                organizationId,
                tenderId
        );


        List<BidDocumentEntity> documents =
                documentRepository
                        .findByOrganizationIdAndTender_IdOrderByUploadedAtDesc(
                                organizationId,
                                tenderId
                        );


        List<BidDocumentAiAnalysisResponse> results =
                new ArrayList<>();


        for (
                BidDocumentEntity document :
                documents
        ) {

            results.add(
                    analyze(
                            organizationId,
                            tenderId,
                            document.getId()
                    )
            );
        }


        return List.copyOf(
                results
        );
    }


    private BidDocumentTextEntity extractOrReuse(
            UUID organizationId,
            UUID tenderId,
            BidDocumentEntity document
    ) {

        BidDocumentTextEntity existing =
                textRepository
                        .findByOrganizationIdAndTender_IdAndDocument_Id(
                                organizationId,
                                tenderId,
                                document.getId()
                        )
                        .orElse(
                                null
                        );


        if (
                existing != null
                &&
                existing.isCurrentFor(
                        document.getSha256()
                )
        ) {

            return existing;
        }


        byte[] content =
                storageGateway
                        .load(
                                document.getStorageKey()
                        );


        BidDocumentExtractionResult result =
                textExtractor
                        .extract(
                                document.getOriginalFilename(),
                                content
                        );


        String textSha256 =
                sha256(
                        result
                                .text()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        BidDocumentTextEntity entity =
                existing == null
                        ? BidDocumentTextEntity
                                .create(
                                        organizationId,
                                        document.getTender(),
                                        document
                                )
                        : existing;


        entity.apply(
                document.getSha256(),
                textSha256,
                result
        );


        return textRepository
                .saveAndFlush(
                        entity
                );
    }


    private BidDocumentAiAnalysisResponse response(
            UUID organizationId,
            UUID tenderId,
            BidDocumentEntity document
    ) {

        BidDocumentTextEntity text =
                textRepository
                        .findByOrganizationIdAndTender_IdAndDocument_Id(
                                organizationId,
                                tenderId,
                                document.getId()
                        )
                        .orElse(
                                null
                        );


        BidDocumentAnalysisEntity analysis =
                analysisRepository
                        .findByOrganizationIdAndTender_IdAndDocument_Id(
                                organizationId,
                                tenderId,
                                document.getId()
                        )
                        .orElse(
                                null
                        );


        if (
                analysis == null
        ) {

            return new BidDocumentAiAnalysisResponse(
                    document.getId(),
                    document.getOriginalFilename(),
                    document.getCategory(),
                    BidDocumentAnalysisStatus.NOT_ANALYZED,
                    settings.provider(),
                    settings.model(),
                    settings.promptVersion(),
                    null,
                    null,
                    null,
                    text == null
                            ? null
                            : text.getCharacterCount(),
                    text == null
                            ? null
                            : text.isTruncated(),
                    text == null
                            ? null
                            : text.getDetectedMediaType(),
                    null,
                    List.of()
            );
        }


        return new BidDocumentAiAnalysisResponse(
                document.getId(),

                document.getOriginalFilename(),

                document.getCategory(),

                analysis.getStatus(),

                analysis.getProvider(),

                analysis.getModel(),

                analysis.getPromptVersion(),

                analysis.getConfidenceScore(),

                analysis.getSummary(),

                analysis.getErrorMessage(),

                text == null
                        ? null
                        : text.getCharacterCount(),

                text == null
                        ? null
                        : text.isTruncated(),

                text == null
                        ? null
                        : text.getDetectedMediaType(),

                analysis.getAnalyzedAt(),

                analysis
                        .getFindings()
                        .stream()
                        .map(
                                finding ->
                                        new BidDocumentAiAnalysisResponse
                                                .FindingResponse(
                                                        finding.getType(),
                                                        finding.getTitle(),
                                                        finding.getDetail(),
                                                        finding.getEvidenceSnippet(),
                                                        finding.getMandatory(),
                                                        finding.getSuggestedRequirementCategory()
                                                )
                        )
                        .toList()
        );
    }


    private TenderMatchEntity requireMatch(
            UUID organizationId,
            UUID tenderId
    ) {

        return matchRepository
                .findByOrganizationIdAndTender_Id(
                        organizationId,
                        tenderId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "No company-to-tender match exists for this organization and opportunity"
                                )
                );
    }


    private TenderMatchEntity requireWritableMatch(
            UUID organizationId,
            UUID tenderId
    ) {

        TenderMatchEntity match =
                requireMatch(
                        organizationId,
                        tenderId
                );


        BidDecisionEntity decision =
                decisionRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
                        .orElse(
                                null
                        );


        if (
                decision != null
                &&
                decision.getOutcome()
                        ==
                        BidDecisionOutcome.NO_GO
        ) {

            throw new InvalidRequestException(
                    "Local AI analysis is locked because the current bid decision is NO-GO"
            );
        }


        return match;
    }


    private BidDocumentEntity requireDocument(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    ) {

        return documentRepository
                .findByIdAndOrganizationIdAndTender_Id(
                        documentId,
                        organizationId,
                        tenderId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Bid document was not found"
                                )
                );
    }


    private BidAiFindingValue finding(
            OllamaBidAnalysisResult.Finding finding
    ) {

        return new BidAiFindingValue(
                finding.type(),

                cleanRequired(
                        finding.title(),
                        "Untitled finding"
                ),

                cleanRequired(
                        finding.detail(),
                        "No detail supplied"
                ),

                clean(
                        finding.evidence()
                ),

                finding.mandatory(),

                finding.requirementCategory()
        );
    }


    private void validateResult(
            OllamaBidAnalysisResult result
    ) {

        if (
                result == null
        ) {

            throw new IllegalStateException(
                    "Local AI returned no analysis"
            );
        }


        if (
                result.summary() == null
                ||
                result.summary()
                        .isBlank()
        ) {

            throw new IllegalStateException(
                    "Local AI returned an empty summary"
            );
        }


        if (
                result.confidence() == null
                ||
                result.confidence() < 0
                ||
                result.confidence() > 100
        ) {

            throw new IllegalStateException(
                    "Local AI returned an invalid confidence score"
            );
        }


        if (
                result.findings() == null
        ) {

            throw new IllegalStateException(
                    "Local AI returned no findings collection"
            );
        }
    }


    private String aiInputWindow(
            String text
    ) {

        int maximum =
                settings.maxInputChars();


        if (
                text.length()
                        <=
                        maximum
        ) {

            return text;
        }


        int headLength =
                (int)
                        Math.floor(
                                maximum * 0.70
                        );


        String marker =
                "\n\n[... MIDDLE CONTENT OMITTED FOR LOCAL SUMMARY ANALYSIS ...]\n\n";


        int tailLength =
                Math.max(
                        0,
                        maximum
                                -
                                headLength
                                -
                                marker.length()
                );


        return text.substring(
                0,
                headLength
        )
                +
                marker
                +
                text.substring(
                        text.length()
                                -
                                tailLength
                );
    }


    private String sha256(
            byte[] content
    ) {

        try {

            MessageDigest digest =
                    MessageDigest
                            .getInstance(
                                    "SHA-256"
                            );


            return HexFormat
                    .of()
                    .formatHex(
                            digest.digest(
                                    content
                            )
                    );


        } catch (
                NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }


    private String cleanRequired(
            String value,
            String fallback
    ) {

        String cleaned =
                clean(
                        value
                );


        return cleaned == null
                ? fallback
                : cleaned;
    }


    private String clean(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String cleaned =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return cleaned.isBlank()
                ? null
                : cleaned;
    }


    private String safeError(
            Throwable throwable
    ) {

        String message =
                throwable.getMessage();


        if (
                message == null
                ||
                message.isBlank()
        ) {

            message =
                    throwable
                            .getClass()
                            .getSimpleName();
        }


        if (
                message.length()
                        >
                        1800
        ) {

            message =
                    message.substring(
                            0,
                            1800
                    );
        }


        return message;
    }

}
