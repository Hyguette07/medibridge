package com.hyguettelabs.medibridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MediBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediBridgeApplication.class, args);
    }
}
