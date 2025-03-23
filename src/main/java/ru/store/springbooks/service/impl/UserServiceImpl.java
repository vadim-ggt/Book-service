package ru.store.springbooks.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.User;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.service.UserService;
import ru.store.springbooks.utils.InMemoryCache;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InMemoryCache<Long, User> userCache;


    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User saveUser(User user) {

        User savedUser = userRepository.save(user);

        userCache.put(savedUser.getId(), savedUser);
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        userCache.put(id, user);
        log.info("User fetched from DB and added to cache: {}", user);
        return user;
    }

    @Override
    public boolean deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);

            userCache.evict(id);
            log.info("User deleted from DB and cache: {}", user.get());
            return true;
        } else {
            return false;
        }
    }


    @Override
    public List<Library> getUserLibraries(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getLibraries();
    }


    @Override
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(updatedUser.getUsername());
        user.setPassword(updatedUser.getPassword());
        user.setEmail(updatedUser.getEmail());

        User savedUser = userRepository.save(user);

        userCache.put(savedUser.getId(), savedUser);
        log.info("User updated in DB and cache: {}", savedUser);
        return savedUser;
    }

}