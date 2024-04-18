package es.mybopi.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



@Service
public class UploadFileService {

    private String ruta = "images\\";

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            byte [] bytes = file.getBytes();
            Path path = Paths.get(ruta).resolve(file.getOriginalFilename()).toAbsolutePath();
            System.out.println(path.toString());
            System.out.println(bytes.toString());
            System.out.println(file.getOriginalFilename());
            System.out.println("-------------------------");
            Files.write(path, bytes);
            return file.getOriginalFilename();
        }

        return "default.jpg";
    }

    public void deleteImage(String name) throws IOException {
        //Files.deleteIfExists(Paths.get(ruta+name));

        String ruta ="images//";
        File file = new File(ruta+name);
        file.delete();
    }

}
