package ru.store.springbooks.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Library Controller", description = "API для управления библиотеками")
@RequestMapping("/api/v1/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;


    @GetMapping
    @Operation(summary = "Получить все библиотеки", description = "Возвращает список всех библиотек")
    @ApiResponse(responseCode = "200", description = "Список библиотек успешно получен")
    public List<Library> getAllLibraries() {
        return libraryService.findAllLibraries();
    }


    @PostMapping
    @Operation(summary = "Создать библиотеку", description = "Создает новую библиотеку ")
    @ApiResponse(responseCode = "200", description = "Библиотека успешно создана")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    public ResponseEntity<Library> createLibrary(@RequestBody Library library) {
        Library savedLibrary = libraryService.saveLibrary(library);
        return ResponseEntity.ok(savedLibrary);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Получить библиотеку по ID", description = "Возвращает библиотеку по идентификатору")
    @ApiResponse(responseCode = "200", description = "Библиотека успешно найдена")
    @ApiResponse(responseCode = "404", description = "Библиотека не найдена")
    public ResponseEntity<Library> getLibraryById(@PathVariable Long id) {
        Library library = libraryService.getLibraryById(id);

        return ResponseEntity.ok(library);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить библиотеку", description = "Удаляет библиотеку по идентификатору")
    @ApiResponse(responseCode = "200", description = "Библиотека успешно удалена")
    @ApiResponse(responseCode = "404", description = "Библиотека не найдена")
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

    @Operation(
            summary = "Добавить пользователя в библиотеку",
            description = "Добавляет пользователя с указанным ID в библиотеку с заданным ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно добавлен в библиотеку"),
            @ApiResponse(responseCode = "404", description = "Пользователь или библиотека не найдены")
    })
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
    @Operation(summary = "Обновить библиотеку", description = "Обновляет информацию о библиотеки")
    @ApiResponse(responseCode = "200", description = "Библиотека успешно обновлена")
    @ApiResponse(responseCode = "404", description = "Библиотека не найдена")

    public ResponseEntity<Library> updateLibrary(@PathVariable Long id,
                                                 @RequestBody Library updatedLibrary) {
        Library library = libraryService.updateLibrary(id, updatedLibrary);
        return ResponseEntity.ok(library);
    }

}
