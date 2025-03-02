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


    public Book getBookByTitle(Map<String, String> params) {
        return books.stream()
                .filter(book ->
                        !params.containsKey("title") || book.getTitle()
                                .equalsIgnoreCase(params.get("title")))
                .filter(book ->
                        !params.containsKey("author") || book.getAuthor()
                                .equalsIgnoreCase(params.get("author")))
                .filter(book -> {
                    return !params.containsKey("year")
                            || book.getYear() == Integer.parseInt(params.get("year"));
                }) // Совпадение по году, если передан параметр year
                .findFirst() // Берем первый найденный элемент
                .orElseThrow(()
                        -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }
}
