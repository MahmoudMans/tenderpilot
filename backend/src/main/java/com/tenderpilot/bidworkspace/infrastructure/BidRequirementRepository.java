package com.tenderpilot.bidworkspace.infrastructure;

import com.tenderpilot.bidworkspace.domain.BidRequirementEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRequirementRepository
        extends JpaRepository<BidRequirementEntity, UUID> {

    List<BidRequirementEntity>
    findByOrganizationIdAndTender_IdOrderByMandatoryDescCreatedAtAsc(
            UUID organizationId,
            UUID tenderId
    );


    Optional<BidRequirementEntity>
    findByIdAndOrganizationIdAndTender_Id(
            UUID requirementId,
            UUID organizationId,
            UUID tenderId
    );

}
