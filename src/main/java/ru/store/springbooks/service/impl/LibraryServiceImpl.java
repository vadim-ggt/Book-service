package ru.store.springbooks.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.User;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.service.LibraryService;
import ru.store.springbooks.utils.InMemoryCache;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final InMemoryCache<Long, Library> libraryCache;

    @Override
    public List<Library> findAllLibraries() {
        return libraryRepository.findAll();
    }

    @Override
    public Library saveLibrary(Library library) {
        Library savedLibrary = libraryRepository.save(library);
        libraryCache.put(savedLibrary.getId(), savedLibrary);
        return savedLibrary;
    }


    @Override
    public Library getLibraryById(Long id) {

        Library cachedLibrary = libraryCache.get(id);
        if (cachedLibrary != null) {
            log.info("Library fetched from cache: {}", cachedLibrary);
            return cachedLibrary;
        }

        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library not found"));
        libraryCache.put(id, library);
        log.info("Library fetched from DB and added to cache: {}", library);
        return library;
    }

    @Override
    public boolean deleteLibrary(Long id) {
        Optional<Library> library = libraryRepository.findById(id);
        if (library.isPresent()) {
            libraryRepository.deleteById(id);
            libraryCache.evict(id);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public Library addUserToLibrary(Long libraryId, Long userId) {
        Optional<Library> library = libraryRepository.findById(libraryId);
        Optional<User> user = userRepository.findById(userId);

        if (library.isEmpty() || user.isEmpty()) {
            throw new RuntimeException("Library or User not found");
        }

        library.get().getUsers().add(user.get());
        user.get().getLibraries().add(library.get());

        Library updatedLibrary = libraryRepository.save(library.get());
        userRepository.save(user.get());
        libraryCache.put(updatedLibrary.getId(), updatedLibrary);
        return updatedLibrary;
    }

    @Override
    public Library updateLibrary(Long id, Library updatedLibrary) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library not found"));
        library.setName(updatedLibrary.getName());
        library.setAddress(updatedLibrary.getAddress());

        Library savedLibrary = libraryRepository.save(library);

        libraryCache.put(savedLibrary.getId(), savedLibrary);

        return savedLibrary;
    }
}
