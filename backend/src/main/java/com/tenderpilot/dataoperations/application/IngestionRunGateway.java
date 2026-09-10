package com.tenderpilot.dataoperations.application;

import com.tenderpilot.dataoperations.domain.IngestionRunObservation;

import java.util.List;

public interface IngestionRunGateway {

    List<IngestionRunObservation> recentRuns(
            int limit
    );

}
