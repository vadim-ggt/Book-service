package ru.store.springbooks.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.store.springbooks.model.Request;
import ru.store.springbooks.model.enums.RequestStatus;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findRequestsByBookId(Long bookId);

    List<Request> findRequestsByUserId(Long userId);

    @Query("SELECT r FROM Request r WHERE r.endDate < :date AND r.status <> :status")
    List<Request> findAllByEndDateBeforeAndStatusNot(@Param("date") LocalDateTime date,
                                                     @Param("status") RequestStatus status);

    @Query("SELECT r FROM Request r WHERE r.user.username = :userName AND r.status = :status")
    List<Request> findAllByUserAndStatus(@Param("userName") String userName,
                                         @Param("status") RequestStatus status);

}