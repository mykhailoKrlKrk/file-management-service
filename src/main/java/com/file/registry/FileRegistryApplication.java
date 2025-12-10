package com.file.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileRegistryApplication.class, args);
    }
}
