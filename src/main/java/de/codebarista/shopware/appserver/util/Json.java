package de.codebarista.shopware.appserver.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for JSON serialization compatible with Shopware's date/time format.
 * <p>
 * This utility provides JSON serialization configured to match Shopware Admin API conventions,
 * including the specific date-time format: {@code yyyy-MM-dd'T'HH:mm:ss.SSSxxx}
 */
public class Json {
    private Json() {

    }

    private static final ObjectWriter JSON_WRITER;

    static {
        // Use same format as shopware's admin api
        var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");
        var timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateFormatter));
        timeModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer(
                OffsetDateTimeSerializer.INSTANCE,
                false,
                dateFormatter,
                JsonFormat.Shape.STRING)
        );
        JSON_WRITER = JsonMapper.builder()
                .addModule(timeModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build()
                .writer();
    }

    /**
     * Converts an object to its JSON string representation.
     * <p>
     * Uses Jackson serialization configured for Shopware compatibility.
     * Date/time values are formatted as {@code yyyy-MM-dd'T'HH:mm:ss.SSSxxx}.
     *
     * @param obj the object to serialize
     * @return the JSON string representation, or the error message if serialization fails
     */
    public static String toJson(Object obj) {
        try {
            return JSON_WRITER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}
