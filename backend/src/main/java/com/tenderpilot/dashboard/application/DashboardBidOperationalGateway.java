package com.tenderpilot.dashboard.application;

import com.tenderpilot.dashboard.domain.DashboardBidOperationalSnapshot;

import java.util.UUID;

public interface DashboardBidOperationalGateway {

    DashboardBidOperationalSnapshot snapshot(
            UUID organizationId,
            UUID tenderId
    );

}
