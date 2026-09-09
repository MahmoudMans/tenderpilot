package com.tenderpilot.company.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.company.api.CompanyProfileResponse;
import com.tenderpilot.company.api.MatchingProfileResponse;
import com.tenderpilot.company.api.UpdateCompanyProfileRequest;

import com.tenderpilot.company.domain.BuyerType;
import com.tenderpilot.company.domain.CertificationValue;
import com.tenderpilot.company.domain.CompanyProfileEntity;
import com.tenderpilot.company.domain.OperatingRegionValue;
import com.tenderpilot.company.domain.ReferenceProjectValue;
import com.tenderpilot.company.domain.TargetCpvValue;

import com.tenderpilot.company.infrastructure.CompanyProfileRepository;

import com.tenderpilot.tenant.CurrentPrincipal;
import com.tenderpilot.tenant.CurrentPrincipalService;

import java.math.BigDecimal;

import java.net.URI;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyProfileService {

    private final CompanyProfileRepository
            companyProfileRepository;


    private final CurrentPrincipalService
            currentPrincipalService;


    private final ProfileCompletenessCalculator
            completenessCalculator;


    public CompanyProfileService(

            CompanyProfileRepository companyProfileRepository,

            CurrentPrincipalService currentPrincipalService,

            ProfileCompletenessCalculator completenessCalculator

    ) {

        this.companyProfileRepository =
                companyProfileRepository;

        this.currentPrincipalService =
                currentPrincipalService;

        this.completenessCalculator =
                completenessCalculator;

    }


    @Transactional(
            readOnly = true
    )
    public CompanyProfileResponse current() {

        CurrentPrincipal principal =

                currentPrincipalService
                        .require();


        return companyProfileRepository
                .findByOrganizationId(
                        principal.organizationId()
                )
                .map(
                        this::map
                )
                .orElseGet(
                        () ->
                                empty(
                                        principal.organizationId()
                                )
                );

    }


    @Transactional
    public CompanyProfileResponse upsert(
            UpdateCompanyProfileRequest request
    ) {

        CurrentPrincipal principal =

                currentPrincipalService
                        .require();


        validate(
                request
        );


        CompanyProfileEntity profile =

                companyProfileRepository
                        .findByOrganizationId(
                                principal.organizationId()
                        )
                        .orElseGet(
                                () ->
                                        CompanyProfileEntity
                                                .create(
                                                        principal.organizationId()
                                                )
                        );


        profile.update(

                cleanRequired(
                        request.legalName()
                ),

                cleanOptional(
                        request.tradingName()
                ),

                cleanRequired(
                        request.industry()
                ),

                cleanRequired(
                        request.description()
                ),

                normalizeWebsite(
                        request.websiteUrl()
                ),

                upper(
                        request.countryCode()
                ),

                cleanRequired(
                        request.headquartersCity()
                ),

                request.employeeBand(),

                request.annualRevenueBand(),

                request.preferredMinContractValue(),

                request.preferredMaxContractValue(),

                upperOptional(
                        request.preferredCurrency()
                ),

                request.yearsExperience(),

                request.minimumNoticeDays(),

                normalizeStrings(
                        request.capabilities()
                ),

                normalizeRegions(
                        request.operatingRegions()
                ),

                normalizeCpvs(
                        request.targetCpvs()
                ),

                normalizeCertifications(
                        request.certifications()
                ),

                normalizeReferences(
                        request.referenceProjects()
                ),

                normalizeBuyerTypes(
                        request.preferredBuyerTypes()
                ),

                normalizeStrings(
                        request.excludedKeywords()
                )

        );


        CompanyProfileEntity saved =

                companyProfileRepository
                        .saveAndFlush(
                                profile
                        );


        return map(
                saved
        );

    }


    @Transactional(
            readOnly = true
    )
    public MatchingProfileResponse matchingSnapshot() {

        CurrentPrincipal principal =

                currentPrincipalService
                        .require();


        CompanyProfileEntity profile =

                companyProfileRepository
                        .findByOrganizationId(
                                principal.organizationId()
                        )
                        .orElse(
                                null
                        );


        if (
                profile == null
        ) {

            return new MatchingProfileResponse(

                    principal.organizationId(),

                    null,

                    0,

                    false,

                    null,

                    null,

                    null,

                    null,

                    null,

                    null,

                    null,

                    List.of(),

                    List.of(),

                    List.of(),

                    List.of(),

                    List.of(),

                    List.of(),

                    List.of()

            );

        }


        CompanyProfileResponse.ProfileCompletenessResponse
                completeness =

                completenessCalculator
                        .calculate(
                                profile
                        );


        boolean ready =

                completeness.score()
                        >= 60

                &&
                !profile
                        .getCapabilities()
                        .isEmpty()

                &&
                !profile
                        .getOperatingRegions()
                        .isEmpty()

                &&
                !profile
                        .getTargetCpvs()
                        .isEmpty();


        return new MatchingProfileResponse(

                profile.getOrganizationId(),

                profile.getId(),

                completeness.score(),

                ready,

                profile.getIndustry(),

                profile.getCountryCode(),

                profile.getPreferredMinContractValue(),

                profile.getPreferredMaxContractValue(),

                profile.getPreferredCurrency(),

                profile.getYearsExperience(),

                profile.getMinimumNoticeDays(),

                profile.getCapabilities(),

                profile
                        .getOperatingRegions()
                        .stream()
                        .map(
                                value ->
                                        new CompanyProfileResponse
                                                .OperatingRegionResponse(

                                                        value.getCountryCode(),

                                                        value.getRegionCode(),

                                                        value.getRegionName()

                                                )
                        )
                        .toList(),

                profile
                        .getTargetCpvs()
                        .stream()
                        .map(
                                value ->
                                        new CompanyProfileResponse
                                                .TargetCpvResponse(

                                                        value.getCode(),

                                                        value.getLabel()

                                                )
                        )
                        .toList(),

                profile
                        .getCertifications()
                        .stream()
                        .map(
                                CertificationValue::getName
                        )
                        .toList(),

                profile
                        .getReferenceProjects()
                        .stream()
                        .map(
                                value ->
                                        new MatchingProfileResponse
                                                .ReferenceSignal(

                                                        value.getProjectName(),

                                                        value.getClientName(),

                                                        value.getCountryCode(),

                                                        value.getRegionName(),

                                                        value.getContractValue(),

                                                        value.getCurrency(),

                                                        value.getDescription()

                                                )
                        )
                        .toList(),

                profile.getPreferredBuyerTypes(),

                profile.getExcludedKeywords()

        );

    }


    private void validate(
            UpdateCompanyProfileRequest request
    ) {

        BigDecimal minimum =
                request.preferredMinContractValue();


        BigDecimal maximum =
                request.preferredMaxContractValue();


        if (
                minimum != null
                &&
                maximum != null
                &&
                minimum.compareTo(
                        maximum
                )
                >
                0
        ) {

            throw new InvalidRequestException(
                    "Preferred minimum contract value cannot exceed the maximum contract value"
            );

        }


        if (
                (
                        minimum != null
                        ||
                        maximum != null
                )
                &&
                cleanOptional(
                        request.preferredCurrency()
                )
                ==
                null
        ) {

            throw new InvalidRequestException(
                    "Preferred currency is required when a contract-value range is configured"
            );

        }


        for (
                UpdateCompanyProfileRequest.CertificationInput
                        certification :
                request.certifications()
        ) {

            if (
                    certification.validFrom()
                            != null

                    &&
                    certification.expiresOn()
                            != null

                    &&
                    certification
                            .validFrom()
                            .isAfter(
                                    certification.expiresOn()
                            )
            ) {

                throw new InvalidRequestException(
                        "Certification validity start date cannot be after its expiration date"
                );

            }

        }


        for (
                UpdateCompanyProfileRequest.ReferenceProjectInput
                        project :
                request.referenceProjects()
        ) {

            if (
                    project.startedOn()
                            != null

                    &&
                    project.completedOn()
                            != null

                    &&
                    project
                            .startedOn()
                            .isAfter(
                                    project.completedOn()
                            )
            ) {

                throw new InvalidRequestException(
                        "Reference project start date cannot be after its completion date"
                );

            }


            if (
                    project.contractValue()
                            != null

                    &&
                    cleanOptional(
                            project.currency()
                    )
                    ==
                    null
            ) {

                throw new InvalidRequestException(
                        "Reference project currency is required when a contract value is configured"
                );

            }

        }

    }


    private List<String> normalizeStrings(
            List<String> values
    ) {

        Map<String, String> distinct =
                new LinkedHashMap<>();


        for (
                String value :
                values
        ) {

            String cleaned =
                    cleanRequired(
                            value
                    );


            distinct.putIfAbsent(

                    cleaned
                            .toLowerCase(
                                    Locale.ROOT
                            ),

                    cleaned

            );

        }


        return List.copyOf(
                distinct.values()
        );

    }


    private List<OperatingRegionValue> normalizeRegions(
            List<UpdateCompanyProfileRequest.OperatingRegionInput>
                    values
    ) {

        Map<String, OperatingRegionValue> distinct =
                new LinkedHashMap<>();


        for (
                UpdateCompanyProfileRequest.OperatingRegionInput value :
                values
        ) {

            OperatingRegionValue normalized =

                    new OperatingRegionValue(

                            upper(
                                    value.countryCode()
                            ),

                            cleanRequired(
                                    value.regionCode()
                            )
                                    .toUpperCase(
                                            Locale.ROOT
                                    ),

                            cleanRequired(
                                    value.regionName()
                            )

                    );


            String key =

                    normalized.getCountryCode()
                            +
                            "|"
                            +
                            normalized.getRegionCode();


            distinct.putIfAbsent(
                    key,
                    normalized
            );

        }


        return List.copyOf(
                distinct.values()
        );

    }


    private List<TargetCpvValue> normalizeCpvs(
            List<UpdateCompanyProfileRequest.TargetCpvInput>
                    values
    ) {

        Map<String, TargetCpvValue> distinct =
                new LinkedHashMap<>();


        for (
                UpdateCompanyProfileRequest.TargetCpvInput value :
                values
        ) {

            String code =
                    value.code()
                            .trim();


            distinct.putIfAbsent(

                    code,

                    new TargetCpvValue(

                            code,

                            cleanRequired(
                                    value.label()
                            )

                    )

            );

        }


        return List.copyOf(
                distinct.values()
        );

    }


    private List<CertificationValue> normalizeCertifications(
            List<UpdateCompanyProfileRequest.CertificationInput>
                    values
    ) {

        List<CertificationValue> result =
                new ArrayList<>();


        for (
                UpdateCompanyProfileRequest.CertificationInput value :
                values
        ) {

            result.add(

                    new CertificationValue(

                            cleanRequired(
                                    value.name()
                            ),

                            cleanRequired(
                                    value.issuer()
                            ),

                            cleanOptional(
                                    value.certificateNumber()
                            ),

                            value.validFrom(),

                            value.expiresOn()

                    )

            );

        }


        return List.copyOf(
                result
        );

    }


    private List<ReferenceProjectValue> normalizeReferences(
            List<UpdateCompanyProfileRequest.ReferenceProjectInput>
                    values
    ) {

        List<ReferenceProjectValue> result =
                new ArrayList<>();


        for (
                UpdateCompanyProfileRequest.ReferenceProjectInput value :
                values
        ) {

            result.add(

                    new ReferenceProjectValue(

                            cleanRequired(
                                    value.projectName()
                            ),

                            cleanRequired(
                                    value.clientName()
                            ),

                            upper(
                                    value.countryCode()
                            ),

                            cleanOptional(
                                    value.regionName()
                            ),

                            value.contractValue(),

                            upperOptional(
                                    value.currency()
                            ),

                            value.startedOn(),

                            value.completedOn(),

                            cleanRequired(
                                    value.description()
                            )

                    )

            );

        }


        return List.copyOf(
                result
        );

    }


    private List<BuyerType> normalizeBuyerTypes(
            List<BuyerType> values
    ) {

        Set<BuyerType> distinct =
                new LinkedHashSet<>(
                        values
                );


        return List.copyOf(
                distinct
        );

    }


    private String normalizeWebsite(
            String website
    ) {

        String cleaned =
                cleanOptional(
                        website
                );


        if (
                cleaned == null
        ) {

            return null;

        }


        try {

            URI uri =
                    URI.create(
                            cleaned
                    );


            if (
                    !(
                            "http"
                                    .equalsIgnoreCase(
                                            uri.getScheme()
                                    )

                            ||
                            "https"
                                    .equalsIgnoreCase(
                                            uri.getScheme()
                                    )
                    )
            ) {

                throw new InvalidRequestException(
                        "Website URL must use http or https"
                );

            }


            if (
                    uri.getHost()
                            ==
                            null
            ) {

                throw new InvalidRequestException(
                        "Website URL must contain a valid host"
                );

            }


            return uri.toString();


        } catch (
                IllegalArgumentException exception
        ) {

            throw new InvalidRequestException(
                    "Website URL is invalid"
            );

        }

    }


    private CompanyProfileResponse map(
            CompanyProfileEntity profile
    ) {

        CompanyProfileResponse.ProfileCompletenessResponse
                completeness =

                completenessCalculator
                        .calculate(
                                profile
                        );


        return new CompanyProfileResponse(

                true,

                profile.getOrganizationId(),

                profile.getId(),

                profile.getLegalName(),

                profile.getTradingName(),

                profile.getIndustry(),

                profile.getDescription(),

                profile.getWebsiteUrl(),

                profile.getCountryCode(),

                profile.getHeadquartersCity(),

                profile.getEmployeeBand(),

                profile.getAnnualRevenueBand(),

                profile.getPreferredMinContractValue(),

                profile.getPreferredMaxContractValue(),

                profile.getPreferredCurrency(),

                profile.getYearsExperience(),

                profile.getMinimumNoticeDays(),

                profile.getCapabilities(),

                profile
                        .getOperatingRegions()
                        .stream()
                        .map(
                                value ->
                                        new CompanyProfileResponse
                                                .OperatingRegionResponse(

                                                        value.getCountryCode(),

                                                        value.getRegionCode(),

                                                        value.getRegionName()

                                                )
                        )
                        .toList(),

                profile
                        .getTargetCpvs()
                        .stream()
                        .map(
                                value ->
                                        new CompanyProfileResponse
                                                .TargetCpvResponse(

                                                        value.getCode(),

                                                        value.getLabel()

                                                )
                        )
                        .toList(),

                profile
                        .getCertifications()
                        .stream()
                        .map(
                                value ->
                                        new CompanyProfileResponse
                                                .CertificationResponse(

                                                        value.getName(),

                                                        value.getIssuer(),

                                                        value.getCertificateNumber(),

                                                        value.getValidFrom(),

                                                        value.getExpiresOn(),

                                                        certificationStatus(
                                                                value.getExpiresOn()
                                                        )

                                                )
                        )
                        .toList(),

                profile
                        .getReferenceProjects()
                        .stream()
                        .map(
                                value ->
                                        new CompanyProfileResponse
                                                .ReferenceProjectResponse(

                                                        value.getProjectName(),

                                                        value.getClientName(),

                                                        value.getCountryCode(),

                                                        value.getRegionName(),

                                                        value.getContractValue(),

                                                        value.getCurrency(),

                                                        value.getStartedOn(),

                                                        value.getCompletedOn(),

                                                        value.getDescription()

                                                )
                        )
                        .toList(),

                profile.getPreferredBuyerTypes(),

                profile.getExcludedKeywords(),

                completeness,

                profile.getCreatedAt(),

                profile.getUpdatedAt()

        );

    }


    private CompanyProfileResponse empty(
            UUID organizationId
    ) {

        return CompanyProfileResponse
                .empty(

                        organizationId,

                        completenessCalculator
                                .calculate(
                                        null
                                )

                );

    }


    private String certificationStatus(
            LocalDate expiresOn
    ) {

        if (
                expiresOn == null
        ) {

            return "UNKNOWN";

        }


        if (
                expiresOn.isBefore(
                        LocalDate.now()
                )
        ) {

            return "EXPIRED";

        }


        return "ACTIVE";

    }


    private String cleanRequired(
            String value
    ) {

        return value
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );

    }


    private String cleanOptional(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        String cleaned =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return cleaned.isBlank()
                ? null
                : cleaned;

    }


    private String upper(
            String value
    ) {

        return cleanRequired(
                value
        )
                .toUpperCase(
                        Locale.ROOT
                );

    }


    private String upperOptional(
            String value
    ) {

        String cleaned =
                cleanOptional(
                        value
                );


        return cleaned == null
                ? null
                : cleaned.toUpperCase(
                        Locale.ROOT
                );

    }

}
