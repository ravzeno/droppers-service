package com.ravzeno.droppers_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class DroppersApplication {
    public static void main(String[] args) {
        SpringApplication.run(DroppersApplication.class, args);
    }
}
