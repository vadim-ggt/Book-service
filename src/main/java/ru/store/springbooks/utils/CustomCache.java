package ru.store.springbooks.utils;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import java.util.LinkedHashMap;


@Slf4j
public class CustomCache<K, V> {

    private final Map<K, V> cache;
    private final ScheduledExecutorService executor;
    private final long maxAgeInMillis;
    private final int maxSize;

    public CustomCache() {
        this.maxAgeInMillis = 60000; // 60 секунд
        this.maxSize = 1000;
        this.cache = new LinkedHashMap<K, V> (maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > maxSize;
                if (shouldRemove) {
                    log.info("Removing eldest entry: {}={} (Cache size: {})",
                            eldest.getKey(), eldest.getValue(), size());
                }
                return shouldRemove;
            }
        };
        this.executor = Executors.newScheduledThreadPool(1);
    }

    public synchronized void put(K key, V value) {
        cache.put(key, value);
        log.info("Added entry: {}={} (Cache size: {})", key, value, cache.size());

        executor.schedule(() -> {
            remove(key);
            log.info("Automatically removed entry: {} (Cache size: {})", key, cache.size());
        }, maxAgeInMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized V get(K key) {
        V value = cache.get(key);
        log.info("Retrieved entry: {}={} (Cache size: {})", key, value, cache.size());
        return value;
    }

    public synchronized void remove(K key) {
        V value = cache.remove(key);
        if (value != null) {
            log.info("Removed entry: {}={} (Cache size: {})", key, value, cache.size());
        }
    }

    public synchronized void clear() {
        cache.clear();
        log.info("Cache cleared.");
    }

    public synchronized int size() {
        return cache.size();
    }

    public void shutdown() {
        log.info("Shutting down cache executor...");
        executor.shutdown();
    }
}

