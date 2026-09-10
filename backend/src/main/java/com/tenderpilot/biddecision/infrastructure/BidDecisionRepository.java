package com.tenderpilot.biddecision.infrastructure;

import com.tenderpilot.biddecision.domain.BidDecisionEntity;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidDecisionRepository
        extends JpaRepository<BidDecisionEntity, UUID> {

    Optional<BidDecisionEntity>
    findByOrganizationIdAndTender_Id(
            UUID organizationId,
            UUID tenderId
    );


    List<BidDecisionEntity>
    findByOrganizationIdOrderByUpdatedAtDesc(
            UUID organizationId
    );


    long countByOrganizationIdAndOutcome(
            UUID organizationId,
            BidDecisionOutcome outcome
    );

}
