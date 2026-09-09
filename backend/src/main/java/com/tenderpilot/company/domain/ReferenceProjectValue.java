package com.tenderpilot.company.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Embeddable
public class ReferenceProjectValue {

    @Column(
            name = "project_name",
            nullable = false,
            length = 220
    )
    private String projectName;


    @Column(
            name = "client_name",
            nullable = false,
            length = 220
    )
    private String clientName;


    @Column(
            name = "country_code",
            nullable = false,
            length = 2
    )
    private String countryCode;


    @Column(
            name = "region_name",
            length = 160
    )
    private String regionName;


    @Column(
            name = "contract_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal contractValue;


    @Column(
            name = "currency",
            length = 3
    )
    private String currency;


    @Column(
            name = "started_on"
    )
    private LocalDate startedOn;


    @Column(
            name = "completed_on"
    )
    private LocalDate completedOn;


    @Column(
            name = "project_description",
            nullable = false,
            length = 3000
    )
    private String description;


    protected ReferenceProjectValue() {
    }


    public ReferenceProjectValue(

            String projectName,

            String clientName,

            String countryCode,

            String regionName,

            BigDecimal contractValue,

            String currency,

            LocalDate startedOn,

            LocalDate completedOn,

            String description

    ) {

        this.projectName =
                projectName;

        this.clientName =
                clientName;

        this.countryCode =
                countryCode;

        this.regionName =
                regionName;

        this.contractValue =
                contractValue;

        this.currency =
                currency;

        this.startedOn =
                startedOn;

        this.completedOn =
                completedOn;

        this.description =
                description;

    }


    public String getProjectName() {

        return projectName;

    }


    public String getClientName() {

        return clientName;

    }


    public String getCountryCode() {

        return countryCode;

    }


    public String getRegionName() {

        return regionName;

    }


    public BigDecimal getContractValue() {

        return contractValue;

    }


    public String getCurrency() {

        return currency;

    }


    public LocalDate getStartedOn() {

        return startedOn;

    }


    public LocalDate getCompletedOn() {

        return completedOn;

    }


    public String getDescription() {

        return description;

    }

}
