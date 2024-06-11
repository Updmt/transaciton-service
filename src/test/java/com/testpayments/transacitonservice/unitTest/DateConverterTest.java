package com.testpayments.transacitonservice.unitTest;

import com.testpayments.transacitonservice.util.DateConverter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;

public class DateConverterTest {

    @Test
    void testConvertStringToLocalDateTime_validDate() {
        String validDate = "12/23";
        LocalDateTime result = DateConverter.convertStringToLocalDateTime(validDate);
        LocalDateTime expected = LocalDateTime.of(2023, Month.DECEMBER, 31, 23, 59);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testConvertStringToLocalDateTime_invalidDate() {
        String invalidDate = "13/2023";
        try {
            DateConverter.convertStringToLocalDateTime(invalidDate);
        } catch (DateTimeParseException e) {
            assertThat(e).isInstanceOf(DateTimeParseException.class);
        }
    }

    @Test
    void testConvertUnixTimestampToLocalDateTime() {
        long unixTimestamp = 1609459200;  // Эквивалент 2021-01-01 00:00 GMT
        ZoneId targetZone = ZoneId.systemDefault();
        LocalDateTime expected = LocalDateTime.of(2021, Month.JANUARY, 1, 0, 0)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(targetZone)
                .toLocalDateTime();
        LocalDateTime result = DateConverter.convertUnixTimestampToLocalDateTime(unixTimestamp);
        assertThat(result).isEqualTo(expected);
    }
}
