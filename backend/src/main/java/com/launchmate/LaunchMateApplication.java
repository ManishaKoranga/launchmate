package com.launchmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LaunchMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaunchMateApplication.class, args);
    }
}
