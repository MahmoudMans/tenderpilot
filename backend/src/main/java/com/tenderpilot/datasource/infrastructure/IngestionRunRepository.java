package com.tenderpilot.datasource.infrastructure;

import com.tenderpilot.datasource.domain.IngestionRunEntity;

import com.tenderpilot.procurement.domain.ProcurementSource;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionRunRepository
        extends JpaRepository<IngestionRunEntity, UUID> {

    Optional<IngestionRunEntity>
    findTop1BySourceOrderByStartedAtDesc(
            ProcurementSource source
    );

}
