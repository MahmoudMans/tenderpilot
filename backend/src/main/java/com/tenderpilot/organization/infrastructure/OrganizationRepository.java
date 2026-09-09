package com.tenderpilot.organization.infrastructure;

import com.tenderpilot.organization.domain.OrganizationEntity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository
        extends JpaRepository<OrganizationEntity, UUID> {

    boolean existsBySlug(
            String slug
    );

}
