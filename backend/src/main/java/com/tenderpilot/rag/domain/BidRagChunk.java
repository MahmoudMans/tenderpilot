package com.tenderpilot.rag.domain;

public record BidRagChunk(

        int index,

        int startOffset,

        int endOffset,

        String text

) {
}
