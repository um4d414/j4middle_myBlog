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
public class DefaultImageService implements ImageService {
    @Value("${application.upload-dir}")
    private String uploadDir;

    @Value("${application.upload-url}")
    private String uploadUrl;

    @Override
    public String saveImage(MultipartFile image) throws IOException {
        var resource = new UrlResource(uploadDir);
        if (!resource.exists()) {
            resource.getFile().mkdirs();
        }
        // Генерируем уникальное имя файла
        var fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        var file = new File(resource.getFile(), fileName);
        image.transferTo(file);
        return uploadUrl + fileName;
    }
}
