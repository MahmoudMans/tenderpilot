package com.tenderpilot.supplierintelligence.domain;

import java.util.UUID;

public record SupplierTenderCpv(

        UUID tenderId,

        String code,

        String description

) {
}
