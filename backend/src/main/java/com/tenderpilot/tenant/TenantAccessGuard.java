package com.tenderpilot.tenant;

import com.tenderpilot.common.domain.ForbiddenOperationException;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class TenantAccessGuard {

    private final CurrentPrincipalService
            currentPrincipalService;


    public TenantAccessGuard(
            CurrentPrincipalService currentPrincipalService
    ) {

        this.currentPrincipalService =
                currentPrincipalService;

    }


    public void requireCurrentOrganization(
            UUID requestedOrganizationId
    ) {

        UUID activeOrganizationId =

                currentPrincipalService
                        .require()
                        .organizationId();


        if (
                !activeOrganizationId
                        .equals(
                                requestedOrganizationId
                        )
        ) {

            throw new ForbiddenOperationException(
                    "Cross-organization access is forbidden"
            );

        }

    }

}
