package com.tenderpilot.notification.api;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import com.tenderpilot.notification.application.NotificationCenterService;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/notifications"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class NotificationCenterController {

    private final NotificationCenterService
            notificationCenterService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public NotificationCenterController(
            NotificationCenterService notificationCenterService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.notificationCenterService =
                notificationCenterService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping
    public NotificationCenterResponse center(
            Authentication authentication
    ) {

        return notificationCenterService
                .center(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }


    @PostMapping(
            "/refresh"
    )
    public NotificationCenterResponse refresh(
            Authentication authentication
    ) {

        return notificationCenterService
                .refresh(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }


    @PostMapping(
            "/watchlist"
    )
    public NotificationCenterResponse createWatchlistItem(
            Authentication authentication,
            @Valid
            @RequestBody
            CreateWatchlistItemRequest request
    ) {

        return notificationCenterService
                .createWatchlistItem(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),

                        request
                );
    }


    @DeleteMapping(
            "/watchlist/{watchlistId}"
    )
    public NotificationCenterResponse removeWatchlistItem(
            Authentication authentication,
            @PathVariable
            UUID watchlistId
    ) {

        return notificationCenterService
                .removeWatchlistItem(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),

                        watchlistId
                );
    }


    @PostMapping(
            "/{notificationId}/acknowledge"
    )
    public NotificationCenterResponse acknowledge(
            Authentication authentication,
            @PathVariable
            UUID notificationId
    ) {

        return notificationCenterService
                .acknowledge(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),

                        notificationId
                );
    }


    @PostMapping(
            "/{notificationId}/dismiss"
    )
    public NotificationCenterResponse dismiss(
            Authentication authentication,
            @PathVariable
            UUID notificationId
    ) {

        return notificationCenterService
                .dismiss(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),

                        notificationId
                );
    }


    @PostMapping(
            "/read-all"
    )
    public NotificationCenterResponse markAllRead(
            Authentication authentication
    ) {

        return notificationCenterService
                .markAllRead(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }

}
