package ru.store.springbooks.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.store.springbooks.service.LogService;

@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Log Controller", description = "API for logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping("/{date}")
    public CompletableFuture<ResponseEntity<Map<String, String>>> generateLogs(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        CompletableFuture<String> future = logService.generateLogFileForDateAsync(date.toString());

        return future.thenApply(taskId ->
                ResponseEntity.accepted().body(Map.of(
                        "taskId", taskId,
                        "status", "PROCESSING",
                        "statusUrl", "/logs/" + taskId + "/status"
                ))
        );
    }

    @GetMapping("/{taskId}/status")
    @Operation(summary = "Get task status")
    public ResponseEntity<Map<String, String>> getTaskStatus(@PathVariable String taskId) {
        String status = logService.getTaskStatus(taskId);
        if ("NOT_FOUND".equals(status)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("status", status));
    }



    @GetMapping("/{taskId}/file")
    @Operation(summary = "Download log file")
    public ResponseEntity<Resource> downloadLogFile(@PathVariable String taskId) {
        try {
            String status = logService.getTaskStatus(taskId);

            if (!status.startsWith("COMPLETED")) {
                return ResponseEntity.status(status.startsWith("FAILED")
                        ? HttpStatus.NOT_FOUND : HttpStatus.TOO_EARLY).build();
            }

            String filePath = logService.getLogFilePath(taskId);
            if (filePath == null || !Files.exists(Paths.get(filePath))) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new InputStreamResource(Files.newInputStream(Paths.get(filePath)));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=logs-" + taskId + ".log")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
