package com.tenderpilot.rag.api;

public record AskBidDocumentsRequest(

        String question,

        Integer topK

) {
}
