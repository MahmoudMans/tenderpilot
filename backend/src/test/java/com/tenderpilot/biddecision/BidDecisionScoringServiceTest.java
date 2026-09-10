package com.tenderpilot.biddecision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.biddecision.application.BidDecisionScoringService;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import com.tenderpilot.common.domain.InvalidRequestException;

import org.junit.jupiter.api.Test;

class BidDecisionScoringServiceTest {

    private final BidDecisionScoringService service =
            new BidDecisionScoringService();


    @Test
    void perfectAssessmentAndStrongMatchRecommendGo() {

        BidAssessmentEvaluation result =
                service.evaluate(
                        scorecard(
                                5
                        ),
                        90,
                        false
                );


        assertThat(
                result.score()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.recommendation()
        )
                .isEqualTo(
                        BidDecisionOutcome.GO
                );
    }


    @Test
    void mediumAssessmentRecommendsHold() {

        BidAssessmentEvaluation result =
                service.evaluate(
                        scorecard(
                                3
                        ),
                        75,
                        false
                );


        assertThat(
                result.score()
        )
                .isEqualTo(
                        60
                );


        assertThat(
                result.recommendation()
        )
                .isEqualTo(
                        BidDecisionOutcome.HOLD
                );
    }


    @Test
    void lowAssessmentRecommendsNoGo() {

        BidAssessmentEvaluation result =
                service.evaluate(
                        scorecard(
                                2
                        ),
                        80,
                        false
                );


        assertThat(
                result.score()
        )
                .isEqualTo(
                        40
                );


        assertThat(
                result.recommendation()
        )
                .isEqualTo(
                        BidDecisionOutcome.NO_GO
                );
    }


    @Test
    void blockedMatchAlwaysRecommendsNoGo() {

        BidAssessmentEvaluation result =
                service.evaluate(
                        scorecard(
                                5
                        ),
                        95,
                        true
                );


        assertThat(
                result.score()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.recommendation()
        )
                .isEqualTo(
                        BidDecisionOutcome.NO_GO
                );
    }


    @Test
    void weakMatchCannotReceiveGoRecommendation() {

        BidAssessmentEvaluation result =
                service.evaluate(
                        scorecard(
                                5
                        ),
                        50,
                        false
                );


        assertThat(
                result.recommendation()
        )
                .isEqualTo(
                        BidDecisionOutcome.NO_GO
                );
    }


    @Test
    void scorecardRejectsValuesAboveFive() {

        assertThatThrownBy(
                () ->
                        service.evaluate(
                                new BidDecisionScorecard(
                                        6,
                                        5,
                                        5,
                                        5,
                                        5,
                                        5
                                ),
                                90,
                                false
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    private BidDecisionScorecard scorecard(
            int value
    ) {

        return new BidDecisionScorecard(
                value,
                value,
                value,
                value,
                value,
                value
        );
    }

}
