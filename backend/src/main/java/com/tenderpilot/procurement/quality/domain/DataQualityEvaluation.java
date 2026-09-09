package com.tenderpilot.procurement.quality.domain;

import java.util.List;

public record DataQualityEvaluation(

        int score,

        DataQualityGrade grade,

        List<DataQualityIssueValue> issues

) {
}
