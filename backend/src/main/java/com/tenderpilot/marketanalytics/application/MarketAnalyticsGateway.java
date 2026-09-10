package com.tenderpilot.marketanalytics.application;

import com.tenderpilot.marketanalytics.domain.MarketAnalyticsCapabilities;
import com.tenderpilot.marketanalytics.domain.MarketAwardObservation;
import com.tenderpilot.marketanalytics.domain.MarketCpvObservation;
import com.tenderpilot.marketanalytics.domain.MarketLocationObservation;
import com.tenderpilot.marketanalytics.domain.MarketQuery;
import com.tenderpilot.marketanalytics.domain.MarketSupplierObservation;
import com.tenderpilot.marketanalytics.domain.MarketTenderObservation;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MarketAnalyticsGateway {

    MarketAnalyticsCapabilities capabilities();


    List<MarketTenderObservation> tenders(
            MarketQuery query
    );


    List<MarketCpvObservation> cpvs(
            Collection<UUID> tenderIds
    );


    List<MarketLocationObservation> locations(
            Collection<UUID> tenderIds
    );


    List<MarketAwardObservation> awards(
            Collection<UUID> tenderIds
    );


    List<MarketSupplierObservation> suppliers(
            Collection<UUID> awardIds
    );

}
