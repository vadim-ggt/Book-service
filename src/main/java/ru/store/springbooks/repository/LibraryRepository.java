package ru.store.springbooks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.store.springbooks.model.Library;


@Repository
public interface LibraryRepository extends JpaRepository<Library, Long> {
}
