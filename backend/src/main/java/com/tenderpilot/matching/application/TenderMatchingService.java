package com.tenderpilot.matching.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.matching.api.TenderMatchOverviewResponse;
import com.tenderpilot.matching.api.TenderMatchRecalculationResponse;
import com.tenderpilot.matching.api.TenderMatchSummaryResponse;

import com.tenderpilot.matching.domain.CompanyMatchProfile;
import com.tenderpilot.matching.domain.TenderMatchEntity;
import com.tenderpilot.matching.domain.TenderMatchEvaluation;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.procurement.domain.ProcurementStatus;
import com.tenderpilot.procurement.domain.TenderEntity;

import com.tenderpilot.procurement.infrastructure.TenderRepository;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class TenderMatchingService {

    private final CompanyMatchProfileProvider
            profileProvider;


    private final TenderRepository
            tenderRepository;


    private final TenderMatchRepository
            matchRepository;


    private final TenderMatchCandidateBuilder
            candidateBuilder;


    private final TenderMatchScorer
            scorer;


    private final OpportunityQueryService
            opportunityQueryService;


    public TenderMatchingService(
            CompanyMatchProfileProvider profileProvider,
            TenderRepository tenderRepository,
            TenderMatchRepository matchRepository,
            TenderMatchCandidateBuilder candidateBuilder,
            TenderMatchScorer scorer,
            OpportunityQueryService opportunityQueryService
    ) {

        this.profileProvider =
                profileProvider;

        this.tenderRepository =
                tenderRepository;

        this.matchRepository =
                matchRepository;

        this.candidateBuilder =
                candidateBuilder;

        this.scorer =
                scorer;

        this.opportunityQueryService =
                opportunityQueryService;
    }


    @Transactional
    public TenderMatchRecalculationResponse recalculate(
            UUID organizationId
    ) {

        CompanyMatchProfile profile =
                profileProvider
                        .load(
                                organizationId
                        );


        if (
                !profile.hasCoreSignals()
        ) {

            throw new InvalidRequestException(
                    "Company profile needs capabilities, target CPVs or operating regions before matching"
            );
        }


        matchRepository
                .deleteByOrganizationId(
                        organizationId
                );


        matchRepository.flush();


        List<TenderEntity> candidates =
                tenderRepository
                        .findTop100ByOrderByPublicationDateDesc()
                        .stream()
                        .filter(
                                tender ->
                                        tender.getStatus()
                                                ==
                                                ProcurementStatus.PUBLISHED
                                        ||
                                        tender.getStatus()
                                                ==
                                                ProcurementStatus.UPDATED
                        )
                        .toList();


        int actionable =
                0;


        int blocked =
                0;


        int excellent =
                0;


        int strong =
                0;


        Instant now =
                Instant.now();


        for (
                TenderEntity tender :
                candidates
        ) {

            TenderMatchEvaluation evaluation =
                    scorer.evaluate(
                            profile,
                            candidateBuilder
                                    .build(
                                            tender
                                    ),
                            now
                    );


            TenderMatchEntity entity =
                    TenderMatchEntity
                            .create(
                                    organizationId,
                                    tender
                            );


            entity.apply(
                    evaluation
            );


            matchRepository
                    .save(
                            entity
                    );


            if (
                    evaluation.blocked()
            ) {

                blocked++;

            } else {

                actionable++;
            }


            if (
                    evaluation.grade()
                            ==
                            TenderMatchGrade.EXCELLENT
            ) {

                excellent++;

            } else if (
                    evaluation.grade()
                            ==
                            TenderMatchGrade.STRONG
            ) {

                strong++;
            }
        }


        matchRepository.flush();


        return new TenderMatchRecalculationResponse(
                candidates.size(),
                actionable,
                blocked,
                excellent,
                strong
        );
    }


    @Transactional(readOnly = true)
    public List<TenderMatchSummaryResponse> list(
            UUID organizationId,
            Integer minimumScore,
            TenderMatchGrade grade,
            boolean includeBlocked
    ) {

        int minimum =
                minimumScore == null
                        ? 0
                        : Math.max(
                                0,
                                Math.min(
                                        100,
                                        minimumScore
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

                .filter(
                        match ->
                                grade == null
                                ||
                                match.getGrade()
                                        ==
                                        grade
                )

                .limit(
                        100
                )

                .map(
                        this::response
                )

                .toList();
    }


    @Transactional(readOnly = true)
    public TenderMatchOverviewResponse overview(
            UUID organizationId
    ) {

        List<TenderMatchEntity> matches =
                matchRepository
                        .findByOrganizationIdOrderByBlockedAscTotalScoreDescEvaluatedAtDesc(
                                organizationId
                        );


        long blocked =
                matches
                        .stream()
                        .filter(
                                TenderMatchEntity::isBlocked
                        )
                        .count();


        long excellent =
                countGrade(
                        matches,
                        TenderMatchGrade.EXCELLENT
                );


        long strong =
                countGrade(
                        matches,
                        TenderMatchGrade.STRONG
                );


        long good =
                countGrade(
                        matches,
                        TenderMatchGrade.GOOD
                );


        double average =
                matches
                        .stream()
                        .filter(
                                match ->
                                        !match.isBlocked()
                        )
                        .mapToInt(
                                TenderMatchEntity::getTotalScore
                        )
                        .average()
                        .orElse(
                                0
                        );


        Instant last =
                matches
                        .stream()
                        .map(
                                TenderMatchEntity::getEvaluatedAt
                        )
                        .max(
                                Instant::compareTo
                        )
                        .orElse(
                                null
                        );


        return new TenderMatchOverviewResponse(
                matches.size(),
                matches.size() - blocked,
                blocked,
                excellent,
                strong,
                good,
                average,
                last
        );
    }


    private long countGrade(
            List<TenderMatchEntity> matches,
            TenderMatchGrade grade
    ) {

        return matches
                .stream()
                .filter(
                        match ->
                                match.getGrade()
                                        ==
                                        grade
                )
                .count();
    }


    private TenderMatchSummaryResponse response(
            TenderMatchEntity match
    ) {

        return new TenderMatchSummaryResponse(
                match.getId(),

                match.getTotalScore(),

                match.getGrade(),

                match.isBlocked(),

                match.getCpvScore(),

                match.getGeographyScore(),

                match.getValueScore(),

                match.getBuyerScore(),

                match.getCapabilityScore(),

                match.getTimingScore(),

                match.getQualityScore(),

                match.getEvaluatedAt(),

                opportunityQueryService
                        .summaryById(
                                match
                                        .getTender()
                                        .getId()
                        ),

                match
                        .getSignals()
                        .stream()
                        .map(
                                signal ->
                                        new TenderMatchSummaryResponse
                                                .SignalResponse(
                                                        signal.getCode(),
                                                        signal.getImpact(),
                                                        signal.getDimension(),
                                                        signal.getEarnedScore(),
                                                        signal.getMaximumScore(),
                                                        signal.getMessage()
                                                )
                        )
                        .toList()
        );
    }

}
