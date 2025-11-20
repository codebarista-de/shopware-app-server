package com.appbackend.repository;

import com.appbackend.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    /**
     * Find all events for a specific shop, ordered by received time descending.
     */
    List<EventLog> findByShopIdOrderByReceivedAtDesc(Long shopId);

    /**
     * Find all events with a specific name for a shop.
     */
    List<EventLog> findByShopIdAndEventName(Long shopId, String eventName);
}
