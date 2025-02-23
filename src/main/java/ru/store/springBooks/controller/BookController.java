package ru.store.springBooks.controller;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.store.springBooks.model.Book;
import ru.store.springBooks.service.BookService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
public class BookController {

    private final BookService  service;

    @GetMapping
    public List<Book> findAllBooks(){
        //todo
        return service.findAllBooks();
    }

    @PostMapping("save_book")
    public Book saveBook(@RequestBody Book book){
        return service.saveBook(book);
    }

    @GetMapping("/{id}")
    public Book findBookById(@PathVariable("id") int id){
        return service.getBookById(id);
    }

    @PutMapping("update_book")
    public Book updateBook(Book book){
        return service.updateBook(book);
    }

    @DeleteMapping("delete_book/{id}")
    public void deleteBook(@PathVariable("id") int id){
        service.deleteBook(id);
    }

    @GetMapping("/searchByTitle")
    public Book getBookByTitle(@RequestParam String title){
        return service.getBookByTitle(title);
    }

}
