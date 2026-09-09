package com.tenderpilot.datasource.api;

import com.tenderpilot.datasource.application.TedDataSourceService;
import com.tenderpilot.datasource.application.TedIngestionService;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/data-sources"
)
public class DataSourceController {

    private final TedDataSourceService
            tedDataSourceService;


    private final TedIngestionService
            tedIngestionService;


    public DataSourceController(

            TedDataSourceService tedDataSourceService,

            TedIngestionService tedIngestionService

    ) {

        this.tedDataSourceService =
                tedDataSourceService;

        this.tedIngestionService =
                tedIngestionService;

    }


    @GetMapping(
            "/ted"
    )
    public TedDataSourceStatusResponse tedStatus() {

        return tedDataSourceService
                .status();

    }


    @PostMapping(
            "/ted/sync"
    )
    @PreAuthorize(
            "hasRole('OWNER')"
    )
    public TedSyncResponse syncTed(

            @RequestParam(
                    defaultValue = "7"
            )
            int days,


            @RequestParam(
                    defaultValue = "25"
            )
            int limit

    ) {

        return tedIngestionService
                .syncRecent(

                        days,

                        limit

                );

    }

}
