package com.tenderpilot.search.api;

public record SearchProjectionStatusResponse(

        boolean enabled,

        boolean reachable,

        boolean indexExists,

        String indexName,

        long indexedDocuments,

        String authority,

        String fallbackEngine

) {
}
