package com.tenderpilot.bidplanning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidplanning.api.CreateBidTaskRequest;
import com.tenderpilot.bidplanning.api.MarkBidSubmittedRequest;
import com.tenderpilot.bidplanning.api.SubmissionAuthorizationRequest;
import com.tenderpilot.bidplanning.api.UpdateBidTaskRequest;
import com.tenderpilot.bidplanning.api.UpdateSubmissionControlRequest;

import com.tenderpilot.bidplanning.application.BidPlanningService;

import com.tenderpilot.bidplanning.domain.BidTaskMilestone;
import com.tenderpilot.bidplanning.domain.BidTaskPriority;
import com.tenderpilot.bidplanning.domain.BidTaskStatus;

import com.tenderpilot.bidplanning.infrastructure.BidPlanningEventRepository;
import com.tenderpilot.bidplanning.infrastructure.BidSubmissionControlRepository;
import com.tenderpilot.bidplanning.infrastructure.BidTaskRepository;

import com.tenderpilot.bidworkspace.api.CreateBidRequirementRequest;

import com.tenderpilot.bidworkspace.application.BidWorkspaceService;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;

import com.tenderpilot.bidworkspace.infrastructure.BidRequirementRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.compliance.api.UpdateComplianceReviewRequest;

import com.tenderpilot.compliance.application.BidComplianceService;

import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

import com.tenderpilot.compliance.infrastructure.BidComplianceReviewRepository;

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
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_bid_planning;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
@Transactional
class BidPlanningIntegrationTest {

    private static final UUID ORG_ONE =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001601"
            );


    private static final UUID ORG_TWO =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000001602"
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
    private BidRequirementRepository
            requirementRepository;


    @Autowired
    private BidComplianceReviewRepository
            complianceReviewRepository;


    @Autowired
    private BidTaskRepository
            taskRepository;


    @Autowired
    private BidSubmissionControlRepository
            submissionControlRepository;


    @Autowired
    private BidPlanningEventRepository
            eventRepository;


    @Autowired
    private BidWorkspaceService
            workspaceService;


    @Autowired
    private BidComplianceService
            complianceService;


    @Autowired
    private BidPlanningService
            planningService;


    @BeforeEach
    void clean() {

        eventRepository.deleteAll();

        taskRepository.deleteAll();

        submissionControlRepository.deleteAll();

        complianceReviewRepository.deleteAll();

        requirementRepository.deleteAll();

        decisionRepository.deleteAll();

        matchRepository.deleteAll();

        eventRepository.flush();

        taskRepository.flush();

        submissionControlRepository.flush();

        complianceReviewRepository.flush();

        requirementRepository.flush();

        decisionRepository.flush();

        matchRepository.flush();
    }


    @Test
    void planningWorkspaceStartsEmptyAndNotReady() {

        UUID tenderId =
                seedTenderAndMatch(
                        "PLAN-EMPTY",
                        ORG_ONE
                );


        var workspace =
                planningService
                        .workspace(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                workspace.tasks()
        )
                .isEmpty();


        assertThat(
                workspace.statistics().totalTasks()
        )
                .isZero();


        assertThat(
                workspace.statistics().readyForSubmission()
        )
                .isFalse();


        assertThat(
                workspace.submissionControl().submissionAuthorized()
        )
                .isFalse();
    }


    @Test
    void taskLifecycleUpdatesPlanningMetricsAndAudit() {

        UUID tenderId =
                seedTenderAndMatch(
                        "PLAN-TASK",
                        ORG_ONE
                );


        var workspace =
                planningService
                        .createTask(
                                ORG_ONE,

                                tenderId,

                                new CreateBidTaskRequest(
                                        "Prepare technical methodology",

                                        "Draft the project execution methodology.",

                                        BidTaskPriority.HIGH,

                                        BidTaskMilestone.TECHNICAL_RESPONSE,

                                        "Bid team",

                                        LocalDate.of(
                                                2099,
                                                10,
                                                10
                                        ),

                                        null
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace.tasks()
        )
                .hasSize(
                        1
                );


        assertThat(
                workspace.statistics().openTasks()
        )
                .isEqualTo(
                        1
                );


        UUID taskId =
                workspace.tasks()
                        .getFirst()
                        .id();


        workspace =
                planningService
                        .updateTask(
                                ORG_ONE,

                                tenderId,

                                taskId,

                                new UpdateBidTaskRequest(
                                        null,
                                        null,
                                        BidTaskStatus.DONE,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace.statistics().openTasks()
        )
                .isZero();


        assertThat(
                workspace.statistics().completedTasks()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                workspace.tasks()
                        .getFirst()
                        .completedAt()
        )
                .isNotNull();


        assertThat(
                eventRepository.count()
        )
                .isEqualTo(
                        2
                );
    }


    @Test
    void taskCanReferenceTenantRequirement() {

        UUID tenderId =
                seedTenderAndMatch(
                        "PLAN-REQ",
                        ORG_ONE
                );


        UUID requirementId =
                createRequirement(
                        tenderId,
                        "Provide ISO 9001 certificate"
                );


        var workspace =
                planningService
                        .createTask(
                                ORG_ONE,

                                tenderId,

                                new CreateBidTaskRequest(
                                        "Validate ISO certificate",

                                        null,

                                        BidTaskPriority.HIGH,

                                        BidTaskMilestone.COMPLIANCE,

                                        "Compliance lead",

                                        LocalDate.of(
                                                2099,
                                                10,
                                                15
                                        ),

                                        requirementId
                                ),

                                "analyst@example.test"
                        );


        assertThat(
                workspace.tasks()
                        .getFirst()
                        .requirementId()
        )
                .isEqualTo(
                        requirementId
                );


        assertThat(
                workspace.tasks()
                        .getFirst()
                        .requirementTitle()
        )
                .isEqualTo(
                        "Provide ISO 9001 certificate"
                );
    }


    @Test
    void crossTenantPlanningAccessIsRejected() {

        UUID tenderId =
                seedTenderAndMatch(
                        "PLAN-TENANT",
                        ORG_ONE
                );


        assertThatThrownBy(
                () ->
                        planningService
                                .workspace(
                                        ORG_TWO,
                                        tenderId
                                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }


    @Test
    void noGoPreservesReadButLocksPlanningWrites() {

        UUID tenderId =
                seedTenderAndMatch(
                        "PLAN-NOGO",
                        ORG_ONE
                );


        createDecision(
                tenderId,
                BidDecisionOutcome.NO_GO
        );


        var workspace =
                planningService
                        .workspace(
                                ORG_ONE,
                                tenderId
                        );


        assertThat(
                workspace.writable()
        )
                .isFalse();


        assertThatThrownBy(
                () ->
                        planningService
                                .createTask(
                                        ORG_ONE,

                                        tenderId,

                                        new CreateBidTaskRequest(
                                                "Blocked task",
                                                null,
                                                BidTaskPriority.MEDIUM,
                                                BidTaskMilestone.OTHER,
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


    @Test
    void submissionRequiresGoComplianceChecklistThenFreezesWorkspace() {

        UUID tenderId =
                seedTenderAndMatch(
                        "PLAN-SUBMIT",
                        ORG_ONE
                );


        UUID requirementId =
                createRequirement(
                        tenderId,
                        "Provide required technical references"
                );


        complianceService
                .review(
                        ORG_ONE,

                        tenderId,

                        requirementId,

                        new UpdateComplianceReviewRequest(
                                ComplianceVerdict.COMPLIANT,
                                ComplianceRiskLevel.LOW,
                                EvidenceStrength.STRONG,
                                "Evidence validated.",
                                "Preserve evidence in final pack."
                        ),

                        "analyst@example.test"
                );


        createDecision(
                tenderId,
                BidDecisionOutcome.GO
        );


        planningService
                .updateSubmissionControl(
                        ORG_ONE,

                        tenderId,

                        new UpdateSubmissionControlRequest(
                                true,
                                true,
                                true,
                                true,
                                true,
                                true,
                                Instant.parse(
                                        "2099-10-20T10:00:00Z"
                                ),
                                "Final controlled submission."
                        ),

                        "analyst@example.test"
                );


        var authorized =
                planningService
                        .authorizeSubmission(
                                ORG_ONE,

                                tenderId,

                                new SubmissionAuthorizationRequest(
                                        true
                                ),

                                "manager@example.test"
                        );


        assertThat(
                authorized.statistics()
                        .readyForSubmission()
        )
                .isTrue();


        var submitted =
                planningService
                        .markSubmitted(
                                ORG_ONE,

                                tenderId,

                                new MarkBidSubmittedRequest(
                                        "PORTAL-RECEIPT-1601",
                                        "Submitted successfully."
                                ),

                                "manager@example.test"
                        );


        assertThat(
                submitted.submissionControl()
                        .submittedAt()
        )
                .isNotNull();


        assertThat(
                submitted.submissionControl()
                        .submissionReference()
        )
                .isEqualTo(
                        "PORTAL-RECEIPT-1601"
                );


        assertThat(
                submitted.writable()
        )
                .isFalse();


        assertThatThrownBy(
                () ->
                        planningService
                                .createTask(
                                        ORG_ONE,

                                        tenderId,

                                        new CreateBidTaskRequest(
                                                "Post submission mutation",
                                                null,
                                                BidTaskPriority.LOW,
                                                BidTaskMilestone.OTHER,
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


    private UUID createRequirement(
            UUID tenderId,
            String title
    ) {

        var workspace =
                workspaceService
                        .createRequirement(
                                ORG_ONE,

                                tenderId,

                                new CreateBidRequirementRequest(
                                        title,

                                        "Planning integration requirement.",

                                        BidRequirementCategory.TECHNICAL,

                                        true,

                                        null,

                                        "CCTP §5",

                                        LocalDate.of(
                                                2099,
                                                10,
                                                10
                                        ),

                                        "Bid team",

                                        null
                                ),

                                "analyst@example.test"
                        );


        return workspace
                .requirements()
                .stream()
                .filter(
                        requirement ->
                                requirement.title()
                                        .equals(
                                                title
                                        )
                )
                .findFirst()
                .orElseThrow()
                .id();
    }


    private void createDecision(
            UUID tenderId,
            BidDecisionOutcome outcome
    ) {

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
                        "Synthetic planning recommendation"
                ),

                90,

                TenderMatchGrade.EXCELLENT,

                "Strong strategic fit",

                "No material risks",

                "Stable assumptions"
        );


        decision.decide(
                outcome,
                "manager@example.test"
        );


        decisionRepository
                .saveAndFlush(
                        decision
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

                "BID_PLANNING_TEST",

                "https://example.test/"
                        +
                        externalId,

                "bid-planning-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "PLANNING-BUYER:"
                                        +
                                        externalId,

                                "Ville de Paris",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                null,

                                null
                        ),

                "Bid planning tender "
                        +
                        externalId,

                "Public-building renovation planning fixture.",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        10
                ),

                Instant.parse(
                        "2099-10-30T16:00:00Z"
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
