package com.tenderpilot.company.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class CertificationValue {

    @Column(
            name = "certification_name",
            nullable = false,
            length = 180
    )
    private String name;


    @Column(
            name = "issuer",
            nullable = false,
            length = 180
    )
    private String issuer;


    @Column(
            name = "certificate_number",
            length = 160
    )
    private String certificateNumber;


    @Column(
            name = "valid_from"
    )
    private LocalDate validFrom;


    @Column(
            name = "expires_on"
    )
    private LocalDate expiresOn;


    protected CertificationValue() {
    }


    public CertificationValue(

            String name,

            String issuer,

            String certificateNumber,

            LocalDate validFrom,

            LocalDate expiresOn

    ) {

        this.name =
                name;

        this.issuer =
                issuer;

        this.certificateNumber =
                certificateNumber;

        this.validFrom =
                validFrom;

        this.expiresOn =
                expiresOn;

    }


    public String getName() {

        return name;

    }


    public String getIssuer() {

        return issuer;

    }


    public String getCertificateNumber() {

        return certificateNumber;

    }


    public LocalDate getValidFrom() {

        return validFrom;

    }


    public LocalDate getExpiresOn() {

        return expiresOn;

    }

}
