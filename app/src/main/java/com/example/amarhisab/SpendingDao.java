package com.example.amarhisab;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface SpendingDao {

    @Insert
    long insert(SpendingEntity spending);

    @Update
    void update(SpendingEntity spending);

    @Delete
    void delete(SpendingEntity spending);

    @Query("SELECT * FROM spending_table WHERE isDeleted = 0 ORDER BY id DESC")
    List<SpendingEntity> getAllSpending();

    @Query("SELECT * FROM spending_table WHERE isDeleted = 1 ORDER BY id DESC")
    List<SpendingEntity> getRecycleBin();

    @Query("UPDATE spending_table SET isDeleted = 1 WHERE id = :id")
    void moveToRecycleBin(int id);

    @Query("UPDATE spending_table SET isDeleted = 0 WHERE id = :id")
    void restoreFromRecycleBin(int id);

    @Query("DELETE FROM spending_table WHERE id = :id")
    void deletePermanently(int id);

    @Query("DELETE FROM spending_table")
    void deleteAll();
}