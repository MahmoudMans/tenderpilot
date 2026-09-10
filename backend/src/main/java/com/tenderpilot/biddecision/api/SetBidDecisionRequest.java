package com.tenderpilot.biddecision.api;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

public record SetBidDecisionRequest(

        BidDecisionOutcome outcome,

        String note

) {
}
