package com.tenderpilot.procurement.quality.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class DataQualityIssueValue {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "issue_code",
            nullable = false,
            length = 100
    )
    private DataQualityIssueCode code;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "severity",
            nullable = false,
            length = 40
    )
    private DataQualitySeverity severity;


    @Column(
            name = "field_name",
            nullable = false,
            length = 160
    )
    private String fieldName;


    @Column(
            name = "message",
            nullable = false,
            length = 800
    )
    private String message;


    protected DataQualityIssueValue() {
    }


    public DataQualityIssueValue(
            DataQualityIssueCode code,
            DataQualitySeverity severity,
            String fieldName,
            String message
    ) {

        this.code = code;
        this.severity = severity;
        this.fieldName = fieldName;
        this.message = message;
    }


    public DataQualityIssueCode getCode() {
        return code;
    }


    public DataQualitySeverity getSeverity() {
        return severity;
    }


    public String getFieldName() {
        return fieldName;
    }


    public String getMessage() {
        return message;
    }

}
