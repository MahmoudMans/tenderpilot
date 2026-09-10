package com.tenderpilot.matching.infrastructure;

import com.tenderpilot.matching.domain.TenderMatchEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderMatchRepository
        extends JpaRepository<TenderMatchEntity, UUID> {

    Optional<TenderMatchEntity>
    findByOrganizationIdAndTender_Id(
            UUID organizationId,
            UUID tenderId
    );


    List<TenderMatchEntity>
    findByOrganizationIdOrderByBlockedAscTotalScoreDescEvaluatedAtDesc(
            UUID organizationId
    );


    long countByOrganizationId(
            UUID organizationId
    );


    long deleteByOrganizationId(
            UUID organizationId
    );

}
