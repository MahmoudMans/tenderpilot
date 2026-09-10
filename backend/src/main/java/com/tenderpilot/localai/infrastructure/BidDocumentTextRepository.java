package com.tenderpilot.localai.infrastructure;

import com.tenderpilot.localai.domain.BidDocumentTextEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidDocumentTextRepository
        extends JpaRepository<BidDocumentTextEntity, UUID> {

    Optional<BidDocumentTextEntity>
    findByOrganizationIdAndTender_IdAndDocument_Id(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    );

}
