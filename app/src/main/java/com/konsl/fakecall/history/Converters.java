package com.konsl.fakecall.history;

import androidx.room.TypeConverter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Converters {
    @TypeConverter
    public static LocalDateTime toDateTime(Long epochSeconds){
        return epochSeconds == null ? null : LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
    }

    @TypeConverter
    public static Long toEpochSeconds(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
