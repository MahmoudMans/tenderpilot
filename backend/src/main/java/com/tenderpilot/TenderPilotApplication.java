package com.tenderpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TenderPilotApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                TenderPilotApplication.class,
                args
        );

    }

}
