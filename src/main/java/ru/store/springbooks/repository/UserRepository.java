package ru.store.springbooks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.store.springbooks.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}