package com.file.registry.controller;

import static com.file.registry.constants.ApplicationConstants.CONTENT_DISPOSITION_TEMPLATE;

import java.time.LocalDate;
import java.util.List;

import com.file.registry.annotation.ValidFileName;
import com.file.registry.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/file-management")
@Tag(
    name = "File Management Resource",
    description = "Operations for uploading, replacing, deleting and retrieving customer XML files."
)
@RequiredArgsConstructor
public class FileManagementController {

  private final FileManagementService fileManagementService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload XML file",
      description = """
          Uploads an XML file with the required name format: <customerName>_<type>_<dd.mm.yyyy>.xml
          
          Processing steps:
          1. Validate the file name format.
          2. Parse XML content and convert it to JSON.
          3. Save the file to the filesystem.
          
          Throws an exception if a file with the same name already exists.
          """
  )
  @ApiResponse(responseCode = "201", description = "File successfully uploaded and processed")
  @ApiResponse(responseCode = "400", description = "Invalid file name or invalid XML content")
  @ApiResponse(responseCode = "409", description = "File with the same name already exists")
  public ResponseEntity<Resource> upload(
      @Parameter(
          description = "XML file with required name format: <customerName>_<type>_<dd.mm.yyyy>.xml",
          required = true
      )
      @RequestPart("file") @ValidFileName MultipartFile file) {
    Resource result = fileManagementService.upload(file);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format(CONTENT_DISPOSITION_TEMPLATE, result.getFilename())
        ).body(result);
  }

  @Operation(
      summary = "Update existing XML file",
      description = """
          Updates an existing XML file using the required name format: <customerName>_<type>_<dd.mm.yyyy>.xml
          
          Processing steps:
          1. Validate the file name format.
          2. Parse XML content and convert it to JSON.
          3. Replace the existing file on the filesystem.
          
          If the file does not exist, a new one will be created.
          """
  )
  @ApiResponse(responseCode = "202", description = "File successfully updated (or created if not existed)")
  @ApiResponse(responseCode = "400", description = "Invalid file name or invalid XML content")
  @ApiResponse(responseCode = "500", description = "Internal server error during file update")
  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Resource> update(
      @Parameter(
          description = "XML file with required name format:<customerName>_<type>_<dd.mm.yyyy>.xml",
          required = true
      )
      @RequestPart("file") @ValidFileName MultipartFile file
  ) {
    Resource updatedFile = fileManagementService.update(file);
    return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format(CONTENT_DISPOSITION_TEMPLATE, updatedFile.getFilename())
        )
        .body(updatedFile);
  }

  @Operation(
      summary = "Get file content by name",
      description = """
          Retrieves a file as a downloadable resource using the required name format: <customerName>_<type>_<dd.mm.yyyy>.xml
          
          Processing steps:
          1. Validate the file name format.
          2. Locate the corresponding JSON file on the filesystem.
          3. Return the file as a downloadable response.
          
          Throws an error if the file does not exist.
          """
  )
  @ApiResponse(responseCode = "200", description = "File successfully retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid file name format")
  @ApiResponse(responseCode = "404", description = "File not found")
  @GetMapping("/{fileName}")
  public ResponseEntity<Resource> getByName(
      @Parameter(
          description = "File name in the format <customerName>_<type>_<dd.mm.yyyy>.xml",
          example = "acme_report_12.10.2025.xml",
          required = true
      )
      @PathVariable String fileName
  ) {
    Resource resource = fileManagementService.getByName(fileName);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format(CONTENT_DISPOSITION_TEMPLATE, resource.getFilename())
        )
        .body(resource);
  }

  @Operation(
      summary = "Get files by date",
      description = """
        Returns a list of file names for a given date.
        
        Processing steps:
        1. Validate the date parameter (ISO format: yyyy-MM-dd).
        2. Retrieve files indexed under the provided date.
        3. Return a list of file names in external XML format.
        
        Returns an empty list if no files exist for the given date.
        """
  )
  @ApiResponse(responseCode = "200", description = "Files successfully retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid date format (expected yyyy-MM-dd)")
  @GetMapping("/find-by-date/{date}")
  public List<String> getFilesByDate(
      @Parameter(
          description = "Date in ISO format: yyyy-MM-dd",
          example = "2025-12-09",
          required = true
      )
      @PathVariable LocalDate date
  ) {
    return fileManagementService.getFilesByDate(date);
  }

  @Operation(
      summary = "Get files by customer",
      description = """
        Returns a list of file names for a given customer.
        
        Processing steps:
        1. Validate the customer name parameter.
        2. Retrieve all files indexed under the provided customer.
        3. Return a list of file names in external XML format.
        
        Returns an empty list if no files exist for the given customer.
        """
  )
  @ApiResponse(responseCode = "200", description = "Files successfully retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid customer name format")
  @GetMapping("/find-by-customer/{customerName}")
  public List<String> getFilesByCustomer(
      @Parameter(
          description = "Customer name (alphanumeric string)",
          example = "acme",
          required = true
      )
      @PathVariable @NotBlank String customerName
  ) {
    return fileManagementService.getFilesByCustomer(customerName);
  }

  @Operation(
      summary = "Get files by type",
      description = """
        Returns a list of file names for a given file type.
        
        Processing steps:
        1. Validate the type parameter.
        2. Retrieve all files indexed under the provided type.
        3. Return a list of file names in external XML format.
        
        Returns an empty list if no files exist for the given type.
        """
  )
  @ApiResponse(responseCode = "200", description = "Files successfully retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid type format")
  @GetMapping("/find-by-type/{type}")
  public List<String> getFilesByType(
      @Parameter(
          description = "File type (alphanumeric string)",
          example = "report",
          required = true
      )
      @PathVariable @NotBlank String type
  ) {
    return fileManagementService.getFilesByType(type);
  }

  @Operation(
      summary = "Delete XML/JSON file by name",
      description = """
          Deletes a file by its name using the required format: <customerName>_<type>_<dd.mm.yyyy>.xml
          
          Processing steps:
          1. Validate the file name format.
          2. Convert XML name to internal JSON filename.
          3. Delete the file from the filesystem.
          
          Throws an error if the file does not exist.
          """
  )
  @ApiResponse(responseCode = "204", description = "File successfully deleted")
  @ApiResponse(responseCode = "400", description = "Invalid file name format")
  @ApiResponse(responseCode = "404", description = "File not found")
  @DeleteMapping("/{fileName}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Parameter(
          description = "File name in the format <customerName>_<type>_<dd.mm.yyyy>.xml",
          example = "acme_report_12.10.2025.xml",
          required = true
      )
      @PathVariable @NotBlank String fileName
  ) {
    fileManagementService.delete(fileName);
  }
}
