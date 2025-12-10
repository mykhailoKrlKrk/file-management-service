package com.file.registry.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.file.registry.exception.InternalErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StorageInitListener implements ApplicationListener<ApplicationReadyEvent> {

  private final Path storagePath;

  public StorageInitListener(@Value("${app.file-storage.path}") String storagePath) {
    this.storagePath = Paths.get(storagePath);
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