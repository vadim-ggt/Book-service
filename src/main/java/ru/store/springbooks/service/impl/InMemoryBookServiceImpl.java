package ru.store.springbooks.service.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.repository.InMemoryBookDao;
import ru.store.springbooks.service.BookService;

@Service
@AllArgsConstructor
public class InMemoryBookServiceImpl implements BookService {

    private final InMemoryBookDao repository;

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
    public void deleteBook(int id) {
        repository.deleteBook(id);
    }

    @Override
    public Book getBookByTitle(Map<String, String> params) {
        return repository.getBookByTitle(params);
    }
}
