package ru.store.springbooks.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.User;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.service.LibraryService;

@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;

    @Override
    public List<Library> findAllLibraries() {
        return libraryRepository.findAll();
    }

    @Override
    public Library saveLibrary(Library library) {
        return libraryRepository.save(library);
    }

    @Override
    public Library getLibraryById(Long id) {
        return libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library not found"));
    }

    @Override
    public void deleteLibrary(Long id) {
        libraryRepository.deleteById(id);
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

        libraryRepository.save(library.get());
        userRepository.save(user.get());

        return library.get();
    }

    @Override
    public Library updateLibrary(Long id, Library updatedLibrary) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library not found"));
        library.setName(updatedLibrary.getName());
        library.setAddress(updatedLibrary.getAddress());
        return libraryRepository.save(library);
    }
}
