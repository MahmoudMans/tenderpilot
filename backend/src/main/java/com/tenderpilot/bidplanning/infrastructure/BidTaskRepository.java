package com.tenderpilot.bidplanning.infrastructure;

import com.tenderpilot.bidplanning.domain.BidTaskEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidTaskRepository
        extends JpaRepository<BidTaskEntity, UUID> {

    List<BidTaskEntity>
    findByOrganizationIdAndTender_IdOrderByDueDateAscCreatedAtAsc(
            UUID organizationId,
            UUID tenderId
    );


    Optional<BidTaskEntity>
    findByIdAndOrganizationIdAndTender_Id(
            UUID taskId,
            UUID organizationId,
            UUID tenderId
    );

}
