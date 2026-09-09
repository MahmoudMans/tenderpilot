package com.tenderpilot.common.domain;

public class ApiAuthenticationException
        extends RuntimeException {

    public ApiAuthenticationException(
            String message
    ) {

        super(
                message
        );

    }

}
