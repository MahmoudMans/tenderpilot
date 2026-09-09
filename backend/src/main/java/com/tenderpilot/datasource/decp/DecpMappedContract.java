package com.tenderpilot.datasource.decp;

import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;
import com.tenderpilot.procurement.award.application.CanonicalAwardUpsertRequest;

public record DecpMappedContract(
        CanonicalTenderUpsertRequest tender,
        CanonicalAwardUpsertRequest award
) {
}
