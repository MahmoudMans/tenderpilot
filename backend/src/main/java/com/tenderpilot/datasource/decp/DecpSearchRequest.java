package com.tenderpilot.datasource.decp;

public record DecpSearchRequest(
        String where,
        int offset,
        int limit
) {
}
