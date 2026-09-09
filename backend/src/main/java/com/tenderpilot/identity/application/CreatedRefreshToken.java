package com.tenderpilot.identity.application;

import com.tenderpilot.identity.domain.RefreshTokenEntity;

public record CreatedRefreshToken(

        String rawToken,

        RefreshTokenEntity entity

) {
}
