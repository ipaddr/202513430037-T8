package com.azhar.reportapps.viewmodel;

import android.app.Application;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.azhar.reportapps.model.ModelDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class InputDataViewModel extends AndroidViewModel {

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;

    public InputDataViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void addLaporan(final String kategori, final String base64Image,
                           final String namaPelapor, final String lokasi,
                           final String tanggal, final String isiLaporan,
                           final String telepon) {

        Toast.makeText(getApplication(), "Mengupload foto & data...", Toast.LENGTH_SHORT).show();

        // 1. Upload Foto ke Firebase Storage
        // Kita ubah Base64 kembali ke byte array untuk diupload
        String path = "images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(path);

        byte[] data = Base64.decode(base64Image, Base64.DEFAULT);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // 2. Ambil URL Download Foto
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                // 3. Simpan Data ke Firestore
                saveToFirestore(kategori, downloadUrl, namaPelapor, lokasi, tanggal, isiLaporan, telepon);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplication(), "Gagal upload foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToFirestore(String kategori, String fotoUrl, String nama, String lokasi, String tanggal, String isi, String telp) {
        String uid = db.collection("laporan").document().getId(); // Generate ID unik
        String userId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "guest";

        ModelDatabase laporan = new ModelDatabase();
        laporan.setUid(uid);
        laporan.setIdUser(userId);
        laporan.setKategori(kategori);
        laporan.setFoto(fotoUrl); // Simpan URL
        laporan.setNamaPelapor(nama);
        laporan.setLokasi(lokasi);
        laporan.setTanggal(tanggal);
        laporan.setIsiLaporan(isi);
        laporan.setTelepon(telp);
        laporan.setStatus("Baru");

        db.collection("laporan").document(uid)
                .set(laporan)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplication(), "Laporan Berhasil Terkirim!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplication(), "Gagal kirim data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}