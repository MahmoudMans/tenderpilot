package com.tenderpilot.localai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidworkspace.api.CreateBidDocumentRequest;

import com.tenderpilot.bidworkspace.application.BidDocumentStorageGateway;
import com.tenderpilot.bidworkspace.application.BidWorkspaceService;

import com.tenderpilot.bidworkspace.domain.BidDocumentCategory;

import com.tenderpilot.bidworkspace.infrastructure.BidDocumentRepository;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.localai.application.BidDocumentAiAnalysisService;
import com.tenderpilot.localai.application.OllamaGateway;

import com.tenderpilot.localai.domain.BidAiFindingType;
import com.tenderpilot.localai.domain.BidDocumentAnalysisStatus;
import com.tenderpilot.localai.domain.OllamaBidAnalysisResult;

import com.tenderpilot.localai.infrastructure.BidDocumentAnalysisRepository;
import com.tenderpilot.localai.infrastructure.BidDocumentTextRepository;

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
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_local_ai;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=true",
                "tenderpilot.local-ai.model=qwen3:4b"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        BidDocumentAiAnalysisIntegrationTest
                .FakeConfiguration.class
)
@Transactional
class BidDocumentAiAnalysisIntegrationTest {

    private static final UUID ORG_ONE =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001301"
            );


    private static final UUID ORG_TWO =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001302"
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
    private BidDocumentTextRepository
            textRepository;


    @Autowired
    private BidDocumentAnalysisRepository
            analysisRepository;


    @Autowired
    private BidWorkspaceService
            workspaceService;


    @Autowired
    private BidDocumentAiAnalysisService
            aiService;


    @Autowired
    private FakeBidDocumentStorageGateway
            storageGateway;


    @Autowired
    private FakeOllamaGateway
            ollamaGateway;


    @BeforeEach
    void clean() {

        analysisRepository.deleteAll();

        textRepository.deleteAll();

        documentRepository.deleteAll();

        decisionRepository.deleteAll();

        matchRepository.deleteAll();

        analysisRepository.flush();

        textRepository.flush();

        documentRepository.flush();

        decisionRepository.flush();

        matchRepository.flush();

        storageGateway.reset();

        ollamaGateway.reset();
    }


    @Test
    void analyzesDocumentAndPersistsStructuredPrivateFindings() {

        UUID tenderId =
                seedTenderAndMatch(
                        "AI-DOCUMENT",
                        ORG_ONE
                );


        UUID documentId =
                uploadTextDocument(
                        tenderId
                );


        var result =
                aiService
                        .analyze(
                                ORG_ONE,
                                tenderId,
                                documentId
                        );


        assertThat(
                result.status()
        )
                .isEqualTo(
                        BidDocumentAnalysisStatus.COMPLETED
                );


        assertThat(
                result.summary()
        )
                .contains(
                        "three references"
                );


        assertThat(
                result.confidence()
        )
                .isEqualTo(
                        91
                );


        assertThat(
                result.findings()
        )
                .hasSize(
                        2
                );


        assertThat(
                result
                        .findings()
                        .getFirst()
                        .type()
        )
                .isEqualTo(
                        BidAiFindingType.REQUIREMENT
                );


        assertThat(
                textRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                analysisRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThatThrownBy(
                () ->
                        aiService
                                .analyses(
                                        ORG_TWO,
                                        tenderId
                                )
        )
                .isInstanceOf(
                        com.tenderpilot.common.domain.ResourceNotFoundException.class
                );
    }


    @Test
    void reanalysisUpdatesExistingAnalysisInsteadOfDuplicatingIt() {

        UUID tenderId =
                seedTenderAndMatch(
                        "AI-REANALYZE",
                        ORG_ONE
                );


        UUID documentId =
                uploadTextDocument(
                        tenderId
                );


        aiService
                .analyze(
                        ORG_ONE,
                        tenderId,
                        documentId
                );


        aiService
                .analyze(
                        ORG_ONE,
                        tenderId,
                        documentId
                );


        assertThat(
                analysisRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                textRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                ollamaGateway.analysisCalls
        )
                .isEqualTo(
                        2
                );
    }


    @Test
    void localAiFailureIsPersistedWithoutDeletingDocument() {

        UUID tenderId =
                seedTenderAndMatch(
                        "AI-FAILURE",
                        ORG_ONE
                );


        UUID documentId =
                uploadTextDocument(
                        tenderId
                );


        ollamaGateway.fail =
                true;


        var result =
                aiService
                        .analyze(
                                ORG_ONE,
                                tenderId,
                                documentId
                        );


        assertThat(
                result.status()
        )
                .isEqualTo(
                        BidDocumentAnalysisStatus.FAILED
                );


        assertThat(
                result.errorMessage()
        )
                .contains(
                        "Synthetic Ollama failure"
                );


        assertThat(
                documentRepository
                        .findById(
                                documentId
                        )
        )
                .isPresent();


        assertThat(
                textRepository.count()
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void noGoDecisionLocksNewAiAnalysis() {

        UUID tenderId =
                seedTenderAndMatch(
                        "AI-NOGO",
                        ORG_ONE
                );


        UUID documentId =
                uploadTextDocument(
                        tenderId
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
                        "Synthetic recommendation"
                ),

                90,

                TenderMatchGrade.EXCELLENT,

                "Strong fit",

                "None",

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


        assertThatThrownBy(
                () ->
                        aiService
                                .analyze(
                                        ORG_ONE,
                                        tenderId,
                                        documentId
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    private UUID uploadTextDocument(
            UUID tenderId
    ) {

        byte[] bytes =
                """
                CCTP — Technical Requirements

                The bidder must provide three comparable public-building
                renovation references completed during the previous five years.

                The technical response must explain the energy-efficiency
                methodology and project governance.

                Final submissions are required before 30 October 2026.
                """
                        .getBytes(
                                StandardCharsets.UTF_8
                        );


        var workspace =
                workspaceService
                        .uploadDocument(
                                ORG_ONE,

                                tenderId,

                                new CreateBidDocumentRequest(
                                        "cctp.txt",

                                        "text/plain",

                                        Base64
                                                .getEncoder()
                                                .encodeToString(
                                                        bytes
                                                ),

                                        BidDocumentCategory.SPECIFICATIONS,

                                        "AI fixture"
                                ),

                                "analyst@example.test"
                        );


        return workspace
                .documents()
                .getFirst()
                .id();
    }


    private UUID seedTenderAndMatch(
            String externalId,
            UUID organizationId
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
                        90,
                        TenderMatchGrade.EXCELLENT,
                        false,
                        25,
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


        return tenderId;
    }


    private CanonicalTenderUpsertRequest tender(
            String externalId
    ) {

        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "LOCAL_AI_TEST",

                "https://example.test/"
                        +
                        externalId,

                "local-ai-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "LOCAL-AI-BUYER:"
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

                "Energy renovation and refurbishment procurement.",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        10
                ),

                Instant.parse(
                        "2026-10-30T16:00:00Z"
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
    static class FakeConfiguration {

        @Bean
        @Primary
        FakeBidDocumentStorageGateway storageGateway() {

            return new FakeBidDocumentStorageGateway();
        }


        @Bean
        @Primary
        FakeOllamaGateway ollamaGateway() {

            return new FakeOllamaGateway();
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
                            ".txt";


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
                        "Synthetic stored document missing"
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


    static class FakeOllamaGateway
            implements OllamaGateway {

        private boolean fail;

        private int analysisCalls;


        void reset() {

            fail =
                    false;

            analysisCalls =
                    0;
        }


        @Override
        public OllamaStatus status() {

            return new OllamaStatus(
                    true,
                    true,
                    true,
                    "OLLAMA",
                    "qwen3:4b",
                    "http://localhost:11434"
            );
        }


        @Override
        public OllamaBidAnalysisResult analyze(
                String filename,
                String extractedText
        ) {

            analysisCalls++;


            if (
                    fail
            ) {

                throw new IllegalStateException(
                        "Synthetic Ollama failure"
                );
            }


            return new OllamaBidAnalysisResult(
                    "The document requires three references and describes technical submission obligations.",

                    91,

                    List.of(
                            new OllamaBidAnalysisResult
                                    .Finding(
                                            BidAiFindingType.REQUIREMENT,
                                            "Three comparable references",
                                            "Provide three comparable renovation references completed during the previous five years.",
                                            "The bidder must provide three comparable public-building renovation references.",
                                            true,
                                            com.tenderpilot.bidworkspace.domain.BidRequirementCategory.TECHNICAL
                                    ),

                            new OllamaBidAnalysisResult
                                    .Finding(
                                            BidAiFindingType.DEADLINE,
                                            "Submission deadline",
                                            "Submission is required before 30 October 2026.",
                                            "Final submissions are required before 30 October 2026.",
                                            true,
                                            com.tenderpilot.bidworkspace.domain.BidRequirementCategory.SUBMISSION
                                    )
                    )
            );
        }
    }

}
