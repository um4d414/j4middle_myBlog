package ru.umd.myblog.app.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.umd.myblog.app.config.service.ServiceTestConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceTestConfig.class})
@TestPropertySource(properties = {
    "application.upload-dir=file:${temp.dir}/uploads/",
    "application.upload-url=file:${temp.dir}/uploads/"
})
public class DefaultImageServiceTest {
    @Autowired
    private ImageService imageService;

    @TempDir
    static Path tempDir;

    private static String resolvedUploadDir;

    @BeforeAll
    static void setup() {
        System.setProperty("temp.dir", tempDir.toAbsolutePath().toString());
        resolvedUploadDir = "file:" + tempDir.toAbsolutePath().toString() + "/uploads/";
    }

    @AfterAll
    static void tearDown() {
        System.clearProperty("temp.dir");
    }

    @Test
    void testSaveImageCreatesFileAndReturnsCorrectPath() throws IOException {
        byte[] content = "dummy image content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", content);

        String resultPath = imageService.saveImage(multipartFile);

        String normalizedResultPath = resultPath.replace("\\", "/");
        String normalizedResolved = resolvedUploadDir.replace("\\", "/");

        assertTrue(normalizedResultPath.startsWith(normalizedResolved),
                   "Путь должен начинаться с " + normalizedResolved);

        String fileName = normalizedResultPath.substring(normalizedResolved.length());
        File savedFile = new File(tempDir.toFile(), "uploads" + File.separator + fileName);
        assertTrue(savedFile.exists(), "Сохраненный файл должен существовать");

        byte[] savedContent = java.nio.file.Files.readAllBytes(savedFile.toPath());
        assertArrayEquals(content, savedContent, "Содержимое файла должно совпадать с исходным");
    }


    @Test
    void testSaveImageGeneratesUniqueFileNames() throws IOException {
        byte[] content = "image content".getBytes();
        MultipartFile file1 = new MockMultipartFile("image", "pic.jpg", "image/jpeg", content);
        MultipartFile file2 = new MockMultipartFile("image", "pic.jpg", "image/jpeg", content);

        String path1 = imageService.saveImage(file1);
        String path2 = imageService.saveImage(file2);

        System.out.println(path1);
        System.out.println(path2);

        assertTrue(path1.startsWith(resolvedUploadDir), "Первый путь должен начинаться с " + resolvedUploadDir);
        assertTrue(path2.startsWith(resolvedUploadDir), "Второй путь должен начинаться с " + resolvedUploadDir);
        String fileName1 = path1.substring(resolvedUploadDir.length());
        String fileName2 = path2.substring(resolvedUploadDir.length());
        assertNotEquals(fileName1, fileName2, "Имена файлов должны быть уникальными");
    }

    @Test
    void testSaveEmptyFile() throws IOException {
        byte[] emptyContent = new byte[0];
        MultipartFile emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", emptyContent);

        String resultPath = imageService.saveImage(emptyFile);
        assertTrue(resultPath.startsWith(resolvedUploadDir), "Путь должен начинаться с " + resolvedUploadDir);

        String fileName = resultPath.substring(resolvedUploadDir.length());
        File savedFile = new File(tempDir.toFile(), "uploads" + File.separator + fileName);
        assertTrue(savedFile.exists(), "Сохраненный файл должен существовать");

        assertEquals(0, savedFile.length(), "Размер файла должен быть 0");
    }
}
