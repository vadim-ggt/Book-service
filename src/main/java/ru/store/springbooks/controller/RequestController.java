package ru.store.springbooks.controller;

import java.util.List;
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
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;


    @PostMapping("/create/{bookId}/{userId}")
    public ResponseEntity<Request> createRequest(@PathVariable Long bookId,
                                                 @PathVariable Long userId) {
        return ResponseEntity.ok(requestService.createRequest(bookId, userId));
    }


    @GetMapping("/by-book/{bookId}")
    public ResponseEntity<List<Request>> getRequestsByBookId(@PathVariable Long bookId) {
        return ResponseEntity.ok(requestService.getRequestsByBook(bookId));
    }


    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<Request>> getRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.getRequestsByUser(userId));
    }


    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> updateRequestStatus(@PathVariable Long requestId,
                                                    @RequestParam RequestStatus status) {
        requestService.updateRequestStatus(requestId, status);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/by-user-status")
    public ResponseEntity<List<Request>> getRequestsByUserAndStatus(
            @RequestParam String userName,
            @RequestParam RequestStatus status) {
        return ResponseEntity.ok(requestService.getRequestsByUserAndStatus(userName, status));
    }
}