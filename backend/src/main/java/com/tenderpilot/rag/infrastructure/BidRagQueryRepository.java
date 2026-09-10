package com.tenderpilot.rag.infrastructure;

import com.tenderpilot.rag.domain.BidRagQueryEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRagQueryRepository
        extends JpaRepository<BidRagQueryEntity, UUID> {

    List<BidRagQueryEntity>
    findTop25ByOrganizationIdAndTender_IdOrderByCreatedAtDesc(
            UUID organizationId,
            UUID tenderId
    );

}
