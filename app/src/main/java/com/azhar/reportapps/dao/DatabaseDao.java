package com.azhar.reportapps.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.azhar.reportapps.model.ModelDatabase;

import java.util.List;

@Dao
public interface DatabaseDao {

    @Query("SELECT * FROM tbl_laporan ORDER BY uid DESC")
    LiveData<List<ModelDatabase>> getAllLaporan();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertData(ModelDatabase... modelDatabases);

    @Query("DELETE FROM tbl_laporan WHERE uid = :uid")
    void deleteLaporan(int uid);

    @Query("UPDATE tbl_laporan SET status = :status WHERE uid = :uid")
    void updateStatus(int uid, String status);
}