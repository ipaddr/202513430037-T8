package com.azhar.reportapps.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.azhar.reportapps.dao.DatabaseDao;
import com.azhar.reportapps.model.ModelDatabase;

@Database(entities = {ModelDatabase.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DatabaseDao databaseDao();
}