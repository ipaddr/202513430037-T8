package com.azhar.reportapps.ui.history;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.azhar.reportapps.viewmodel.HistoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.HistoryAdapterCallback {

    RecyclerView rvHistory;
    HistoryAdapter historyAdapter;
    HistoryViewModel historyViewModel;
    List<ModelDatabase> modelDatabaseList = new ArrayList<>();
    TextView tvNotFound;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setInitLayout();
        setViewModel();
    }

    private void setInitLayout() {
        toolbar = findViewById(R.id.toolbar);
        tvNotFound = findViewById(R.id.tvNotFound);
        rvHistory = findViewById(R.id.rvHistory);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, modelDatabaseList, this);
        rvHistory.setAdapter(historyAdapter);
    }

    private void setViewModel() {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        // Observer untuk data dari Firebase
        historyViewModel.getDataLaporan().observe(this, modelDatabases -> {
            if (modelDatabases.size() != 0) {
                tvNotFound.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);

                // Update data di Adapter
                historyAdapter.setData(modelDatabases);
            } else {
                tvNotFound.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            }
        });
    }

    // INI YANG DIJALANKAN SAAT ITEM DIKLIK (Adapter Callback)
    @Override
    public void onDelete(ModelDatabase modelDatabase) {
        // Kita ubah fungsi delete ini menjadi konfirmasi update status
        showDialogUpdateStatus(modelDatabase);
    }

    private void showDialogUpdateStatus(final ModelDatabase model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Status");
        builder.setMessage("Apakah Anda ingin memproses laporan ini menjadi SELESAI?");
        builder.setPositiveButton("Ya, Proses", (dialog, which) -> {
            // Panggil ViewModel untuk update ke Firebase
            historyViewModel.updateStatusLaporan(model.getKey(), model.getStatus());
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        // Tambahan tombol hapus jika memang ingin menghapus permanen (Opsional)
        builder.setNeutralButton("Hapus Data", (dialog, which) -> {
            historyViewModel.deleteData(model);
        });

        builder.show();
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