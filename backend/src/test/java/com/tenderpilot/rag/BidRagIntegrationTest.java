package com.tenderpilot.rag;

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
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.localai.application.BidDocumentTextExtractor;

import com.tenderpilot.localai.domain.BidDocumentExtractionResult;

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

import com.tenderpilot.rag.api.AskBidDocumentsRequest;

import com.tenderpilot.rag.application.BidRagService;
import com.tenderpilot.rag.application.OllamaRagGateway;

import com.tenderpilot.rag.domain.OllamaRagAnswerResult;
import com.tenderpilot.rag.domain.RagContextItem;

import com.tenderpilot.rag.infrastructure.BidRagChunkRepository;
import com.tenderpilot.rag.infrastructure.BidRagQueryRepository;

import java.math.BigDecimal;

import java.nio.charset.StandardCharsets;

import java.time.Instant;
import java.time.LocalDate;

import java.util.ArrayList;
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
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_rag;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=true",
                "tenderpilot.local-ai.model=qwen3:4b",
                "tenderpilot.rag.enabled=true",
                "tenderpilot.rag.embedding-model=nomic-embed-text-v2-moe",
                "tenderpilot.rag.chunk-size-chars=400",
                "tenderpilot.rag.chunk-overlap-chars=50",
                "tenderpilot.rag.minimum-similarity=0.10"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        BidRagIntegrationTest
                .FakeConfiguration.class
)
@Transactional
class BidRagIntegrationTest {

    private static final UUID ORG_ONE =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001401"
            );


    private static final UUID ORG_TWO =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001402"
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
    private BidRagChunkRepository
            chunkRepository;


    @Autowired
    private BidRagQueryRepository
            queryRepository;


    @Autowired
    private BidWorkspaceService
            workspaceService;


    @Autowired
    private BidRagService
            ragService;


    @Autowired
    private FakeBidDocumentStorageGateway
            storageGateway;


    @Autowired
    private FakeOllamaRagGateway
            ollamaGateway;


    @BeforeEach
    void clean() {

        queryRepository.deleteAll();

        chunkRepository.deleteAll();

        textRepository.deleteAll();

        documentRepository.deleteAll();

        decisionRepository.deleteAll();

        matchRepository.deleteAll();

        queryRepository.flush();

        chunkRepository.flush();

        textRepository.flush();

        documentRepository.flush();

        decisionRepository.flush();

        matchRepository.flush();

        storageGateway.reset();

        ollamaGateway.reset();
    }


    @Test
    void rebuildCreatesPrivateDocumentChunksAndEmbeddings() {

        UUID tenderId =
                seedTenderAndMatch(
                        "RAG-INDEX",
                        ORG_ONE
                );


        uploadDocument(
                tenderId
        );


        var result =
                ragService
                        .rebuild(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                result.indexedDocuments()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.chunks()
        )
                .isPositive();


        assertThat(
                ragService
                        .indexStatus(
                                ORG_ONE,
                                tenderId
                        )
                        .ready()
        )
                .isTrue();


        assertThat(
                chunkRepository.count()
        )
                .isPositive();
    }


    @Test
    void questionAnswerIsGroundedAndPersistsExactCitation() {

        UUID tenderId =
                seedTenderAndMatch(
                        "RAG-QA",
                        ORG_ONE
                );


        uploadDocument(
                tenderId
        );


        ragService
                .rebuild(
                        ORG_ONE,
                        tenderId
                );


        var answer =
                ragService
                        .ask(
                                ORG_ONE,

                                tenderId,

                                new AskBidDocumentsRequest(
                                        "How many comparable references are required?",
                                        3
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                answer.supported()
        )
                .isTrue();


        assertThat(
                answer.answer()
        )
                .contains(
                        "three"
                );


        assertThat(
                answer.citations()
        )
                .hasSize(
                        1
                );


        assertThat(
                answer
                        .citations()
                        .getFirst()
                        .filename()
        )
                .isEqualTo(
                        "cctp.txt"
                );


        assertThat(
                queryRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                ragService
                        .history(
                                ORG_ONE,
                                tenderId
                        )
        )
                .hasSize(
                        1
                );
    }


    @Test
    void crossTenantRagAccessIsRejected() {

        UUID tenderId =
                seedTenderAndMatch(
                        "RAG-TENANT",
                        ORG_ONE
                );


        uploadDocument(
                tenderId
        );


        ragService
                .rebuild(
                        ORG_ONE,
                        tenderId
                );


        assertThatThrownBy(
                () ->
                        ragService
                                .indexStatus(
                                        ORG_TWO,
                                        tenderId
                                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }


    @Test
    void noGoDecisionBlocksNewRagGeneration() {

        UUID tenderId =
                seedTenderAndMatch(
                        "RAG-NOGO",
                        ORG_ONE
                );


        uploadDocument(
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

                "No material risks",

                "Stable assumptions"
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
                        ragService
                                .rebuild(
                                        ORG_ONE,
                                        tenderId
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );


        assertThatThrownBy(
                () ->
                        ragService
                                .ask(
                                        ORG_ONE,

                                        tenderId,

                                        new AskBidDocumentsRequest(
                                                "What are the requirements?",
                                                3
                                        ),

                                        "analyst@example.test"
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    private void uploadDocument(
            UUID tenderId
    ) {

        String text =
                """
                CCTP — Technical Requirements

                The bidder must provide three comparable renovation
                project references completed during the previous five years.

                The response must explain the energy-efficiency methodology,
                delivery governance, staffing and quality-control approach.

                The final tender submission deadline is 30 October 2026.
                """
                        .repeat(
                                4
                        );


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
                                                text.getBytes(
                                                        StandardCharsets.UTF_8
                                                )
                                        ),

                                BidDocumentCategory.SPECIFICATIONS,

                                "RAG integration fixture"
                        ),

                        "analyst@example.test"
                );
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

                "PRIVATE_RAG_TEST",

                "https://example.test/"
                        +
                        externalId,

                "private-rag-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "RAG-BUYER:"
                                        +
                                        externalId,

                                "Ville de Paris",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                null,

                                null
                        ),

                "Public-building renovation "
                        +
                        externalId,

                "Energy renovation procurement.",

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
        BidDocumentTextExtractor textExtractor() {

            return (
                    filename,
                    content
            ) -> {

                String value =
                        new String(
                                content,
                                StandardCharsets.UTF_8
                        );


                return new BidDocumentExtractionResult(
                        value,
                        value.length(),
                        false,
                        "text/plain",
                        "FAKE_RAG_EXTRACTOR_V1"
                );
            };
        }


        @Bean
        @Primary
        FakeOllamaRagGateway ragGateway() {

            return new FakeOllamaRagGateway();
        }
    }


    static class FakeBidDocumentStorageGateway
            implements BidDocumentStorageGateway {

        private final Map<String, byte[]> storage =
                new LinkedHashMap<>();


        void reset() {

            storage.clear();
        }


        @Override
        public StoredBidDocument store(
                UUID organizationId,
                UUID tenderId,
                String originalFilename,
                byte[] content
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


            storage.put(
                    key,
                    content.clone()
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

            byte[] value =
                    storage.get(
                            storageKey
                    );


            if (
                    value == null
            ) {

                throw new IllegalStateException(
                        "Synthetic RAG document is missing"
                );
            }


            return value.clone();
        }


        @Override
        public void delete(
                String storageKey
        ) {

            storage.remove(
                    storageKey
            );
        }

    }


    static class FakeOllamaRagGateway
            implements OllamaRagGateway {

        void reset() {
        }


        @Override
        public RagModelStatus status() {

            return new RagModelStatus(
                    true,
                    true,
                    true,
                    true,
                    "qwen3:4b",
                    "nomic-embed-text-v2-moe",
                    "http://localhost:11434"
            );
        }


        @Override
        public List<List<Double>> embed(
                List<String> inputs
        ) {

            List<List<Double>> result =
                    new ArrayList<>();


            for (
                    String ignored :
                    inputs
            ) {

                result.add(
                        List.of(
                                1.0,
                                0.5,
                                0.25
                        )
                );
            }


            return List.copyOf(
                    result
            );
        }


        @Override
        public OllamaRagAnswerResult answer(
                String question,
                List<RagContextItem> contexts
        ) {

            return new OllamaRagAnswerResult(
                    "The bidder must provide three comparable renovation project references.",

                    true,

                    List.of(
                            contexts
                                    .getFirst()
                                    .sourceId()
                    )
            );
        }

    }

}
