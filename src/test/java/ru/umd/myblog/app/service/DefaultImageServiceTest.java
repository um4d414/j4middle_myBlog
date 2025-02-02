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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceTestConfig.class})
@TestPropertySource(properties = {"upload.dir=file:${temp.dir}"})
public class DefaultImageServiceTest {
    @Autowired
    private ImageService imageService;

    // Создаем временную директорию для теста
    @TempDir
    static Path tempDir;

    // Будем использовать этот путь для проверки
    private static String resolvedUploadDir;

    @BeforeAll
    static void setup() {
        // Задаем системное свойство, чтобы оно подставилось в upload.dir
        System.setProperty("temp.dir", tempDir.toAbsolutePath().toString());
        resolvedUploadDir = "file:" + tempDir.toAbsolutePath();
    }

    @AfterAll
    static void tearDown() {
        System.clearProperty("temp.dir");
    }

    @Test
    void testSaveImageCreatesFileAndReturnsCorrectPath() throws IOException {
        // Создаем фиктивный MultipartFile с тестовыми данными
        byte[] content = "dummy image content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", content);

        // Вызываем метод сохранения изображения
        String resultPath = imageService.saveImage(multipartFile);

        // Проверяем, что возвращенный путь начинается с заданного значения
        assertTrue(resultPath.startsWith(resolvedUploadDir),
                   "Путь должен начинаться с " + resolvedUploadDir);

        // Извлекаем имя файла из результата
        String fileName = resultPath.substring(resolvedUploadDir.length());
        File savedFile = new File(tempDir.toFile(), fileName);
        assertTrue(savedFile.exists(), "Сохраненный файл должен существовать");

        // Проверяем, что содержимое файла соответствует исходному
        byte[] savedContent = java.nio.file.Files.readAllBytes(savedFile.toPath());
        assertArrayEquals(content, savedContent, "Содержимое файла должно совпадать с исходным");
    }
}
