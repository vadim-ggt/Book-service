package ru.store.springbooks.service;

import java.util.List;
import ru.store.springbooks.model.Book;


public interface BookService {

    List<Book> findAllBooks();

    Book saveBook(Book book);

    Book getBookById(int id);

    void deleteBook(int id);

    Book getBookByTitle(Map<String, String> params);
}
