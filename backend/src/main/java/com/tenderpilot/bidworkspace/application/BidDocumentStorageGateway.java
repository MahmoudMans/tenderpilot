package com.tenderpilot.bidworkspace.application;

import java.util.UUID;

public interface BidDocumentStorageGateway {

    StoredBidDocument store(
            UUID organizationId,
            UUID tenderId,
            String originalFilename,
            byte[] content
    );


    byte[] load(
            String storageKey
    );


    void delete(
            String storageKey
    );


    record StoredBidDocument(
            String storageKey,
            String storedFilename
    ) {
    }

}
