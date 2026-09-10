package com.tenderpilot.marketanalytics.domain;

import java.util.UUID;

public record MarketCpvObservation(

        UUID tenderId,

        String code,

        String description

) {
}
