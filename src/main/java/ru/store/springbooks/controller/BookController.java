package ru.store.springbooks.controller;

import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.service.BookService;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {


    private final BookService  service;
    private final LibraryRepository libraryRepository;

    @GetMapping
    public List<Book> findAllBooks() {
        return service.findAllBooks();
    }


    @PostMapping
    public ResponseEntity<Book> saveBook(@RequestBody Book book) {
        System.out.println("Received Book: " + book);
        if (book.getLibrary() == null) {
            throw new RuntimeException("Library is null!");
        }
        if (book.getLibrary().getId() == null) {
            throw new RuntimeException("Library ID is required!");
        }
        System.out.println("Library ID: " + book.getLibrary().getId()); // Логирование ID

        Library library = libraryRepository.findById(book.getLibrary().getId())
                .orElseThrow(() -> new RuntimeException("Library not found"));

        book.setLibrary(library);

        Book savedBook = service.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }


    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<Book> findBookById(@PathVariable("id") Long id) {
            Book book = service.getBookById(id);
            return ResponseEntity.ok(book);
    }


    @DeleteMapping("/delete_book/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable("id") Long id) {
            boolean isDeleted = service.deleteBook(id);
            if (isDeleted) {
                return ResponseEntity.ok("Книга успешно удалена");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Книга с данным ID не найдена");
            }

    }


    @GetMapping("/search")
    public List<Book> searchBook(@RequestParam Map<String, String> params) {
        return service.searchBook(params);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {

            Book book = service.updateBook(id, updatedBook);
            return ResponseEntity.ok(book);

    }
}