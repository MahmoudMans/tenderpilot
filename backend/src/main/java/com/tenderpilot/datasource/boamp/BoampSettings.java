package com.tenderpilot.datasource.boamp;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class BoampSettings {

    private final String baseUrl;

    private final String recordsPath;

    private final String datasetId;

    private final int defaultLookbackDays;

    private final int defaultLimit;

    private final int maxLimit;

    private final boolean manualSyncEnabled;


    public BoampSettings(

            @Value(
                    "${tenderpilot.boamp.base-url:https://boamp-datadila.opendatasoft.com}"
            )
            String baseUrl,


            @Value(
                    "${tenderpilot.boamp.records-path:/api/explore/v2.1/catalog/datasets/boamp/records}"
            )
            String recordsPath,


            @Value(
                    "${tenderpilot.boamp.dataset-id:boamp}"
            )
            String datasetId,


            @Value(
                    "${tenderpilot.boamp.default-lookback-days:7}"
            )
            int defaultLookbackDays,


            @Value(
                    "${tenderpilot.boamp.default-limit:25}"
            )
            int defaultLimit,


            @Value(
                    "${tenderpilot.boamp.max-limit:100}"
            )
            int maxLimit,


            @Value(
                    "${tenderpilot.boamp.manual-sync-enabled:true}"
            )
            boolean manualSyncEnabled

    ) {

        this.baseUrl =
                stripTrailingSlash(
                        baseUrl
                );

        this.recordsPath =
                normalizePath(
                        recordsPath
                );

        this.datasetId =
                datasetId.trim();

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

                        Math.min(
                                100,
                                maxLimit
                        )

                );

        this.manualSyncEnabled =
                manualSyncEnabled;

    }


    private String stripTrailingSlash(
            String value
    ) {

        String result =
                value.trim();


        while (
                result.endsWith(
                        "/"
                )
        ) {

            result =
                    result.substring(
                            0,
                            result.length()
                                    -
                                    1
                    );

        }


        return result;

    }


    private String normalizePath(
            String value
    ) {

        String result =
                value.trim();


        return result.startsWith(
                "/"
        )
                ? result
                : "/" + result;

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
