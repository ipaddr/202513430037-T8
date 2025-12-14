package com.azhar.reportapps.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.azhar.reportapps.viewmodel.HistoryViewModel;

public class DetailActivity extends AppCompatActivity {

    TextView tvTitle, tvStatus, tvLokasi, tvIsi, tvTanggal, tvWaktu;
    CardView cvInfoUtama;
    ImageView imgBuktiDetail;

    // Komponen Timeline
    ImageView imgStep1, imgStep2, imgStep3;
    View line1, line2;
    TextView tvTextStep2, tvTextStep3, tvDescStep2;

    // Animasi Loading
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
            getSupportActionBar().setTitle("");
        }

        // Load animasi putar
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_infinite);

        // Inisialisasi ViewModel
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

        btnCekStatus.setOnClickListener(v -> {
            if (modelDatabase == null) return;

            // Animasi menghilangkan card info agar fokus ke timeline
            cvInfoUtama.animate().alpha(0f).translationY(-100).setDuration(500).withEndAction(() -> cvInfoUtama.setVisibility(View.GONE)).start();

            Toast.makeText(this, "Menghubungkan ke Cloud...", Toast.LENGTH_SHORT).show();
            btnCekStatus.setEnabled(false);
            btnCekStatus.setText("Memproses...");

            // --- SIMULASI UPDATE STATUS KE FIREBASE ---

            // TIMER 1 (5 Detik) -> Update status ke "Proses"
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                animateStep(2);
                // Update di Firestore (UID String)
                historyViewModel.updateStatusLaporan(modelDatabase.getUid(), "Proses");
            }, 5000);

            // TIMER 2 (15 Detik) -> Update status ke "Selesai"
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                animateStep(3);
                btnCekStatus.setText("Selesai");
                btnCekStatus.setBackgroundColor(Color.parseColor("#10B981"));

                // Update di Firestore (UID String)
                historyViewModel.updateStatusLaporan(modelDatabase.getUid(), "Selesai");

                Toast.makeText(DetailActivity.this, "Laporan Selesai & Terupdate di Server!", Toast.LENGTH_LONG).show();
            }, 15000);
        });
    }

    private void setData() {
        // Ambil data yang dikirim dari Activity sebelumnya
        modelDatabase = (ModelDatabase) getIntent().getSerializableExtra("DATA_LAPORAN");

        if (modelDatabase != null) {
            tvTitle.setText(modelDatabase.getKategori());
            tvLokasi.setText(modelDatabase.getLokasi());
            tvIsi.setText(modelDatabase.getIsiLaporan());
            tvTanggal.setText(modelDatabase.getTanggal());
            tvWaktu.setText(modelDatabase.getTanggal());

            // --- BAGIAN INI PENTING: LOAD FOTO DENGAN GLIDE ---
            // Karena data 'getFoto()' sekarang adalah URL (String), bukan Base64.
            if (modelDatabase.getFoto() != null && !modelDatabase.getFoto().isEmpty()) {
                Glide.with(this)
                        .load(modelDatabase.getFoto()) // Load URL dari Firebase
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache agar hemat kuota
                        .placeholder(R.drawable.ic_image_upload) // Gambar sementara loading
                        .error(R.drawable.ic_image_upload) // Gambar jika gagal load
                        .into(imgBuktiDetail);
            }

            // Cek Status untuk mengatur tampilan timeline saat pertama dibuka
            String status = modelDatabase.getStatus();
            if (status != null && status.equalsIgnoreCase("Selesai")) {
                animateStep(3);
                btnCekStatus.setVisibility(View.GONE);
                cvInfoUtama.setVisibility(View.GONE);
            } else if (status != null && (status.equalsIgnoreCase("Proses") || status.equalsIgnoreCase("Ditangani"))) {
                animateStep(2);
            } else {
                animateStep(1);
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
            tvStatus.setText("Baru");
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
            tvDescStep2.setTextColor(colorBlue);
            tvDescStep2.setText("Petugas sedang menuju lokasi...");

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
            imgStep3.animate().scaleX(1f).scaleY(1f).setInterpolator(new OvershootInterpolator()).setDuration(500).start();
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