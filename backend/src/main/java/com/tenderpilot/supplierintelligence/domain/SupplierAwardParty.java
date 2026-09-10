package com.tenderpilot.supplierintelligence.domain;

import java.util.UUID;

public record SupplierAwardParty(

        UUID awardId,

        String supplierName

) {
}
