package com.tenderpilot.bidplanning.api;

public record MarkBidSubmittedRequest(

        String submissionReference,

        String notes

) {
}
