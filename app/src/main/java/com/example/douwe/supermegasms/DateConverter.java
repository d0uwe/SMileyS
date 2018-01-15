package com.example.douwe.supermegasms;

/**
 * Created by douwe on 1/15/18.
 */

import android.arch.persistence.room.TypeConverter;

import java.util.Date;
class DateConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}