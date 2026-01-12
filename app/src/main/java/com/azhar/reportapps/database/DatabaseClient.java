package com.azhar.reportapps.database;

import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {

    private Context context;
    private static DatabaseClient mInstance;
    private AppDatabase appDatabase;

    private DatabaseClient(Context context) {
        this.context = context;

        // Membangun database dengan migrasi destruktif agar tidak crash saat update tabel
        appDatabase = Room.databaseBuilder(context, AppDatabase.class, "laporan_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(context);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}