package com.tenderpilot.localai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.localai.infrastructure.ApacheTikaBidDocumentTextExtractor;
import com.tenderpilot.localai.infrastructure.LocalAiSettings;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ApacheTikaBidDocumentTextExtractorTest {

    @Test
    void extractsPlainTextLocally() {

        ApacheTikaBidDocumentTextExtractor extractor =
                extractor(
                        120000,
                        60000
                );


        var result =
                extractor.extract(
                        "requirements.txt",

                        """
                        Contract requirement:
                        The bidder must provide three references.

                        Deadline:
                        30 October 2026.
                        """
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );


        assertThat(
                result.text()
        )
                .contains(
                        "three references"
                )
                .contains(
                        "30 October 2026"
                );


        assertThat(
                result.extractorVersion()
        )
                .isEqualTo(
                        "APACHE_TIKA_3_3_2"
                );


        assertThat(
                result.characterCount()
        )
                .isPositive();
    }


    @Test
    void rejectsImageBecauseStep13IsTextExtractionOnly() {

        ApacheTikaBidDocumentTextExtractor extractor =
                extractor(
                        120000,
                        60000
                );


        assertThatThrownBy(
                () ->
                        extractor.extract(
                                "scan.png",
                                new byte[] {
                                        1,
                                        2,
                                        3
                                }
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    @Test
    void rejectsEmptyDocument() {

        ApacheTikaBidDocumentTextExtractor extractor =
                extractor(
                        120000,
                        60000
                );


        assertThatThrownBy(
                () ->
                        extractor.extract(
                                "empty.txt",
                                new byte[0]
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    private ApacheTikaBidDocumentTextExtractor extractor(
            int maxExtracted,
            int maxInput
    ) {

        return new ApacheTikaBidDocumentTextExtractor(
                new LocalAiSettings(
                        true,
                        "OLLAMA",
                        "http://localhost:11434/",
                        "qwen3:4b",
                        maxExtracted,
                        maxInput,
                        "BID_DOCUMENT_ANALYSIS_V1"
                )
        );
    }

}
