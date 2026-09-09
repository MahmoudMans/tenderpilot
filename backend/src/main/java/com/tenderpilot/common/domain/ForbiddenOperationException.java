package com.tenderpilot.common.domain;

public class ForbiddenOperationException
        extends RuntimeException {

    public ForbiddenOperationException(
            String message
    ) {

        super(
                message
        );

    }

}
