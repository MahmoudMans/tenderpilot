package com.tenderpilot.supplierintelligence.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import java.nio.charset.StandardCharsets;

import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class SupplierKeyCodec {

    public String encode(
            String supplierName
    ) {

        String normalized =
                normalize(
                        supplierName
                );


        if (
                normalized == null
        ) {

            throw new IllegalArgumentException(
                    "Supplier name is required"
            );
        }


        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        normalized
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );
    }


    public String decode(
            String supplierKey
    ) {

        if (
                supplierKey == null
                ||
                supplierKey.isBlank()
        ) {

            throw new InvalidRequestException(
                    "Supplier key is required"
            );
        }


        try {

            String decoded =
                    new String(
                            Base64
                                    .getUrlDecoder()
                                    .decode(
                                            supplierKey
                                    ),

                            StandardCharsets.UTF_8
                    );


            String normalized =
                    normalize(
                            decoded
                    );


            if (
                    normalized == null
            ) {

                throw new InvalidRequestException(
                        "Supplier key is invalid"
                );
            }


            return normalized;


        } catch (
                IllegalArgumentException exception
        ) {

            throw new InvalidRequestException(
                    "Supplier key is invalid"
            );
        }
    }


    private String normalize(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return result.isBlank()
                ? null
                : result;
    }

}
