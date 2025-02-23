package ru.store.springBooks.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.store.springBooks.model.Book;
import ru.store.springBooks.repository.InMemoryBookDAO;
import ru.store.springBooks.service.BookService;

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
