package com.tenderpilot.localai.domain;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;

import java.util.List;

public record OllamaBidAnalysisResult(

        String summary,

        Integer confidence,

        List<Finding> findings

) {

    public record Finding(

            BidAiFindingType type,

            String title,

            String detail,

            String evidence,

            Boolean mandatory,

            BidRequirementCategory requirementCategory

    ) {
    }

}
