package com.tenderpilot.search.application;

public record SearchReindexResult(

        int discoveredCount,

        int indexedCount,

        int failedCount,

        String lastError

) {
}
