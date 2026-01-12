package com.azhar.reportapps.ui.history;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.azhar.reportapps.viewmodel.HistoryViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.File;

public class DetailActivity extends AppCompatActivity {

    TextView tvTitle, tvStatus, tvLokasi, tvIsi, tvTanggal, tvWaktu;
    CardView cvInfoUtama;
    ImageView imgBuktiDetail;

    // Komponen Timeline Status
    ImageView imgStep1, imgStep2, imgStep3;
    View line1, line2;
    TextView tvTextStep2, tvTextStep3, tvDescStep2;

    // Loading Animation
    View containerStep2;
    ImageView imgStep2Static, imgStep2Loading;

    MaterialButton btnCekStatus;
    ModelDatabase modelDatabase;
    HistoryViewModel historyViewModel;
    Animation rotateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Laporan");
        }

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_infinite);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setInitLayout();
        setData();
    }

    private void setInitLayout() {
        cvInfoUtama = findViewById(R.id.cvInfoUtama);
        tvTitle = findViewById(R.id.tvTitleDetail);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvLokasi = findViewById(R.id.tvLokasiDetail);
        tvIsi = findViewById(R.id.tvIsiDetail);
        tvTanggal = findViewById(R.id.tvTanggalDetail);
        tvWaktu = findViewById(R.id.tvWaktuDibuat);
        imgBuktiDetail = findViewById(R.id.imgBuktiDetail);

        imgStep1 = findViewById(R.id.imgStep1);
        line1 = findViewById(R.id.line1);
        containerStep2 = findViewById(R.id.containerStep2);
        imgStep2Static = findViewById(R.id.imgStep2Static);
        imgStep2Loading = findViewById(R.id.imgStep2Loading);
        tvTextStep2 = findViewById(R.id.tvTextStep2);
        tvDescStep2 = findViewById(R.id.tvDescStep2);
        line2 = findViewById(R.id.line2);
        imgStep3 = findViewById(R.id.imgStep3);
        tvTextStep3 = findViewById(R.id.tvTextStep3);

        btnCekStatus = findViewById(R.id.btnCekStatus);

        // --- LOGIKA UTAMA ANIMASI PROSES LAPORAN ---
        btnCekStatus.setOnClickListener(v -> {
            if (modelDatabase == null) return;

            // 1. Hilangkan Info Utama (Fade Out)
            cvInfoUtama.animate()
                    .alpha(0f)
                    .translationY(-100)
                    .setDuration(600)
                    .withEndAction(() -> cvInfoUtama.setVisibility(View.GONE))
                    .start();

            Toast.makeText(this, "Menghubungkan ke petugas...", Toast.LENGTH_SHORT).show();
            btnCekStatus.setEnabled(false);
            btnCekStatus.setText("Sedang Memproses...");

            // 2. DELAY 2 DETIK: Ubah status jadi "Proses" & Animasi Step 2
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                animateStep(2);
                historyViewModel.updateStatusLaporan(modelDatabase.getKey(), "Proses");
            }, 2000);

            // 3. DELAY 7 DETIK LAGI: Ubah status jadi "Selesai" & Animasi Step 3
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                animateStep(3);
                btnCekStatus.setText("Laporan Selesai");
                btnCekStatus.setBackgroundColor(Color.parseColor("#10B981")); // Hijau

                historyViewModel.updateStatusLaporan(modelDatabase.getKey(), "Selesai");

                Toast.makeText(DetailActivity.this, "Laporan telah diselesaikan!", Toast.LENGTH_LONG).show();
            }, 7000);
        });
    }

    private void setData() {
        modelDatabase = (ModelDatabase) getIntent().getSerializableExtra("DATA_LAPORAN");
        if (modelDatabase != null) {
            tvTitle.setText(modelDatabase.getKategori());
            tvLokasi.setText(modelDatabase.getLokasi());
            tvIsi.setText(modelDatabase.getIsiLaporan());
            tvTanggal.setText(modelDatabase.getTanggal());
            tvWaktu.setText(modelDatabase.getTanggal());

            try {
                String fotoData = modelDatabase.getFoto();
                if (fotoData != null && !fotoData.isEmpty() && !fotoData.equals("-")) {
                    if (fotoData.startsWith("/")) {
                        File imgFile = new File(fotoData);
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            imgBuktiDetail.setImageBitmap(myBitmap);
                        } else {
                            imgBuktiDetail.setImageResource(R.drawable.ic_image_upload);
                        }
                    } else {
                        byte[] decodedString = Base64.decode(fotoData, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imgBuktiDetail.setImageBitmap(decodedByte);
                    }
                } else {
                    imgBuktiDetail.setImageResource(R.drawable.ic_image_upload);
                }
            } catch (Exception e) {
                imgBuktiDetail.setImageResource(R.drawable.ic_image_upload);
            }

            // Set Status Awal
            String status = modelDatabase.getStatus();
            if (status != null && status.equalsIgnoreCase("Selesai")) {
                animateStep(3);
                btnCekStatus.setVisibility(View.GONE);
                cvInfoUtama.setVisibility(View.GONE);
            } else if (status != null && status.equalsIgnoreCase("Proses")) {
                animateStep(2);
                cvInfoUtama.setVisibility(View.GONE);
                btnCekStatus.setEnabled(false);
                btnCekStatus.setText("Sedang Diproses...");
            } else {
                animateStep(1); // Default
            }
        }
    }

    private void animateStep(int step) {
        int colorBlue = Color.parseColor("#2563EB");
        int colorGreen = Color.parseColor("#10B981");
        int colorGray = Color.parseColor("#E5E7EB");
        int colorTextActive = Color.parseColor("#111827");
        int colorTextInactive = Color.parseColor("#9CA3AF");

        if (step == 1) {
            tvStatus.setText("Laporan Baru");
            tvStatus.setTextColor(colorBlue);

            imgStep1.setImageResource(R.drawable.ic_modern_check);
            imgStep1.setColorFilter(colorBlue);
            line1.setBackgroundColor(colorGray);

            imgStep2Loading.clearAnimation();
            imgStep2Loading.setVisibility(View.GONE);
            imgStep2Static.setVisibility(View.VISIBLE);
            imgStep2Static.setColorFilter(colorTextInactive);
            tvTextStep2.setTextColor(colorTextInactive);
            line2.setBackgroundColor(colorGray);

        } else if (step == 2) {
            tvStatus.setText("Sedang Ditangani");
            tvStatus.setTextColor(Color.parseColor("#F59E0B"));

            imgStep1.setColorFilter(colorGreen);
            line1.setBackgroundColor(colorBlue);

            imgStep2Static.setVisibility(View.GONE);
            imgStep2Loading.setVisibility(View.VISIBLE);
            imgStep2Loading.startAnimation(rotateAnimation);

            tvTextStep2.setTextColor(colorTextActive);
            tvTextStep2.setText("Sedang Diproses");
            tvDescStep2.setVisibility(View.VISIBLE);
            tvDescStep2.setText("Petugas sedang menuju lokasi...");
            tvDescStep2.setTextColor(colorBlue);

        } else if (step == 3) {
            tvStatus.setText("Selesai");
            tvStatus.setTextColor(colorGreen);

            line1.setBackgroundColor(colorGreen);
            imgStep1.setColorFilter(colorGreen);

            imgStep2Loading.clearAnimation();
            imgStep2Loading.setVisibility(View.GONE);

            imgStep2Static.setVisibility(View.VISIBLE);
            imgStep2Static.setImageResource(R.drawable.ic_modern_check);
            imgStep2Static.setColorFilter(null);

            line2.setBackgroundColor(colorGreen);
            tvTextStep2.setTextColor(colorTextActive);
            tvDescStep2.setText("Penanganan selesai.");

            imgStep3.setImageResource(R.drawable.ic_modern_check);
            imgStep3.setColorFilter(null);
            tvTextStep3.setTextColor(colorTextActive);

            imgStep3.setScaleX(0f); imgStep3.setScaleY(0f);
            imgStep3.animate()
                    .scaleX(1.3f).scaleY(1.3f)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(500)
                    .withEndAction(() -> {
                        imgStep3.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    }).start();
        }
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