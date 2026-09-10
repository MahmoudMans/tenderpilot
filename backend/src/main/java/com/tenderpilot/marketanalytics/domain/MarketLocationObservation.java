package com.tenderpilot.marketanalytics.domain;

import java.util.UUID;

public record MarketLocationObservation(

        UUID tenderId,

        String countryCode,

        String regionCode,

        String regionName,

        String city

) {
}
