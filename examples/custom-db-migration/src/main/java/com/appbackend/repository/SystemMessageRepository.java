package com.appbackend.repository;

import com.appbackend.entity.SystemMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {

    /**
     * Find the currently active system message, if any.
     */
    Optional<SystemMessage> findFirstByActiveTrue();
}
