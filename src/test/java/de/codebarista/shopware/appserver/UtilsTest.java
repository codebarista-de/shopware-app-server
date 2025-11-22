package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.util.Json;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {
    record TestObjectWithDate (OffsetDateTime dateTime) {};

    @Test
    public void jsonDateSerialization() {
        var testObjectWithDate = new TestObjectWithDate(OffsetDateTime.parse("2024-10-20T00:00:00.000+00:00"));
        String orderJson = Json.toJson(testObjectWithDate);
        assertThat(orderJson).contains("\"dateTime\":\"2024-10-20T00:00:00.000+00:00\"");
    }
}
