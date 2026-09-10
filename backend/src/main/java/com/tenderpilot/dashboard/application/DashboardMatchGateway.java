package com.tenderpilot.dashboard.application;

import com.tenderpilot.dashboard.domain.DashboardMatchObservation;

import java.util.List;
import java.util.UUID;

public interface DashboardMatchGateway {

    List<DashboardMatchObservation> matches(
            UUID organizationId
    );

}
