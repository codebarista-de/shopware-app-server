package com.appbackend.repository;

import com.appbackend.entity.SystemMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SystemMessageRepositoryTest {

    @Autowired
    private SystemMessageRepository repository;

    @Test
    void shouldSaveAndRetrieveSystemMessage() {
        // Given
        SystemMessage message = new SystemMessage(
                "System maintenance scheduled for tonight",
                "Admin",
                true
        );

        // When
        SystemMessage saved = repository.save(message);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMessage()).isEqualTo("System maintenance scheduled for tonight");
        assertThat(saved.getCreator()).isEqualTo("Admin");
        assertThat(saved.getActive()).isTrue();

        // Verify we can find it again
        Optional<SystemMessage> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMessage()).isEqualTo("System maintenance scheduled for tonight");
    }

    @Test
    void shouldFindActiveSystemMessage() {
        // Given
        SystemMessage inactive = new SystemMessage(
                "Old system message",
                "Admin",
                false
        );
        repository.save(inactive);

        SystemMessage active = new SystemMessage(
                "Current system message active",
                "System",
                true
        );
        repository.save(active);

        // When
        Optional<SystemMessage> result = repository.findFirstByActiveTrue();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("Current system message active");
        assertThat(result.get().getActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNoActiveSystemMessage() {
        // Given
        SystemMessage inactive = new SystemMessage(
                "Inactive message",
                "Admin",
                false
        );
        repository.save(inactive);

        // When
        Optional<SystemMessage> result = repository.findFirstByActiveTrue();

        // Then
        assertThat(result).isEmpty();
    }
}
