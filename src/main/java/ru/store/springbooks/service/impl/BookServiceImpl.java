package ru.store.springbooks.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.repository.BookRepository;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.service.BookService;
import ru.store.springbooks.utils.InMemoryCache;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final LibraryRepository libraryRepository;
    private final InMemoryCache<Long, Book> bookCache;


    @Override
    public List<Book> findAllBooks() {
        List<Book> books = repository.findAll();
        if (books.isEmpty()) {
            log.info("Книги не найдены в базе данных.");
        }
        return books;
    }


    @Override
    public Book saveBook(Book book) {
        log.info("Получен запрос на сохранение книги: {}", book);

        if (book.getLibrary() == null || book.getLibrary().getId() == null) {
            log.error("Ошибка: Library ID отсутствует!");
            throw new RuntimeException("Library ID is required!");
        }

        Library library = libraryRepository.findById(book.getLibrary().getId())
                .orElseThrow(() -> {
                    log.error("Ошибка: Библиотека с ID {} не найдена!", book.getLibrary().getId());
                    return new RuntimeException("Library not found!");
                });
        Book savedBook = repository.save(book);

        book.setLibrary(library);
        log.info("Сохранение книги с библиотекой: {}", library);

        bookCache.put(savedBook.getId(), savedBook);
        log.info("Книга добавлена в кеш: {}", savedBook);

        return savedBook;
    }


    @Override
    public Book getBookById(Long id) {
        Book cachedBook = bookCache.get(id);
        if (cachedBook != null) {
            log.info("Книга получена из кеша: {}", cachedBook);
            return cachedBook;
        }

        Book book = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));
        bookCache.put(id, book);
        log.info("Книга загружена из БД и добавлена в кеш: {}", book);
        return book;
    }


    @Override
    public boolean deleteBook(Long id) {
        Optional<Book> book = repository.findById(id);
        if (book.isPresent()) {
            repository.deleteById(id);
            bookCache.evict(id);
            log.info("Книга удалена из кеша и базы данных: {}", id);
            return true;
        }
        return false;
    }


    @Override
    public List<Book> searchBook(Map<String, String> params) {
        String title = params.get("title");
        String author = params.get("author");
        Integer year = params.containsKey("year") ? Integer.parseInt(params.get("year")) : null;

        if (title != null && author != null && year != null) {
            return repository.findByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(title, author, year);
        } else if (title != null && author != null) {
            return repository.findByTitleIgnoreCaseAndAuthorIgnoreCase(title, author);
        } else if (title != null) {
            return repository.findByTitleIgnoreCase(title);
        } else if (author != null) {
            return repository.findByAuthorIgnoreCase(author);
        } else if (year != null) {
            return repository.findByYear(year);
        }
        return repository.findAll();
    }


    @Override
    public Book updateBook(Long id, Book updatedBook) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setYear(updatedBook.getYear());

        Book savedBook = repository.save(book);
        bookCache.put(id, savedBook); // Обновляем кеш
        log.info("Книга обновлена в кешe: {}", savedBook);
        return savedBook;
    }
}