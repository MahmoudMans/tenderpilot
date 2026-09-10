package com.tenderpilot.bidworkspace.application;

import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.api.BidDocumentContentResponse;
import com.tenderpilot.bidworkspace.api.BidWorkspaceResponse;
import com.tenderpilot.bidworkspace.api.CreateBidDocumentRequest;
import com.tenderpilot.bidworkspace.api.CreateBidRequirementRequest;
import com.tenderpilot.bidworkspace.api.UpdateBidRequirementRequest;

import com.tenderpilot.bidworkspace.domain.BidDocumentCategory;
import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;
import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementEntity;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import com.tenderpilot.bidworkspace.infrastructure.BidDocumentRepository;
import com.tenderpilot.bidworkspace.infrastructure.BidRequirementRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.matching.domain.TenderMatchEntity;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BidWorkspaceService {

    private final TenderMatchRepository
            matchRepository;


    private final BidDecisionRepository
            decisionRepository;


    private final BidDocumentRepository
            documentRepository;


    private final BidRequirementRepository
            requirementRepository;


    private final BidDocumentStorageGateway
            storageGateway;


    private final OpportunityQueryService
            opportunityQueryService;


    public BidWorkspaceService(
            TenderMatchRepository matchRepository,
            BidDecisionRepository decisionRepository,
            BidDocumentRepository documentRepository,
            BidRequirementRepository requirementRepository,
            BidDocumentStorageGateway storageGateway,
            OpportunityQueryService opportunityQueryService
    ) {

        this.matchRepository =
                matchRepository;

        this.decisionRepository =
                decisionRepository;

        this.documentRepository =
                documentRepository;

        this.requirementRepository =
                requirementRepository;

        this.storageGateway =
                storageGateway;

        this.opportunityQueryService =
                opportunityQueryService;
    }


    @Transactional(readOnly = true)
    public BidWorkspaceResponse workspace(
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


        List<BidDocumentEntity> documents =
                documentRepository
                        .findByOrganizationIdAndTender_IdOrderByUploadedAtDesc(
                                organizationId,
                                tenderId
                        );


        List<BidRequirementEntity> requirements =
                requirementRepository
                        .findByOrganizationIdAndTender_IdOrderByMandatoryDescCreatedAtAsc(
                                organizationId,
                                tenderId
                        );


        return response(
                match,
                decision,
                documents,
                requirements
        );
    }


    @Transactional
    public BidWorkspaceResponse uploadDocument(
            UUID organizationId,
            UUID tenderId,
            CreateBidDocumentRequest request,
            String actor
    ) {

        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Bid-document request is required"
            );
        }


        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        String originalFilename =
                required(
                        request.originalFilename(),
                        "Original filename is required"
                );


        byte[] content =
                decodeBase64(
                        request.contentBase64()
                );


        String sha256 =
                sha256(
                        content
                );


        documentRepository
                .findByOrganizationIdAndTender_IdAndSha256(
                        organizationId,
                        tenderId,
                        sha256
                )
                .ifPresent(
                        existing -> {

                            throw new InvalidRequestException(
                                    "This document already exists in the bid workspace"
                            );

                        }
                );


        BidDocumentStorageGateway.StoredBidDocument stored =
                storageGateway
                        .store(
                                organizationId,
                                tenderId,
                                originalFilename,
                                content
                        );


        try {

            BidDocumentEntity document =
                    BidDocumentEntity
                            .create(
                                    organizationId,
                                    match.getTender(),
                                    request.category() == null
                                            ? BidDocumentCategory.OTHER
                                            : request.category(),
                                    originalFilename,
                                    stored.storedFilename(),
                                    clean(
                                            request.contentType()
                                    ),
                                    content.length,
                                    sha256,
                                    stored.storageKey(),
                                    clean(
                                            request.note()
                                    ),
                                    actor(
                                            actor
                                    )
                            );


            documentRepository
                    .saveAndFlush(
                            document
                    );


        } catch (
                RuntimeException exception
        ) {

            storageGateway
                    .delete(
                            stored.storageKey()
                    );


            throw exception;
        }


        return workspace(
                organizationId,
                tenderId
        );
    }


    @Transactional(readOnly = true)
    public BidDocumentContentResponse documentContent(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    ) {

        requireMatch(
                organizationId,
                tenderId
        );


        BidDocumentEntity document =
                requireDocument(
                        organizationId,
                        tenderId,
                        documentId
                );


        byte[] content =
                storageGateway
                        .load(
                                document.getStorageKey()
                        );


        return new BidDocumentContentResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                Base64
                        .getEncoder()
                        .encodeToString(
                                content
                        )
        );
    }


    @Transactional
    public BidWorkspaceResponse deleteDocument(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    ) {

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


        List<BidRequirementEntity> requirements =
                requirementRepository
                        .findByOrganizationIdAndTender_IdOrderByMandatoryDescCreatedAtAsc(
                                organizationId,
                                tenderId
                        );


        for (
                BidRequirementEntity requirement :
                requirements
        ) {

            if (
                    requirement.getSourceDocument()
                            !=
                            null
                    &&
                    requirement
                            .getSourceDocument()
                            .getId()
                            .equals(
                                    documentId
                            )
            ) {

                requirement.detachSourceDocument();

                requirementRepository
                        .save(
                                requirement
                        );
            }
        }


        requirementRepository.flush();


        documentRepository
                .delete(
                        document
                );


        documentRepository.flush();


        storageGateway
                .delete(
                        document.getStorageKey()
                );


        return workspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidWorkspaceResponse createRequirement(
            UUID organizationId,
            UUID tenderId,
            CreateBidRequirementRequest request,
            String actor
    ) {

        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Bid-requirement request is required"
            );
        }


        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        BidDocumentEntity sourceDocument =
                optionalDocument(
                        organizationId,
                        tenderId,
                        request.sourceDocumentId()
                );


        BidRequirementEntity requirement =
                BidRequirementEntity
                        .create(
                                organizationId,
                                match.getTender(),
                                sourceDocument,
                                required(
                                        request.title(),
                                        "Requirement title is required"
                                ),
                                clean(
                                        request.description()
                                ),
                                request.category() == null
                                        ? BidRequirementCategory.OTHER
                                        : request.category(),
                                request.mandatory() == null
                                        ||
                                        request.mandatory(),
                                clean(
                                        request.sourceReference()
                                ),
                                request.dueDate(),
                                clean(
                                        request.assignedTo()
                                ),
                                clean(
                                        request.evidenceNotes()
                                ),
                                actor(
                                        actor
                                )
                        );


        requirementRepository
                .saveAndFlush(
                        requirement
                );


        return workspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidWorkspaceResponse updateRequirement(
            UUID organizationId,
            UUID tenderId,
            UUID requirementId,
            UpdateBidRequirementRequest request,
            String actor
    ) {

        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Bid-requirement update is required"
            );
        }


        requireWritableMatch(
                organizationId,
                tenderId
        );


        BidRequirementEntity requirement =
                requirementRepository
                        .findByIdAndOrganizationIdAndTender_Id(
                                requirementId,
                                organizationId,
                                tenderId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Bid requirement was not found"
                                        )
                        );


        BidDocumentEntity sourceDocument =
                request.sourceDocumentId()
                        ==
                        null
                        ? requirement.getSourceDocument()
                        : requireDocument(
                                organizationId,
                                tenderId,
                                request.sourceDocumentId()
                        );


        requirement.update(
                sourceDocument,

                request.title() == null
                        ? requirement.getTitle()
                        : required(
                                request.title(),
                                "Requirement title cannot be empty"
                        ),

                request.description() == null
                        ? requirement.getDescription()
                        : clean(
                                request.description()
                        ),

                request.category() == null
                        ? requirement.getCategory()
                        : request.category(),

                request.status() == null
                        ? requirement.getStatus()
                        : request.status(),

                request.mandatory() == null
                        ? requirement.isMandatory()
                        : request.mandatory(),

                request.sourceReference() == null
                        ? requirement.getSourceReference()
                        : clean(
                                request.sourceReference()
                        ),

                request.dueDate() == null
                        ? requirement.getDueDate()
                        : request.dueDate(),

                request.assignedTo() == null
                        ? requirement.getAssignedTo()
                        : clean(
                                request.assignedTo()
                        ),

                request.evidenceNotes() == null
                        ? requirement.getEvidenceNotes()
                        : clean(
                                request.evidenceNotes()
                        ),

                actor(
                        actor
                )
        );


        requirementRepository
                .saveAndFlush(
                        requirement
                );


        return workspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidWorkspaceResponse deleteRequirement(
            UUID organizationId,
            UUID tenderId,
            UUID requirementId
    ) {

        requireWritableMatch(
                organizationId,
                tenderId
        );


        BidRequirementEntity requirement =
                requirementRepository
                        .findByIdAndOrganizationIdAndTender_Id(
                                requirementId,
                                organizationId,
                                tenderId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Bid requirement was not found"
                                        )
                        );


        requirementRepository
                .delete(
                        requirement
                );


        requirementRepository.flush();


        return workspace(
                organizationId,
                tenderId
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
                    "Bid workspace is locked because the current decision is NO-GO. Reopen the decision before editing."
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


    private BidDocumentEntity optionalDocument(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    ) {

        return documentId == null
                ? null
                : requireDocument(
                        organizationId,
                        tenderId,
                        documentId
                );
    }


    private BidWorkspaceResponse response(
            TenderMatchEntity match,
            BidDecisionEntity decision,
            List<BidDocumentEntity> documents,
            List<BidRequirementEntity> requirements
    ) {

        int completed =
                (int)
                        requirements
                                .stream()
                                .filter(
                                        requirement ->
                                                requirement.getStatus()
                                                        ==
                                                        BidRequirementStatus.SATISFIED
                                                ||
                                                requirement.getStatus()
                                                        ==
                                                        BidRequirementStatus.NOT_APPLICABLE
                                )
                                .count();


        int gaps =
                (int)
                        requirements
                                .stream()
                                .filter(
                                        requirement ->
                                                requirement.getStatus()
                                                        ==
                                                        BidRequirementStatus.GAP
                                )
                                .count();


        int mandatoryGaps =
                (int)
                        requirements
                                .stream()
                                .filter(
                                        requirement ->
                                                requirement.isMandatory()
                                                &&
                                                requirement.getStatus()
                                                        ==
                                                        BidRequirementStatus.GAP
                                )
                                .count();


        int completionPercentage =
                requirements.isEmpty()
                        ? 0
                        : (int)
                                Math.round(
                                        completed
                                                *
                                                100.0
                                                /
                                                requirements.size()
                                );


        BidDecisionOutcome decisionOutcome =
                decision == null
                        ? null
                        : decision.getOutcome();


        boolean writable =
                decisionOutcome
                        !=
                        BidDecisionOutcome.NO_GO;


        return new BidWorkspaceResponse(
                match
                        .getTender()
                        .getId(),

                match.getTotalScore(),

                match.getGrade(),

                decisionOutcome,

                writable,

                writable
                        ? null
                        : "Current decision is NO-GO. Reopen it before modifying the workspace.",

                opportunityQueryService
                        .summaryById(
                                match
                                        .getTender()
                                        .getId()
                        ),

                new BidWorkspaceResponse
                        .Statistics(
                                documents.size(),
                                requirements.size(),
                                completed,
                                gaps,
                                mandatoryGaps,
                                completionPercentage
                        ),

                documents
                        .stream()
                        .map(
                                this::documentResponse
                        )
                        .toList(),

                requirements
                        .stream()
                        .map(
                                this::requirementResponse
                        )
                        .toList()
        );
    }


    private BidWorkspaceResponse.DocumentResponse
    documentResponse(
            BidDocumentEntity document
    ) {

        return new BidWorkspaceResponse
                .DocumentResponse(
                        document.getId(),
                        document.getCategory(),
                        document.getOriginalFilename(),
                        document.getContentType(),
                        document.getSizeBytes(),
                        document.getSha256(),
                        document.getNote(),
                        document.getUploadedBy(),
                        document.getUploadedAt()
                );
    }


    private BidWorkspaceResponse.RequirementResponse
    requirementResponse(
            BidRequirementEntity requirement
    ) {

        return new BidWorkspaceResponse
                .RequirementResponse(
                        requirement.getId(),

                        requirement.getSourceDocument()
                                ==
                                null
                                ? null
                                : requirement
                                        .getSourceDocument()
                                        .getId(),

                        requirement.getSourceDocument()
                                ==
                                null
                                ? null
                                : requirement
                                        .getSourceDocument()
                                        .getOriginalFilename(),

                        requirement.getTitle(),

                        requirement.getDescription(),

                        requirement.getCategory(),

                        requirement.getStatus(),

                        requirement.isMandatory(),

                        requirement.getSourceReference(),

                        requirement.getDueDate(),

                        requirement.getAssignedTo(),

                        requirement.getEvidenceNotes(),

                        requirement.getCreatedBy(),

                        requirement.getUpdatedBy(),

                        requirement.getCreatedAt(),

                        requirement.getUpdatedAt()
                );
    }


    private byte[] decodeBase64(
            String value
    ) {

        String cleaned =
                required(
                        value,
                        "Document content is required"
                );


        try {

            return Base64
                    .getDecoder()
                    .decode(
                            cleaned.getBytes(
                                    StandardCharsets.US_ASCII
                            )
                    );


        } catch (
                IllegalArgumentException exception
        ) {

            throw new InvalidRequestException(
                    "Document content is not valid Base64"
            );
        }
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


    private String required(
            String value,
            String message
    ) {

        String cleaned =
                clean(
                        value
                );


        if (
                cleaned == null
        ) {

            throw new InvalidRequestException(
                    message
            );
        }


        return cleaned;
    }


    private String actor(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );


        return cleaned == null
                ? "unknown"
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

}
