package com.tenderpilot.company.application;

import com.tenderpilot.company.api.CompanyProfileResponse;
import com.tenderpilot.company.domain.CompanyProfileEntity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ProfileCompletenessCalculator {

    public CompanyProfileResponse.ProfileCompletenessResponse calculate(
            CompanyProfileEntity profile
    ) {

        if (
                profile == null
        ) {

            return new CompanyProfileResponse
                    .ProfileCompletenessResponse(

                            0,

                            emptySections()

                    );

        }


        List<CompanyProfileResponse.CompletenessSectionResponse>
                sections =
                new ArrayList<>();


        int identity =
                0;

        identity += present(
                profile.getLegalName()
        )
                ? 4
                : 0;

        identity += present(
                profile.getIndustry()
        )
                ? 4
                : 0;

        identity += present(
                profile.getDescription()
        )
                ? 4
                : 0;

        identity += present(
                profile.getCountryCode()
        )
                ? 4
                : 0;

        identity += present(
                profile.getHeadquartersCity()
        )
                ? 4
                : 0;


        sections.add(
                section(
                        "identity",
                        "Company identity",
                        identity,
                        20
                )
        );


        int commercial =
                0;

        commercial +=
                profile.getPreferredMinContractValue()
                        != null
                        ? 4
                        : 0;

        commercial +=
                profile.getPreferredMaxContractValue()
                        != null
                        ? 4
                        : 0;

        commercial +=
                present(
                        profile.getPreferredCurrency()
                )
                        ? 2
                        : 0;

        commercial +=
                profile.getYearsExperience()
                        != null
                        ? 3
                        : 0;

        commercial +=
                profile.getMinimumNoticeDays()
                        != null
                        ? 2
                        : 0;


        sections.add(
                section(
                        "commercial",
                        "Commercial strategy",
                        commercial,
                        15
                )
        );


        int capabilities =

                Math.min(
                        20,
                        profile
                                .getCapabilities()
                                .size()
                                *
                                4
                );


        sections.add(
                section(
                        "capabilities",
                        "Capabilities",
                        capabilities,
                        20
                )
        );


        int regions =

                Math.min(
                        10,
                        profile
                                .getOperatingRegions()
                                .size()
                                *
                                5
                );


        sections.add(
                section(
                        "geography",
                        "Operating geography",
                        regions,
                        10
                )
        );


        int cpvs =

                Math.min(
                        10,
                        profile
                                .getTargetCpvs()
                                .size()
                                *
                                2
                );


        sections.add(
                section(
                        "cpv",
                        "Target CPV codes",
                        cpvs,
                        10
                )
        );


        int certifications =

                Math.min(
                        10,
                        profile
                                .getCertifications()
                                .size()
                                *
                                5
                );


        sections.add(
                section(
                        "certifications",
                        "Certifications",
                        certifications,
                        10
                )
        );


        int references =

                Math.min(
                        15,
                        profile
                                .getReferenceProjects()
                                .size()
                                *
                                5
                );


        sections.add(
                section(
                        "references",
                        "Reference projects",
                        references,
                        15
                )
        );


        int total =

                sections
                        .stream()
                        .mapToInt(
                                CompanyProfileResponse
                                        .CompletenessSectionResponse
                                        ::points
                        )
                        .sum();


        return new CompanyProfileResponse
                .ProfileCompletenessResponse(

                        total,

                        List.copyOf(
                                sections
                        )

                );

    }


    private List<
            CompanyProfileResponse.CompletenessSectionResponse
            > emptySections() {

        return List.of(

                section(
                        "identity",
                        "Company identity",
                        0,
                        20
                ),

                section(
                        "commercial",
                        "Commercial strategy",
                        0,
                        15
                ),

                section(
                        "capabilities",
                        "Capabilities",
                        0,
                        20
                ),

                section(
                        "geography",
                        "Operating geography",
                        0,
                        10
                ),

                section(
                        "cpv",
                        "Target CPV codes",
                        0,
                        10
                ),

                section(
                        "certifications",
                        "Certifications",
                        0,
                        10
                ),

                section(
                        "references",
                        "Reference projects",
                        0,
                        15
                )

        );

    }


    private CompanyProfileResponse.CompletenessSectionResponse
    section(

            String key,

            String label,

            int points,

            int maximum

    ) {

        return new CompanyProfileResponse
                .CompletenessSectionResponse(

                        key,

                        label,

                        points,

                        maximum

                );

    }


    private boolean present(
            String value
    ) {

        return value != null
                &&
                !value.isBlank();

    }

}
