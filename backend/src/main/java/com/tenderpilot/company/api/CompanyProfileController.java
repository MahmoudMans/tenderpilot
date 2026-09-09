package com.tenderpilot.company.api;

import com.tenderpilot.company.application.CompanyProfileService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/company/profile"
)
public class CompanyProfileController {

    private final CompanyProfileService
            companyProfileService;


    public CompanyProfileController(
            CompanyProfileService companyProfileService
    ) {

        this.companyProfileService =
                companyProfileService;

    }


    @GetMapping
    public CompanyProfileResponse current() {

        return companyProfileService
                .current();

    }


    @PutMapping
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public CompanyProfileResponse update(

            @Valid
            @RequestBody
            UpdateCompanyProfileRequest request

    ) {

        return companyProfileService
                .upsert(
                        request
                );

    }


    @GetMapping(
            "/matching-snapshot"
    )
    public MatchingProfileResponse matchingSnapshot() {

        return companyProfileService
                .matchingSnapshot();

    }

}
