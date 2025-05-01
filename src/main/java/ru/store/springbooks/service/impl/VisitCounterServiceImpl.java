package ru.store.springbooks.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.service.VisitCounterService;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitCounterServiceImpl implements VisitCounterService {

    private final Map<String, AtomicInteger> visitCounts = new ConcurrentHashMap<>();

    @Override
    public void incrementVisitCount(String url) {
        visitCounts.computeIfAbsent(url, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public int getVisitCount(String url) {
        return visitCounts.getOrDefault(url, new AtomicInteger(0)).get();
    }

    @Override
    public int getTotalVisitCount() {
        return visitCounts.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    @Override
    public Map<String, Integer> getAllVisitCounts() {
        // Преобразуем в Map<String, Integer> чтобы не светить наружу AtomicInteger
        Map<String, Integer> snapshot = new ConcurrentHashMap<>();
        visitCounts.forEach((key, value) -> snapshot.put(key, value.get()));
        return snapshot;
    }
}
