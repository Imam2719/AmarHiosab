package com.example.amarhisab;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spending_table")
public class SpendingEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public double amount;
    public String note;
    public String date;
    public String time;
    public byte[] imageBytes;
    public boolean isDeleted; // Recycle bin এর জন্য

    public SpendingEntity(double amount, String note, String date, String time, byte[] imageBytes, boolean isDeleted) {
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.time = time;
        this.imageBytes = imageBytes;
        this.isDeleted = isDeleted;
    }
}