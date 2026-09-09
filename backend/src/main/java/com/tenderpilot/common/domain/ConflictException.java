package com.tenderpilot.common.domain;

public class ConflictException
        extends RuntimeException {

    public ConflictException(
            String message
    ) {

        super(
                message
        );

    }

}
