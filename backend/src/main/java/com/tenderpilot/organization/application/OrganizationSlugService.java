package com.tenderpilot.organization.application;

import com.tenderpilot.organization.infrastructure.OrganizationRepository;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class OrganizationSlugService {

    private final OrganizationRepository
            organizationRepository;


    public OrganizationSlugService(
            OrganizationRepository organizationRepository
    ) {

        this.organizationRepository =
                organizationRepository;

    }


    public String generate(
            String organizationName
    ) {

        String normalized =
                Normalizer
                        .normalize(
                                organizationName,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}",
                                ""
                        )
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replaceAll(
                                "[^a-z0-9]+",
                                "-"
                        )
                        .replaceAll(
                                "^-+|-+$",
                                ""
                        );


        if (
                normalized.isBlank()
        ) {

            normalized =
                    "organization";

        }


        String candidate =
                normalized;


        if (
                !organizationRepository
                        .existsBySlug(
                                candidate
                        )
        ) {

            return candidate;

        }


        do {

            candidate =
                    normalized
                            +
                            "-"
                            +
                            UUID
                                    .randomUUID()
                                    .toString()
                                    .substring(
                                            0,
                                            8
                                    );

        } while (
                organizationRepository
                        .existsBySlug(
                                candidate
                        )
        );


        return candidate;

    }

}
