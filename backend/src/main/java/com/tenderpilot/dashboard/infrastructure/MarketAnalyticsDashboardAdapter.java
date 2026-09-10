package com.tenderpilot.dashboard.infrastructure;

import com.tenderpilot.dashboard.application.DashboardPublicMarketGateway;

import com.tenderpilot.dashboard.domain.DashboardMarketSignal;

import com.tenderpilot.marketanalytics.api.MarketExplorerResponse;

import com.tenderpilot.marketanalytics.application.MarketAnalyticsService;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class MarketAnalyticsDashboardAdapter
        implements DashboardPublicMarketGateway {

    private final MarketAnalyticsService
            marketAnalyticsService;


    public MarketAnalyticsDashboardAdapter(
            MarketAnalyticsService marketAnalyticsService
    ) {

        this.marketAnalyticsService =
                marketAnalyticsService;
    }


    @Override
    public DashboardMarketSignal snapshot(
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        MarketExplorerResponse market =
                marketAnalyticsService
                        .explore(
                                null,
                                null,
                                null,
                                null,
                                null,
                                dateFrom,
                                dateTo
                        );


        MarketExplorerResponse.CpvBreakdown leadingCpv =
                market
                        .topCpvs()
                        .stream()
                        .findFirst()
                        .orElse(
                                null
                        );


        return new DashboardMarketSignal(
                true,

                market
                        .summary()
                        .noticeCount(),

                market
                        .summary()
                        .activeNoticeCount(),

                market
                        .summary()
                        .uniqueBuyerCount(),

                market
                        .summary()
                        .uniqueObservedSupplierCount(),

                market
                        .summary()
                        .buyerConcentration()
                        .name(),

                market
                        .summary()
                        .supplierConcentration()
                        .name(),

                leadingCpv == null
                        ? null
                        : leadingCpv.code(),

                leadingCpv == null
                        ? null
                        : leadingCpv.sharePercentage(),

                market
                        .summary()
                        .awardCoveragePercentage()
        );
    }

}
