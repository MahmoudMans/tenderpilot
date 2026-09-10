package com.tenderpilot.bidworkspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.bidworkspace.application.BidDocumentStorageGateway;

import com.tenderpilot.bidworkspace.infrastructure.BidDocumentStorageSettings;
import com.tenderpilot.bidworkspace.infrastructure.LocalBidDocumentStorageGateway;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Path;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

class LocalBidDocumentStorageGatewayTest {

    @TempDir
    Path tempDirectory;


    @Test
    void storesAndLoadsDocumentRoundTrip() {

        LocalBidDocumentStorageGateway gateway =
                gateway(
                        1024
                );


        byte[] content =
                "Tender specifications"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );


        BidDocumentStorageGateway.StoredBidDocument stored =
                gateway.store(
                        organization(),
                        tender(),
                        "specifications.pdf",
                        content
                );


        assertThat(
                stored.storageKey()
        )
                .contains(
                        organization()
                                .toString()
                )
                .contains(
                        tender()
                                .toString()
                )
                .endsWith(
                        ".pdf"
                );


        assertThat(
                gateway.load(
                        stored.storageKey()
                )
        )
                .containsExactly(
                        content
                );
    }


    @Test
    void sanitizesTraversalFromOriginalFilename() {

        LocalBidDocumentStorageGateway gateway =
                gateway(
                        1024
                );


        BidDocumentStorageGateway.StoredBidDocument stored =
                gateway.store(
                        organization(),
                        tender(),
                        "../../dangerous-name.pdf",
                        new byte[] {
                                1,
                                2,
                                3
                        }
                );


        assertThat(
                stored.storageKey()
        )
                .doesNotContain(
                        ".."
                );


        assertThat(
                stored.storedFilename()
        )
                .endsWith(
                        ".pdf"
                );
    }


    @Test
    void rejectsUnsupportedFileExtension() {

        LocalBidDocumentStorageGateway gateway =
                gateway(
                        1024
                );


        assertThatThrownBy(
                () ->
                        gateway.store(
                                organization(),
                                tender(),
                                "script.exe",
                                new byte[] {
                                        1
                                }
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    @Test
    void rejectsFilesAboveConfiguredLimit() {

        LocalBidDocumentStorageGateway gateway =
                gateway(
                        4
                );


        assertThatThrownBy(
                () ->
                        gateway.store(
                                organization(),
                                tender(),
                                "large.pdf",
                                new byte[] {
                                        1,
                                        2,
                                        3,
                                        4,
                                        5
                                }
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    @Test
    void deleteRemovesStoredContent() {

        LocalBidDocumentStorageGateway gateway =
                gateway(
                        1024
                );


        BidDocumentStorageGateway.StoredBidDocument stored =
                gateway.store(
                        organization(),
                        tender(),
                        "delete-me.pdf",
                        new byte[] {
                                5,
                                4,
                                3
                        }
                );


        gateway.delete(
                stored.storageKey()
        );


        assertThatThrownBy(
                () ->
                        gateway.load(
                                stored.storageKey()
                        )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }


    private LocalBidDocumentStorageGateway gateway(
            long maxBytes
    ) {

        return new LocalBidDocumentStorageGateway(
                new BidDocumentStorageSettings(
                        tempDirectory.toString(),
                        maxBytes
                )
        );
    }


    private UUID organization() {

        return UUID.fromString(
                "10000000-0000-0000-0000-000000001200"
        );
    }


    private UUID tender() {

        return UUID.fromString(
                "20000000-0000-0000-0000-000000001200"
        );
    }

}
