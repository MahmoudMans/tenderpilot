package com.tenderpilot.dataoperations.api;

import com.tenderpilot.dataoperations.application.DataOperationsService;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/data-operations"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class DataOperationsController {

    private final DataOperationsService
            dataOperationsService;


    public DataOperationsController(
            DataOperationsService dataOperationsService
    ) {

        this.dataOperationsService =
                dataOperationsService;
    }


    @GetMapping
    public DataOperationsResponse overview(

            @RequestParam(
                    required = false
            )
            Integer historyLimit

    ) {

        return dataOperationsService
                .overview(
                        historyLimit
                );
    }

}
