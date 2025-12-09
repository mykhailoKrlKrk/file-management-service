package com.file.registry.service;

import static com.file.registry.constants.ApplicationConstants.JSON_EXTENSION;
import static com.file.registry.constants.ApplicationConstants.XML_EXTENSION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.file.registry.exception.ConflictException;
import com.file.registry.exception.FileProcessingException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileManagementService {

  private final Path storagePath;
  private final XmlMapper xmlMapper;
  private final ObjectMapper jsonMapper;

  public FileManagementService(
      @Value("${app.file-storage.path}")
      String storagePath,
      XmlMapper xmlMapper,
      @Qualifier("jsonObjectMapper") ObjectMapper jsonMapper) {
    this.storagePath = Paths.get(storagePath);
    this.xmlMapper = xmlMapper;
    this.jsonMapper = jsonMapper;
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(storagePath);
    } catch (IOException e) {
      throw new FileProcessingException("Failed to create storage directory: ", e);
    }
  }

  public void upload(MultipartFile file) {
    String fileName =
        file.getOriginalFilename().replaceAll(XML_EXTENSION, JSON_EXTENSION);
    Path filePath = storagePath.resolve(fileName);

    if (Files.exists(filePath)) {
      throw new ConflictException("Failed: file with provided name already exist!");
    }

    try {
      JsonNode xmlTree = xmlMapper.readTree(file.getInputStream());
      String jsonContent = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlTree);

      Path jsonPath = storagePath.resolve(fileName);
      Files.writeString(jsonPath, jsonContent);
      log.info("Successfully uploaded file: {}", fileName);
    } catch (IOException e) {
      log.error("Failed to upload file: {}", fileName, e);
      throw new FileProcessingException("Failed to process file: ", e);
    }
  }
}
