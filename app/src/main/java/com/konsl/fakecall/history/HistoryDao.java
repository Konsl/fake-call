package com.konsl.fakecall.history;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM HistoryEntry ORDER BY time DESC")
    LiveData<List<HistoryEntry>> getAllLive();

    @Query("SELECT * FROM HistoryEntry one WHERE time =" +
            "(SELECT MAX(two.time) FROM HistoryEntry two WHERE one.phone_number = two.phone_number)" +
            "ORDER BY time DESC")
    LiveData<List<HistoryEntry>> getAllLiveWithoutDuplicates();

    @Insert
    void append(HistoryEntry entry);

    @Query("DELETE FROM HistoryEntry")
    void clear();
}
