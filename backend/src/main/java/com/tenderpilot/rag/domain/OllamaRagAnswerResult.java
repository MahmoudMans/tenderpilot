package com.tenderpilot.rag.domain;

import java.util.List;

public record OllamaRagAnswerResult(

        String answer,

        Boolean supported,

        List<String> sourceIds

) {
}
