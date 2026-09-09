package com.tenderpilot.common.domain;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(
            String message
    ) {

        super(
                message
        );

    }

}
