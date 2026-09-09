package com.tenderpilot.procurement.infrastructure;

import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.TenderSourceRecordEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderSourceRecordRepository
        extends JpaRepository<TenderSourceRecordEntity, UUID> {

    Optional<TenderSourceRecordEntity>
    findBySourceAndExternalId(

            ProcurementSource source,

            String externalId

    );

}
