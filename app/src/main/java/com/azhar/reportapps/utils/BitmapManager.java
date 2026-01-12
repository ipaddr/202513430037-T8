package com.azhar.reportapps.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapManager {

    // Mengubah Bitmap menjadi String Base64 (Opsional, jika ingin simpan string)
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Mengubah Base64 menjadi Bitmap
    public static Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    // --- PERBAIKAN UTAMA: SIMPAN KE INTERNAL STORAGE (ANTI CRASH) ---
    public static String saveBitmapToInternalStorage(Context context, Bitmap bitmapImage) {
        if (bitmapImage == null) return "";

        // 1. Buat folder khusus di dalam penyimpanan internal aplikasi
        // Mode Private = hanya aplikasi ini yang bisa akses (Aman & Tidak butuh izin Storage)
        File directory = context.getDir("laporan_images", Context.MODE_PRIVATE);

        // 2. Buat nama file unik
        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        File mypath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // 3. Kompres gambar (Kualitas 90%)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Kembalikan string kosong jika gagal
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 4. Kembalikan ALAMAT LENGKAP file tersebut
        return mypath.getAbsolutePath();
    }
}