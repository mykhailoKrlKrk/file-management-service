package com.file.registry.service;

import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.CUSTOMER_INDEX_NAME;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.DATE_INDEX_NAME;
import static com.file.registry.constants.ApplicationConstants.FilePartsConstants.TYPE_INDEX_NAME;
import static com.file.registry.constants.ApplicationConstants.JSON_EXTENSION;
import static com.file.registry.constants.ApplicationConstants.XML_EXTENSION;
import static com.file.registry.constants.TestApplicationConstants.JSON_FILE_NAME;
import static com.file.registry.constants.TestApplicationConstants.XML_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.file.registry.exception.ConflictException;
import com.file.registry.exception.NotFoundException;
import com.file.registry.properties.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
public class FileManagementServiceTest {

    public static final String STORAGE_FILE = "storage";

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @TempDir
    private Path tempStorageDir;

    private FileManagementService fileManagementService;

    @BeforeEach
    void setUp() throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        String storageLocation = tempStorageDir + "/storage";
        Files.createDirectories(Path.of(storageLocation));
        StorageProperties storageProperties = new StorageProperties(storageLocation);

        fileManagementService = new FileManagementService(
                xmlMapper,
                storageProperties,
                jsonMapper
        );
    }

    @Test
    @DisplayName("Verify: uploaded file converted to JSON & saved in proper way")
    void uploadXmlFile_shouldConvertsToJsonAndStoresInProperStructure() throws Exception {
        // Given
        MockMultipartFile multipartFile = createMultipartFileFromResource();

        // When
        Resource result = fileManagementService.upload(multipartFile);

        // Then
        Path storageRoot = tempStorageDir.resolve(STORAGE_FILE);
        Path mainJsonFile = storageRoot.resolve(JSON_FILE_NAME);

        assertMainJsonFileExistsAndIsValidJson(mainJsonFile);
        assertIndexDirectoriesExist(storageRoot);
        assertThat(result.exists())
                .as("Returned Resource should point to existing stored file")
                .isTrue();
    }

    @Test
    @DisplayName("Upload existing XML file should throw ConflictException (409)")
    void uploadExistingXmlFile_shouldThrowConflictException() throws Exception {
        // Given
        MockMultipartFile multipartFile = createMultipartFileFromResource();
        fileManagementService.upload(multipartFile);

        // When / Then
        assertThatThrownBy(() -> fileManagementService.upload(multipartFile))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exist");
    }

    @Test
    @DisplayName("Verify: update overwrites existing file content without changing path")
    void updateFile_shouldReturnUpdatedFile() throws Exception {
        // Given
        Path storageRoot = tempStorageDir.resolve(STORAGE_FILE);
        Path mainJsonFile = storageRoot.resolve(JSON_FILE_NAME);

        MockMultipartFile initialFile = new MockMultipartFile(
                "file",
                XML_FILE_NAME,
                "application/xml",
                "<root><value>initial</value></root>".getBytes()
        );
        fileManagementService.upload(initialFile);

        String initialJsonContent = Files.readString(mainJsonFile);
        MockMultipartFile updatedFile = new MockMultipartFile(
                "file",
                XML_FILE_NAME,
                "application/xml",
                "<root><value>updated</value></root>".getBytes()
        );

        // When
        Resource result = fileManagementService.update(updatedFile);

        // Then
        String updatedJsonContent = Files.readString(mainJsonFile);
        assertThat(updatedJsonContent)
                .as("Updated JSON content should differ from initial content")
                .isNotEqualTo(initialJsonContent);
        assertThat(result.exists())
                .as("Returned Resource from update() should point to existing stored file")
                .isTrue();
    }

    @Test
    @DisplayName("Verify: getByName returns Resource for existing file")
    void getByName_shouldReturnRequestedFile() throws Exception {
        // Given
        MockMultipartFile multipartFile = createMultipartFileFromResource();
        fileManagementService.upload(multipartFile);

        Path storageRoot = tempStorageDir.resolve(STORAGE_FILE);
        Path mainJsonFile = storageRoot.resolve(JSON_FILE_NAME);

        // When
        Resource resource = fileManagementService.getByName(XML_FILE_NAME);

        // Then
        assertThat(resource.exists())
                .as("Resource returned by getByName() should exist")
                .isTrue();

        assertThat(resource.getFile().toPath())
                .as("Resource path should match stored JSON path")
                .isEqualTo(mainJsonFile);
    }

    @Test
    @DisplayName("Verify: getByName throws NotFoundException for missing file")
    void getByName_throwsNotFoundException_whenFileDoesNotExist() {
        // Given
        String missingFileName = "unknown_docs_2025-12-31.xml";

        // When / Then
        assertThatThrownBy(() -> fileManagementService.getByName(missingFileName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    @DisplayName("Verify: getFilesByDate returns files for given date")
    void getFilesByDate_returnsFilesFromDateIndex() throws Exception {
        // Given
        MockMultipartFile multipartFile = createMultipartFileFromResource(XML_FILE_NAME);
        fileManagementService.upload(multipartFile);
        LocalDate date = LocalDate.of(2025, 12, 16);

        // When
        List<String> files = fileManagementService.getFilesByDate(date);

        // Then
        assertThat(files)
                .as("Should return list with JSON file for given date")
                .contains(JSON_FILE_NAME);
    }

    @Test
    @DisplayName("Verify: getFilesByCustomer returns files for given customer")
    void getFilesByCustomer_returnsFilesFromCustomerIndex() throws Exception {
        // Given
        String fileName1 = "testcustomer_docs_2025-12-16.xml";
        String fileName2 = "testcustomer_report_2025-12-17.xml";

        MockMultipartFile file1 = createMultipartFileFromResource(fileName1);

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                fileName2,
                "application/xml",
                "<root><value>another</value></root>".getBytes()
        );

        fileManagementService.upload(file1);
        fileManagementService.upload(file2);

        // When
        List<String> files =
                fileManagementService.getFilesByCustomer("testcustomer");

        // Then
        assertThat(files)
                .as("Should return all JSON files for customer 'testcustomer'")
                .contains(
                        fileName1.replace(XML_EXTENSION, JSON_EXTENSION),
                        fileName2.replace(XML_EXTENSION, JSON_EXTENSION)
                );
    }

    @Test
    @DisplayName("Verify: getFilesByType returns files for given type")
    void getFilesByType_returnsFilesFromTypeIndex() throws Exception {
        // Given
        String docsFile = "testcustomer_docs_2025-12-16.xml";
        String anotherDocsFile = "othercustomer_docs_2025-12-17.xml";
        String otherTypeFile = "testcustomer_report_2025-12-18.xml";

        MockMultipartFile f1 = createMultipartFileFromResource(docsFile);

        MockMultipartFile f2 = new MockMultipartFile(
                "file",
                anotherDocsFile,
                "application/xml",
                "<root><value>docs2</value></root>".getBytes()
        );

        MockMultipartFile f3 = new MockMultipartFile(
                "file",
                otherTypeFile,
                "application/xml",
                "<root><value>report</value></root>".getBytes()
        );

        fileManagementService.upload(f1);
        fileManagementService.upload(f2);
        fileManagementService.upload(f3);

        // When
        List<String> files = fileManagementService.getFilesByType("docs");

        // Then
        assertThat(files)
                .as("Should return only files of type 'docs'")
                .contains(
                        docsFile.replace(XML_EXTENSION, JSON_EXTENSION),
                        anotherDocsFile.replace(XML_EXTENSION, JSON_EXTENSION)
                )
                .doesNotContain(otherTypeFile.replace(XML_EXTENSION, JSON_EXTENSION));
    }

    @Test
    @DisplayName("Verify: delete removes existing JSON file")
    void delete_existingFile_shouldRemoveIt() {
        // Given
        String originalFileName = "testcustomer_docs_2025-12-16.xml";
        String expectedJsonName = "testcustomer_docs_2025-12-16.json";

        MockMultipartFile multipartFile = multipartXml(originalFileName);
        fileManagementService.upload(multipartFile);

        Path storageRoot = tempStorageDir.resolve("storage");
        Path mainJsonFile = storageRoot.resolve(expectedJsonName);

        assertThat(Files.exists(mainJsonFile))
                .as("Precondition: JSON file should exist before delete")
                .isTrue();

        // When
        fileManagementService.delete(originalFileName);

        // Then
        assertThat(Files.exists(mainJsonFile))
                .as("JSON file should be removed after delete()")
                .isFalse();

        assertThatThrownBy(() -> fileManagementService.getByName(originalFileName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    @DisplayName("Verify: delete throws NotFoundException when file does not exist")
    void delete_missingFile_shouldThrowNotFoundException() {
        // given
        String missingFileName = "unknown_docs_2025-12-31.xml";

        // when / then
        assertThatThrownBy(() -> fileManagementService.delete(missingFileName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("File not found");
    }

    private MockMultipartFile createMultipartFileFromResource() throws IOException {
        ClassPathResource xmlResource = new ClassPathResource(
                com.file.registry.constants.TestApplicationConstants.XML_FILE_NAME);
        byte[] xmlBytes = xmlResource.getInputStream().readAllBytes();

        return new MockMultipartFile(
                "file",
                com.file.registry.constants.TestApplicationConstants.XML_FILE_NAME,
                "application/xml",
                xmlBytes
        );
    }

    private MockMultipartFile createMultipartFileFromResource(String resourceName) throws IOException {
        ClassPathResource xmlResource = new ClassPathResource(resourceName);
        byte[] xmlBytes = xmlResource.getInputStream().readAllBytes();

        return new MockMultipartFile(
                "file",
                resourceName,
                "application/xml",
                xmlBytes
        );
    }

    private void assertMainJsonFileExistsAndIsValidJson(Path mainJsonFile) throws IOException {
        assertThat(Files.exists(mainJsonFile))
                .as("Main JSON file should be stored under storage root")
                .isTrue();

        String jsonContent = Files.readString(mainJsonFile);
        JsonNode jsonNode = jsonMapper.readTree(jsonContent);

        assertThat(jsonNode)
                .as("Stored file should contain valid JSON")
                .isNotNull();
    }

    private void assertIndexDirectoriesExist(Path storageRoot) {
        assertThat(storageRoot.resolve(CUSTOMER_INDEX_NAME).resolve("testcustomer"))
                .as("Customer index directory should exist")
                .exists()
                .satisfies(Files::isDirectory);

        assertThat(storageRoot.resolve(TYPE_INDEX_NAME).resolve("docs"))
                .as("Type index directory should exist")
                .exists()
                .satisfies(Files::isDirectory);

        assertThat(storageRoot.resolve(DATE_INDEX_NAME).resolve("2025-12-16"))
                .as("Date index directory should exist")
                .exists()
                .satisfies(Files::isDirectory);
    }

    private MockMultipartFile multipartXml(String fileName) {
        String xml = "<root><value>" + fileName + "</value></root>";
        return new MockMultipartFile("file", fileName, "application/xml", xml.getBytes());
    }
}
