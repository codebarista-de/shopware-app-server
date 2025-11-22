package com.appbackend.repository;

import com.appbackend.entity.EventLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class EventLogRepositoryTest {

    @Autowired
    private EventLogRepository repository;

    @BeforeEach
    void clear() {
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveEventLog() {
        // Given
        EventLog event = new EventLog(
                1L,
                "order.written",
                LocalDateTime.now()
        );

        // When
        EventLog saved = repository.save(event);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getShopId()).isEqualTo(1L);
        assertThat(saved.getEventName()).isEqualTo("order.written");
        assertThat(saved.getReceivedAt()).isNotNull();
    }

    @Test
    void shouldFindEventsByShopIdOrderedByTime() {
        // Given
        Long shopId = 1L;
        EventLog event1 = new EventLog(shopId, "event.first", LocalDateTime.now().minusHours(2));
        EventLog event2 = new EventLog(shopId, "event.second", LocalDateTime.now().minusHours(1));
        EventLog event3 = new EventLog(shopId, "event.third", LocalDateTime.now());

        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        // When
        List<EventLog> events = repository.findByShopIdOrderByReceivedAtDesc(shopId);

        // Then
        assertThat(events).hasSize(3);
        assertThat(events.get(0).getEventName()).isEqualTo("event.third");
        assertThat(events.get(1).getEventName()).isEqualTo("event.second");
        assertThat(events.get(2).getEventName()).isEqualTo("event.first");
    }

    @Test
    void shouldFindEventsByShopIdAndEventName() {
        // Given
        Long shopId = 1L;
        repository.save(new EventLog(shopId, "order.written", LocalDateTime.now()));
        repository.save(new EventLog(shopId, "order.written", LocalDateTime.now()));
        repository.save(new EventLog(shopId, "product.written", LocalDateTime.now()));

        // When
        List<EventLog> orderEvents = repository.findByShopIdAndEventName(shopId, "order.written");

        // Then
        assertThat(orderEvents).hasSize(2);
        assertThat(orderEvents).allMatch(e -> e.getEventName().equals("order.written"));
    }

    @Test
    void shouldOnlyReturnEventsForSpecificShop() {
        // Given
        repository.save(new EventLog(1L, "event.shop1", LocalDateTime.now()));
        repository.save(new EventLog(2L, "event.shop2", LocalDateTime.now()));

        // When
        List<EventLog> shop1Events = repository.findByShopIdOrderByReceivedAtDesc(1L);
        List<EventLog> shop2Events = repository.findByShopIdOrderByReceivedAtDesc(2L);

        // Then
        assertThat(shop1Events).hasSize(1);
        assertThat(shop1Events.get(0).getShopId()).isEqualTo(1L);

        assertThat(shop2Events).hasSize(1);
        assertThat(shop2Events.get(0).getShopId()).isEqualTo(2L);
    }
}
