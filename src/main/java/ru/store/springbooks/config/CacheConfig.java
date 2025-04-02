package ru.store.springbooks.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.User;
import ru.store.springbooks.utils.CustomCache;

@Configuration
public class CacheConfig {

    @Bean
    public CustomCache<Long, Request> requestCache() {
        return new CustomCache<>();
    }

    @Bean
    public CustomCache<Long, User> userCache() {
        return new CustomCache<>();
    }

    @Bean
    public CustomCache<Long, Book> bookCache() {
        return new CustomCache<>();
    }

    @Bean
    public CustomCache<Long, Library> libraryCache() {
        return new CustomCache<>();
    }
}
