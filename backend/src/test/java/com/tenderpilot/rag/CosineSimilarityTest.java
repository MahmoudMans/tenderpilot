package com.tenderpilot.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.rag.application.CosineSimilarity;

import java.util.List;

import org.junit.jupiter.api.Test;

class CosineSimilarityTest {

    private final CosineSimilarity similarity =
            new CosineSimilarity();


    @Test
    void identicalVectorsScoreOne() {

        assertThat(
                similarity.score(
                        List.of(
                                1.0,
                                2.0,
                                3.0
                        ),

                        List.of(
                                1.0,
                                2.0,
                                3.0
                        )
                )
        )
                .isCloseTo(
                        1.0,
                        org.assertj.core.data.Offset.offset(
                                0.000001
                        )
                );
    }


    @Test
    void orthogonalVectorsScoreZero() {

        assertThat(
                similarity.score(
                        List.of(
                                1.0,
                                0.0
                        ),

                        List.of(
                                0.0,
                                1.0
                        )
                )
        )
                .isCloseTo(
                        0.0,
                        org.assertj.core.data.Offset.offset(
                                0.000001
                        )
                );
    }


    @Test
    void dimensionMismatchIsRejected() {

        assertThatThrownBy(
                () ->
                        similarity.score(
                                List.of(
                                        1.0
                                ),

                                List.of(
                                        1.0,
                                        2.0
                                )
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

}
