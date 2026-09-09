package com.tenderpilot.procurement.award.infrastructure;

import com.tenderpilot.procurement.award.domain.TenderAwardEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderAwardRepository
        extends JpaRepository<TenderAwardEntity, UUID> {

    Optional<TenderAwardEntity> findByTender_Id(
            UUID tenderId
    );
}
