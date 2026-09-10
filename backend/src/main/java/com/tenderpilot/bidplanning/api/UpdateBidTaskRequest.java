package com.tenderpilot.bidplanning.api;

import com.tenderpilot.bidplanning.domain.BidTaskMilestone;
import com.tenderpilot.bidplanning.domain.BidTaskPriority;
import com.tenderpilot.bidplanning.domain.BidTaskStatus;

import java.time.LocalDate;

import java.util.UUID;

public record UpdateBidTaskRequest(

        String title,

        String description,

        BidTaskStatus status,

        BidTaskPriority priority,

        BidTaskMilestone milestone,

        String assignedTo,

        LocalDate dueDate,

        UUID requirementId

) {
}
