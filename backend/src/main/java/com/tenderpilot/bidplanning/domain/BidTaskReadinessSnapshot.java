package com.tenderpilot.bidplanning.domain;

import java.time.LocalDate;

public record BidTaskReadinessSnapshot(

        BidTaskStatus status,

        BidTaskPriority priority,

        LocalDate dueDate

) {
}
