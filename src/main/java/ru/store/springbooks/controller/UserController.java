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
import ru.store.springbooks.model.User;
import ru.store.springbooks.service.UserService;

@RestController
@Tag(name = "User Controller", description = "API для управления пользователями")
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить всех  пользователей", description = "Возвращает список всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }


    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по идентификатору")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по идентификатору")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно удален")
    @ApiResponse(responseCode = "404", description = "Пользователь не найдена")

    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            boolean isDeleted = userService.deleteUser(id);
            if (isDeleted) {
                return ResponseEntity.ok("Пользователь успешно удален");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Пользователь с данным ID не найден");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при удалении пользователя: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Получить библиотеки пользователя",
            description = "Возвращает список библиотек, в которых состоит пользователь по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список библиотек успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}/libraries")
    public ResponseEntity<List<?>> getUserLibraries(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserLibraries(id));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя", description = "Обновляет информацию о пользователе")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлена")
    @ApiResponse(responseCode = "404", description = "Пользователь не найдена")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
            User user = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
    }


}
