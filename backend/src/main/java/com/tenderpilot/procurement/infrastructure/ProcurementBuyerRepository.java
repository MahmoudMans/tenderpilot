package com.tenderpilot.procurement.infrastructure;

import com.tenderpilot.procurement.domain.ProcurementBuyerEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementBuyerRepository
        extends JpaRepository<ProcurementBuyerEntity, UUID> {

    Optional<ProcurementBuyerEntity> findByCanonicalKey(
            String canonicalKey
    );

}
