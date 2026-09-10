package com.tenderpilot.matching.api;

public record TenderMatchRecalculationResponse(

        int evaluatedCount,

        int actionableCount,

        int blockedCount,

        int excellentCount,

        int strongCount

) {
}
