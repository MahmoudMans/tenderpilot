package com.tenderpilot.localai.infrastructure;

import com.tenderpilot.localai.domain.BidDocumentAnalysisEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidDocumentAnalysisRepository
        extends JpaRepository<BidDocumentAnalysisEntity, UUID> {

    Optional<BidDocumentAnalysisEntity>
    findByOrganizationIdAndTender_IdAndDocument_Id(
            UUID organizationId,
            UUID tenderId,
            UUID documentId
    );


    List<BidDocumentAnalysisEntity>
    findByOrganizationIdAndTender_IdOrderByAnalyzedAtDesc(
            UUID organizationId,
            UUID tenderId
    );

}
