package com.tenderpilot.compliance.infrastructure;

import com.tenderpilot.compliance.domain.BidComplianceReviewEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidComplianceReviewRepository
        extends JpaRepository<
                BidComplianceReviewEntity,
                UUID
        > {

    List<BidComplianceReviewEntity>
    findByOrganizationIdAndTender_Id(
            UUID organizationId,
            UUID tenderId
    );


    Optional<BidComplianceReviewEntity>
    findByRequirement_IdAndOrganizationIdAndTender_Id(
            UUID requirementId,
            UUID organizationId,
            UUID tenderId
    );

}
