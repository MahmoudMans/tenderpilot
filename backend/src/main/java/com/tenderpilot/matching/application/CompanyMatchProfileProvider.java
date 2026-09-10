package com.tenderpilot.matching.application;

import com.tenderpilot.matching.domain.CompanyMatchProfile;

import java.util.UUID;

public interface CompanyMatchProfileProvider {

    CompanyMatchProfile load(
            UUID organizationId
    );

}
