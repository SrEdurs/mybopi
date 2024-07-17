package es.mybopi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            Path path = Paths.get(uploadDir).resolve(file.getOriginalFilename()).toAbsolutePath();
            Files.createDirectories(path.getParent()); // Crear directorios si no existen
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            return file.getOriginalFilename();
        }
        return "default.jpg";
    }

    public void deleteImage(String name) throws IOException {
        Path path = Paths.get(uploadDir).resolve(name).toAbsolutePath();
        Files.deleteIfExists(path);
    }
}
