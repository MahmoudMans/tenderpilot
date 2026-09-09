package com.tenderpilot.company.infrastructure;

import com.tenderpilot.company.domain.CompanyProfileEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProfileRepository
        extends JpaRepository<CompanyProfileEntity, UUID> {

    Optional<CompanyProfileEntity> findByOrganizationId(
            UUID organizationId
    );

}
