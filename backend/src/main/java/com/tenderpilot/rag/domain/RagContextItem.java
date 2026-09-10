package com.tenderpilot.rag.domain;

import java.util.UUID;

public record RagContextItem(

        String sourceId,

        UUID chunkId,

        UUID documentId,

        String filename,

        int chunkIndex,

        double similarity,

        String text

) {
}
