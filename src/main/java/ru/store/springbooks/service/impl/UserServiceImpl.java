package ru.store.springbooks.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.exception.EmailAlreadyExistsException;
import ru.store.springbooks.exception.EntityNotFoundException;
import ru.store.springbooks.exception.InvalidPasswordException;
import ru.store.springbooks.exception.UsernameAlreadyExistsException;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.User;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.service.UserService;
import ru.store.springbooks.utils.CustomCache;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomCache<Long, User> userCache;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    @Override
    public User saveUser(User user) {
        // Проверка на уникальность email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        // Проверка на уникальность username
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException(user.getUsername());
        }

        // Проверка пароля
        if (!isPasswordValid(user.getPassword())) {
            throw new InvalidPasswordException();
        }

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);

        // Добавляем пользователя в кеш
        userCache.put(savedUser.getId(), savedUser);
        log.info("User saved and added to cache: {}", savedUser);

        return savedUser;
    }

    @Override
    public User getUserById(Long id) {
        User cachedUser = userCache.get(id);
        if (cachedUser != null) {
            log.info("User fetched from cache: {}", cachedUser);
            return cachedUser;
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        userCache.put(id, user);
        log.info("User fetched from DB and added to cache: {}", user);
        return user;
    }


    @Override
    public boolean deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        userRepository.deleteById(id);
        userCache.remove(id);
        log.info("User deleted from DB and cache: {}", id);
        return true;
    }


    @Override
    public List<Library> getUserLibraries(Long id) {
        User user = getUserById(id);
        return user.getLibraries();
    }


    @Override
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        user.setUsername(updatedUser.getUsername());
        user.setPassword(updatedUser.getPassword());
        user.setEmail(updatedUser.getEmail());

        User savedUser = userRepository.save(user);
        userCache.put(savedUser.getId(), savedUser);
        log.info("User updated in DB and cache: {}", savedUser);
        return savedUser;
    }



    private boolean isPasswordValid(String password) {

        if (password.length() < 8) {
            return false;
        }


        Pattern digitPattern = Pattern.compile(".*\\d.*");
        Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");

        if (!digitPattern.matcher(password).matches()) {
            return false;
        }

        return upperCasePattern.matcher(password).matches();

    }


}
