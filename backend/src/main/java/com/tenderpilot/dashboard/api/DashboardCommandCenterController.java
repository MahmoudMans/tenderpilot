package com.tenderpilot.dashboard.api;

import com.tenderpilot.dashboard.application.DashboardCommandCenterService;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/dashboard"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class DashboardCommandCenterController {

    private final DashboardCommandCenterService
            commandCenterService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public DashboardCommandCenterController(
            DashboardCommandCenterService commandCenterService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.commandCenterService =
                commandCenterService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/command-center"
    )
    public DashboardCommandCenterResponse commandCenter(
            Authentication authentication
    ) {

        return commandCenterService
                .commandCenter(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }

}
