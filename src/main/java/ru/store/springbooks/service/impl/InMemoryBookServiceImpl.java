package ru.store.springbooks.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.repository.InMemoryBookDAO;
import ru.store.springbooks.service.BookService;

import java.util.List;

@Service
@AllArgsConstructor
public class InMemoryBookServiceImpl implements BookService {

    private final InMemoryBookDAO repository;

    @Override
    public List<Book> findAllBooks() {
        return repository.findAllBooks();
    }

    @Override
    public Book saveBook(Book book) {
        return repository.saveBook(book);
    }

    @Override
    public Book getBookById(int id) {
        return repository.getBookById(id);
    }

    @Override
    public Book updateBook(Book book) {
        return repository.updateBook(book);
    }

    @Override
    public void deleteBook(int id) {
        repository.deleteBook(id);
    }

    @Override
    public Book getBookByTitle(String title) {
        return repository.getBookByTitle(title);
    }
}
