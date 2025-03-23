package ru.store.springbooks.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.User;
import ru.store.springbooks.utils.InMemoryCache;

@Configuration
public class CacheConfig {

    @Bean
    public InMemoryCache<Long, Request> requestCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Long, User> userCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Long, Book> bookCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Long, Library> libraryCache() {
        return new InMemoryCache<>();
    }

}
