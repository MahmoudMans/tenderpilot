package com.tenderpilot.procurement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CpvClassificationValue {

    @Column(
            name = "cpv_code",
            nullable = false,
            length = 8
    )
    private String code;


    @Column(
            name = "cpv_label",
            nullable = false,
            length = 300
    )
    private String label;


    protected CpvClassificationValue() {
    }


    public CpvClassificationValue(

            String code,

            String label

    ) {

        this.code =
                code;

        this.label =
                label;

    }


    public String getCode() {

        return code;

    }


    public String getLabel() {

        return label;

    }

}
