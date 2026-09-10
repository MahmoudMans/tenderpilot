package com.tenderpilot.marketanalytics.domain;

import java.math.BigDecimal;

import java.util.UUID;

public record MarketAwardObservation(

        UUID awardId,

        UUID tenderId,

        BigDecimal amount,

        String currency

) {
}
