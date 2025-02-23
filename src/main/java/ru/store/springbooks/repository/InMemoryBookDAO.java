package ru.store.springbooks.repository;

import org.springframework.stereotype.Repository;
import ru.store.springbooks.model.Book;

import java.util.stream.IntStream;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryBookDAO {

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
                .filter(element->element.getId()==id)
                .findFirst()
                .orElse(null);
    }

    public Book updateBook(Book book) {
        var bookIndex = IntStream.range(0, books.size())
                .filter(index-> books.get(index).getTitle().equals(book.getTitle()))
                .findFirst()
                        .orElse(-1);

        if(bookIndex>-1){
            books.set(bookIndex, book);
                return book;
        }

        return null;
    }


    public void deleteBook(int id) {
        var book=getBookById(id);
        if(book !=null)
            books.remove(book);
    }


    public Book getBookByTitle(String title) {
        return books.stream()
                .filter(book -> book.getTitle().equals(title)) // Совпадение по названию
                .findFirst() // Берем первый найденный элемент
                .orElse(null); // Если книги не найдены, возвращаем null
    }

}
