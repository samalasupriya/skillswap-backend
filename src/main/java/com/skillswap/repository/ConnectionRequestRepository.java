package com.skillswap.repository;

import com.skillswap.model.ConnectionRequest;
import com.skillswap.model.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    List<ConnectionRequest> findBySenderId(Long senderId);

    // is there already a PENDING or ACCEPTED request between these two users (either direction)?
    @Query("select count(c) > 0 from ConnectionRequest c " +
           "where ((c.sender.id = :a and c.receiver.id = :b) or (c.sender.id = :b and c.receiver.id = :a)) " +
           "and c.status in (com.skillswap.model.ConnectionStatus.PENDING, com.skillswap.model.ConnectionStatus.ACCEPTED)")
    boolean existsActiveBetween(@Param("a") Long a, @Param("b") Long b);

    @Query("select c from ConnectionRequest c " +
           "where c.status = com.skillswap.model.ConnectionStatus.ACCEPTED " +
           "and (c.sender.id = :id or c.receiver.id = :id)")
    List<ConnectionRequest> findAcceptedFor(@Param("id") Long id);
}