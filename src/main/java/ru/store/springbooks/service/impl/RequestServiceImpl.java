    package ru.store.springbooks.service.impl;


    import lombok.RequiredArgsConstructor;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Service;
    import ru.store.springbooks.repository.BookRepository;
    import ru.store.springbooks.repository.RequestRepository;
    import ru.store.springbooks.repository.UserRepository;
    import ru.store.springbooks.model.Request;
    import ru.store.springbooks.model.Book;
    import ru.store.springbooks.model.User;
    import ru.store.springbooks.model.enums.RequestStatus;
    import ru.store.springbooks.service.RequestService;
    import ru.store.springbooks.utils.InMemoryCache;

    import java.time.LocalDateTime;
    import java.util.List;


    @Service
    @RequiredArgsConstructor
    public class RequestServiceImpl implements RequestService {

        private final RequestRepository requestRepository;
        private final BookRepository bookRepository;
        private final UserRepository userRepository;
        private final InMemoryCache<Long, Request> requestCache;


        @Override
        public Request createRequest(Long bookId, Long userId) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Книга не найдена"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Request request = Request.builder()
                    .book(book)
                    .user(user)
                    .status(RequestStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();


            Request savedRequest = requestRepository.save(request);


            requestCache.put(savedRequest.getId(), savedRequest);

            return savedRequest;
        }

        @Override
        public void updateRequestStatus(Long requestId, RequestStatus status) {
            Request request = requestCache.get(requestId);
            if (request == null) {
                request = requestRepository.findById(requestId)
                        .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
            }

            request.setStatus(status);

            if (status == RequestStatus.ACTIVE) {
                request.setStartDate(LocalDateTime.now());
                request.setEndDate(LocalDateTime.now().plusMinutes(1));
            }

            requestRepository.save(request);
            requestCache.put(request.getId(), request);
        }

        @Override
        public List<Request> getRequestsByBook(Long bookId) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Книга не найдена"));
            return requestRepository.findRequestsByBookId(bookId);
        }

        @Override
        public List<Request> getRequestsByUser(Long userId) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            return requestRepository.findRequestsByUserId(userId);
        }

        @Override
        public void updateOverdueRequests() {
            LocalDateTime today = LocalDateTime.now();

            List<Request> overdueRequests = requestRepository.findAllByEndDateBeforeAndStatusNot(today, RequestStatus.RETURNED);

            for (Request request : overdueRequests) {
                request.setStatus(RequestStatus.OVERDUE);
            }

            requestRepository.saveAll(overdueRequests);
        }

        @Override
        public List<Request> getRequestsByUserAndStatus(Long userId, RequestStatus status) {
            return requestRepository.findAllByUserAndStatus(userId, status);
        }

        @Scheduled(cron = "0 0 * * * ?")
        public void scheduledOverdueUpdate() {
            updateOverdueRequests();
        }

    }
