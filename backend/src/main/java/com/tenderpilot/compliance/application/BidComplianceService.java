package com.tenderpilot.compliance.application;

import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.domain.BidRequirementEntity;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import com.tenderpilot.bidworkspace.infrastructure.BidRequirementRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.compliance.api.BidComplianceMatrixResponse;
import com.tenderpilot.compliance.api.UpdateComplianceReviewRequest;

import com.tenderpilot.compliance.domain.BidComplianceReviewEntity;
import com.tenderpilot.compliance.domain.ComplianceReadiness;
import com.tenderpilot.compliance.domain.ComplianceRequirementSnapshot;
import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

import com.tenderpilot.compliance.infrastructure.BidComplianceReviewRepository;

import com.tenderpilot.matching.domain.TenderMatchEntity;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BidComplianceService {

    private final TenderMatchRepository
            matchRepository;


    private final BidDecisionRepository
            decisionRepository;


    private final BidRequirementRepository
            requirementRepository;


    private final BidComplianceReviewRepository
            reviewRepository;


    private final ComplianceReadinessEngine
            readinessEngine;


    public BidComplianceService(
            TenderMatchRepository matchRepository,
            BidDecisionRepository decisionRepository,
            BidRequirementRepository requirementRepository,
            BidComplianceReviewRepository reviewRepository,
            ComplianceReadinessEngine readinessEngine
    ) {

        this.matchRepository =
                matchRepository;

        this.decisionRepository =
                decisionRepository;

        this.requirementRepository =
                requirementRepository;

        this.reviewRepository =
                reviewRepository;

        this.readinessEngine =
                readinessEngine;
    }


    @Transactional(readOnly = true)
    public BidComplianceMatrixResponse matrix(
            UUID organizationId,
            UUID tenderId
    ) {

        requireMatch(
                organizationId,
                tenderId
        );


        return buildMatrix(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidComplianceMatrixResponse review(
            UUID organizationId,
            UUID tenderId,
            UUID requirementId,
            UpdateComplianceReviewRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        if (
                request == null
                ||
                request.verdict() == null
        ) {

            throw new InvalidRequestException(
                    "Compliance verdict is required"
            );
        }


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


        NormalizedReview normalized =
                normalizeReview(
                        request
                );


        BidComplianceReviewEntity review =
                reviewRepository
                        .findByRequirement_IdAndOrganizationIdAndTender_Id(
                                requirementId,
                                organizationId,
                                tenderId
                        )
                        .orElseGet(
                                () ->
                                        BidComplianceReviewEntity
                                                .create(
                                                        organizationId,
                                                        match.getTender(),
                                                        requirement
                                                )
                        );


        review.review(
                normalized.verdict(),

                normalized.riskLevel(),

                normalized.evidenceStrength(),

                clean(
                        request.reviewRationale()
                ),

                clean(
                        request.mitigationAction()
                ),

                actor(
                        actor
                )
        );


        reviewRepository
                .saveAndFlush(
                        review
                );


        BidRequirementStatus synchronizedStatus =
                requirementStatus(
                        normalized.verdict()
                );


        requirement.update(
                requirement.getSourceDocument(),

                requirement.getTitle(),

                requirement.getDescription(),

                requirement.getCategory(),

                synchronizedStatus,

                requirement.isMandatory(),

                requirement.getSourceReference(),

                requirement.getDueDate(),

                requirement.getAssignedTo(),

                requirement.getEvidenceNotes(),

                actor(
                        actor
                )
        );


        requirementRepository
                .saveAndFlush(
                        requirement
                );


        return buildMatrix(
                organizationId,
                tenderId
        );
    }


    private BidComplianceMatrixResponse buildMatrix(
            UUID organizationId,
            UUID tenderId
    ) {

        List<BidRequirementEntity> requirements =
                requirementRepository
                        .findByOrganizationIdAndTender_IdOrderByMandatoryDescCreatedAtAsc(
                                organizationId,
                                tenderId
                        );


        Map<UUID, BidComplianceReviewEntity> reviews =
                new HashMap<>();


        for (
                BidComplianceReviewEntity review :
                reviewRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
        ) {

            reviews.put(
                    review
                            .getRequirement()
                            .getId(),
                    review
            );
        }


        List<ComplianceRequirementSnapshot> snapshots =
                requirements
                        .stream()
                        .map(
                                requirement ->
                                        snapshot(
                                                requirement,
                                                reviews.get(
                                                        requirement.getId()
                                                )
                                        )
                        )
                        .toList();


        ComplianceReadiness readiness =
                readinessEngine
                        .calculate(
                                snapshots
                        );


        int reviewed =
                (int)
                        snapshots
                                .stream()
                                .filter(
                                        snapshot ->
                                                snapshot.verdict()
                                                        !=
                                                        ComplianceVerdict.UNASSESSED
                                )
                                .count();


        int mandatory =
                (int)
                        requirements
                                .stream()
                                .filter(
                                        BidRequirementEntity::isMandatory
                                )
                                .count();


        int compliant =
                count(
                        snapshots,
                        ComplianceVerdict.COMPLIANT
                );


        int partial =
                count(
                        snapshots,
                        ComplianceVerdict.PARTIAL
                );


        int nonCompliant =
                count(
                        snapshots,
                        ComplianceVerdict.NON_COMPLIANT
                );


        int unassessed =
                count(
                        snapshots,
                        ComplianceVerdict.UNASSESSED
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


        BidDecisionOutcome outcome =
                decision == null
                        ? null
                        : decision.getOutcome();


        boolean writable =
                outcome
                        !=
                        BidDecisionOutcome.NO_GO;


        List<BidComplianceMatrixResponse.Row> rows =
                requirements
                        .stream()
                        .map(
                                requirement ->
                                        row(
                                                requirement,
                                                reviews.get(
                                                        requirement.getId()
                                                )
                                        )
                        )
                        .toList();


        return new BidComplianceMatrixResponse(
                tenderId,

                outcome,

                writable,

                writable
                        ? null
                        : "Current bid decision is NO-GO. Reopen the decision before changing compliance reviews.",

                new BidComplianceMatrixResponse
                        .Statistics(
                                requirements.size(),

                                reviewed,

                                mandatory,

                                compliant,

                                partial,

                                nonCompliant,

                                unassessed,

                                readiness.mandatoryBlockerCount(),

                                readiness.mandatoryIncompleteCount(),

                                readiness.criticalRiskCount(),

                                readiness.readinessScore(),

                                readiness.readyForSubmission()
                        ),

                rows
        );
    }


    private BidComplianceMatrixResponse.Row row(
            BidRequirementEntity requirement,
            BidComplianceReviewEntity review
    ) {

        ComplianceRequirementSnapshot snapshot =
                snapshot(
                        requirement,
                        review
                );


        return new BidComplianceMatrixResponse
                .Row(
                        requirement.getId(),

                        requirement.getTitle(),

                        requirement.getDescription(),

                        requirement.getCategory(),

                        requirement.getStatus(),

                        requirement.isMandatory(),

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

                        requirement.getSourceReference(),

                        requirement.getDueDate(),

                        requirement.getAssignedTo(),

                        requirement.getEvidenceNotes(),

                        snapshot.verdict(),

                        snapshot.riskLevel(),

                        snapshot.evidenceStrength(),

                        readinessEngine
                                .rowScore(
                                        snapshot.verdict(),
                                        snapshot.riskLevel(),
                                        snapshot.evidenceStrength()
                                ),

                        readinessEngine
                                .priority(
                                        snapshot
                                ),

                        readinessEngine
                                .nextAction(
                                        snapshot
                                ),

                        review == null
                                ? null
                                : review.getReviewRationale(),

                        review == null
                                ? null
                                : review.getMitigationAction(),

                        review == null
                                ? null
                                : review.getReviewedBy(),

                        review == null
                                ? null
                                : review.getReviewedAt()
                );
    }


    private ComplianceRequirementSnapshot snapshot(
            BidRequirementEntity requirement,
            BidComplianceReviewEntity review
    ) {

        if (
                review == null
        ) {

            return new ComplianceRequirementSnapshot(
                    requirement.isMandatory(),
                    ComplianceVerdict.UNASSESSED,
                    ComplianceRiskLevel.UNASSESSED,
                    EvidenceStrength.NONE
            );
        }


        return new ComplianceRequirementSnapshot(
                requirement.isMandatory(),
                review.getVerdict(),
                review.getRiskLevel(),
                review.getEvidenceStrength()
        );
    }


    private int count(
            List<ComplianceRequirementSnapshot> snapshots,
            ComplianceVerdict verdict
    ) {

        return (int)
                snapshots
                        .stream()
                        .filter(
                                snapshot ->
                                        snapshot.verdict()
                                                ==
                                                verdict
                        )
                        .count();
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
                    "Compliance matrix is locked because the current bid decision is NO-GO"
            );
        }


        return match;
    }


    private NormalizedReview normalizeReview(
            UpdateComplianceReviewRequest request
    ) {

        ComplianceVerdict verdict =
                request.verdict();


        if (
                verdict
                        ==
                        ComplianceVerdict.UNASSESSED
        ) {

            return new NormalizedReview(
                    verdict,
                    ComplianceRiskLevel.UNASSESSED,
                    EvidenceStrength.NONE
            );
        }


        if (
                verdict
                        ==
                        ComplianceVerdict.NOT_APPLICABLE
        ) {

            return new NormalizedReview(
                    verdict,
                    ComplianceRiskLevel.UNASSESSED,
                    EvidenceStrength.NONE
            );
        }


        if (
                request.riskLevel() == null
                ||
                request.riskLevel()
                        ==
                        ComplianceRiskLevel.UNASSESSED
        ) {

            throw new InvalidRequestException(
                    "Risk level must be assessed for reviewed requirements"
            );
        }


        EvidenceStrength evidenceStrength =
                request.evidenceStrength() == null
                        ? EvidenceStrength.NONE
                        : request.evidenceStrength();


        if (
                (
                        verdict
                                ==
                                ComplianceVerdict.COMPLIANT
                        ||
                        verdict
                                ==
                                ComplianceVerdict.PARTIAL
                )
                &&
                evidenceStrength
                        ==
                        EvidenceStrength.NONE
        ) {

            throw new InvalidRequestException(
                    "Compliant or partial requirements require supporting evidence strength"
            );
        }


        return new NormalizedReview(
                verdict,
                request.riskLevel(),
                evidenceStrength
        );
    }


    private BidRequirementStatus requirementStatus(
            ComplianceVerdict verdict
    ) {

        return switch (
                verdict
        ) {

            case UNASSESSED ->
                    BidRequirementStatus.NOT_REVIEWED;

            case COMPLIANT ->
                    BidRequirementStatus.SATISFIED;

            case PARTIAL ->
                    BidRequirementStatus.IN_PROGRESS;

            case NON_COMPLIANT ->
                    BidRequirementStatus.GAP;

            case NOT_APPLICABLE ->
                    BidRequirementStatus.NOT_APPLICABLE;
        };
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


    private record NormalizedReview(

            ComplianceVerdict verdict,

            ComplianceRiskLevel riskLevel,

            EvidenceStrength evidenceStrength

    ) {
    }

}
