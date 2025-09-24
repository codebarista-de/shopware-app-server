package de.codebarista.shopware.appbackend;

import de.codebarista.shopware.appbackend.sdk.util.JsonUtils;
import de.codebarista.shopware.model.core.Order;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {
    @Test
    public void jsonDateSerialization() {
        var order = new Order();
        order.setOrderDateTime(OffsetDateTime.parse("2024-10-20T00:00:00.000+00:00"));
        String orderJson = JsonUtils.toJson(order);
        assertThat(orderJson).contains("\"orderDateTime\":\"2024-10-20T00:00:00.000+00:00\"");
    }
}
