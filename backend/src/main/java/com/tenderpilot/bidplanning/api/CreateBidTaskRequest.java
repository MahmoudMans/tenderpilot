package com.tenderpilot.bidplanning.api;

import com.tenderpilot.bidplanning.domain.BidTaskMilestone;
import com.tenderpilot.bidplanning.domain.BidTaskPriority;

import java.time.LocalDate;

import java.util.UUID;

public record CreateBidTaskRequest(

        String title,

        String description,

        BidTaskPriority priority,

        BidTaskMilestone milestone,

        String assignedTo,

        LocalDate dueDate,

        UUID requirementId

) {
}
