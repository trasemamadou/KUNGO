package com.kungo.common.util;

import com.kungo.common.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("error.validation");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension != null ? "." + extension : "");

        try {
            Path targetDir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + subFolder + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("error.validation");
        }
    }

    @Override
    public void delete(String relativeUrl) {
        if (!StringUtils.hasText(relativeUrl)) {
            return;
        }
        try {
            String cleaned = relativeUrl.replaceFirst("^/uploads/", "");
            Path file = Paths.get(uploadDir, cleaned).toAbsolutePath().normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", relativeUrl, e.getMessage());
        }
    }
}
