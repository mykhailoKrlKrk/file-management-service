package com.file.registry.listener;

import com.file.registry.exception.InternalErrorException;
import com.file.registry.properties.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StorageInitListener implements ApplicationListener<ApplicationReadyEvent> {

    private final Path storagePath;

    public StorageInitListener(StorageProperties storageProperties) {
        this.storagePath = Paths.get(storageProperties.getPath());
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new InternalErrorException("Failed to create storage directory: ", e);
        }
    }
}
