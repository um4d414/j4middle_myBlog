package ru.umd.myblog.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Primary
public class DefaultImageService implements ImageService {
    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public String saveImage(MultipartFile image) throws IOException {
        var resource = new UrlResource(uploadDir);
        if (!resource.exists()) {
            resource.getFile().mkdirs();
        }

        var fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        var file = new File(resource.getFile(), fileName);
        image.transferTo(file);

        return uploadDir + fileName;
    }
}
