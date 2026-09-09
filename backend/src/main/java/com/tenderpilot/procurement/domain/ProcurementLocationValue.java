package com.tenderpilot.procurement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProcurementLocationValue {

    @Column(
            name = "country_code",
            nullable = false,
            length = 2
    )
    private String countryCode;


    @Column(
            name = "region_code",
            length = 40
    )
    private String regionCode;


    @Column(
            name = "region_name",
            length = 180
    )
    private String regionName;


    @Column(
            name = "city",
            length = 180
    )
    private String city;


    protected ProcurementLocationValue() {
    }


    public ProcurementLocationValue(

            String countryCode,

            String regionCode,

            String regionName,

            String city

    ) {

        this.countryCode =
                countryCode;

        this.regionCode =
                regionCode;

        this.regionName =
                regionName;

        this.city =
                city;

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


    public String getCity() {

        return city;

    }

}
