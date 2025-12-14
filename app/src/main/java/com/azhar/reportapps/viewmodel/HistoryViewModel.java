package com.azhar.reportapps.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.azhar.reportapps.model.ModelDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private MutableLiveData<List<ModelDatabase>> dataLaporan = new MutableLiveData<>();
    private FirebaseFirestore db;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        loadDataLaporan();
    }

    public LiveData<List<ModelDatabase>> getDataLaporan() {
        return dataLaporan;
    }

    private void loadDataLaporan() {
        db.collection("laporan")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        List<ModelDatabase> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ModelDatabase model = doc.toObject(ModelDatabase.class);
                            if (model != null) {
                                model.setUid(doc.getId());
                                list.add(model);
                            }
                        }
                        dataLaporan.setValue(list);
                    }
                });
    }

    public void deleteDataById(String uid) {
        db.collection("laporan").document(uid).delete();
    }

    public void updateStatusLaporan(String uid, String status) {
        db.collection("laporan").document(uid).update("status", status);
    }
}