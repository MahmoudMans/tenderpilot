package com.tenderpilot.dashboard.application;

import com.tenderpilot.dashboard.domain.DashboardMarketSignal;

import java.time.LocalDate;

public interface DashboardPublicMarketGateway {

    DashboardMarketSignal snapshot(
            LocalDate dateFrom,
            LocalDate dateTo
    );

}
