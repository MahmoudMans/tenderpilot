package com.tenderpilot.datasource.ted;

public record TedBuyerData(

        String name,

        String identifier,

        String countryCode,

        String city,

        String websiteUrl,

        String legalType

) {
}
