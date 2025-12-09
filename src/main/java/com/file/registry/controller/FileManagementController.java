package com.file.registry.controller;

import com.file.registry.annotation.ValidFileName;
import com.file.registry.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
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
                Uploads an XML file with the required name format: <customerName>_<dd.mm.yyyy>.xml
                
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
  @ResponseStatus(HttpStatus.CREATED)
  public void upload(
      @Parameter(
          description = "XML file with required name format: <customerName>_<dd.mm.yyyy>.xml",
          required = true
      )
      @RequestPart("file") @ValidFileName MultipartFile file) {
    fileManagementService.upload(file);
  }
}
