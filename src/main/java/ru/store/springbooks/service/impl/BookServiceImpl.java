package ru.store.springbooks.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.exception.EntityNotFoundException;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.repository.BookRepository;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.service.BookService;
import ru.store.springbooks.utils.CustomCache;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final LibraryRepository libraryRepository;
    private final CustomCache<Long, Book> bookCache;


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
                    return new EntityNotFoundException("Library", book.getLibrary().getId());
                });

        book.setLibrary(library);
        Book savedBook = repository.save(book);
        log.info("Сохранение книги с библиотекой: {}", library);

        bookCache.put(savedBook.getId(), savedBook); // Добавляем в кеш
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
                .orElseThrow(() -> new EntityNotFoundException("Book", id));

        bookCache.put(id, book); // Добавляем в кеш
        log.info("Книга загружена из БД и добавлена в кеш: {}", book);
        return book;
    }


    @Override
    public boolean deleteBook(Long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", id));

        repository.deleteById(id);
        bookCache.remove(id); // Удаляем из кеша
        log.info("Книга удалена из кеша и базы данных: {}", id);
        return true;
    }


    @Override
    public List<Book> searchBook(Map<String, String> params) {
        String title = params.get("title");
        String author = params.get("author");
        Integer year = params.containsKey("year") ? Integer.parseInt(params.get("year")) : null;

        List<Book> books;

        if (title != null && author != null && year != null) {
            books = repository.findByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(title, author, year);
        } else if (title != null && author != null) {
            books = repository.findByTitleIgnoreCaseAndAuthorIgnoreCase(title, author);
        } else if (title != null) {
            books = repository.findByTitleIgnoreCase(title);
        } else if (author != null) {
            books = repository.findByAuthorIgnoreCase(author);
        } else if (year != null) {
            books = repository.findByYear(year);
        } else {
            books = repository.findAll();
        }

        if (books.isEmpty()) {
            throw new EntityNotFoundException("Book", null); // Можно сделать null для ID, так как книги не найдены
        }

        return books;
    }


    @Override
    public Book updateBook(Long id, Book updatedBook) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", id));

        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setYear(updatedBook.getYear());

        Book savedBook = repository.save(book);
        bookCache.put(id, savedBook); // Обновляем кеш
        log.info("Книга обновлена в кеше: {}", savedBook);
        return savedBook;
    }

}