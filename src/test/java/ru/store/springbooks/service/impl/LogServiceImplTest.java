package ru.store.springbooks.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import ru.store.springbooks.service.LogService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class LogServiceImplTest {

    private LogService logService;
    private Path tempDir;
    private Path sourceLogFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("log-test");
        sourceLogFile = tempDir.resolve("test-app.log");

        Files.write(sourceLogFile, String.join(System.lineSeparator(),
                "2024-04-20 INFO Something happened",
                "2024-04-21 INFO Another thing",
                "2024-04-21 ERROR Failed to process"
        ).getBytes());

        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);

        logService = new LogServiceImpl(sourceLogFile.toString(), logDir.toString());
    }

    @Test
    void getTaskStatus_unknownTask() {
        assertEquals("NOT_FOUND", logService.getTaskStatus("nonexistent"));
    }

    @Test
    void generateLogFileForDateAsync_noLogFile() throws Exception {
        Files.deleteIfExists(sourceLogFile);
        String taskId = logService.generateLogFileForDateAsync("2024-04-20").get();

        assertTrue(logService.getTaskStatus(taskId).startsWith("FAILED: no source log file"));
        assertNull(logService.getLogFilePath(taskId));
    }

    @Test
    void generateLogFileForDateAsync_success() throws ExecutionException, InterruptedException, IOException {
        String date = "2024-04-21";
        String taskId = logService.generateLogFileForDateAsync(date).get();

        assertEquals("COMPLETED", logService.getTaskStatus(taskId));

        String path = logService.getLogFilePath(taskId);
        assertNotNull(path);

        String content = Files.readString(Path.of(path));
        assertTrue(content.contains("Another thing"));
        assertTrue(content.contains("Failed to process"));
    }

    @Test
    void generateLogFileForDateAsync_noEntries() throws ExecutionException, InterruptedException {
        String date = "2024-04-19";
        String taskId = logService.generateLogFileForDateAsync(date).get();

        assertTrue(logService.getTaskStatus(taskId).startsWith("FAILED"));
        assertNull(logService.getLogFilePath(taskId));
    }

    @Test
    void generateAndReturnLogFile_success() throws IOException {
        String date = "2024-04-21";
        InputStreamResource resource = (InputStreamResource) logService.generateAndReturnLogFile(date);

        assertNotNull(resource);
        String content = new String(resource.getInputStream().readAllBytes());

        assertTrue(content.contains("Another thing"));
        assertTrue(content.contains("Failed to process"));
    }

    @Test
    void generateAndReturnLogFile_noEntries() {
        String date = "2024-04-19";
        Exception ex = assertThrows(RuntimeException.class, () ->
                logService.generateAndReturnLogFile(date));
        assertTrue(ex.getMessage().contains("Нет записей"));
    }

    @Test
    void generateAndReturnLogFile_noLogFile() {
        Path original = sourceLogFile;
        try {
            Files.deleteIfExists(sourceLogFile);
        } catch (IOException e) {
            // Ignore
        }

        Exception ex = assertThrows(RuntimeException.class, () ->
                logService.generateAndReturnLogFile("2024-04-20"));
        assertTrue(ex.getMessage().contains("Исходный лог-файл не найден"));
    }
}