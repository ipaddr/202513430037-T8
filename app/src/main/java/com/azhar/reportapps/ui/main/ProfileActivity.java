package com.azhar.reportapps.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView; // Pastikan import ini ada

import com.azhar.reportapps.R;
import com.azhar.reportapps.view.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    // Deklarasi Variabel
    CardView btnTentang, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup Toolbar (Tombol Back)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profil Pengguna");
        }

        setInitLayout();
    }

    private void setInitLayout() {
        // 1. Hubungkan variabel dengan ID di XML
        btnTentang = findViewById(R.id.btnTentang);
        btnLogout = findViewById(R.id.btnLogout);

        // 2. Logika Tombol Tentang Aplikasi (LANGKAH 4 ANDA)
        btnTentang.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        // 3. Logika Tombol Logout (Jika ada)
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}