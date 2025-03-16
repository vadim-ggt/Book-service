package ru.store.springbooks.service;

import java.util.List;
import ru.store.springbooks.model.Library;

public interface LibraryService {

    List<Library> findAllLibraries();

    Library saveLibrary(Library library);

    Library getLibraryById(Long id);

    void deleteLibrary(Long id);

    Library addUserToLibrary(Long libraryId, Long userId);  // Добавить пользователя в библиотеку

    Library updateLibrary(Long id, Library updatedLibrary);

}