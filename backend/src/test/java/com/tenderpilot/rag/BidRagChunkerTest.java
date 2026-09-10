package com.tenderpilot.rag;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.rag.application.BidRagChunker;

import com.tenderpilot.rag.infrastructure.RagSettings;

import org.junit.jupiter.api.Test;

class BidRagChunkerTest {

    @Test
    void shortTextProducesSingleChunk() {

        BidRagChunker chunker =
                chunker(
                        400,
                        50
                );


        var chunks =
                chunker.chunk(
                        "The bidder must provide three project references."
                );


        assertThat(
                chunks
        )
                .hasSize(
                        1
                );


        assertThat(
                chunks
                        .getFirst()
                        .text()
        )
                .contains(
                        "three project references"
                );
    }


    @Test
    void longTextProducesMultipleBoundedChunks() {

        BidRagChunker chunker =
                chunker(
                        400,
                        50
                );


        String text =
                (
                        "Technical requirements and qualification evidence. "
                                +
                                "The bidder must provide relevant project references. "
                )
                        .repeat(
                                20
                        );


        var chunks =
                chunker.chunk(
                        text
                );


        assertThat(
                chunks.size()
        )
                .isGreaterThan(
                        1
                );


        assertThat(
                chunks
        )
                .allSatisfy(
                        chunk ->
                                assertThat(
                                        chunk.text().length()
                                )
                                        .isLessThanOrEqualTo(
                                                400
                                        )
                );
    }


    @Test
    void chunksUseOverlap() {

        BidRagChunker chunker =
                chunker(
                        400,
                        80
                );


        String text =
                "word "
                        .repeat(
                                300
                        );


        var chunks =
                chunker.chunk(
                        text
                );


        assertThat(
                chunks.size()
        )
                .isGreaterThan(
                        1
                );


        assertThat(
                chunks
                        .get(
                                1
                        )
                        .startOffset()
        )
                .isLessThan(
                        chunks
                                .getFirst()
                                .endOffset()
                );
    }


    @Test
    void blankTextProducesNoChunks() {

        assertThat(
                chunker(
                        400,
                        50
                )
                        .chunk(
                                "   \n\n "
                        )
        )
                .isEmpty();
    }


    private BidRagChunker chunker(
            int size,
            int overlap
    ) {

        return new BidRagChunker(
                new RagSettings(
                        true,
                        "nomic-embed-text-v2-moe",
                        size,
                        overlap,
                        5,
                        8,
                        0.15,
                        1200,
                        "PRIVATE_BID_RAG_V1"
                )
        );
    }

}
