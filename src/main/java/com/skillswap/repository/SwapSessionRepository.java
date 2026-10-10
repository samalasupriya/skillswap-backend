package com.skillswap.repository;

import com.skillswap.model.SwapSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SwapSessionRepository extends JpaRepository<SwapSession, Long> {

    @Query("select s from SwapSession s " +
           "where s.connection.sender.id = :id or s.connection.receiver.id = :id " +
           "order by s.scheduledAt")
    List<SwapSession> findForUser(@Param("id") Long id);
}