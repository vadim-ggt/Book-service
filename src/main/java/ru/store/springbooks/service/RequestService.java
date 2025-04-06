package ru.store.springbooks.service;

import java.util.List;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.enums.RequestStatus;



public interface RequestService {

    Request createRequest(Long bookId, Long userId);

    void updateRequestStatus(Long requestId, RequestStatus status);

    List<Request> getRequestsByBook(Long bookId);

    List<Request> getRequestsByUser(Long userId);

    void updateOverdueRequests();

    List<Request> getRequestsByUserAndStatus(String userName, RequestStatus status);
}
