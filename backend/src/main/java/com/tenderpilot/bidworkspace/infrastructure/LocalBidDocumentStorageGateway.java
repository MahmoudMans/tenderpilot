package com.tenderpilot.bidworkspace.infrastructure;

import com.tenderpilot.bidworkspace.application.BidDocumentStorageGateway;

import com.tenderpilot.common.domain.InvalidRequestException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class LocalBidDocumentStorageGateway
        implements BidDocumentStorageGateway {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "doc",
                    "docx",
                    "xls",
                    "xlsx",
                    "csv",
                    "txt",
                    "rtf",
                    "odt",
                    "ods",
                    "zip",
                    "png",
                    "jpg",
                    "jpeg"
            );


    private final BidDocumentStorageSettings
            settings;


    public LocalBidDocumentStorageGateway(
            BidDocumentStorageSettings settings
    ) {

        this.settings =
                settings;
    }


    @Override
    public StoredBidDocument store(
            UUID organizationId,
            UUID tenderId,
            String originalFilename,
            byte[] content
    ) {

        if (
                organizationId == null
                ||
                tenderId == null
        ) {

            throw new InvalidRequestException(
                    "Organization and tender identifiers are required for document storage"
            );
        }


        String safeFilename =
                safeFilename(
                        originalFilename
                );


        String extension =
                extension(
                        safeFilename
                );


        if (
                !ALLOWED_EXTENSIONS.contains(
                        extension
                )
        ) {

            throw new InvalidRequestException(
                    "Unsupported bid-document extension: "
                            +
                            extension
            );
        }


        if (
                content == null
                ||
                content.length == 0
        ) {

            throw new InvalidRequestException(
                    "Bid document cannot be empty"
            );
        }


        if (
                content.length
                        >
                        settings.maxFileSizeBytes()
        ) {

            throw new InvalidRequestException(
                    "Bid document exceeds the configured maximum file size"
            );
        }


        String storedFilename =
                UUID.randomUUID()
                        +
                        "."
                        +
                        extension;


        String storageKey =
                organizationId
                        +
                        "/"
                        +
                        tenderId
                        +
                        "/"
                        +
                        storedFilename;


        Path target =
                resolveSafe(
                        storageKey
                );


        try {

            Files.createDirectories(
                    target.getParent()
            );


            Files.write(
                    target,
                    content,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );


            return new StoredBidDocument(
                    storageKey,
                    storedFilename
            );


        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Unable to store bid document",
                    exception
            );
        }
    }


    @Override
    public byte[] load(
            String storageKey
    ) {

        Path target =
                resolveSafe(
                        storageKey
                );


        if (
                !Files.isRegularFile(
                        target
                )
        ) {

            throw new ResourceNotFoundException(
                    "Stored bid document was not found"
            );
        }


        try {

            return Files.readAllBytes(
                    target
            );

        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Unable to read stored bid document",
                    exception
            );
        }
    }


    @Override
    public void delete(
            String storageKey
    ) {

        Path target =
                resolveSafe(
                        storageKey
                );


        try {

            Files.deleteIfExists(
                    target
            );


        } catch (
                IOException exception
        ) {

            throw new IllegalStateException(
                    "Unable to delete stored bid document",
                    exception
            );
        }
    }


    private Path resolveSafe(
            String storageKey
    ) {

        if (
                storageKey == null
                ||
                storageKey.isBlank()
        ) {

            throw new InvalidRequestException(
                    "Document storage key is required"
            );
        }


        Path resolved =
                settings
                        .root()
                        .resolve(
                                storageKey
                        )
                        .normalize();


        if (
                !resolved.startsWith(
                        settings.root()
                )
        ) {

            throw new InvalidRequestException(
                    "Invalid bid-document storage path"
            );
        }


        return resolved;
    }


    private String safeFilename(
            String value
    ) {

        if (
                value == null
                ||
                value.isBlank()
        ) {

            throw new InvalidRequestException(
                    "Original filename is required"
            );
        }


        try {

            String result =
                    Path.of(
                            value
                    )
                            .getFileName()
                            .toString()
                            .trim();


            if (
                    result.isBlank()
            ) {

                throw new InvalidRequestException(
                        "Original filename is invalid"
                );
            }


            return result;


        } catch (
                InvalidPathException exception
        ) {

            throw new InvalidRequestException(
                    "Original filename is invalid"
            );
        }
    }


    private String extension(
            String filename
    ) {

        int dot =
                filename.lastIndexOf(
                        '.'
                );


        if (
                dot <= 0
                ||
                dot
                        ==
                        filename.length() - 1
        ) {

            throw new InvalidRequestException(
                    "Bid document must include a supported file extension"
            );
        }


        return filename
                .substring(
                        dot + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }

}
