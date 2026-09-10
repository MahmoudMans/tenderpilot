package com.tenderpilot.rag.infrastructure;

import com.tenderpilot.rag.domain.BidRagChunkEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRagChunkRepository
        extends JpaRepository<BidRagChunkEntity, UUID> {

    List<BidRagChunkEntity>
    findByOrganizationIdAndTender_IdOrderByDocument_IdAscChunkIndexAsc(
            UUID organizationId,
            UUID tenderId
    );


    long countByOrganizationIdAndTender_Id(
            UUID organizationId,
            UUID tenderId
    );


    void deleteByOrganizationIdAndTender_Id(
            UUID organizationId,
            UUID tenderId
    );

}
