package ru.store.springbooks.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.store.springbooks.model.Book;



@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleIgnoreCase(String title);

    List<Book> findByAuthorIgnoreCase(String author);

    List<Book> findByYear(int year);

    List<Book> findByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);

    List<Book> findByTitleIgnoreCaseAndAuthorIgnoreCaseAndYear(String title,
                                                               String author,
                                                               int year);
}