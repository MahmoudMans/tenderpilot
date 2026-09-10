package com.tenderpilot.localai.domain;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class BidAiFindingValue {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "finding_type",
            nullable = false,
            length = 50
    )
    private BidAiFindingType type;


    @Column(
            name = "title",
            nullable = false,
            length = 600
    )
    private String title;


    @Column(
            name = "detail",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String detail;


    @Column(
            name = "evidence_snippet",
            length = 1500
    )
    private String evidenceSnippet;


    @Column(
            name = "mandatory"
    )
    private Boolean mandatory;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "suggested_requirement_category",
            length = 50
    )
    private BidRequirementCategory
            suggestedRequirementCategory;


    protected BidAiFindingValue() {
    }


    public BidAiFindingValue(
            BidAiFindingType type,
            String title,
            String detail,
            String evidenceSnippet,
            Boolean mandatory,
            BidRequirementCategory suggestedRequirementCategory
    ) {

        this.type =
                type;

        this.title =
                title;

        this.detail =
                detail;

        this.evidenceSnippet =
                evidenceSnippet;

        this.mandatory =
                mandatory;

        this.suggestedRequirementCategory =
                suggestedRequirementCategory;
    }


    public BidAiFindingType getType() {
        return type;
    }


    public String getTitle() {
        return title;
    }


    public String getDetail() {
        return detail;
    }


    public String getEvidenceSnippet() {
        return evidenceSnippet;
    }


    public Boolean getMandatory() {
        return mandatory;
    }


    public BidRequirementCategory
    getSuggestedRequirementCategory() {

        return suggestedRequirementCategory;
    }

}
