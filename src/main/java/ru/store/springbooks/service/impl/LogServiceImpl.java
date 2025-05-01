package ru.store.springbooks.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import ru.store.springbooks.service.LogService;


@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private final String logFilePath;
    private final String logsDir;

    // Основной конструктор (используется приложением)
    public LogServiceImpl() {
        this("app.log", "logs/");
    }

    // Для тестов или переопределения
    public LogServiceImpl(String logFilePath, String logsDir) {
        this.logFilePath = logFilePath;
        this.logsDir = logsDir;
    }

    private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();
    private final Map<String, String> taskFilePathMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @Override
    public CompletableFuture<String> generateLogFileForDateAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        taskStatusMap.put(taskId, "PROCESSING");

        return CompletableFuture.supplyAsync(() -> {
            try {
                Path sourcePath = Paths.get(logFilePath);
                if (!Files.exists(sourcePath)) {
                    taskStatusMap.put(taskId, "FAILED: no source log file");
                    return taskId;
                }

                String filteredLogs;
                try (var stream = Files.lines(sourcePath)) {
                    filteredLogs = stream
                            .filter(line -> line.contains(date))
                            .collect(Collectors.joining(System.lineSeparator()));
                }

                if (filteredLogs.isEmpty()) {
                    taskStatusMap.put(taskId, "FAILED: no entries for date");
                    return taskId;
                }

                Path outputDir = Paths.get(logsDir);
                if (!Files.exists(outputDir)) {
                    Files.createDirectories(outputDir);
                }

                Path outputPath = outputDir.resolve("log-" + taskId + ".log");
                Files.write(outputPath, filteredLogs.getBytes());

                taskFilePathMap.put(taskId, outputPath.toString());

                // Планируем смену статуса через 5 секунд
                scheduler.schedule(() -> {
                    taskStatusMap.put(taskId, "COMPLETED");
                }, 20, TimeUnit.SECONDS);

            } catch (IOException e) {
                log.error("Error generating log file", e);
                taskStatusMap.put(taskId, "FAILED: " + e.getMessage());
            }
            return taskId;
        });
    }



    @Override
    public String getTaskStatus(String taskId) {
        return taskStatusMap.getOrDefault(taskId, "NOT_FOUND");
    }

    @Override
    public String getLogFilePath(String taskId) {
        return taskFilePathMap.get(taskId);
    }

    @Override
    public InputStreamResource generateAndReturnLogFile(String date) {
        try {
            Path sourcePath = Paths.get(logFilePath);
            if (!Files.exists(sourcePath)) {
                throw new FileNotFoundException("Исходный лог-файл не найден");
            }

            String filteredLogs;
            try (var stream = Files.lines(sourcePath)) {
                filteredLogs = stream
                        .filter(line -> line.contains(date))
                        .collect(Collectors.joining(System.lineSeparator()));
            }

            if (filteredLogs.isEmpty()) {
                throw new RuntimeException("Нет записей в логах на указанную дату");
            }

            Path tempFile = Files.createTempFile("log-" + date, ".log");
            Files.write(tempFile, filteredLogs.getBytes());
            return new InputStreamResource(Files.newInputStream(tempFile));
        } catch (IOException e) {
            log.error("Ошибка при создании лог-файла", e);
            throw new RuntimeException("Ошибка при формировании лог-файла: " + e.getMessage());
        }
    }
}