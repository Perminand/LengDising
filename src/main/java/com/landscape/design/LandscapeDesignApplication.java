package com.landscape.design;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LandscapeDesignApplication {

    public static void main(String[] args) {
        SpringApplication.run(LandscapeDesignApplication.class, args);
    }
}
