package ru.store.springbooks.service;

import java.util.List;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.User;

public interface UserService {

    List<User> findAllUsers();

    User saveUser(User user);

    User getUserById(Long id);

    void deleteUser(Long id);

    List<Library> getUserLibraries(Long id);  // Получить библиотеки пользователя

    List<Book> getUserBooks(Long id);

    User updateUser(Long id, User updatedUser);

    Book rentBook(Long userId, Long bookId);
}