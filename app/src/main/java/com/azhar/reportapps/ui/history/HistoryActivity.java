package com.azhar.reportapps.ui.history;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.azhar.reportapps.viewmodel.HistoryViewModel;


// --- BARIS PENTING YANG HILANG TADI ---
// --------------------------------------

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.HistoryAdapterCallback {

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private HistoryViewModel historyViewModel;
    private List<ModelDatabase> modelDatabase = new ArrayList<>();
    private TextView tvNotFound;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setToolbar();
        setInitLayout();
        setViewModel();
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setInitLayout() {
        rvHistory = findViewById(R.id.rvHistory);
        tvNotFound = findViewById(R.id.tvNotFound);

        tvNotFound.setVisibility(View.GONE);

        historyAdapter = new HistoryAdapter(this, modelDatabase, this);
        rvHistory.setHasFixedSize(true);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setViewModel() {
        // Inisialisasi ViewModel
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        // Observasi data dari Firebase (Realtime)
        historyViewModel.getDataLaporan().observe(this, new Observer<List<ModelDatabase>>() {
            @Override
            public void onChanged(List<ModelDatabase> modelDatabases) {
                // Cek apakah data ada?
                if (modelDatabases.size() != 0) {
                    historyAdapter.setData(modelDatabases);
                    tvNotFound.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                } else {
                    tvNotFound.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                }
            }
        });
    }

    // --- FITUR HAPUS DATA (CALLBACK DARI ADAPTER) ---
    @Override
    public void onDelete(ModelDatabase modelDatabase) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Hapus riwayat ini?");
        alertDialogBuilder.setPositiveButton("Ya, Hapus", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Panggil method delete di ViewModel (UID sekarang String)
                historyViewModel.deleteDataById(modelDatabase.getUid());
                Toast.makeText(HistoryActivity.this, "Data berhasil dihapus dari Cloud", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialogBuilder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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