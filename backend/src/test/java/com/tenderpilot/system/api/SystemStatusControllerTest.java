package com.tenderpilot.system.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenderpilot.system.application.SystemStatusService;
import com.tenderpilot.system.domain.DependencyStatus;
import com.tenderpilot.system.domain.SystemStatus;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class SystemStatusControllerTest {

    @Test
    void delegatesStatusRequestToApplicationService() {

        SystemStatusService service =
                mock(
                        SystemStatusService.class
                );


        SystemStatus expected =

                new SystemStatus(

                        "TenderPilot",

                        "0.1.0-SNAPSHOT",

                        "test",

                        Instant.parse(
                                "2026-09-09T12:00:00Z"
                        ),

                        List.of(

                                new DependencyStatus(

                                        "PostgreSQL",

                                        "UP",

                                        "Schema version 1"

                                )

                        )

                );


        when(
                service.currentStatus()
        )
                .thenReturn(
                        expected
                );


        SystemStatusController controller =

                new SystemStatusController(
                        service
                );


        assertThat(
                controller.status()
        )
                .isSameAs(
                        expected
                );

    }

}
