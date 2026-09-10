package com.tenderpilot.marketanalytics.domain;

import java.util.UUID;

public record MarketSupplierObservation(

        UUID awardId,

        String supplierName

) {
}
