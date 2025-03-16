package ru.store.springbooks.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.repository.BookRepository;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.service.BookService;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;

    @Override
    public List<Book> findAllBooks() {
        return repository.findAll(); // Исправленный вызов метода из JpaRepository
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

        book.setLibrary(library); // Устанавливаем библиотеку перед сохранением
        log.info("Сохранение книги с библиотекой: {}", library);

        return repository.save(book);
    }

    @Override
    public Book getBookById(Long id) {
        return repository.findById(id) // JpaRepository предоставляет findById()
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));
    }

    @Override
    public void deleteBook(Long id) {
        repository.deleteById(id); // Исправленный вызов метода из JpaRepository
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
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setYear(updatedBook.getYear());
        return bookRepository.save(book);
    }
}
