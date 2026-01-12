package com.azhar.reportapps.ui.report;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.azhar.reportapps.R;
import com.azhar.reportapps.utils.BitmapManager;
import com.azhar.reportapps.viewmodel.InputDataViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    public static final String DATA_TITLE = "TITLE";
    public static final String DATA_KATEGORI = "KATEGORI";

    private final int PERMISSION_ID = 44;
    private final int REQ_CAMERA = 100;
    private final int REQ_CAMERA_PERMISSION = 101; // Kode request permission baru

    String strTitle, strKategori;
    String strFilePath = "";

    Toolbar toolbar;
    TextView tvTitle;
    ImageView imageLaporan;
    ExtendedFloatingActionButton fabSend;
    EditText inputNama, inputLokasi, inputTanggal, inputLaporan, inputTelepon;
    InputDataViewModel inputDataViewModel;
    FusedLocationProviderClient fusedLocationProviderClient;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        setInitLayout();
        setViewModel();
        getLastLocation();
    }

    private void setInitLayout() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tvTitle);
        imageLaporan = findViewById(R.id.imageLaporan);
        fabSend = findViewById(R.id.fabSend);
        inputNama = findViewById(R.id.inputNama);
        inputLokasi = findViewById(R.id.inputLokasi);
        inputTanggal = findViewById(R.id.inputTanggal);
        inputLaporan = findViewById(R.id.inputLaporan);
        inputTelepon = findViewById(R.id.inputTelepon);

        strTitle = getIntent().getStringExtra(DATA_TITLE);
        strKategori = getIntent().getStringExtra(DATA_KATEGORI);
        if (strTitle != null) tvTitle.setText(strTitle);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        inputTanggal.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(ReportActivity.this, (view1, year, month, dayOfMonth) -> {
                inputTanggal.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // --- PERBAIKAN 2: Cek Permission Kamera Sebelum Membuka ---
        imageLaporan.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Jika izin belum diberikan, minta izin
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            } else {
                // Jika izin sudah ada, buka kamera
                openCamera();
            }
        });

        inputLokasi.setFocusable(false);
        inputLokasi.setOnClickListener(v -> getLastLocation());

        fabSend.setOnClickListener(view -> {
            String strNama = inputNama.getText().toString();
            String strLokasi = inputLokasi.getText().toString();
            String strTanggal = inputTanggal.getText().toString();
            String strIsi = inputLaporan.getText().toString();
            String strTelepon = inputTelepon.getText().toString();

            if (strFilePath == null || strFilePath.isEmpty()) {
                Toast.makeText(this, "Silakan ambil foto bukti terlebih dahulu!", Toast.LENGTH_SHORT).show();
            } else if (strNama.isEmpty() || strLokasi.isEmpty() || strTanggal.isEmpty() || strIsi.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();
                inputDataViewModel.addLaporan(strKategori, strFilePath, strNama, strLokasi, strTanggal, strIsi, strTelepon);
            }
        });
    }

    // Method helper untuk membuka kamera
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, REQ_CAMERA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Kamera tidak ditemukan di HP ini", Toast.LENGTH_SHORT).show();
        }
    }

    // Menangani hasil permintaan izin
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // Izin diberikan, buka kamera
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto bukti", Toast.LENGTH_SHORT).show();
            }
        }

        // Handle izin lokasi juga jika perlu (kode lama menggunakan kode request berbeda)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private void setViewModel() {
        inputDataViewModel = new ViewModelProvider(this).get(InputDataViewModel.class);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang mengirim laporan...");
        progressDialog.setCancelable(false);

        inputDataViewModel.isSuccess.observe(this, success -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (success) {
                Toast.makeText(this, "Laporan Berhasil Terkirim!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal mengirim laporan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAMERA && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageLaporan.setImageBitmap(bitmap);
                strFilePath = BitmapManager.saveBitmapToInternalStorage(this, bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLastLocation() {
        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location == null) requestNewLocationData();
                else inputLokasi.setText(getCityName(location.getLatitude(), location.getLongitude()));
            });
        } else {
            requestPermissions();
        }
    }

    private void requestNewLocationData() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setInterval(0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location mLastLocation = locationResult.getLastLocation();
                if(mLastLocation != null) inputLokasi.setText(getCityName(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        }, Looper.myLooper());
    }

    private String getCityName(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        } catch (IOException e) { e.printStackTrace(); }
        return lat + "," + lng;
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}