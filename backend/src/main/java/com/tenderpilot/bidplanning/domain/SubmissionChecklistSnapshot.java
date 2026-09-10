package com.tenderpilot.bidplanning.domain;

public record SubmissionChecklistSnapshot(

        boolean portalReady,

        boolean signaturesReady,

        boolean technicalResponseReady,

        boolean commercialResponseReady,

        boolean administrativePackReady,

        boolean finalReviewReady,

        boolean submissionAuthorized

) {

    public int completedCount() {

        int result =
                0;


        if (portalReady) {
            result++;
        }


        if (signaturesReady) {
            result++;
        }


        if (technicalResponseReady) {
            result++;
        }


        if (commercialResponseReady) {
            result++;
        }


        if (administrativePackReady) {
            result++;
        }


        if (finalReviewReady) {
            result++;
        }


        if (submissionAuthorized) {
            result++;
        }


        return result;
    }


    public boolean complete() {

        return completedCount()
                ==
                7;
    }

}
