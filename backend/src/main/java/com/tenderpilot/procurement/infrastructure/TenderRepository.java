package com.tenderpilot.procurement.infrastructure;

import com.tenderpilot.procurement.domain.TenderEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderRepository
        extends JpaRepository<TenderEntity, UUID> {

    List<TenderEntity>
    findTop100ByOrderByPublicationDateDesc();

}
