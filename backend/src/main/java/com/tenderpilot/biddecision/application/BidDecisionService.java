package com.tenderpilot.biddecision.application;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.biddecision.api.BidDecisionOverviewResponse;
import com.tenderpilot.biddecision.api.BidDecisionQueueItemResponse;
import com.tenderpilot.biddecision.api.BidDecisionWorkspaceResponse;
import com.tenderpilot.biddecision.api.SetBidDecisionRequest;
import com.tenderpilot.biddecision.api.UpdateBidAssessmentRequest;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionEventType;
import com.tenderpilot.biddecision.domain.BidDecisionHistoryEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import com.tenderpilot.biddecision.infrastructure.BidDecisionHistoryRepository;
import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.matching.domain.TenderMatchEntity;
import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidDecisionService {

    private final TenderMatchRepository
            matchRepository;


    private final BidDecisionRepository
            decisionRepository;


    private final BidDecisionHistoryRepository
            historyRepository;


    private final BidDecisionScoringService
            scoringService;


    private final OpportunityQueryService
            opportunityQueryService;


    public BidDecisionService(
            TenderMatchRepository matchRepository,
            BidDecisionRepository decisionRepository,
            BidDecisionHistoryRepository historyRepository,
            BidDecisionScoringService scoringService,
            OpportunityQueryService opportunityQueryService
    ) {

        this.matchRepository =
                matchRepository;

        this.decisionRepository =
                decisionRepository;

        this.historyRepository =
                historyRepository;

        this.scoringService =
                scoringService;

        this.opportunityQueryService =
                opportunityQueryService;
    }


    @Transactional(readOnly = true)
    public List<BidDecisionQueueItemResponse> queue(
            UUID organizationId,
            Integer minimumMatchScore,
            boolean includeBlocked
    ) {

        int minimum =
                minimumMatchScore == null
                        ? 0
                        : Math.max(
                                0,
                                Math.min(
                                        100,
                                        minimumMatchScore
                                )
                        );


        return matchRepository
                .findByOrganizationIdOrderByBlockedAscTotalScoreDescEvaluatedAtDesc(
                        organizationId
                )
                .stream()

                .filter(
                        match ->
                                includeBlocked
                                ||
                                !match.isBlocked()
                )

                .filter(
                        match ->
                                match.getTotalScore()
                                        >=
                                        minimum
                )

                .limit(
                        100
                )

                .map(
                        this::queueItem
                )

                .toList();
    }


    @Transactional(readOnly = true)
    public BidDecisionWorkspaceResponse workspace(
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


        return workspaceResponse(
                match,
                decision
        );
    }


    @Transactional
    public BidDecisionWorkspaceResponse assess(
            UUID organizationId,
            UUID tenderId,
            UpdateBidAssessmentRequest request,
            String actor
    ) {

        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Bid assessment request is required"
            );
        }


        TenderMatchEntity match =
                requireMatch(
                        organizationId,
                        tenderId
                );


        BidDecisionScorecard scorecard =
                new BidDecisionScorecard(
                        requiredScore(
                                "strategicFit",
                                request.strategicFit()
                        ),

                        requiredScore(
                                "capabilityConfidence",
                                request.capabilityConfidence()
                        ),

                        requiredScore(
                                "commercialAttractiveness",
                                request.commercialAttractiveness()
                        ),

                        requiredScore(
                                "deliveryFeasibility",
                                request.deliveryFeasibility()
                        ),

                        requiredScore(
                                "resourceCapacity",
                                request.resourceCapacity()
                        ),

                        requiredScore(
                                "riskConfidence",
                                request.riskConfidence()
                        )
                );


        BidAssessmentEvaluation evaluation =
                scoringService
                        .evaluate(
                                scorecard,
                                match.getTotalScore(),
                                match.isBlocked()
                        );


        BidDecisionEntity decision =
                decisionRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
                        .orElseGet(
                                () ->
                                        BidDecisionEntity
                                                .create(
                                                        organizationId,
                                                        match.getTender()
                                                )
                        );


        BidDecisionOutcome currentOutcome =
                decision.getOutcome();


        decision.applyAssessment(
                scorecard,
                evaluation,
                match.getTotalScore(),
                match.getGrade(),
                clean(
                        request.rationale()
                ),
                clean(
                        request.keyRisks()
                ),
                clean(
                        request.assumptions()
                )
        );


        decision =
                decisionRepository
                        .saveAndFlush(
                                decision
                        );


        historyRepository
                .saveAndFlush(
                        BidDecisionHistoryEntity
                                .create(
                                        decision,
                                        BidDecisionEventType.ASSESSMENT_UPDATED,
                                        currentOutcome,
                                        currentOutcome,
                                        decision.getDecisionScore(),
                                        actor(
                                                actor
                                        ),
                                        clean(
                                                request.rationale()
                                        )
                                )
                );


        return workspaceResponse(
                match,
                decision
        );
    }


    @Transactional
    public BidDecisionWorkspaceResponse decide(
            UUID organizationId,
            UUID tenderId,
            SetBidDecisionRequest request,
            String actor
    ) {

        if (
                request == null
                ||
                request.outcome() == null
        ) {

            throw new InvalidRequestException(
                    "Bid decision outcome is required"
            );
        }


        if (
                request.outcome()
                        ==
                        BidDecisionOutcome.PENDING
        ) {

            throw new InvalidRequestException(
                    "Use the reopen operation to return a decision to PENDING"
            );
        }


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
                        .orElseThrow(
                                () ->
                                        new InvalidRequestException(
                                                "Complete the bid assessment before making a final decision"
                                        )
                        );


        BidDecisionOutcome previous =
                decision.getOutcome();


        decision.decide(
                request.outcome(),
                actor(
                        actor
                )
        );


        decision =
                decisionRepository
                        .saveAndFlush(
                                decision
                        );


        historyRepository
                .saveAndFlush(
                        BidDecisionHistoryEntity
                                .create(
                                        decision,
                                        BidDecisionEventType.OUTCOME_CHANGED,
                                        previous,
                                        request.outcome(),
                                        decision.getDecisionScore(),
                                        actor(
                                                actor
                                        ),
                                        clean(
                                                request.note()
                                        )
                                )
                );


        return workspaceResponse(
                match,
                decision
        );
    }


    @Transactional
    public BidDecisionWorkspaceResponse reopen(
            UUID organizationId,
            UUID tenderId,
            String actor
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
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Bid decision was not found"
                                        )
                        );


        BidDecisionOutcome previous =
                decision.getOutcome();


        decision.reopen();


        decision =
                decisionRepository
                        .saveAndFlush(
                                decision
                        );


        historyRepository
                .saveAndFlush(
                        BidDecisionHistoryEntity
                                .create(
                                        decision,
                                        BidDecisionEventType.REOPENED,
                                        previous,
                                        BidDecisionOutcome.PENDING,
                                        decision.getDecisionScore(),
                                        actor(
                                                actor
                                        ),
                                        "Decision reopened for review."
                                )
                );


        return workspaceResponse(
                match,
                decision
        );
    }


    @Transactional(readOnly = true)
    public BidDecisionOverviewResponse overview(
            UUID organizationId
    ) {

        List<BidDecisionEntity> decisions =
                decisionRepository
                        .findByOrganizationIdOrderByUpdatedAtDesc(
                                organizationId
                        );


        double average =
                decisions
                        .stream()
                        .mapToInt(
                                BidDecisionEntity::getDecisionScore
                        )
                        .average()
                        .orElse(
                                0
                        );


        Instant lastUpdated =
                decisions
                        .stream()
                        .map(
                                BidDecisionEntity::getUpdatedAt
                        )
                        .max(
                                Instant::compareTo
                        )
                        .orElse(
                                null
                        );


        return new BidDecisionOverviewResponse(
                matchRepository
                        .countByOrganizationId(
                                organizationId
                        ),

                decisions.size(),

                count(
                        decisions,
                        BidDecisionOutcome.PENDING
                ),

                count(
                        decisions,
                        BidDecisionOutcome.GO
                ),

                count(
                        decisions,
                        BidDecisionOutcome.HOLD
                ),

                count(
                        decisions,
                        BidDecisionOutcome.NO_GO
                ),

                average,

                lastUpdated
        );
    }


    private long count(
            List<BidDecisionEntity> decisions,
            BidDecisionOutcome outcome
    ) {

        return decisions
                .stream()
                .filter(
                        decision ->
                                decision.getOutcome()
                                        ==
                                        outcome
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


    private BidDecisionQueueItemResponse queueItem(
            TenderMatchEntity match
    ) {

        BidDecisionEntity decision =
                decisionRepository
                        .findByOrganizationIdAndTender_Id(
                                match.getOrganizationId(),
                                match
                                        .getTender()
                                        .getId()
                        )
                        .orElse(
                                null
                        );


        BidDecisionQueueItemResponse.DecisionSummary
                summary =
                decision == null
                        ? null
                        : new BidDecisionQueueItemResponse
                                .DecisionSummary(
                                        decision.getId(),
                                        decision.getOutcome(),
                                        decision.getRecommendedOutcome(),
                                        decision.getDecisionScore(),
                                        decision.getUpdatedAt()
                                );


        return new BidDecisionQueueItemResponse(
                match.getId(),

                match
                        .getTender()
                        .getId(),

                match.getTotalScore(),

                match.getGrade(),

                match.isBlocked(),

                opportunityQueryService
                        .summaryById(
                                match
                                        .getTender()
                                        .getId()
                        ),

                summary
        );
    }


    private BidDecisionWorkspaceResponse workspaceResponse(
            TenderMatchEntity match,
            BidDecisionEntity decision
    ) {

        List<BidDecisionWorkspaceResponse.HistoryResponse>
                history =
                decision == null
                        ? List.of()
                        : historyRepository
                                .findByDecision_IdOrderByCreatedAtAsc(
                                        decision.getId()
                                )
                                .stream()
                                .map(
                                        event ->
                                                new BidDecisionWorkspaceResponse
                                                        .HistoryResponse(
                                                                event.getId(),
                                                                event.getEventType(),
                                                                event.getPreviousOutcome(),
                                                                event.getNewOutcome(),
                                                                event.getDecisionScore(),
                                                                event.getActor(),
                                                                event.getNote(),
                                                                event.getCreatedAt()
                                                        )
                                )
                                .toList();


        BidDecisionWorkspaceResponse.DecisionResponse
                decisionResponse =
                decision == null
                        ? null
                        : new BidDecisionWorkspaceResponse
                                .DecisionResponse(
                                        decision.getId(),
                                        decision.getOutcome(),
                                        decision.getRecommendedOutcome(),
                                        decision.getDecisionScore(),
                                        decision.getMatchScoreSnapshot(),
                                        decision.getMatchGradeSnapshot(),
                                        decision.getStrategicFit(),
                                        decision.getCapabilityConfidence(),
                                        decision.getCommercialAttractiveness(),
                                        decision.getDeliveryFeasibility(),
                                        decision.getResourceCapacity(),
                                        decision.getRiskConfidence(),
                                        decision.getRecommendationReason(),
                                        decision.getRationale(),
                                        decision.getKeyRisks(),
                                        decision.getAssumptions(),
                                        decision.getDecidedBy(),
                                        decision.getDecidedAt(),
                                        decision.getCreatedAt(),
                                        decision.getUpdatedAt()
                                );


        return new BidDecisionWorkspaceResponse(
                match
                        .getTender()
                        .getId(),

                match.getTotalScore(),

                match.getGrade(),

                match.isBlocked(),

                opportunityQueryService
                        .summaryById(
                                match
                                        .getTender()
                                        .getId()
                        ),

                decisionResponse,

                history
        );
    }


    private int requiredScore(
            String name,
            Integer value
    ) {

        if (
                value == null
        ) {

            throw new InvalidRequestException(
                    name
                            +
                            " is required"
            );
        }


        return value;
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
