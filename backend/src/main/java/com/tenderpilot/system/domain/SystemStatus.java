package com.tenderpilot.system.domain;

import java.time.Instant;
import java.util.List;

public record SystemStatus(

        String application,

        String version,

        String environment,

        Instant timestamp,

        List<DependencyStatus> dependencies

) {
}
