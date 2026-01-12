package com.azhar.reportapps.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.azhar.reportapps.R;
import com.azhar.reportapps.ui.history.HistoryActivity;
import com.azhar.reportapps.ui.report.ReportActivity;
import com.azhar.reportapps.utils.BitmapManager;
import com.azhar.reportapps.utils.Constant;
import com.azhar.reportapps.view.LoginActivity;
import com.azhar.reportapps.viewmodel.InputDataViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_PERMISSION = 100;
    private FusedLocationProviderClient fusedLocationClient;

    private CardView cvSOS, cvPemadam, cvAmbulance, cvBencana;
    private CardView cvHistory, cvProfile, cvLogout;
    private TextView tvGreeting, tvNotifBadge;
    private ImageView imgNotification;

    private InputDataViewModel inputDataViewModel;
    private Vibrator vibrator;
    private boolean isSosTransaction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setStatusBar();
        setPermission();

        inputDataViewModel = new ViewModelProvider(this).get(InputDataViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setInitLayout();
        setGreeting();
        getLocation();

        // PANGGIL FUNGSI BARU
        observeNotificationCount();
        observeInputStatus();
    }

    private void observeInputStatus() {
        inputDataViewModel.isSuccess.observe(this, success -> {
            if (isSosTransaction) {
                if (success) {
                    Toast.makeText(this, "SOS TERKIRIM! Data masuk ke Riwayat.", Toast.LENGTH_LONG).show();
                    isSosTransaction = false;
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Gagal mengirim SOS. Cek koneksi internet.", Toast.LENGTH_SHORT).show();
                    isSosTransaction = false;
                }
            }
        });
    }

    // --- PERBAIKAN: Mengambil jumlah notifikasi dari Firebase ---
    private void observeNotificationCount() {
        inputDataViewModel.getLiveNotificationCount().observe(this, count -> {
            if (count != null && count > 0) {
                tvNotifBadge.setVisibility(View.VISIBLE);
                tvNotifBadge.setText(String.valueOf(count));
            } else {
                tvNotifBadge.setVisibility(View.GONE);
            }
        });
    }

    private void setGreeting() {
        tvGreeting = findViewById(R.id.tvGreeting);
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            tvGreeting.setText("Selamat Pagi, Warga!");
        } else if (timeOfDay >= 12 && timeOfDay < 15) {
            tvGreeting.setText("Selamat Siang, Warga!");
        } else if (timeOfDay >= 15 && timeOfDay < 18) {
            tvGreeting.setText("Selamat Sore, Warga!");
        } else {
            tvGreeting.setText("Selamat Malam, Warga!");
        }
    }

    private void setInitLayout() {
        cvSOS = findViewById(R.id.cvSOS);
        cvPemadam = findViewById(R.id.cvPemadam);
        cvAmbulance = findViewById(R.id.cvAmbulance);
        cvBencana = findViewById(R.id.cvBencana);
        cvHistory = findViewById(R.id.cvHistory);
        cvProfile = findViewById(R.id.cvProfile);
        cvLogout = findViewById(R.id.cvLogout);
        imgNotification = findViewById(R.id.imgNotification);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);

        try {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down);
            cvSOS.startAnimation(anim);
        } catch (Exception e) { e.printStackTrace(); }

        imgNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        cvSOS.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ KONFIRMASI SOS")
                    .setMessage("Apakah Anda dalam keadaan darurat? Lokasi Anda akan dikirim ke petugas.")
                    .setPositiveButton("YA, KIRIM", (dialog, which) -> sendSOS())
                    .setNegativeButton("BATAL", null)
                    .show();
        });

        cvPemadam.setOnClickListener(v -> openReport("Laporan Kebakaran", "Kebakaran"));
        cvAmbulance.setOnClickListener(v -> openReport("Laporan Medis", "Medis"));
        cvBencana.setOnClickListener(v -> openReport("Laporan Bencana Alam", "Bencana"));

        cvHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        cvProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        cvLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Yakin ingin keluar?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void sendSOS() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        String namaPelapor = "Warga (Tanpa Login)";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) namaPelapor = user.getEmail();

        String imageFilePath = "";
        try {
            int resourceId = R.drawable.bg_bencana;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
            if (bitmap != null) {
                imageFilePath = BitmapManager.saveBitmapToInternalStorage(this, bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageFilePath = "-";
        }

        String tanggal = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String lokasi = (Constant.lokasiPengaduan != null && !Constant.lokasiPengaduan.isEmpty())
                ? Constant.lokasiPengaduan
                : "Lokasi Darurat (Koordinat GPS)";

        isSosTransaction = true;
        Toast.makeText(this, "Sedang mengirim sinyal SOS...", Toast.LENGTH_SHORT).show();

        inputDataViewModel.addLaporan(
                "SOS DARURAT",
                imageFilePath,
                namaPelapor,
                lokasi,
                tanggal,
                "DARURAT! Butuh bantuan segera di lokasi ini.",
                "0812-0000-SOS"
        );
    }

    private void openReport(String title, String kategori) {
        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
        intent.putExtra(ReportActivity.DATA_TITLE, title);
        intent.putExtra(ReportActivity.DATA_KATEGORI, kategori);
        startActivity(intent);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Constant.lokasiPengaduan = list.get(0).getAddressLine(0);
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }
        });
    }

    private void setPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
        }
    }

    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (on) layoutParams.flags |= bits; else layoutParams.flags &= ~bits;
        window.setAttributes(layoutParams);
    }
}