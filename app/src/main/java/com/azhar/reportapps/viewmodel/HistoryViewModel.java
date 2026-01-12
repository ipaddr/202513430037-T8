package com.azhar.reportapps.viewmodel;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.azhar.reportapps.model.ModelDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private MutableLiveData<List<ModelDatabase>> dataLaporan = new MutableLiveData<>();
    DatabaseReference databaseRef;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        // Pastikan URL Database sesuai dengan punya Anda (Asia Southeast)
        databaseRef = FirebaseDatabase.getInstance("https://siagawarga-aa282-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("tbl_laporan");

        loadDataHistory();
    }

    public LiveData<List<ModelDatabase>> getDataLaporan() {
        return dataLaporan;
    }

    private void loadDataHistory() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ModelDatabase> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ModelDatabase model = data.getValue(ModelDatabase.class);
                    if (model != null) {
                        model.setKey(data.getKey()); // Simpan Key Firebase
                        list.add(model);
                    }
                }
                Collections.reverse(list); // Urutan terbaru paling atas
                dataLaporan.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplication(), "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // UPDATE STATUS (Bisa untuk "Proses" maupun "Selesai")
    public void updateStatusLaporan(String key, String statusBaru) {
        if (key != null) {
            databaseRef.child(key).child("status").setValue(statusBaru)
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplication(), "Gagal update status", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void deleteData(ModelDatabase modelDatabase) {
        if (modelDatabase.getKey() != null) {
            databaseRef.child(modelDatabase.getKey()).removeValue();
        }
    }
}