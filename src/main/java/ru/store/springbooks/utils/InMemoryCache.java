package ru.store.springbooks.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCache<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();


    public V get(K key) {
        return cache.get(key);
    }


    public void put(K key, V value) {
        cache.put(key, value);
    }


    public void evict(K key) {
        cache.remove(key);
    }


    public void clear() {
        cache.clear();
    }

}

