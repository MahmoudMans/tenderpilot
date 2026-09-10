package com.tenderpilot.bidworkspace.infrastructure;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class BidDocumentStorageSettings {

    private final Path root;

    private final long maxFileSizeBytes;


    public BidDocumentStorageSettings(

            @Value(
                    "${tenderpilot.bid-documents.storage-root:./.tenderpilot-storage/bid-documents}"
            )
            String root,

            @Value(
                    "${tenderpilot.bid-documents.max-file-size-bytes:15728640}"
            )
            long maxFileSizeBytes

    ) {

        this.root =
                Path.of(
                        root
                )
                        .toAbsolutePath()
                        .normalize();

        this.maxFileSizeBytes =
                Math.max(
                        1,
                        maxFileSizeBytes
                );
    }


    public Path root() {
        return root;
    }


    public long maxFileSizeBytes() {
        return maxFileSizeBytes;
    }

}
