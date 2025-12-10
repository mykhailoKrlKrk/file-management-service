package com.file.registry.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app.file-storage")
public class StorageProperties {

    private String path;
}
