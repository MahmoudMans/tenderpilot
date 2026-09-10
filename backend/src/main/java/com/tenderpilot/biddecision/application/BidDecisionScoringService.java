package com.tenderpilot.biddecision.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.biddecision.domain.BidAssessmentEvaluation;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;
import com.tenderpilot.biddecision.domain.BidDecisionScorecard;

import org.springframework.stereotype.Component;

@Component
public class BidDecisionScoringService {

    /*
     * 0..5 input per dimension.
     *
     * Strategic fit              25
     * Capability confidence      20
     * Commercial attractiveness  20
     * Delivery feasibility       15
     * Resource capacity          10
     * Risk confidence            10
     * -----------------------------
     * Total                     100
     */

    public BidAssessmentEvaluation evaluate(
            BidDecisionScorecard scorecard,
            int matchScore,
            boolean matchBlocked
    ) {

        validate(
                "strategicFit",
                scorecard.strategicFit()
        );

        validate(
                "capabilityConfidence",
                scorecard.capabilityConfidence()
        );

        validate(
                "commercialAttractiveness",
                scorecard.commercialAttractiveness()
        );

        validate(
                "deliveryFeasibility",
                scorecard.deliveryFeasibility()
        );

        validate(
                "resourceCapacity",
                scorecard.resourceCapacity()
        );

        validate(
                "riskConfidence",
                scorecard.riskConfidence()
        );


        int score =
                scorecard.strategicFit() * 5
                        +
                        scorecard.capabilityConfidence() * 4
                        +
                        scorecard.commercialAttractiveness() * 4
                        +
                        scorecard.deliveryFeasibility() * 3
                        +
                        scorecard.resourceCapacity() * 2
                        +
                        scorecard.riskConfidence() * 2;


        if (
                matchBlocked
        ) {

            return new BidAssessmentEvaluation(
                    score,
                    BidDecisionOutcome.NO_GO,
                    "Matching contains a blocker, so the recommended decision is NO-GO."
            );
        }


        if (
                matchScore < 40
        ) {

            return new BidAssessmentEvaluation(
                    score,
                    BidDecisionOutcome.NO_GO,
                    "Company-to-tender match is below the minimum strategic fit threshold."
            );
        }


        if (
                score >= 75
                &&
                matchScore >= 70
        ) {

            return new BidAssessmentEvaluation(
                    score,
                    BidDecisionOutcome.GO,
                    "Decision score and opportunity match both support active bid pursuit."
            );
        }


        if (
                score >= 55
                &&
                matchScore >= 55
        ) {

            return new BidAssessmentEvaluation(
                    score,
                    BidDecisionOutcome.HOLD,
                    "Opportunity remains viable but requires additional validation before a GO decision."
            );
        }


        return new BidAssessmentEvaluation(
                score,
                BidDecisionOutcome.NO_GO,
                "Decision score or opportunity match is below the recommended pursuit threshold."
        );
    }


    private void validate(
            String name,
            int value
    ) {

        if (
                value < 0
                ||
                value > 5
        ) {

            throw new InvalidRequestException(
                    name
                            +
                            " must be between 0 and 5"
            );
        }
    }

}
