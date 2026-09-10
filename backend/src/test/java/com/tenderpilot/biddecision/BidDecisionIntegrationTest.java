package com.tenderpilot.biddecision;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.biddecision.api.BidDecisionWorkspaceResponse;
import com.tenderpilot.biddecision.api.SetBidDecisionRequest;
import com.tenderpilot.biddecision.api.UpdateBidAssessmentRequest;

import com.tenderpilot.biddecision.application.BidDecisionService;

import com.tenderpilot.biddecision.domain.BidDecisionEventType;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.matching.domain.TenderMatchEntity;
import com.tenderpilot.matching.domain.TenderMatchEvaluation;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_bid_decision;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false"
        }
)
@ActiveProfiles(
        "test"
)
@Transactional
class BidDecisionIntegrationTest {

    private static final UUID ORG_ONE =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000111"
            );


    private static final UUID ORG_TWO =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000222"
            );


    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private TenderMatchRepository
            matchRepository;


    @Autowired
    private BidDecisionRepository
            decisionRepository;


    @Autowired
    private BidDecisionService
            decisionService;


    @BeforeEach
    void clean() {

        decisionRepository.deleteAll();

        matchRepository.deleteAll();

        decisionRepository.flush();

        matchRepository.flush();
    }


    @Test
    void assessmentCreatesDecisionAndAuditHistory() {

        UUID tenderId =
                seedTenderAndMatch(
                        "BID-ASSESSMENT",
                        ORG_ONE,
                        90
                );


        BidDecisionWorkspaceResponse workspace =
                decisionService
                        .assess(
                                ORG_ONE,

                                tenderId,

                                assessment(
                                        5
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace.decision()
        )
                .isNotNull();


        assertThat(
                workspace
                        .decision()
                        .decisionScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                workspace
                        .decision()
                        .recommendedOutcome()
        )
                .isEqualTo(
                        BidDecisionOutcome.GO
                );


        assertThat(
                workspace
                        .decision()
                        .outcome()
        )
                .isEqualTo(
                        BidDecisionOutcome.PENDING
                );


        assertThat(
                workspace.history()
        )
                .hasSize(
                        1
                );


        assertThat(
                workspace
                        .history()
                        .getFirst()
                        .eventType()
        )
                .isEqualTo(
                        BidDecisionEventType.ASSESSMENT_UPDATED
                );
    }


    @Test
    void bidManagerCanProgressAssessmentToGoAndReopen() {

        UUID tenderId =
                seedTenderAndMatch(
                        "BID-DECISION",
                        ORG_ONE,
                        90
                );


        decisionService
                .assess(
                        ORG_ONE,
                        tenderId,
                        assessment(
                                5
                        ),
                        "analyst@example.test"
                );


        BidDecisionWorkspaceResponse decided =
                decisionService
                        .decide(
                                ORG_ONE,

                                tenderId,

                                new SetBidDecisionRequest(
                                        BidDecisionOutcome.GO,
                                        "Approved for pursuit."
                                ),

                                "manager@example.test"
                        );


        assertThat(
                decided
                        .decision()
                        .outcome()
        )
                .isEqualTo(
                        BidDecisionOutcome.GO
                );


        assertThat(
                decided
                        .decision()
                        .decidedBy()
        )
                .isEqualTo(
                        "manager@example.test"
                );


        assertThat(
                decided.history()
        )
                .hasSize(
                        2
                );


        BidDecisionWorkspaceResponse reopened =
                decisionService
                        .reopen(
                                ORG_ONE,
                                tenderId,
                                "manager@example.test"
                        );


        assertThat(
                reopened
                        .decision()
                        .outcome()
        )
                .isEqualTo(
                        BidDecisionOutcome.PENDING
                );


        assertThat(
                reopened.history()
        )
                .hasSize(
                        3
                );
    }


    @Test
    void decisionsAndQueueRemainTenantIsolated() {

        UUID tenderId =
                seedTenderAndMatch(
                        "BID-TENANT",
                        ORG_ONE,
                        90
                );


        seedMatch(
                tenderId,
                ORG_TWO,
                65
        );


        decisionService
                .assess(
                        ORG_ONE,
                        tenderId,
                        assessment(
                                5
                        ),
                        "org-one@example.test"
                );


        assertThat(
                decisionService
                        .queue(
                                ORG_ONE,
                                null,
                                false
                        )
        )
                .hasSize(
                        1
                );


        assertThat(
                decisionService
                        .queue(
                                ORG_TWO,
                                null,
                                false
                        )
        )
                .hasSize(
                        1
                );


        assertThat(
                decisionService
                        .queue(
                                ORG_TWO,
                                null,
                                false
                        )
                        .getFirst()
                        .decision()
        )
                .isNull();


        assertThat(
                decisionService
                        .overview(
                                ORG_ONE
                        )
                        .totalDecisions()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                decisionService
                        .overview(
                                ORG_TWO
                        )
                        .totalDecisions()
        )
                .isZero();
    }


    private UUID seedTenderAndMatch(
            String externalId,
            UUID organizationId,
            int score
    ) {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                tender(
                                        externalId
                                )
                        );


        seedMatch(
                tenderId,
                organizationId,
                score
        );


        return tenderId;
    }


    private void seedMatch(
            UUID tenderId,
            UUID organizationId,
            int score
    ) {

        var tender =
                opportunityTender(
                        tenderId
                );


        TenderMatchEntity match =
                TenderMatchEntity
                        .create(
                                organizationId,
                                tender
                        );


        match.apply(
                new TenderMatchEvaluation(
                        score,

                        score >= 85
                                ? TenderMatchGrade.EXCELLENT
                                : score >= 70
                                        ? TenderMatchGrade.STRONG
                                        : TenderMatchGrade.GOOD,

                        false,

                        Math.min(
                                25,
                                score
                        ),

                        15,

                        15,

                        10,

                        15,

                        5,

                        5,

                        List.of()
                )
        );


        matchRepository
                .saveAndFlush(
                        match
                );
    }


    private com.tenderpilot.procurement.domain.TenderEntity
    opportunityTender(
            UUID tenderId
    ) {

        /*
         * TenderMatchRepository is JPA-based; the canonical import
         * has already persisted this tender. We retrieve it through
         * the match repository entity manager indirectly by using
         * the canonical repository bean resolved through Spring.
         *
         * The helper below is replaced by Spring injection via
         * TenderRepository field.
         */
        return tenderRepository
                .findById(
                        tenderId
                )
                .orElseThrow();
    }


    @Autowired
    private com.tenderpilot.procurement.infrastructure.TenderRepository
            tenderRepository;


    private UpdateBidAssessmentRequest assessment(
            int score
    ) {

        return new UpdateBidAssessmentRequest(
                score,
                score,
                score,
                score,
                score,
                score,
                "Strong strategic fit and relevant reference base.",
                "Resource scheduling requires confirmation.",
                "Tender timetable remains unchanged."
        );
    }


    private CanonicalTenderUpsertRequest tender(
            String externalId
    ) {

        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "BID_DECISION_TEST",

                "https://example.test/"
                        +
                        externalId,

                "bid-decision-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "BID-BUYER:"
                                        +
                                        externalId,

                                "Ville de Paris",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                null,

                                null
                        ),

                "Public-building energy renovation "
                        +
                        externalId,

                "Energy renovation and refurbishment of public property.",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        10
                ),

                Instant.parse(
                        "2026-10-25T16:00:00Z"
                ),

                new BigDecimal(
                        "1000000"
                ),

                new BigDecimal(
                        "2000000"
                ),

                "EUR",

                "FR",

                "IDF",

                "Île-de-France",

                false,

                false,

                true,

                List.of(
                        new CanonicalTenderUpsertRequest
                                .CpvInput(
                                        "45453000",
                                        "Overhaul and refurbishment work"
                                )
                ),

                List.of(
                        new CanonicalTenderUpsertRequest
                                .LocationInput(
                                        "FR",
                                        "IDF",
                                        "Île-de-France",
                                        "Paris"
                                )
                ),

                List.of()
        );
    }

}
