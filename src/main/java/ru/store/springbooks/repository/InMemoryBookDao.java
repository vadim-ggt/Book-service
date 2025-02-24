package ru.store.springbooks.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import ru.store.springbooks.model.Book;

@Repository
public class InMemoryBookDao {

    private final List<Book> books = new ArrayList<>();


    public List<Book> findAllBooks() {
        return books;
    }


    public Book saveBook(Book book) {
        books.add(book);
        return book;
    }


    public Book getBookById(int id) {
        return books.stream()
                .filter(element -> element.getId() == id)
                .findFirst()
                .orElse(null);
    }


    public void deleteBook(int id) {
        var book = getBookById(id);
        if (book != null) {
            books.remove(book);
        }
    }


    public Book getBookByTitle(String title) {
        return books.stream()
                .filter(book -> book.getTitle().equals(title)) // Совпадение по названию
                .findFirst() // Берем первый найденный элемент
                .orElse(null); // Если книги не найдены, возвращаем null
    }
}
