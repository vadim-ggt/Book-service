package ru.store.springBooks.service;


import org.springframework.stereotype.Service;
import ru.store.springBooks.model.Book;

import java.util.List;



public interface BookService {
     List<Book> findAllBooks();
     Book saveBook(Book book);
     Book getBookById(int id);
     Book updateBook(Book book);
     void deleteBook(int id);
     Book getBookByTitle(String title);
}
