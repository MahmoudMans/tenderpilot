package com.tenderpilot.bidplanning.infrastructure;

import com.tenderpilot.bidplanning.domain.BidSubmissionControlEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidSubmissionControlRepository
        extends JpaRepository<
                BidSubmissionControlEntity,
                UUID
        > {

    Optional<BidSubmissionControlEntity>
    findByOrganizationIdAndTender_Id(
            UUID organizationId,
            UUID tenderId
    );

}
