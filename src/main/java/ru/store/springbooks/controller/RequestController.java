package ru.store.springbooks.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.enums.RequestStatus;
import ru.store.springbooks.service.RequestService;

@RestController
@Tag(name = "Request Controller", description = "API для управления заявками")
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;


    @PostMapping("/create/{bookId}/{userId}")
    @Operation(summary = "Создать заявку", description = "Создает новую заявку в библиотеке")
    @ApiResponse(responseCode = "200", description = "Заявка успешно создана")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    public ResponseEntity<Request> createRequest(@PathVariable Long bookId,
                                                 @PathVariable Long userId) {
        return ResponseEntity.ok(requestService.createRequest(bookId, userId));
    }


    @GetMapping("/by-book/{bookId}")
    @Operation(summary = "Получить заявку по ID", description = "Возвращает заявку по идентификатору")
    @ApiResponse(responseCode = "200", description = "Заявка успешно найдена")
    @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    public ResponseEntity<List<Request>> getRequestsByBookId(@PathVariable Long bookId) {
        return ResponseEntity.ok(requestService.getRequestsByBook(bookId));
    }

    @Operation(
            summary = "Получить заявки пользователя",
            description = "Возвращает список всех заявок, сделанных пользователем по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<Request>> getRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.getRequestsByUser(userId));
    }


    @PatchMapping("/{requestId}/status")
    @Operation(summary = "Обновить статус заявки", description = "Обновляет информацию о заявке")
    @ApiResponse(responseCode = "200", description = "Заявка успешно обновлена")
    @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    public ResponseEntity<Void> updateRequestStatus(@PathVariable Long requestId,
                                                    @RequestParam RequestStatus status) {
        requestService.updateRequestStatus(requestId, status);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получить заявки пользователя по статусу",
            description = "Возвращает список заявок пользователя по его имени и статусу заявки"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок успешно получен"),
            @ApiResponse(responseCode = "400", description = "Неверный статус заявки или отсутствуют параметры"),
            @ApiResponse(responseCode = "404", description = "Пользователь или заявки не найдены"),
    })
    @GetMapping("/by-user-status")
    public ResponseEntity<List<Request>> getRequestsByUserAndStatus(
            @RequestParam String userName,
            @RequestParam RequestStatus status) {
        return ResponseEntity.ok(requestService.getRequestsByUserAndStatus(userName, status));
    }
}