package com.azhar.reportapps.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.azhar.reportapps.dao.DatabaseDao;
import com.azhar.reportapps.database.DatabaseClient;
import com.azhar.reportapps.model.ModelDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InputDataViewModel extends AndroidViewModel {

    private DatabaseDao databaseDao;
    public MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private MutableLiveData<Integer> notificationCount = new MutableLiveData<>();
    private DatabaseReference databaseRef;

    public InputDataViewModel(@NonNull Application application) {
        super(application);
        databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
        databaseRef = FirebaseDatabase.getInstance("https://siagawarga-aa282-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("tbl_laporan");

        listenToNotificationUpdates();
    }

    private void listenToNotificationUpdates() {
        String currentUserEmail;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            currentUserEmail = user.getEmail();
        } else {
            currentUserEmail = "anonymous";
        }

        final String finalUserEmail = currentUserEmail;

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countSelesai = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    ModelDatabase model = data.getValue(ModelDatabase.class);
                    if (model != null) {
                        // PERBAIKAN: Cek berdasarkan EMAIL, bukan Nama
                        if (model.getEmail() != null && model.getEmail().equalsIgnoreCase(finalUserEmail)
                                && model.getStatus() != null && model.getStatus().equalsIgnoreCase("Selesai")) {
                            countSelesai++;
                        }
                    }
                }
                notificationCount.postValue(countSelesai);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public LiveData<Integer> getLiveNotificationCount() {
        return notificationCount;
    }

    public void addLaporan(String kategori, String imagePath, String nama,
                           String lokasi, String tanggal, String isi_laporan, String telepon) {

        ModelDatabase model = new ModelDatabase();
        model.setKategori(kategori);
        model.setFoto(imagePath);
        model.setNama(nama);
        model.setLokasi(lokasi);
        model.setTanggal(tanggal);
        model.setIsiLaporan(isi_laporan);
        model.setTelepon(telepon);
        model.setStatus("Baru");

        // PERBAIKAN: Simpan Email Pelapor secara otomatis
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            model.setEmail(user.getEmail());
        } else {
            model.setEmail("anonymous");
        }

        String key = databaseRef.push().getKey();
        if (key == null) key = UUID.randomUUID().toString();
        model.setKey(key);

        databaseRef.child(key).setValue(model)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveToLocal(model);
                    } else {
                        isSuccess.setValue(false);
                    }
                });
    }

    private void saveToLocal(ModelDatabase model) {
        Completable.fromAction(() -> databaseDao.insertData(model))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> isSuccess.setValue(true), throwable -> isSuccess.setValue(true));
    }

    public LiveData<java.util.List<ModelDatabase>> getAllLaporan() {
        return databaseDao.getAllLaporan();
    }
}