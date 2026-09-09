package com.tenderpilot.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.company.api.CompanyProfileResponse;
import com.tenderpilot.company.api.MatchingProfileResponse;
import com.tenderpilot.company.api.UpdateCompanyProfileRequest;

import com.tenderpilot.company.application.CompanyProfileService;

import com.tenderpilot.company.domain.AnnualRevenueBand;
import com.tenderpilot.company.domain.BuyerType;
import com.tenderpilot.company.domain.EmployeeBand;

import com.tenderpilot.identity.api.RegisterRequest;

import com.tenderpilot.identity.application.AuthenticationService;
import com.tenderpilot.identity.application.IssuedSession;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles(
        "test"
)
@Transactional
class CompanyProfileIntegrationTest {

    @Autowired
    private AuthenticationService
            authenticationService;


    @Autowired
    private CompanyProfileService
            companyProfileService;


    @Autowired
    private JwtDecoder
            jwtDecoder;


    @AfterEach
    void cleanSecurityContext() {

        SecurityContextHolder
                .clearContext();

    }


    @Test
    void newOrganizationStartsWithEmptyCompanyProfile() {

        IssuedSession session =
                register(
                        "owner-a@example.test",
                        "Aurelia Renovation"
                );


        authenticate(
                session
        );


        CompanyProfileResponse profile =

                companyProfileService
                        .current();


        assertThat(
                profile.exists()
        )
                .isFalse();


        assertThat(
                profile.completeness()
                        .score()
        )
                .isZero();

    }


    @Test
    void completeProfileProducesMatchingReadySnapshot() {

        IssuedSession session =
                register(
                        "owner-b@example.test",
                        "Aurelia Renovation"
                );


        authenticate(
                session
        );


        CompanyProfileResponse profile =

                companyProfileService
                        .upsert(
                                completeRequest()
                        );


        assertThat(
                profile.exists()
        )
                .isTrue();


        assertThat(
                profile.completeness()
                        .score()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                profile.capabilities()
        )
                .containsExactly(

                        "Energy renovation",

                        "Insulation",

                        "HVAC",

                        "Roofing",

                        "Interior renovation"

                );


        MatchingProfileResponse snapshot =

                companyProfileService
                        .matchingSnapshot();


        assertThat(
                snapshot.readyForMatching()
        )
                .isTrue();


        assertThat(
                snapshot.completenessScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                snapshot.targetCpvs()
        )
                .extracting(
                        CompanyProfileResponse
                                .TargetCpvResponse
                                ::code
                )
                .contains(
                        "45000000",
                        "45453000"
                );

    }


    @Test
    void companyProfilesAreStrictlyTenantIsolated() {

        IssuedSession organizationA =
                register(
                        "owner-c@example.test",
                        "Aurelia Renovation"
                );


        IssuedSession organizationB =
                register(
                        "owner-d@example.test",
                        "Boreal Construction"
                );


        authenticate(
                organizationA
        );


        companyProfileService
                .upsert(
                        completeRequest()
                );


        CompanyProfileResponse profileA =

                companyProfileService
                        .current();


        authenticate(
                organizationB
        );


        CompanyProfileResponse profileB =

                companyProfileService
                        .current();


        assertThat(
                profileA.exists()
        )
                .isTrue();


        assertThat(
                profileB.exists()
        )
                .isFalse();


        assertThat(
                profileB.organizationId()
        )
                .isEqualTo(
                        organizationB
                                .response()
                                .user()
                                .organization()
                                .id()
                );

    }


    @Test
    void invalidContractRangeIsRejected() {

        IssuedSession session =
                register(
                        "owner-e@example.test",
                        "Aurelia Renovation"
                );


        authenticate(
                session
        );


        UpdateCompanyProfileRequest valid =
                completeRequest();


        UpdateCompanyProfileRequest invalid =

                new UpdateCompanyProfileRequest(

                        valid.legalName(),

                        valid.tradingName(),

                        valid.industry(),

                        valid.description(),

                        valid.websiteUrl(),

                        valid.countryCode(),

                        valid.headquartersCity(),

                        valid.employeeBand(),

                        valid.annualRevenueBand(),

                        new BigDecimal(
                                "9000000"
                        ),

                        new BigDecimal(
                                "1000000"
                        ),

                        valid.preferredCurrency(),

                        valid.yearsExperience(),

                        valid.minimumNoticeDays(),

                        valid.capabilities(),

                        valid.operatingRegions(),

                        valid.targetCpvs(),

                        valid.certifications(),

                        valid.referenceProjects(),

                        valid.preferredBuyerTypes(),

                        valid.excludedKeywords()

                );


        assertThatThrownBy(
                () ->
                        companyProfileService
                                .upsert(
                                        invalid
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                )
                .hasMessageContaining(
                        "minimum contract value"
                );

    }


    private IssuedSession register(

            String email,

            String organizationName

    ) {

        return authenticationService
                .register(
                        new RegisterRequest(

                                "Portfolio Owner",

                                organizationName,

                                email,

                                "VeryStrongPassword123!"

                        )
                );

    }


    private void authenticate(
            IssuedSession session
    ) {

        Jwt jwt =

                jwtDecoder
                        .decode(
                                session
                                        .response()
                                        .accessToken()
                        );


        String role =
                jwt.getClaimAsString(
                        "role"
                );


        JwtAuthenticationToken authentication =

                new JwtAuthenticationToken(

                        jwt,

                        List.of(

                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )

                        )

                );


        SecurityContext context =

                SecurityContextHolder
                        .createEmptyContext();


        context.setAuthentication(
                authentication
        );


        SecurityContextHolder
                .setContext(
                        context
                );

    }


    private UpdateCompanyProfileRequest completeRequest() {

        return new UpdateCompanyProfileRequest(

                "Aurelia Renovation SAS",

                "Aurelia Renovation",

                "Building renovation",

                "Public-building renovation specialist focused on energy efficiency and occupied-site projects.",

                "https://example.com",

                "FR",

                "Paris",

                EmployeeBand.FIFTY_TO_TWO_FORTY_NINE,

                AnnualRevenueBand.FROM_10M_TO_50M,

                new BigDecimal(
                        "250000"
                ),

                new BigDecimal(
                        "6000000"
                ),

                "EUR",

                18,

                14,

                List.of(

                        "Energy renovation",

                        "Insulation",

                        "HVAC",

                        "Roofing",

                        "Interior renovation"

                ),

                List.of(

                        new UpdateCompanyProfileRequest
                                .OperatingRegionInput(

                                        "FR",

                                        "IDF",

                                        "Île-de-France"

                                ),

                        new UpdateCompanyProfileRequest
                                .OperatingRegionInput(

                                        "FR",

                                        "HDF",

                                        "Hauts-de-France"

                                )

                ),

                List.of(

                        new UpdateCompanyProfileRequest
                                .TargetCpvInput(

                                        "45000000",

                                        "Construction work"

                                ),

                        new UpdateCompanyProfileRequest
                                .TargetCpvInput(

                                        "45453000",

                                        "Overhaul and refurbishment work"

                                ),

                        new UpdateCompanyProfileRequest
                                .TargetCpvInput(

                                        "45321000",

                                        "Thermal insulation work"

                                ),

                        new UpdateCompanyProfileRequest
                                .TargetCpvInput(

                                        "45331000",

                                        "Heating ventilation and air-conditioning installation work"

                                ),

                        new UpdateCompanyProfileRequest
                                .TargetCpvInput(

                                        "45260000",

                                        "Roof works and other special trade construction works"

                                )

                ),

                List.of(

                        new UpdateCompanyProfileRequest
                                .CertificationInput(

                                        "RGE",

                                        "Qualibat",

                                        "DEMO-RGE-001",

                                        LocalDate.of(
                                                2025,
                                                1,
                                                1
                                        ),

                                        LocalDate.of(
                                                2028,
                                                12,
                                                31
                                        )

                                ),

                        new UpdateCompanyProfileRequest
                                .CertificationInput(

                                        "ISO 9001",

                                        "Demo Certification Body",

                                        "DEMO-ISO-001",

                                        LocalDate.of(
                                                2025,
                                                1,
                                                1
                                        ),

                                        LocalDate.of(
                                                2028,
                                                12,
                                                31
                                        )

                                )

                ),

                List.of(

                        new UpdateCompanyProfileRequest
                                .ReferenceProjectInput(

                                        "Victor Hugo School Renovation",

                                        "Demo Municipality",

                                        "FR",

                                        "Île-de-France",

                                        new BigDecimal(
                                                "1800000"
                                        ),

                                        "EUR",

                                        LocalDate.of(
                                                2024,
                                                2,
                                                1
                                        ),

                                        LocalDate.of(
                                                2025,
                                                1,
                                                31
                                        ),

                                        "Occupied-school energy renovation involving insulation, HVAC modernization and interior works."

                                ),

                        new UpdateCompanyProfileRequest
                                .ReferenceProjectInput(

                                        "Republic Residence Rehabilitation",

                                        "Demo Housing Office",

                                        "FR",

                                        "Île-de-France",

                                        new BigDecimal(
                                                "3100000"
                                        ),

                                        "EUR",

                                        LocalDate.of(
                                                2023,
                                                3,
                                                1
                                        ),

                                        LocalDate.of(
                                                2024,
                                                4,
                                                30
                                        ),

                                        "Residential rehabilitation involving envelope insulation, roofing and occupied-site coordination."

                                ),

                        new UpdateCompanyProfileRequest
                                .ReferenceProjectInput(

                                        "Municipal Building Retrofit",

                                        "Demo Public Agency",

                                        "FR",

                                        "Hauts-de-France",

                                        new BigDecimal(
                                                "2200000"
                                        ),

                                        "EUR",

                                        LocalDate.of(
                                                2022,
                                                6,
                                                1
                                        ),

                                        LocalDate.of(
                                                2023,
                                                5,
                                                31
                                        ),

                                        "Public-building retrofit covering HVAC, energy performance and interior refurbishment."

                                )

                ),

                List.of(

                        BuyerType.LOCAL_AUTHORITY,

                        BuyerType.EDUCATION,

                        BuyerType.SOCIAL_HOUSING

                ),

                List.of(

                        "nuclear",

                        "marine construction"

                )

        );

    }

}
