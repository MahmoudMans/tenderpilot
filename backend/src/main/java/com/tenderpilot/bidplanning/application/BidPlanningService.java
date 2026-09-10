package com.tenderpilot.bidplanning.application;

import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.biddecision.infrastructure.BidDecisionRepository;

import com.tenderpilot.bidplanning.api.BidPlanningWorkspaceResponse;
import com.tenderpilot.bidplanning.api.CreateBidTaskRequest;
import com.tenderpilot.bidplanning.api.MarkBidSubmittedRequest;
import com.tenderpilot.bidplanning.api.SubmissionAuthorizationRequest;
import com.tenderpilot.bidplanning.api.UpdateBidTaskRequest;
import com.tenderpilot.bidplanning.api.UpdateSubmissionControlRequest;

import com.tenderpilot.bidplanning.domain.BidPlanningEventEntity;
import com.tenderpilot.bidplanning.domain.BidPlanningEventType;
import com.tenderpilot.bidplanning.domain.BidSubmissionControlEntity;
import com.tenderpilot.bidplanning.domain.BidSubmissionReadiness;
import com.tenderpilot.bidplanning.domain.BidTaskEntity;
import com.tenderpilot.bidplanning.domain.BidTaskMilestone;
import com.tenderpilot.bidplanning.domain.BidTaskPriority;
import com.tenderpilot.bidplanning.domain.BidTaskReadinessSnapshot;
import com.tenderpilot.bidplanning.domain.BidTaskStatus;
import com.tenderpilot.bidplanning.domain.SubmissionChecklistSnapshot;

import com.tenderpilot.bidplanning.infrastructure.BidPlanningEventRepository;
import com.tenderpilot.bidplanning.infrastructure.BidSubmissionControlRepository;
import com.tenderpilot.bidplanning.infrastructure.BidTaskRepository;

import com.tenderpilot.bidworkspace.domain.BidRequirementEntity;

import com.tenderpilot.bidworkspace.infrastructure.BidRequirementRepository;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.compliance.api.BidComplianceMatrixResponse;

import com.tenderpilot.compliance.application.BidComplianceService;

import com.tenderpilot.matching.domain.TenderMatchEntity;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class BidPlanningService {

    private final TenderMatchRepository
            matchRepository;


    private final BidDecisionRepository
            decisionRepository;


    private final BidRequirementRepository
            requirementRepository;


    private final BidComplianceService
            complianceService;


    private final BidTaskRepository
            taskRepository;


    private final BidSubmissionControlRepository
            submissionControlRepository;


    private final BidPlanningEventRepository
            eventRepository;


    private final BidSubmissionReadinessEngine
            readinessEngine;


    public BidPlanningService(
            TenderMatchRepository matchRepository,
            BidDecisionRepository decisionRepository,
            BidRequirementRepository requirementRepository,
            BidComplianceService complianceService,
            BidTaskRepository taskRepository,
            BidSubmissionControlRepository submissionControlRepository,
            BidPlanningEventRepository eventRepository,
            BidSubmissionReadinessEngine readinessEngine
    ) {

        this.matchRepository =
                matchRepository;

        this.decisionRepository =
                decisionRepository;

        this.requirementRepository =
                requirementRepository;

        this.complianceService =
                complianceService;

        this.taskRepository =
                taskRepository;

        this.submissionControlRepository =
                submissionControlRepository;

        this.eventRepository =
                eventRepository;

        this.readinessEngine =
                readinessEngine;
    }


    @Transactional(readOnly = true)
    public BidPlanningWorkspaceResponse workspace(
            UUID organizationId,
            UUID tenderId
    ) {

        requireMatch(
                organizationId,
                tenderId
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidPlanningWorkspaceResponse createTask(
            UUID organizationId,
            UUID tenderId,
            CreateBidTaskRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Task request is required"
            );
        }


        BidRequirementEntity requirement =
                optionalRequirement(
                        organizationId,
                        tenderId,
                        request.requirementId()
                );


        BidTaskEntity task =
                BidTaskEntity
                        .create(
                                organizationId,

                                match.getTender(),

                                requirement,

                                required(
                                        request.title(),
                                        "Task title is required"
                                ),

                                clean(
                                        request.description()
                                ),

                                request.priority() == null
                                        ? BidTaskPriority.MEDIUM
                                        : request.priority(),

                                request.milestone() == null
                                        ? BidTaskMilestone.OTHER
                                        : request.milestone(),

                                clean(
                                        request.assignedTo()
                                ),

                                request.dueDate(),

                                actor(
                                        actor
                                )
                        );


        taskRepository
                .saveAndFlush(
                        task
                );


        event(
                organizationId,
                match,
                BidPlanningEventType.TASK_CREATED,
                task.getId(),
                actor,
                "Task created: "
                        +
                        task.getTitle()
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidPlanningWorkspaceResponse updateTask(
            UUID organizationId,
            UUID tenderId,
            UUID taskId,
            UpdateBidTaskRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Task update is required"
            );
        }


        BidTaskEntity task =
                taskRepository
                        .findByIdAndOrganizationIdAndTender_Id(
                                taskId,
                                organizationId,
                                tenderId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Bid task was not found"
                                        )
                        );


        BidRequirementEntity requirement =
                request.requirementId() == null
                        ? task.getRequirement()
                        : optionalRequirement(
                                organizationId,
                                tenderId,
                                request.requirementId()
                        );


        task.update(
                requirement,

                request.title() == null
                        ? task.getTitle()
                        : required(
                                request.title(),
                                "Task title cannot be blank"
                        ),

                request.description() == null
                        ? task.getDescription()
                        : clean(
                                request.description()
                        ),

                request.status() == null
                        ? task.getStatus()
                        : request.status(),

                request.priority() == null
                        ? task.getPriority()
                        : request.priority(),

                request.milestone() == null
                        ? task.getMilestone()
                        : request.milestone(),

                request.assignedTo() == null
                        ? task.getAssignedTo()
                        : clean(
                                request.assignedTo()
                        ),

                request.dueDate() == null
                        ? task.getDueDate()
                        : request.dueDate(),

                actor(
                        actor
                )
        );


        taskRepository
                .saveAndFlush(
                        task
                );


        event(
                organizationId,
                match,
                BidPlanningEventType.TASK_UPDATED,
                task.getId(),
                actor,
                "Task updated: "
                        +
                        task.getTitle()
                        +
                        " ["
                        +
                        task.getStatus()
                        +
                        "]"
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidPlanningWorkspaceResponse deleteTask(
            UUID organizationId,
            UUID tenderId,
            UUID taskId,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        BidTaskEntity task =
                taskRepository
                        .findByIdAndOrganizationIdAndTender_Id(
                                taskId,
                                organizationId,
                                tenderId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Bid task was not found"
                                        )
                        );


        String title =
                task.getTitle();


        taskRepository
                .delete(
                        task
                );


        taskRepository.flush();


        event(
                organizationId,
                match,
                BidPlanningEventType.TASK_DELETED,
                taskId,
                actor,
                "Task deleted: "
                        +
                        title
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidPlanningWorkspaceResponse updateSubmissionControl(
            UUID organizationId,
            UUID tenderId,
            UpdateSubmissionControlRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Submission-control update is required"
            );
        }


        BidSubmissionControlEntity control =
                control(
                        organizationId,
                        tenderId,
                        match,
                        actor
                );


        control.updateChecklist(
                value(
                        request.portalReady(),
                        control.isPortalReady()
                ),

                value(
                        request.signaturesReady(),
                        control.isSignaturesReady()
                ),

                value(
                        request.technicalResponseReady(),
                        control.isTechnicalResponseReady()
                ),

                value(
                        request.commercialResponseReady(),
                        control.isCommercialResponseReady()
                ),

                value(
                        request.administrativePackReady(),
                        control.isAdministrativePackReady()
                ),

                value(
                        request.finalReviewReady(),
                        control.isFinalReviewReady()
                ),

                request.plannedSubmissionAt() == null
                        ? control.getPlannedSubmissionAt()
                        : request.plannedSubmissionAt(),

                request.notes() == null
                        ? control.getNotes()
                        : clean(
                                request.notes()
                        ),

                actor(
                        actor
                )
        );


        submissionControlRepository
                .saveAndFlush(
                        control
                );


        event(
                organizationId,
                match,
                BidPlanningEventType.SUBMISSION_CONTROL_UPDATED,
                null,
                actor,
                "Submission checklist updated"
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidPlanningWorkspaceResponse authorizeSubmission(
            UUID organizationId,
            UUID tenderId,
            SubmissionAuthorizationRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Submission authorization request is required"
            );
        }


        BidSubmissionControlEntity control =
                control(
                        organizationId,
                        tenderId,
                        match,
                        actor
                );


        control.authorize(
                request.authorized(),
                actor(
                        actor
                )
        );


        submissionControlRepository
                .saveAndFlush(
                        control
                );


        event(
                organizationId,
                match,
                BidPlanningEventType.SUBMISSION_AUTHORIZATION_CHANGED,
                null,
                actor,
                request.authorized()
                        ? "Submission authorized"
                        : "Submission authorization revoked"
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    @Transactional
    public BidPlanningWorkspaceResponse markSubmitted(
            UUID organizationId,
            UUID tenderId,
            MarkBidSubmittedRequest request,
            String actor
    ) {

        TenderMatchEntity match =
                requireWritableMatch(
                        organizationId,
                        tenderId
                );


        if (
                request == null
        ) {

            throw new InvalidRequestException(
                    "Submission confirmation is required"
            );
        }


        String reference =
                required(
                        request.submissionReference(),
                        "Submission reference is required"
                );


        BidPlanningWorkspaceResponse before =
                buildWorkspace(
                        organizationId,
                        tenderId
                );


        if (
                !before
                        .statistics()
                        .readyForSubmission()
        ) {

            throw new InvalidRequestException(
                    "Bid cannot be marked submitted until the decision, compliance, tasks and submission checklist are all ready"
            );
        }


        BidSubmissionControlEntity control =
                submissionControlRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
                        .orElseThrow(
                                () ->
                                        new InvalidRequestException(
                                                "Submission control has not been prepared"
                                        )
                        );


        control.markSubmitted(
                reference,

                clean(
                        request.notes()
                ),

                actor(
                        actor
                )
        );


        submissionControlRepository
                .saveAndFlush(
                        control
                );


        event(
                organizationId,
                match,
                BidPlanningEventType.SUBMITTED,
                null,
                actor,
                "Bid marked submitted with reference: "
                        +
                        reference
        );


        return buildWorkspace(
                organizationId,
                tenderId
        );
    }


    private BidPlanningWorkspaceResponse buildWorkspace(
            UUID organizationId,
            UUID tenderId
    ) {

        List<BidTaskEntity> tasks =
                taskRepository
                        .findByOrganizationIdAndTender_IdOrderByDueDateAscCreatedAtAsc(
                                organizationId,
                                tenderId
                        );


        BidSubmissionControlEntity control =
                submissionControlRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
                        .orElse(
                                null
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


        BidDecisionOutcome decisionOutcome =
                decision == null
                        ? null
                        : decision.getOutcome();


        BidComplianceMatrixResponse compliance =
                complianceService
                        .matrix(
                                organizationId,
                                tenderId
                        );


        BidSubmissionReadiness readiness =
                readinessEngine
                        .calculate(
                                decisionOutcome,

                                compliance
                                        .statistics()
                                        .readyForSubmission(),

                                tasks
                                        .stream()
                                        .map(
                                                task ->
                                                        new BidTaskReadinessSnapshot(
                                                                task.getStatus(),
                                                                task.getPriority(),
                                                                task.getDueDate()
                                                        )
                                        )
                                        .toList(),

                                checklist(
                                        control
                                ),

                                LocalDate.now(
                                        ZoneOffset.UTC
                                )
                        );


        boolean submitted =
                control != null
                &&
                control.getSubmittedAt()
                        !=
                        null;


        boolean writable =
                decisionOutcome
                        !=
                        BidDecisionOutcome.NO_GO
                &&
                !submitted;


        String lockReason =
                null;


        if (
                submitted
        ) {

            lockReason =
                    "Bid has been marked submitted. The planning workspace is frozen for auditability.";


        } else if (
                decisionOutcome
                        ==
                        BidDecisionOutcome.NO_GO
        ) {

            lockReason =
                    "Current bid decision is NO-GO. Reopen the decision before changing the bid plan.";
        }


        return new BidPlanningWorkspaceResponse(
                tenderId,

                decisionOutcome,

                compliance
                        .statistics()
                        .readyForSubmission(),

                compliance
                        .statistics()
                        .readinessScore(),

                writable,

                lockReason,

                new BidPlanningWorkspaceResponse
                        .Statistics(
                                readiness.readinessScore(),
                                readiness.totalTasks(),
                                readiness.openTasks(),
                                readiness.completedTasks(),
                                readiness.blockedTasks(),
                                readiness.overdueTasks(),
                                readiness.criticalOpenTasks(),
                                readiness.taskCompletionPercentage(),
                                readiness.checklistCompletionPercentage(),
                                readiness.readyForSubmission()
                        ),

                controlResponse(
                        control
                ),

                tasks
                        .stream()
                        .map(
                                this::taskResponse
                        )
                        .toList(),

                eventRepository
                        .findTop50ByOrganizationIdAndTender_IdOrderByCreatedAtDesc(
                                organizationId,
                                tenderId
                        )
                        .stream()
                        .map(
                                event ->
                                        new BidPlanningWorkspaceResponse
                                                .Event(
                                                        event.getId(),
                                                        event.getEventType(),
                                                        event.getTaskId(),
                                                        event.getActor(),
                                                        event.getDetail(),
                                                        event.getCreatedAt()
                                                )
                        )
                        .toList()
        );
    }


    private BidPlanningWorkspaceResponse.Task taskResponse(
            BidTaskEntity task
    ) {

        boolean overdue =
                task.getStatus()
                        !=
                        BidTaskStatus.DONE
                &&
                task.getStatus()
                        !=
                        BidTaskStatus.CANCELLED
                &&
                task.getDueDate()
                        !=
                        null
                &&
                task.getDueDate()
                        .isBefore(
                                LocalDate.now(
                                        ZoneOffset.UTC
                                )
                        );


        return new BidPlanningWorkspaceResponse
                .Task(
                        task.getId(),

                        task.getRequirement() == null
                                ? null
                                : task
                                        .getRequirement()
                                        .getId(),

                        task.getRequirement() == null
                                ? null
                                : task
                                        .getRequirement()
                                        .getTitle(),

                        task.getTitle(),

                        task.getDescription(),

                        task.getStatus(),

                        task.getPriority(),

                        task.getMilestone(),

                        task.getAssignedTo(),

                        task.getDueDate(),

                        overdue,

                        task.getCompletedAt(),

                        task.getCreatedBy(),

                        task.getUpdatedBy(),

                        task.getCreatedAt(),

                        task.getUpdatedAt()
                );
    }


    private BidPlanningWorkspaceResponse.SubmissionControl
    controlResponse(
            BidSubmissionControlEntity control
    ) {

        if (
                control == null
        ) {

            return new BidPlanningWorkspaceResponse
                    .SubmissionControl(
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
        }


        return new BidPlanningWorkspaceResponse
                .SubmissionControl(
                        control.isPortalReady(),
                        control.isSignaturesReady(),
                        control.isTechnicalResponseReady(),
                        control.isCommercialResponseReady(),
                        control.isAdministrativePackReady(),
                        control.isFinalReviewReady(),
                        control.isSubmissionAuthorized(),
                        control.getPlannedSubmissionAt(),
                        control.getSubmittedAt(),
                        control.getSubmissionReference(),
                        control.getSubmittedBy(),
                        control.getNotes(),
                        control.getUpdatedBy()
                );
    }


    private SubmissionChecklistSnapshot checklist(
            BidSubmissionControlEntity control
    ) {

        if (
                control == null
        ) {

            return new SubmissionChecklistSnapshot(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
            );
        }


        return new SubmissionChecklistSnapshot(
                control.isPortalReady(),
                control.isSignaturesReady(),
                control.isTechnicalResponseReady(),
                control.isCommercialResponseReady(),
                control.isAdministrativePackReady(),
                control.isFinalReviewReady(),
                control.isSubmissionAuthorized()
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
                    "Bid planning is locked because the current decision is NO-GO"
            );
        }


        BidSubmissionControlEntity control =
                submissionControlRepository
                        .findByOrganizationIdAndTender_Id(
                                organizationId,
                                tenderId
                        )
                        .orElse(
                                null
                        );


        if (
                control != null
                &&
                control.getSubmittedAt()
                        !=
                        null
        ) {

            throw new InvalidRequestException(
                    "Bid planning is frozen because the bid has already been marked submitted"
            );
        }


        return match;
    }


    private BidRequirementEntity optionalRequirement(
            UUID organizationId,
            UUID tenderId,
            UUID requirementId
    ) {

        if (
                requirementId == null
        ) {

            return null;
        }


        return requirementRepository
                .findByIdAndOrganizationIdAndTender_Id(
                        requirementId,
                        organizationId,
                        tenderId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Linked bid requirement was not found"
                                )
                );
    }


    private BidSubmissionControlEntity control(
            UUID organizationId,
            UUID tenderId,
            TenderMatchEntity match,
            String actor
    ) {

        return submissionControlRepository
                .findByOrganizationIdAndTender_Id(
                        organizationId,
                        tenderId
                )
                .orElseGet(
                        () ->
                                BidSubmissionControlEntity
                                        .create(
                                                organizationId,
                                                match.getTender(),
                                                actor(
                                                        actor
                                                )
                                        )
                );
    }


    private void event(
            UUID organizationId,
            TenderMatchEntity match,
            BidPlanningEventType type,
            UUID taskId,
            String actor,
            String detail
    ) {

        eventRepository
                .saveAndFlush(
                        BidPlanningEventEntity
                                .create(
                                        organizationId,
                                        match.getTender(),
                                        type,
                                        taskId,
                                        actor(
                                                actor
                                        ),
                                        detail
                                )
                );
    }


    private boolean value(
            Boolean requested,
            boolean current
    ) {

        return requested == null
                ? current
                : requested;
    }


    private String required(
            String value,
            String message
    ) {

        String result =
                clean(
                        value
                );


        if (
                result == null
        ) {

            throw new InvalidRequestException(
                    message
            );
        }


        return result;
    }


    private String actor(
            String value
    ) {

        String result =
                clean(
                        value
                );


        return result == null
                ? "unknown"
                : result;
    }


    private String clean(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return result.isBlank()
                ? null
                : result;
    }

}
