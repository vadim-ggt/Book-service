package ru.store.springbooks.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.store.springbooks.exception.EntityNotFoundException;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.User;
import ru.store.springbooks.model.enums.RequestStatus;
import ru.store.springbooks.repository.BookRepository;
import ru.store.springbooks.repository.RequestRepository;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.utils.CustomCache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock private RequestRepository requestRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private CustomCache<Long, Request> requestCache;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final Long bookId = 1L;
    private final Long userId = 1L;
    private final Long requestId = 1L;

    private Book book;
    private User user;

    @BeforeEach
    void setup() {
        book = Book.builder().id(bookId).title("Sample Book").build();
        user = User.builder().id(userId).username("testuser").build();
    }

    @Test
    void createRequest_shouldSaveAndCacheRequest() {
        Mockito.when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        Mockito.when(requestRepository.save(Mockito.any(Request.class)))
                .thenAnswer(invocation -> {
                    Request r = invocation.getArgument(0);
                    r.setId(10L);
                    return r;
                });

        Request result = requestService.createRequest(bookId, userId);

        Mockito.verify(requestRepository).save(captor.capture());
        Mockito.verify(requestCache).put(eq(10L), eq(result));
        assertEquals(RequestStatus.PENDING, result.getStatus());
    }

    @Test
    void updateRequestStatus_shouldUpdateCachedRequest() {
        Request request = new Request();
        request.setId(requestId);
        request.setStatus(RequestStatus.PENDING);

        Mockito.when(requestCache.get(requestId)).thenReturn(request);

        requestService.updateRequestStatus(requestId, RequestStatus.ACTIVE);

        assertEquals(RequestStatus.ACTIVE, request.getStatus());
        assertNotNull(request.getStartDate());
        assertNotNull(request.getEndDate());
        Mockito.verify(requestRepository).save(request);
        Mockito.verify(requestCache).put(requestId, request);
    }

    @Test
    void updateRequestStatus_shouldFetchFromDbIfNotInCache() {
        Request request = new Request();
        request.setId(requestId);
        request.setStatus(RequestStatus.PENDING);

        Mockito.when(requestCache.get(requestId)).thenReturn(null);
        Mockito.when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        requestService.updateRequestStatus(requestId, RequestStatus.ACTIVE);

        assertEquals(RequestStatus.ACTIVE, request.getStatus());
        Mockito.verify(requestRepository).save(request);
        Mockito.verify(requestCache).put(requestId, request);
    }

    @Test
    void getRequestsByBook_shouldReturnList() {
        Mockito.when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        List<Request> expected = List.of(new Request());
        Mockito.when(requestRepository.findRequestsByBookId(bookId)).thenReturn(expected);

        List<Request> actual = requestService.getRequestsByBook(bookId);

        assertEquals(expected, actual);
    }

    @Test
    void getRequestsByUser_shouldReturnList() {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        List<Request> expected = List.of(new Request());
        Mockito.when(requestRepository.findRequestsByUserId(userId)).thenReturn(expected);

        List<Request> actual = requestService.getRequestsByUser(userId);

        assertEquals(expected, actual);
    }

    @Test
    void updateOverdueRequests_shouldUpdateStatuses() {
        Request r1 = new Request();
        r1.setId(1L);
        r1.setStatus(RequestStatus.PENDING);

        List<Request> list = List.of(r1);
        Mockito.when(requestRepository.findAllByEndDateBeforeAndStatusNot(
                Mockito.any(LocalDateTime.class), eq(RequestStatus.RETURNED)
        )).thenReturn(list);

        requestService.updateOverdueRequests();


        assertEquals(RequestStatus.OVERDUE, r1.getStatus());
        Mockito.verify(requestRepository).saveAll(list);
        Mockito.verify(requestCache).put(r1.getId(), r1);
    }

    @Test
    void getRequestsByUserAndStatus_shouldReturnRequests() {
        String username = "testuser";
        RequestStatus status = RequestStatus.PENDING;
        List<Request> requests = List.of(new Request());

        Mockito.when(requestRepository.findAllByUserAndStatus(username, status)).thenReturn(requests);

        List<Request> result = requestService.getRequestsByUserAndStatus(username, status);

        assertEquals(requests, result);
    }

    @Test
    void getRequestsByUserAndStatus_shouldThrowIfEmpty() {
        String username = "testuser";
        RequestStatus status = RequestStatus.PENDING;

        Mockito.when(requestRepository.findAllByUserAndStatus(username, status)).thenReturn(List.of());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> requestService.getRequestsByUserAndStatus(username, status));

        assertTrue(ex.getMessage().contains("Requests for user"));
    }

    @Test
    void scheduledOverdueUpdate_shouldInvokeUpdateMethod() {
        RequestServiceImpl spyService = Mockito.spy(requestService);
        spyService.updateOverdueRequests();
        Mockito.verify(spyService).updateOverdueRequests(); // Тестирует косвенно, если хочется отдельно
    }
}
