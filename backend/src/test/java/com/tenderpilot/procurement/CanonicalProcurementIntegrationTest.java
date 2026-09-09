package com.tenderpilot.procurement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.identity.api.RegisterRequest;

import com.tenderpilot.identity.application.AuthenticationService;
import com.tenderpilot.identity.application.IssuedSession;

import com.tenderpilot.procurement.api.OpportunityDetailResponse;
import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;
import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.procurement.infrastructure.TenderRepository;
import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

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
class CanonicalProcurementIntegrationTest {

    @Autowired
    private CanonicalTenderImportService
            importService;


    @Autowired
    private OpportunityQueryService
            queryService;


    @Autowired
    private TenderRepository
            tenderRepository;


    @Autowired
    private TenderSourceRecordRepository
            sourceRecordRepository;


    @Autowired
    private AuthenticationService
            authenticationService;


    @Autowired
    private JwtDecoder
            jwtDecoder;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder
                .clearContext();

    }


    @Test
    void importsCanonicalTenderWithBuyerLotsCpvsLocationsAndLineage() {

        UUID tenderId =

                importService
                        .upsert(
                                request(
                                        "TED-001",
                                        "Energy renovation of municipal buildings"
                                )
                        );


        OpportunityDetailResponse detail =

                queryService
                        .detail(
                                tenderId
                        );


        assertThat(
                detail.title()
        )
                .isEqualTo(
                        "Energy renovation of municipal buildings"
                );


        assertThat(
                detail.buyer()
                        .legalName()
        )
                .isEqualTo(
                        "Demo City Council"
                );


        assertThat(
                detail.cpvs()
        )
                .extracting(
                        OpportunityDetailResponse
                                .CpvResponse
                                ::code
                )
                .containsExactly(

                        "45453000",

                        "45321000"

                );


        assertThat(
                detail.locations()
        )
                .hasSize(
                        1
                );


        assertThat(
                detail.lots()
        )
                .hasSize(
                        2
                );


        assertThat(
                detail.sources()
        )
                .hasSize(
                        1
                );


        assertThat(
                detail.sources()
                        .getFirst()
                        .source()
        )
                .isEqualTo(
                        ProcurementSource.TED
                );

    }


    @Test
    void sameSourceExternalIdentifierUpdatesExistingTender() {

        UUID firstId =

                importService
                        .upsert(
                                request(
                                        "TED-002",
                                        "Initial procurement title"
                                )
                        );


        UUID secondId =

                importService
                        .upsert(
                                request(
                                        "TED-002",
                                        "Updated procurement title"
                                )
                        );


        assertThat(
                secondId
        )
                .isEqualTo(
                        firstId
                );


        assertThat(
                tenderRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                sourceRecordRepository.count()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                queryService
                        .detail(
                                firstId
                        )
                        .title()
        )
                .isEqualTo(
                        "Updated procurement title"
                );

    }


    @Test
    void opportunitySearchFiltersByCpvAndSource() {

        importService
                .upsert(
                        request(
                                "TED-003",
                                "Public school refurbishment"
                        )
                );


        List<OpportunitySummaryResponse> cpvMatches =

                queryService
                        .list(

                                "45453000",

                                ProcurementStatus.PUBLISHED,

                                ProcurementSource.TED

                        );


        assertThat(
                cpvMatches
        )
                .hasSize(
                        1
                );


        assertThat(
                cpvMatches
                        .getFirst()
                        .sources()
        )
                .containsExactly(
                        ProcurementSource.TED
                );


        List<OpportunitySummaryResponse> wrongSource =

                queryService
                        .list(

                                "45453000",

                                null,

                                ProcurementSource.BOAMP

                        );


        assertThat(
                wrongSource
        )
                .isEmpty();

    }


    @Test
    void canonicalPublicOpportunityIsSharedAcrossOrganizations() {

        UUID tenderId =

                importService
                        .upsert(
                                request(
                                        "TED-004",
                                        "Shared public opportunity"
                                )
                        );


        IssuedSession organizationA =

                register(
                        "procurement-a@example.test",
                        "Organization A"
                );


        IssuedSession organizationB =

                register(
                        "procurement-b@example.test",
                        "Organization B"
                );


        authenticate(
                organizationA
        );


        List<UUID> organizationAIds =

                queryService
                        .list(
                                "Shared public opportunity",
                                null,
                                null
                        )
                        .stream()
                        .map(
                                OpportunitySummaryResponse::id
                        )
                        .toList();


        authenticate(
                organizationB
        );


        List<UUID> organizationBIds =

                queryService
                        .list(
                                "Shared public opportunity",
                                null,
                                null
                        )
                        .stream()
                        .map(
                                OpportunitySummaryResponse::id
                        )
                        .toList();


        assertThat(
                organizationAIds
        )
                .contains(
                        tenderId
                );


        assertThat(
                organizationBIds
        )
                .contains(
                        tenderId
                );

    }


    @Test
    void invalidTenderValueRangeIsRejected() {

        CanonicalTenderUpsertRequest valid =

                request(
                        "TED-005",
                        "Invalid range example"
                );


        CanonicalTenderUpsertRequest invalid =

                new CanonicalTenderUpsertRequest(

                        valid.source(),

                        valid.externalId(),

                        valid.externalVersion(),

                        valid.noticeType(),

                        valid.sourceUrl(),

                        valid.payloadChecksum(),

                        valid.sourcePublishedAt(),

                        valid.buyer(),

                        valid.title(),

                        valid.description(),

                        valid.status(),

                        valid.procedureType(),

                        valid.contractNature(),

                        valid.publicationDate(),

                        valid.submissionDeadline(),

                        new BigDecimal(
                                "9000000"
                        ),

                        new BigDecimal(
                                "1000000"
                        ),

                        valid.currency(),

                        valid.countryCode(),

                        valid.primaryRegionCode(),

                        valid.primaryRegionName(),

                        valid.frameworkAgreement(),

                        valid.jointProcurement(),

                        valid.electronicSubmission(),

                        valid.cpvs(),

                        valid.locations(),

                        valid.lots()

                );


        assertThatThrownBy(
                () ->
                        importService
                                .upsert(
                                        invalid
                                )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                )
                .hasMessageContaining(
                        "minimum estimated value"
                );

    }


    private CanonicalTenderUpsertRequest request(

            String externalId,

            String title

    ) {

        return new CanonicalTenderUpsertRequest(

                ProcurementSource.TED,

                externalId,

                "1",

                "CONTRACT_NOTICE",

                "https://example.test/notices/" + externalId,

                "demo-checksum-" + externalId,

                Instant.parse(
                        "2026-09-08T09:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(

                                "FR-BUYER-DEMO-CITY",

                                "Demo City Council",

                                BuyerCategory.LOCAL_AUTHORITY,

                                "FR",

                                "Paris",

                                "BUYER-DEMO-001",

                                "https://example.test/buyer"

                        ),

                title,

                "Comprehensive energy-efficiency renovation of public buildings.",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                ContractNature.WORKS,

                LocalDate.of(
                        2026,
                        9,
                        8
                ),

                Instant.parse(
                        "2026-10-20T16:00:00Z"
                ),

                new BigDecimal(
                        "1500000"
                ),

                new BigDecimal(
                        "4500000"
                ),

                "EUR",

                "FR",

                "IDF",

                "Île-de-France",

                false,

                false,

                true,

                List.of(

                        new CanonicalTenderUpsertRequest
                                .CpvInput(

                                        "45453000",

                                        "Overhaul and refurbishment work"

                                ),

                        new CanonicalTenderUpsertRequest
                                .CpvInput(

                                        "45321000",

                                        "Thermal insulation work"

                                )

                ),

                List.of(

                        new CanonicalTenderUpsertRequest
                                .LocationInput(

                                        "FR",

                                        "IDF",

                                        "Île-de-France",

                                        "Paris"

                                )

                ),

                List.of(

                        new CanonicalTenderUpsertRequest
                                .LotInput(

                                        "1",

                                        "Building envelope",

                                        "Insulation, façades and roofing works.",

                                        new BigDecimal(
                                                "800000"
                                        ),

                                        new BigDecimal(
                                                "2200000"
                                        ),

                                        "EUR",

                                        List.of(

                                                new CanonicalTenderUpsertRequest
                                                        .CpvInput(

                                                                "45321000",

                                                                "Thermal insulation work"

                                                        )

                                        )

                                ),

                        new CanonicalTenderUpsertRequest
                                .LotInput(

                                        "2",

                                        "Interior refurbishment",

                                        "Interior renovation and finishing works.",

                                        new BigDecimal(
                                                "700000"
                                        ),

                                        new BigDecimal(
                                                "2300000"
                                        ),

                                        "EUR",

                                        List.of(

                                                new CanonicalTenderUpsertRequest
                                                        .CpvInput(

                                                                "45453000",

                                                                "Overhaul and refurbishment work"

                                                        )

                                        )

                                )

                )

        );

    }


    private IssuedSession register(

            String email,

            String organization

    ) {

        return authenticationService
                .register(
                        new RegisterRequest(

                                "Procurement Reader",

                                organization,

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

}
