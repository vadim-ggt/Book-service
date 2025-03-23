package ru.store.springbooks.service;

import java.util.List;
import java.util.Map;
import ru.store.springbooks.model.Book;


public interface BookService {

    List<Book> findAllBooks();

    Book saveBook(Book book);

    Book getBookById(Long id);

    boolean deleteBook(Long id);

    List<Book> searchBook(Map<String, String> params);

    Book updateBook(Long id, Book updatedBook);
}
