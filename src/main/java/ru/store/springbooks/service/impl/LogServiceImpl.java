package ru.store.springbooks.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.service.LogService;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();
    private final Map<String, String> taskFilePathMap = new ConcurrentHashMap<>();

    private static final String LOGS_DIR = "logs/"; // лог-файлы сюда
    private static final String SOURCE_LOG_FILE = "app.log"; // исходный лог-файл

    @Override
    public CompletableFuture<String> generateLogFileForDateAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        taskStatusMap.put(taskId, "PROCESSING");

        return CompletableFuture.supplyAsync(() -> {
            try {
                Path sourcePath = Paths.get(SOURCE_LOG_FILE);
                if (!Files.exists(sourcePath)) {
                    taskStatusMap.put(taskId, "FAILED: no source log file");
                    return taskId;
                }

                String filteredLogs = Files.lines(sourcePath)
                        .filter(line -> line.contains(date))
                        .collect(Collectors.joining(System.lineSeparator()));

                if (filteredLogs.isEmpty()) {
                    taskStatusMap.put(taskId, "FAILED: no entries for date");
                    return taskId;
                }

                Path outputDir = Paths.get(LOGS_DIR);
                if (!Files.exists(outputDir)) {
                    Files.createDirectories(outputDir);
                }

                Path outputPath = outputDir.resolve("log-" + taskId + ".log");
                Files.write(outputPath, filteredLogs.getBytes());

                taskStatusMap.put(taskId, "COMPLETED");
                taskFilePathMap.put(taskId, outputPath.toString());
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
}