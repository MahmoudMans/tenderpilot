package com.tenderpilot.identity.application;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class IdentityNormalizer {

    public String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );

    }


    public String cleanDisplayText(
            String value
    ) {

        return value
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );

    }

}
