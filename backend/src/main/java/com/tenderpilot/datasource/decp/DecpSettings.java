package com.tenderpilot.datasource.decp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DecpSettings {

    private final String baseUrl;
    private final String recordsPath;
    private final String datasetId;
    private final int defaultLookbackDays;
    private final int defaultLimit;
    private final int maxLimit;
    private final boolean manualSyncEnabled;

    public DecpSettings(
            @Value(
                    "${tenderpilot.decp.base-url:https://data.economie.gouv.fr}"
            )
            String baseUrl,

            @Value(
                    "${tenderpilot.decp.records-path:/api/explore/v2.1/catalog/datasets/decp-2022-marches-valides/records}"
            )
            String recordsPath,

            @Value(
                    "${tenderpilot.decp.dataset-id:decp-2022-marches-valides}"
            )
            String datasetId,

            @Value(
                    "${tenderpilot.decp.default-lookback-days:7}"
            )
            int defaultLookbackDays,

            @Value(
                    "${tenderpilot.decp.default-limit:25}"
            )
            int defaultLimit,

            @Value(
                    "${tenderpilot.decp.max-limit:100}"
            )
            int maxLimit,

            @Value(
                    "${tenderpilot.decp.manual-sync-enabled:true}"
            )
            boolean manualSyncEnabled
    ) {

        this.baseUrl =
                removeTrailingSlash(baseUrl);

        this.recordsPath =
                normalizePath(recordsPath);

        this.datasetId =
                datasetId.trim();

        this.defaultLookbackDays =
                Math.max(1, defaultLookbackDays);

        this.defaultLimit =
                Math.max(1, defaultLimit);

        this.maxLimit =
                Math.max(
                        1,
                        Math.min(100, maxLimit)
                );

        this.manualSyncEnabled =
                manualSyncEnabled;
    }

    private String removeTrailingSlash(
            String value
    ) {

        String cleaned = value.trim();

        while (cleaned.endsWith("/")) {
            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 1
                    );
        }

        return cleaned;
    }

    private String normalizePath(
            String value
    ) {

        String cleaned = value.trim();

        return cleaned.startsWith("/")
                ? cleaned
                : "/" + cleaned;
    }

    public String datasetPageUrl() {

        return baseUrl
                +
                "/explore/dataset/"
                +
                datasetId
                +
                "/";
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String recordsPath() {
        return recordsPath;
    }

    public String datasetId() {
        return datasetId;
    }

    public int defaultLookbackDays() {
        return defaultLookbackDays;
    }

    public int defaultLimit() {
        return defaultLimit;
    }

    public int maxLimit() {
        return maxLimit;
    }

    public boolean manualSyncEnabled() {
        return manualSyncEnabled;
    }
}
