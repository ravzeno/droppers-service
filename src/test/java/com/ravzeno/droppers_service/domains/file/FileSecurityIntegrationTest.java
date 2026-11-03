package com.ravzeno.droppers_service.domains.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.exception.core.FileConflictException;
import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.commons.exception.core.SecurityViolationException;
import com.ravzeno.droppers_service.commons.security.TokenHasher;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import com.ravzeno.droppers_service.domains.storage.SecurePathResolver;
import com.ravzeno.droppers_service.domains.storage.StorageService;
import com.ravzeno.droppers_service.domains.transfer.UploadTransferService;
import com.ravzeno.droppers_service.domains.trash.TrashService;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashCriteria;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashSort;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "droppers.upload.max-file-size-bytes=8",
            "droppers.security.admin-token=integration-admin"
        })
@ActiveProfiles("test")
@AutoConfigureMockMvc
class FileSecurityIntegrationTest {
    @Autowired private StorageService storage;
    @Autowired private FileService files;
    @Autowired private SecurePathResolver paths;
    @Autowired private UploadTransferService uploads;
    @Autowired private DeviceService devices;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TrashService trash;

    @TempDir Path storageRoot;

    @BeforeEach
    void configureStorageRoot() {
        storage.setRoot(storageRoot.toString());
    }

    @Test
    void rejectsPathsThatEscapeTheConfiguredRoot() {
        assertThatThrownBy(() -> files.download("../outside.txt"))
                .isInstanceOf(SecurityViolationException.class);
    }

    @Test
    void duplicateUploadDoesNotOverwriteExistingFile() throws Exception {
        Path original = storageRoot.resolve("same.txt");
        Files.writeString(original, "original");
        MockMultipartFile replacement =
                new MockMultipartFile("file", "same.txt", "text/plain", "next".getBytes());

        assertThatThrownBy(() -> files.upload("", replacement))
                .isInstanceOf(FileConflictException.class);
        assertThat(Files.readString(original)).isEqualTo("original");
    }

    @Test
    void rejectsDirectAndTransferUploadsOverConfiguredSize() throws Exception {
        MockMultipartFile oversized =
                new MockMultipartFile("file", "large.txt", "text/plain", "123456789".getBytes());

        assertThatThrownBy(() -> files.upload("", oversized))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> uploads.create("", "large.txt", 9L))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void allowsResolvingTheConfiguredRoot() throws Exception {
        assertThat(paths.resolveDirectory("")).isEqualTo(storageRoot.toRealPath());
    }

    @Test
    void publicDeviceAndBackOfficeRoutesApplyTheirOwnTokenPolicies() throws Exception {
        mockMvc.perform(get("/public/api/v1/health")).andExpect(status().isOk());
        mockMvc.perform(get("/device/api/v1/files")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/back-office/api/v1/devices/count"))
                .andExpect(status().isUnauthorized());

        String deviceToken = "integration-device-token";
        devices.register("integration-device", "integration", TokenHasher.sha256(deviceToken));
        mockMvc.perform(get("/device/api/v1/files").header("X-Droppers-Token", deviceToken))
                .andExpect(status().isOk());
        mockMvc.perform(
                        get("/back-office/api/v1/devices/count")
                                .header("X-Droppers-Admin-Token", "integration-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void createsAndCompletesAnUploadTransferThroughTheApi() throws Exception {
        String deviceToken = "upload-integration-device-token";
        devices.register(
                "upload-integration-device", "upload integration", TokenHasher.sha256(deviceToken));

        var createResponse =
                mockMvc.perform(
                                post("/device/api/v1/transfers/uploads")
                                        .header("X-Droppers-Token", deviceToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"destinationPath":"","fileName":"transfer.txt","expectedBytes":5}
                                                """))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String transferId = objectMapper.readTree(createResponse).path("data").path("id").asText();

        mockMvc.perform(
                        put("/device/api/v1/transfers/uploads/{id}/content", transferId)
                                .header("X-Droppers-Token", deviceToken)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .content("hello".getBytes()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        assertThat(Files.readString(storageRoot.resolve("transfer.txt"))).isEqualTo("hello");
    }

    @Test
    void trashReportsTheFullSizeOfNestedFilesAndRestoresThem() throws Exception {
        Path folder = Files.createDirectory(storageRoot.resolve("folder"));
        Files.writeString(folder.resolve("first.txt"), "abc");
        Path nested = Files.createDirectory(folder.resolve("nested"));
        Files.writeString(nested.resolve("second.txt"), "de");

        trash.trash("folder");
        var page =
                trash.getAll(
                        GetAllTrashCriteria.DEFAULT,
                        GetAllTrashSort.DEFAULT,
                        new PageOption(1, 50));
        var trashedFolder =
                page.items().stream()
                        .filter(item -> item.name().equals("folder"))
                        .findFirst()
                        .orElseThrow();

        assertThat(trashedFolder.size()).isEqualTo(5);
        trash.restore(trashedFolder.id());
        assertThat(Files.readString(storageRoot.resolve("folder/nested/second.txt")))
                .isEqualTo("de");
    }
}
