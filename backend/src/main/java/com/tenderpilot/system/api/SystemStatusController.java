package com.tenderpilot.system.api;

import com.tenderpilot.system.application.SystemStatusService;
import com.tenderpilot.system.domain.SystemStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/system"
)
public class SystemStatusController {

    private final SystemStatusService
            systemStatusService;


    public SystemStatusController(
            SystemStatusService systemStatusService
    ) {

        this.systemStatusService =
                systemStatusService;

    }


    @GetMapping(
            "/status"
    )
    public SystemStatus status() {

        return systemStatusService
                .currentStatus();

    }

}
