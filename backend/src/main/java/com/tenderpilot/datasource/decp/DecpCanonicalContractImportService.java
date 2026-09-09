package com.tenderpilot.datasource.decp;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.award.application.CanonicalAwardImportService;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DecpCanonicalContractImportService {

    private final CanonicalTenderImportService
            tenderImportService;

    private final CanonicalAwardImportService
            awardImportService;

    public DecpCanonicalContractImportService(
            CanonicalTenderImportService tenderImportService,
            CanonicalAwardImportService awardImportService
    ) {
        this.tenderImportService =
                tenderImportService;

        this.awardImportService =
                awardImportService;
    }

    @Transactional
    public UUID upsert(
            DecpMappedContract mapped
    ) {

        UUID tenderId =
                tenderImportService
                        .upsert(
                                mapped.tender()
                        );

        awardImportService
                .upsert(
                        tenderId,
                        mapped.award()
                );

        return tenderId;
    }
}
