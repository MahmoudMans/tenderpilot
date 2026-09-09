package com.tenderpilot.datasource.ted;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class TedSettings {

    private final String baseUrl;

    private final String searchPath;

    private final int defaultLookbackDays;

    private final int defaultLimit;

    private final int maxLimit;

    private final boolean manualSyncEnabled;


    public TedSettings(

            @Value(
                    "${tenderpilot.ted.base-url:https://api.ted.europa.eu}"
            )
            String baseUrl,


            @Value(
                    "${tenderpilot.ted.search-path:/v3/notices/search}"
            )
            String searchPath,


            @Value(
                    "${tenderpilot.ted.default-lookback-days:7}"
            )
            int defaultLookbackDays,


            @Value(
                    "${tenderpilot.ted.default-limit:25}"
            )
            int defaultLimit,


            @Value(
                    "${tenderpilot.ted.max-limit:100}"
            )
            int maxLimit,


            @Value(
                    "${tenderpilot.ted.manual-sync-enabled:true}"
            )
            boolean manualSyncEnabled

    ) {

        this.baseUrl =
                stripTrailingSlash(
                        baseUrl
                );

        this.searchPath =
                normalizePath(
                        searchPath
                );

        this.defaultLookbackDays =
                Math.max(
                        1,
                        defaultLookbackDays
                );

        this.defaultLimit =
                Math.max(
                        1,
                        defaultLimit
                );

        this.maxLimit =
                Math.max(
                        1,
                        maxLimit
                );

        this.manualSyncEnabled =
                manualSyncEnabled;

    }


    private String stripTrailingSlash(
            String value
    ) {

        String trimmed =
                value.trim();


        while (
                trimmed.endsWith(
                        "/"
                )
        ) {

            trimmed =
                    trimmed.substring(
                            0,
                            trimmed.length()
                                    -
                                    1
                    );

        }


        return trimmed;

    }


    private String normalizePath(
            String value
    ) {

        String trimmed =
                value.trim();


        return trimmed.startsWith(
                "/"
        )
                ? trimmed
                : "/" + trimmed;

    }


    public String baseUrl() {

        return baseUrl;

    }


    public String searchPath() {

        return searchPath;

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
