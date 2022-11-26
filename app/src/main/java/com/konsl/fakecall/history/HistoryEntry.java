package com.konsl.fakecall.history;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class HistoryEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    public LocalDateTime time;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryEntry that = (HistoryEntry) o;
        return id == that.id && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, phoneNumber, time);
    }
}
