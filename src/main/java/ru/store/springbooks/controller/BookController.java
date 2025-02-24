package ru.store.springbooks.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.service.BookService;


@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
public class BookController {

    private final BookService  service;

    @GetMapping
    public List<Book> findAllBooks() {
        return service.findAllBooks();
    }

    @PostMapping("save_book")
    public Book saveBook(@RequestBody Book book) {
        return service.saveBook(book);
    }

    @GetMapping("/{id}")
    public Book findBookById(@PathVariable("id") int id) {
        return service.getBookById(id);
    }


    @DeleteMapping("delete_book/{id}")
    public void deleteBook(@PathVariable("id") int id) {
        service.deleteBook(id);
    }

    @GetMapping("/searchByTitle")
    public Book getBookByTitle(@RequestParam String title) {
        return service.getBookByTitle(title);
    }

}
