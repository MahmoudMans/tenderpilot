package com.tenderpilot.search.api;

public record SearchReindexResponse(

        int discoveredCount,

        int indexedCount,

        int failedCount,

        String lastError

) {
}
