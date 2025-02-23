package ru.store.springbooks.service;


import ru.store.springbooks.model.Book;

import java.util.List;



public interface BookService {
     List<Book> findAllBooks();
     Book saveBook(Book book);
     Book getBookById(int id);
     Book updateBook(Book book);
     void deleteBook(int id);
     Book getBookByTitle(String title);
}
