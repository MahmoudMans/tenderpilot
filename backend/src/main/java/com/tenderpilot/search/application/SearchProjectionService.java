package com.tenderpilot.search.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.procurement.domain.TenderEntity;
import com.tenderpilot.procurement.infrastructure.TenderRepository;

import com.tenderpilot.search.domain.ProcurementSearchDocument;

import com.tenderpilot.search.infrastructure.OpenSearchSettings;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchProjectionService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    SearchProjectionService.class
            );


    private final OpenSearchGateway
            gateway;


    private final OpenSearchSettings
            settings;


    private final TenderRepository
            tenderRepository;


    private final ProcurementSearchDocumentBuilder
            documentBuilder;


    public SearchProjectionService(

            OpenSearchGateway gateway,

            OpenSearchSettings settings,

            TenderRepository tenderRepository,

            ProcurementSearchDocumentBuilder documentBuilder

    ) {

        this.gateway =
                gateway;

        this.settings =
                settings;

        this.tenderRepository =
                tenderRepository;

        this.documentBuilder =
                documentBuilder;

    }


    public boolean indexAfterWrite(
            UUID tenderId
    ) {

        if (
                !settings.enabled()
                ||
                !settings.autoIndex()
        ) {
            return false;
        }


        return indexBestEffort(
                tenderId
        );

    }


    public boolean indexBestEffort(
            UUID tenderId
    ) {

        if (
                !settings.enabled()
        ) {
            return false;
        }


        try {

            gateway.ensureIndex();


            ProcurementSearchDocument document =
                    documentBuilder
                            .build(
                                    tenderId
                            );


            gateway.index(
                    document
            );


            return true;

        } catch (
                RuntimeException exception
        ) {

            /*
             * PostgreSQL is authoritative.
             *
             * A search projection failure must never roll back
             * procurement ingestion.
             */
            log.warn(
                    "Unable to update OpenSearch projection for tender {}",
                    tenderId,
                    exception
            );


            return false;

        }

    }


    @Transactional(
            readOnly = true
    )
    public SearchReindexResult rebuild() {

        if (
                !settings.enabled()
        ) {

            throw new InvalidRequestException(
                    "OpenSearch projection is disabled"
            );

        }


        gateway.recreateIndex();


        List<TenderEntity> tenders =
                tenderRepository.findAll();


        int indexed =
                0;


        int failed =
                0;


        String lastError =
                null;


        for (
                TenderEntity tender :
                tenders
        ) {

            try {

                gateway.index(
                        documentBuilder
                                .build(
                                        tender.getId()
                                )
                );


                indexed++;

            } catch (
                    RuntimeException exception
            ) {

                failed++;

                lastError =
                        safeMessage(
                                exception
                        );

            }

        }


        return new SearchReindexResult(
                tenders.size(),
                indexed,
                failed,
                lastError
        );

    }


    private String safeMessage(
            Throwable throwable
    ) {

        String message =
                throwable.getMessage();


        return message == null
                ||
                message.isBlank()
                ? throwable
                        .getClass()
                        .getSimpleName()
                : message;

    }

}
