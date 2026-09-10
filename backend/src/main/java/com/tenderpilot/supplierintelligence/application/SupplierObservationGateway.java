package com.tenderpilot.supplierintelligence.application;

import com.tenderpilot.supplierintelligence.domain.SupplierAwardObservation;
import com.tenderpilot.supplierintelligence.domain.SupplierAwardParty;
import com.tenderpilot.supplierintelligence.domain.SupplierTenderCpv;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SupplierObservationGateway {

    List<String> searchSupplierNames(
            String query,
            String buyerCountry,
            int limit
    );


    List<SupplierAwardObservation> observationsBySupplier(
            String supplierName
    );


    List<SupplierAwardParty> suppliersForAwards(
            Collection<UUID> awardIds
    );


    List<SupplierTenderCpv> cpvsForTenders(
            Collection<UUID> tenderIds
    );

}
