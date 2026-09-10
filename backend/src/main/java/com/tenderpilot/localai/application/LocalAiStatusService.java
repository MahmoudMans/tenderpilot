package com.tenderpilot.localai.application;

import com.tenderpilot.localai.api.LocalAiStatusResponse;

import org.springframework.stereotype.Service;

@Service
public class LocalAiStatusService {

    private final OllamaGateway
            ollamaGateway;


    public LocalAiStatusService(
            OllamaGateway ollamaGateway
    ) {

        this.ollamaGateway =
                ollamaGateway;
    }


    public LocalAiStatusResponse status() {

        OllamaGateway.OllamaStatus status =
                ollamaGateway.status();


        return new LocalAiStatusResponse(
                status.enabled(),
                status.reachable(),
                status.modelAvailable(),
                status.provider(),
                status.model(),
                status.baseUrl(),
                "LOCAL_ONLY",
                "ORGANIZATION_PRIVATE"
        );
    }

}
