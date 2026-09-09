package com.tenderpilot.organization.infrastructure;

import com.tenderpilot.organization.domain.OrganizationMembershipEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMembershipRepository
        extends JpaRepository<OrganizationMembershipEntity, UUID> {

    Optional<OrganizationMembershipEntity>
    findFirstByUser_IdOrderByCreatedAtAsc(
            UUID userId
    );


    Optional<OrganizationMembershipEntity>
    findByUser_IdAndOrganization_Id(

            UUID userId,

            UUID organizationId

    );


    List<OrganizationMembershipEntity>
    findAllByOrganization_IdOrderByCreatedAtAsc(
            UUID organizationId
    );

}
