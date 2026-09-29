package com.thantzin.thantflash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ThantFlashApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThantFlashApplication.class, args);
    }
}
