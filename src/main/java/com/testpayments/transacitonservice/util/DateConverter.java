package com.testpayments.transacitonservice.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@UtilityClass
public class DateConverter {

    private static final String EXP_DATE_FORMAT = "MM/yy";
    private static final int NEXT_UNIT_INCREMENT = 1;
    private static final int MINUTES_TO_DECREMENT = 1;

    public static LocalDateTime convertStringToLocalDateTime(String expDateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EXP_DATE_FORMAT);
        YearMonth expDate = YearMonth.parse(expDateString, formatter);
        return expDate.plusMonths(NEXT_UNIT_INCREMENT).atDay(NEXT_UNIT_INCREMENT).atStartOfDay().minusMinutes(MINUTES_TO_DECREMENT);
    }

    public static LocalDateTime convertUnixTimestampToLocalDateTime(Long unixTimestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp),
                TimeZone.getDefault().toZoneId());
    }
}
