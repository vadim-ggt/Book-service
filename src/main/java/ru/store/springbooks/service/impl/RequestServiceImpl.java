package ru.store.springbooks.service.impl;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.store.springbooks.exception.EntityNotFoundException;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.User;
import ru.store.springbooks.model.enums.RequestStatus;
import ru.store.springbooks.repository.BookRepository;
import ru.store.springbooks.repository.RequestRepository;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.service.RequestService;
import ru.store.springbooks.utils.CustomCache;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CustomCache<Long, Request> requestCache;


    @Override
    public Request createRequest(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book", bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        Request request = Request.builder()
                .book(book)
                .user(user)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Request savedRequest = requestRepository.save(request);
        requestCache.put(savedRequest.getId(), savedRequest);

        log.info("Created and cached new request: {}", savedRequest);
        return savedRequest;
    }


    @Override
    public void updateRequestStatus(Long requestId, RequestStatus status) {


        Request request = requestCache.get(requestId);
        if (request == null) {
            request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("Request", requestId));
        }

        request.setStatus(status);
        if (status == RequestStatus.ACTIVE) {
            request.setStartDate(LocalDateTime.now());
            request.setEndDate(LocalDateTime.now().plusMinutes(1));
        }


        requestRepository.save(request);
        requestCache.put(request.getId(), request);

        log.info("Updated request status to {}: {}", status, request);
    }


    @Override
    public List<Request> getRequestsByBook(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book", bookId));
        return requestRepository.findRequestsByBookId(bookId);
    }


    @Override
    public List<Request> getRequestsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        return requestRepository.findRequestsByUserId(userId);
    }


    @Override
    public void updateOverdueRequests() {
        LocalDateTime today = LocalDateTime.now();
        List<Request> overdueRequests = requestRepository
                .findAllByEndDateBeforeAndStatusNot(today, RequestStatus.RETURNED);

        for (Request request : overdueRequests) {
            request.setStatus(RequestStatus.OVERDUE);
            requestCache.put(request.getId(), request);
        }

        requestRepository.saveAll(overdueRequests);
        log.info("Updated {} overdue requests", overdueRequests.size());
    }


    @Override
    public List<Request> getRequestsByUserAndStatus(String userName, RequestStatus status) {

        List<Request> requests = requestRepository.findAllByUserAndStatus(userName, status);

        if (requests.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("Requests for user '%s' with status '%s'", userName, status)
            );
        }

        return requests;
    }


    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledOverdueUpdate() {
        updateOverdueRequests();
    }
}
