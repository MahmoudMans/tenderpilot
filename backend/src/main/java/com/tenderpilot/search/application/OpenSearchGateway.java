package com.tenderpilot.search.application;

import com.tenderpilot.search.domain.ProcurementSearchDocument;
import com.tenderpilot.search.domain.ProcurementSearchQuery;

import java.util.List;
import java.util.UUID;

public interface OpenSearchGateway {

    boolean isAvailable();


    boolean indexExists();


    void ensureIndex();


    void recreateIndex();


    void index(
            ProcurementSearchDocument document
    );


    long count();


    SearchPage search(
            ProcurementSearchQuery query
    );


    record SearchPage(
            long total,
            List<SearchMatch> matches
    ) {
    }


    record SearchMatch(
            UUID tenderId,
            double score
    ) {
    }

}
