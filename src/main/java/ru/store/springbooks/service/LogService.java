package ru.store.springbooks.service;

import org.springframework.core.io.Resource;

import java.util.concurrent.CompletableFuture;


public interface LogService {

    Resource generateAndReturnLogFile(String date);

    CompletableFuture<String> generateLogFileForDateAsync(String date);

    String getTaskStatus(String taskId);

    String getLogFilePath(String taskId);

}