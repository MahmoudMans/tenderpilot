package com.tenderpilot.datasource.api;

import com.tenderpilot.datasource.application.BoampDataSourceService;
import com.tenderpilot.datasource.application.BoampIngestionService;

import com.tenderpilot.datasource.application.DecpDataSourceService;
import com.tenderpilot.datasource.application.DecpIngestionService;

import com.tenderpilot.datasource.application.TedDataSourceService;
import com.tenderpilot.datasource.application.TedIngestionService;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data-sources")
public class DataSourceController {

    private final TedDataSourceService
            tedDataSourceService;

    private final TedIngestionService
            tedIngestionService;

    private final BoampDataSourceService
            boampDataSourceService;

    private final BoampIngestionService
            boampIngestionService;

    private final DecpDataSourceService
            decpDataSourceService;

    private final DecpIngestionService
            decpIngestionService;

    public DataSourceController(
            TedDataSourceService tedDataSourceService,
            TedIngestionService tedIngestionService,
            BoampDataSourceService boampDataSourceService,
            BoampIngestionService boampIngestionService,
            DecpDataSourceService decpDataSourceService,
            DecpIngestionService decpIngestionService
    ) {

        this.tedDataSourceService =
                tedDataSourceService;

        this.tedIngestionService =
                tedIngestionService;

        this.boampDataSourceService =
                boampDataSourceService;

        this.boampIngestionService =
                boampIngestionService;

        this.decpDataSourceService =
                decpDataSourceService;

        this.decpIngestionService =
                decpIngestionService;
    }

    @GetMapping("/ted")
    public TedDataSourceStatusResponse tedStatus() {
        return tedDataSourceService.status();
    }

    @PostMapping("/ted/sync")
    @PreAuthorize("hasRole('OWNER')")
    public TedSyncResponse syncTed(
            @RequestParam(defaultValue = "7")
            int days,

            @RequestParam(defaultValue = "25")
            int limit
    ) {

        return tedIngestionService
                .syncRecent(days, limit);
    }

    @GetMapping("/boamp")
    public BoampDataSourceStatusResponse
    boampStatus() {
        return boampDataSourceService.status();
    }

    @PostMapping("/boamp/sync")
    @PreAuthorize("hasRole('OWNER')")
    public BoampSyncResponse syncBoamp(
            @RequestParam(defaultValue = "7")
            int days,

            @RequestParam(defaultValue = "25")
            int limit
    ) {

        return boampIngestionService
                .syncRecent(days, limit);
    }

    @GetMapping("/decp")
    public DecpDataSourceStatusResponse
    decpStatus() {
        return decpDataSourceService.status();
    }

    @PostMapping("/decp/sync")
    @PreAuthorize("hasRole('OWNER')")
    public DecpSyncResponse syncDecp(
            @RequestParam(defaultValue = "7")
            int days,

            @RequestParam(defaultValue = "25")
            int limit
    ) {

        return decpIngestionService
                .syncRecent(days, limit);
    }
}
