package com.file.registry.service;

import static com.file.registry.constants.ApplicationConstants.FILE_NAME_SPLITTER;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.CUSTOMER_INDEX_NAME;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.CUSTOMER_INDEX_POSITION;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.DATE_INDEX_NAME;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.DATE_INDEX_POSITION;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.TYPE_INDEX_NAME;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.TYPE_INDEX_POSITION;
import static com.file.registry.constants.ApplicationConstants.JSON_EXTENSION;
import static com.file.registry.constants.ApplicationConstants.XML_EXTENSION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.file.registry.exception.ConflictException;
import com.file.registry.exception.InternalErrorException;
import com.file.registry.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

  public Resource upload(MultipartFile file) {
    String fileName =
        file.getOriginalFilename().replaceAll(XML_EXTENSION, JSON_EXTENSION);
    Path filePath = storagePath.resolve(fileName);

    if (Files.exists(filePath)) {
      throw new ConflictException("Failed: file with provided name already exist!");
    }
    log.info("Successfully uploaded file: {}", fileName);
    return save(file, fileName);
  }

  public Resource update(MultipartFile file) {
    String fileName =
        file.getOriginalFilename().replaceAll(XML_EXTENSION, JSON_EXTENSION);
    log.info("Successfully updated file: {}", fileName);
    return save(file, fileName);
  }

  public Resource getByName(final String fileName) {
    Path filePath =
        storagePath.resolve(fileName.replaceAll(XML_EXTENSION, JSON_EXTENSION));

    if (!Files.exists(filePath)) {
      throw new NotFoundException("File not found: " + fileName);
    }
    return new FileSystemResource(filePath);
  }

  public List<String> getFilesByDate(final LocalDate date) {
    Path dateDirectory = storagePath.resolve(DATE_INDEX_NAME).resolve(date.toString());
    return getFilesByDirectory(DATE_INDEX_NAME, dateDirectory);
  }

  public List<String> getFilesByCustomer(final String customerName) {
    Path customerDirectory = storagePath.resolve(CUSTOMER_INDEX_NAME).resolve(customerName);
    return getFilesByDirectory(CUSTOMER_INDEX_NAME, customerDirectory);
  }

  public List<String> getFilesByType(final String type) {
    Path typeDirectory = storagePath.resolve(TYPE_INDEX_NAME).resolve(type);
    return getFilesByDirectory(TYPE_INDEX_NAME, typeDirectory);
  }

  public void delete(final String fileName) {
    Path filePath =
        storagePath.resolve(fileName.replace(XML_EXTENSION, JSON_EXTENSION));

    if (!Files.exists(filePath)) {
      throw new NotFoundException("File not found: " + fileName);
    }
    try {
      Files.delete(filePath);
      log.info("Successfully deleted file: {}", fileName);
    } catch (IOException e) {
      log.error("Failed to delete file: {}", fileName, e);
      throw new InternalErrorException("Failed to delete file: " + fileName, e);
    }
  }

  private Resource save(MultipartFile file, final String fileName) {
    try {
      JsonNode xmlTree = xmlMapper.readTree(file.getInputStream());
      String jsonContent = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlTree);

      Path jsonPath = storagePath.resolve(fileName);
      Path path = Files.writeString(jsonPath, jsonContent);
      createFileIndexStructure(fileName, path);
      return new FileSystemResource(path);
    } catch (IOException e) {
      log.error("Failed to updated file: {}", fileName, e);
      throw new InternalErrorException("Failed to process file: ", e);
    }
  }

  private static List<String> getFilesByDirectory(String searchIndex, Path targetDirectory) {
    if (!Files.exists(targetDirectory)) {
      log.info("Files by provided index is not found!");
      return List.of();
    }

    try (Stream<Path> stream = Files.list(targetDirectory)) {
      return stream.filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .toList();
    } catch (IOException e) {
      log.error("Failed to list files by index: {}", searchIndex, e);
      throw new InternalErrorException("Failed to get files by index: " + searchIndex, e);
    }
  }

  private void createFileIndexStructure(String fileName, Path originPath) {
    String base = fileName.substring(0, fileName.length() - JSON_EXTENSION.length());
    String[] parts = base.split(FILE_NAME_SPLITTER);

    String customer = parts[CUSTOMER_INDEX_POSITION];
    String type = parts[TYPE_INDEX_POSITION];
    String date = parts[DATE_INDEX_POSITION];

    createSymlink(storagePath.resolve(CUSTOMER_INDEX_NAME).resolve(customer), fileName, originPath);
    createSymlink(storagePath.resolve(TYPE_INDEX_NAME).resolve(type), fileName, originPath);
    createSymlink(storagePath.resolve(DATE_INDEX_NAME).resolve(date), fileName, originPath);
  }

  private void createSymlink(Path directory, String fileName, Path originPath) {
    try {
      Files.createDirectories(directory);
      Path link = directory.resolve(fileName);

      Files.deleteIfExists(link);
      Files.createSymbolicLink(link, originPath);
    } catch (IOException e) {
      throw new InternalErrorException("Failed to create symlink: " + directory, e);
    }
  }
}
