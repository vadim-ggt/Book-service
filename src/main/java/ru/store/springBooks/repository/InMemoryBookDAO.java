package ru.store.springBooks.repository;

import org.springframework.stereotype.Repository;
import ru.store.springBooks.model.Book;

import java.util.Objects;
import java.util.stream.IntStream;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryBookDAO {

    private final List<Book> BOOKS = new ArrayList<>();


    public List<Book> findAllBooks() {
        return BOOKS;
    }

    public Book saveBook(Book book) {
        BOOKS.add(book);
        return book;
    }

    public Book getBookById(int id) {
        return BOOKS.stream()
                .filter(element->element.getId()==id)
                .findFirst()
                .orElse(null);
    }

    public Book updateBook(Book book) {
        var bookIndex = IntStream.range(0, BOOKS.size())
                .filter(index->BOOKS.get(index).getTitle().equals(book.getTitle()))
                .findFirst()
                        .orElse(-1);

        if(bookIndex>-1){
            BOOKS.set(bookIndex, book);
                return book;
        }

        return null;
    }


    public void deleteBook(int id) {
        var book=getBookById(id);
        if(book !=null)
            BOOKS.remove(book);
    }


    public Book getBookByTitle(String title) {
        return BOOKS.stream()
                .filter(book -> book.getTitle().equals(title)) // Совпадение по названию
                .findFirst() // Берем первый найденный элемент
                .orElse(null); // Если книги не найдены, возвращаем null
    }

}
