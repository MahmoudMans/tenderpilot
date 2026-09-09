package com.tenderpilot.company.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OperatingRegionValue {

    @Column(
            name = "country_code",
            nullable = false,
            length = 2
    )
    private String countryCode;


    @Column(
            name = "region_code",
            nullable = false,
            length = 40
    )
    private String regionCode;


    @Column(
            name = "region_name",
            nullable = false,
            length = 160
    )
    private String regionName;


    protected OperatingRegionValue() {
    }


    public OperatingRegionValue(

            String countryCode,

            String regionCode,

            String regionName

    ) {

        this.countryCode =
                countryCode;

        this.regionCode =
                regionCode;

        this.regionName =
                regionName;

    }


    public String getCountryCode() {

        return countryCode;

    }


    public String getRegionCode() {

        return regionCode;

    }


    public String getRegionName() {

        return regionName;

    }

}
