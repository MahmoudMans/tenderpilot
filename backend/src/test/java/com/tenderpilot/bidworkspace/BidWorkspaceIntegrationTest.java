package com.tenderpilot.bidworkspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.api.CreateBidDocumentRequest;
import com.tenderpilot.bidworkspace.api.CreateBidRequirementRequest;
import com.tenderpilot.bidworkspace.api.UpdateBidRequirementRequest;

import com.tenderpilot.bidworkspace.application.BidDocumentStorageGateway;
import com.tenderpilot.bidworkspace.application.BidWorkspaceService;

import com.tenderpilot.bidworkspace.domain.BidDocumentCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import com.tenderpilot.bidworkspace.infrastructure.BidDocumentRepository;
import com.tenderpilot.bidworkspace.infrastructure.BidRequirementRepository;

import com.tenderpilot.common.domain.InvalidRequestException;

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

import com.tenderpilot.procurement.infrastructure.TenderRepository;

import java.math.BigDecimal;

import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.time.LocalDate;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_bid_workspace;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        BidWorkspaceIntegrationTest
                .FakeStorageConfiguration.class
)
@Transactional
class BidWorkspaceIntegrationTest {

    private static final UUID ORG_ONE =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001201"
            );


    private static final UUID ORG_TWO =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001202"
            );


    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private TenderRepository
            tenderRepository;


    @Autowired
    private TenderMatchRepository
            matchRepository;


    @Autowired
    private BidDecisionRepository
            decisionRepository;


    @Autowired
    private BidDocumentRepository
            documentRepository;


    @Autowired
    private BidRequirementRepository
            requirementRepository;


    @Autowired
    private BidWorkspaceService
            workspaceService;


    @Autowired
    private FakeBidDocumentStorageGateway
            storageGateway;


    @BeforeEach
    void clean() {

        requirementRepository.deleteAll();

        documentRepository.deleteAll();

        decisionRepository.deleteAll();

        matchRepository.deleteAll();

        requirementRepository.flush();

        documentRepository.flush();

        decisionRepository.flush();

        matchRepository.flush();

        storageGateway.reset();
    }


    @Test
    void workspaceIsTenantScopedAndStartsEmpty() {

        UUID tenderId =
                seedTenderAndMatch(
                        "WORKSPACE-EMPTY",
                        ORG_ONE,
                        90
                );


        var workspace =
                workspaceService
                        .workspace(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                workspace.documents()
        )
                .isEmpty();


        assertThat(
                workspace.requirements()
        )
                .isEmpty();


        assertThat(
                workspace.writable()
        )
                .isTrue();


        assertThatThrownBy(
                () ->
                        workspaceService
                                .workspace(
                                        ORG_TWO,
                                        tenderId
                                )
        )
                .isInstanceOf(
                        RuntimeException.class
                );
    }


    @Test
    void documentUploadPersistsChecksumAndContent() {

        UUID tenderId =
                seedTenderAndMatch(
                        "WORKSPACE-DOCUMENT",
                        ORG_ONE,
                        90
                );


        byte[] content =
                "Tender specification fixture"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );


        var workspace =
                workspaceService
                        .uploadDocument(
                                ORG_ONE,
                                tenderId,

                                new CreateBidDocumentRequest(
                                        "cctp.pdf",
                                        "application/pdf",
                                        Base64
                                                .getEncoder()
                                                .encodeToString(
                                                        content
                                                ),
                                        BidDocumentCategory.SPECIFICATIONS,
                                        "Main technical specification"
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace.documents()
        )
                .hasSize(
                        1
                );


        assertThat(
                workspace
                        .documents()
                        .getFirst()
                        .sha256()
        )
                .hasSize(
                        64
                );


        UUID documentId =
                workspace
                        .documents()
                        .getFirst()
                        .id();


        var downloaded =
                workspaceService
                        .documentContent(
                                ORG_ONE,
                                tenderId,
                                documentId
                        );


        assertThat(
                Base64
                        .getDecoder()
                        .decode(
                                downloaded.contentBase64()
                        )
        )
                .containsExactly(
                        content
                );


        assertThat(
                workspace
                        .statistics()
                        .documentCount()
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void requirementLifecycleUpdatesCompletionAndGapMetrics() {

        UUID tenderId =
                seedTenderAndMatch(
                        "WORKSPACE-REQUIREMENT",
                        ORG_ONE,
                        90
                );


        var workspace =
                workspaceService
                        .createRequirement(
                                ORG_ONE,
                                tenderId,

                                new CreateBidRequirementRequest(
                                        "Provide three similar project references",
                                        "Three references completed during the last five years.",
                                        BidRequirementCategory.TECHNICAL,
                                        true,
                                        null,
                                        "CCTP §4.2",
                                        LocalDate.of(
                                                2026,
                                                10,
                                                10
                                        ),
                                        "Bid team",
                                        null
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace.requirements()
        )
                .hasSize(
                        1
                );


        UUID requirementId =
                workspace
                        .requirements()
                        .getFirst()
                        .id();


        workspace =
                workspaceService
                        .updateRequirement(
                                ORG_ONE,
                                tenderId,
                                requirementId,

                                new UpdateBidRequirementRequest(
                                        null,
                                        null,
                                        null,
                                        BidRequirementStatus.GAP,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        "Only two matching references currently available."
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace
                        .statistics()
                        .gapRequirementCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                workspace
                        .statistics()
                        .mandatoryGapCount()
        )
                .isEqualTo(
                        1
                );


        workspace =
                workspaceService
                        .updateRequirement(
                                ORG_ONE,
                                tenderId,
                                requirementId,

                                new UpdateBidRequirementRequest(
                                        null,
                                        null,
                                        null,
                                        BidRequirementStatus.SATISFIED,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        "Third reference validated."
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace
                        .statistics()
                        .completedRequirementCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                workspace
                        .statistics()
                        .completionPercentage()
        )
                .isEqualTo(
                        100
                );
    }


    @Test
    void noGoDecisionLocksWorkspaceWritesButPreservesReadAccess() {

        UUID tenderId =
                seedTenderAndMatch(
                        "WORKSPACE-NOGO",
                        ORG_ONE,
                        90
                );


        TenderMatchEntity match =
                matchRepository
                        .findByOrganizationIdAndTender_Id(
                                ORG_ONE,
                                tenderId
                        )
                        .orElseThrow();


        BidDecisionEntity decision =
                BidDecisionEntity
                        .create(
                                ORG_ONE,
                                match.getTender()
                        );


        decision.applyAssessment(
                new BidDecisionScorecard(
                        5,
                        5,
                        5,
                        5,
                        5,
                        5
                ),

                new BidAssessmentEvaluation(
                        100,
                        BidDecisionOutcome.GO,
                        "Synthetic integration recommendation"
                ),

                90,

                TenderMatchGrade.EXCELLENT,

                "Strong fit",

                "No material risks",

                "Stable scope"
        );


        decision.decide(
                BidDecisionOutcome.NO_GO,
                "manager@example.test"
        );


        decisionRepository
                .saveAndFlush(
                        decision
                );


        var workspace =
                workspaceService
                        .workspace(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                workspace.writable()
        )
                .isFalse();


        assertThat(
                workspace.decisionOutcome()
        )
                .isEqualTo(
                        BidDecisionOutcome.NO_GO
                );


        assertThatThrownBy(
                () ->
                        workspaceService
                                .createRequirement(
                                        ORG_ONE,
                                        tenderId,

                                        new CreateBidRequirementRequest(
                                                "Blocked requirement",
                                                null,
                                                BidRequirementCategory.OTHER,
                                                true,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        ),

                                        "analyst@example.test"
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
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


        var tender =
                tenderRepository
                        .findById(
                                tenderId
                        )
                        .orElseThrow();


        TenderMatchEntity match =
                TenderMatchEntity
                        .create(
                                organizationId,
                                tender
                        );


        match.apply(
                new TenderMatchEvaluation(
                        score,
                        TenderMatchGrade.EXCELLENT,
                        false,
                        25,
                        15,
                        15,
                        10,
                        15,
                        10,
                        10,
                        List.of()
                )
        );


        matchRepository
                .saveAndFlush(
                        match
                );


        return tenderId;
    }


    private CanonicalTenderUpsertRequest tender(
            String externalId
    ) {

        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "BID_WORKSPACE_TEST",

                "https://example.test/"
                        +
                        externalId,

                "bid-workspace-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "WORKSPACE-BUYER:"
                                        +
                                        externalId,

                                "Ville de Paris",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                null,

                                null
                        ),

                "Energy renovation tender "
                        +
                        externalId,

                "Energy renovation and refurbishment of a public building.",

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


    @TestConfiguration
    static class FakeStorageConfiguration {

        @Bean
        @Primary
        FakeBidDocumentStorageGateway
        fakeBidDocumentStorageGateway() {

            return new FakeBidDocumentStorageGateway();
        }
    }


    static class FakeBidDocumentStorageGateway
            implements BidDocumentStorageGateway {

        private final Map<String, byte[]> content =
                new LinkedHashMap<>();


        void reset() {

            content.clear();
        }


        @Override
        public StoredBidDocument store(
                UUID organizationId,
                UUID tenderId,
                String originalFilename,
                byte[] bytes
        ) {

            String key =
                    organizationId
                            +
                            "/"
                            +
                            tenderId
                            +
                            "/"
                            +
                            UUID.randomUUID()
                            +
                            ".pdf";


            content.put(
                    key,
                    bytes.clone()
            );


            return new StoredBidDocument(
                    key,
                    key.substring(
                            key.lastIndexOf(
                                    '/'
                            )
                                    +
                                    1
                    )
            );
        }


        @Override
        public byte[] load(
                String storageKey
        ) {

            byte[] bytes =
                    content.get(
                            storageKey
                    );


            if (
                    bytes == null
            ) {

                throw new IllegalStateException(
                        "Synthetic document missing"
                );
            }


            return bytes.clone();
        }


        @Override
        public void delete(
                String storageKey
        ) {

            content.remove(
                    storageKey
            );
        }
    }

}
