package ru.store.springbooks.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.service.LibraryService;

@RestController
@RequestMapping("/api/v1/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public List<Library> getAllLibraries() {
        return libraryService.findAllLibraries();
    }

    @PostMapping
    public ResponseEntity<Library> createLibrary(@RequestBody Library library) {
        Library savedLibrary = libraryService.saveLibrary(library);
        return ResponseEntity.ok(savedLibrary);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Library> getLibraryById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(libraryService.getLibraryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLibrary(@PathVariable Long id) {
        try {
            boolean isDeleted = libraryService.deleteLibrary(id);
            if (isDeleted) {
                return ResponseEntity.ok("Библиотека успешно удалена");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Библиотека с данным ID не найдена");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при удалении библиотеки: " + e.getMessage());
        }
    }


    @PostMapping("/{libraryId}/addUser/{userId}")
    public ResponseEntity<Library>  addUserToLibrary(@PathVariable Long libraryId,
                                                    @PathVariable Long userId) {
        try {
            Library updatedLibrary = libraryService.addUserToLibrary(libraryId, userId);
            return ResponseEntity.ok(updatedLibrary);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Library> updateLibrary(@PathVariable Long id,
                                                 @RequestBody Library updatedLibrary) {
        Library library = libraryService.updateLibrary(id, updatedLibrary);
        return ResponseEntity.ok(library);
    }

}
