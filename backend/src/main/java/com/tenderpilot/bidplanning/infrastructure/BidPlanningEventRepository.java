package com.tenderpilot.bidplanning.infrastructure;

import com.tenderpilot.bidplanning.domain.BidPlanningEventEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidPlanningEventRepository
        extends JpaRepository<
                BidPlanningEventEntity,
                UUID
        > {

    List<BidPlanningEventEntity>
    findTop50ByOrganizationIdAndTender_IdOrderByCreatedAtDesc(
            UUID organizationId,
            UUID tenderId
    );

}
