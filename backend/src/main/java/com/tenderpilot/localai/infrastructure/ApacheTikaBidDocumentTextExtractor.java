package com.tenderpilot.localai.infrastructure;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.localai.application.BidDocumentTextExtractor;

import com.tenderpilot.localai.domain.BidDocumentExtractionResult;

import java.io.ByteArrayInputStream;

import java.util.Locale;
import java.util.Set;

import org.apache.tika.Tika;

import org.apache.tika.metadata.Metadata;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;

import org.apache.tika.sax.BodyContentHandler;

import org.springframework.stereotype.Component;

@Component
public class ApacheTikaBidDocumentTextExtractor
        implements BidDocumentTextExtractor {

    public static final String EXTRACTOR_VERSION =
            "APACHE_TIKA_3_3_2";


    private static final Set<String> ANALYZABLE_EXTENSIONS =
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
                    "ods"
            );


    private final LocalAiSettings settings;


    public ApacheTikaBidDocumentTextExtractor(
            LocalAiSettings settings
    ) {

        this.settings =
                settings;
    }


    @Override
    public BidDocumentExtractionResult extract(
            String filename,
            byte[] content
    ) {

        String extension =
                extension(
                        filename
                );


        if (
                !ANALYZABLE_EXTENSIONS.contains(
                        extension
                )
        ) {

            throw new InvalidRequestException(
                    "This file type is stored safely but is not supported for text-based AI analysis"
            );
        }


        if (
                content == null
                ||
                content.length == 0
        ) {

            throw new InvalidRequestException(
                    "Cannot analyze an empty document"
            );
        }


        Tika tika =
                new Tika();


        String detectedType;


        try {

            detectedType =
                    tika.detect(
                            content,
                            filename
                    );


        } catch (
                RuntimeException exception
        ) {

            detectedType =
                    "application/octet-stream";
        }


        AutoDetectParser parser =
                new AutoDetectParser();


        /*
         * Tika's write limit prevents the AI extraction layer from
         * accepting an unbounded text payload.
         */
        BodyContentHandler handler =
                new BodyContentHandler(
                        settings.maxExtractedChars()
                );


        Metadata metadata =
                new Metadata();


        ParseContext context =
                new ParseContext();


        boolean truncated =
                false;


        try (
                ByteArrayInputStream input =
                        new ByteArrayInputStream(
                                content
                        )
        ) {

            parser.parse(
                    input,
                    handler,
                    metadata,
                    context
            );


        } catch (
                Exception exception
        ) {

            String partial =
                    handler.toString();


            if (
                    partial.length()
                            >=
                            Math.max(
                                    100,
                                    settings.maxExtractedChars() - 1000
                            )
            ) {

                truncated =
                        true;

            } else {

                throw new InvalidRequestException(
                        "Unable to extract readable text from this document"
                );
            }
        }


        String normalized =
                normalize(
                        handler.toString()
                );


        if (
                normalized.length()
                        >
                        settings.maxExtractedChars()
        ) {

            normalized =
                    normalized.substring(
                            0,
                            settings.maxExtractedChars()
                    );

            truncated =
                    true;
        }


        if (
                normalized.isBlank()
        ) {

            throw new InvalidRequestException(
                    "No machine-readable text was found in this document"
            );
        }


        return new BidDocumentExtractionResult(
                normalized,
                normalized.length(),
                truncated,
                detectedType,
                EXTRACTOR_VERSION
        );
    }


    private String extension(
            String filename
    ) {

        if (
                filename == null
                ||
                filename.isBlank()
        ) {

            return "";
        }


        int dot =
                filename.lastIndexOf(
                        '.'
                );


        if (
                dot < 0
                ||
                dot
                        ==
                        filename.length() - 1
        ) {

            return "";
        }


        return filename
                .substring(
                        dot + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private String normalize(
            String text
    ) {

        if (
                text == null
        ) {

            return "";
        }


        return text
                .replace(
                        "\u0000",
                        ""
                )
                .replaceAll(
                        "[\\t\\x0B\\f\\r]+",
                        " "
                )
                .replaceAll(
                        " +",
                        " "
                )
                .replaceAll(
                        "\\n{3,}",
                        "\n\n"
                )
                .trim();
    }

}
