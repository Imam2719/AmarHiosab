// FILE PATH: app/src/main/java/com/example/amarhisab/AppDatabase.java
// এই ফাইলটি MainActivity.java এর পাশে রাখুন

package com.example.amarhisab;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SpendingEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract SpendingDao spendingDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "amar_hisab_database"
                    )
                    .allowMainThreadQueries() // শুধুমাত্র development এর জন্য
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}