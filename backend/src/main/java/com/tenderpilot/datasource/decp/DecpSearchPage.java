package com.tenderpilot.datasource.decp;

import java.util.List;

public record DecpSearchPage(
        long total,
        List<DecpAwardRecord> records
) {
}
