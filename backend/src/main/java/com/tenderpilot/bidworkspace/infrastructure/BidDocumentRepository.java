package com.tenderpilot.bidworkspace.infrastructure;

import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidDocumentRepository
        extends JpaRepository<BidDocumentEntity, UUID> {

    List<BidDocumentEntity>
    findByOrganizationIdAndTender_IdOrderByUploadedAtDesc(
            UUID organizationId,
            UUID tenderId
    );


    Optional<BidDocumentEntity>
    findByIdAndOrganizationIdAndTender_Id(
            UUID documentId,
            UUID organizationId,
            UUID tenderId
    );


    Optional<BidDocumentEntity>
    findByOrganizationIdAndTender_IdAndSha256(
            UUID organizationId,
            UUID tenderId,
            String sha256
    );

}
