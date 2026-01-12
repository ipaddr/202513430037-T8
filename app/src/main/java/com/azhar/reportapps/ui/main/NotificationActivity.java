package com.azhar.reportapps.ui.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotification;
    private NotificationAdapter notificationAdapter;
    private List<ModelDatabase> notificationList = new ArrayList<>();
    private TextView tvNoData;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Laporan Selesai");
        }

        rvNotification = findViewById(R.id.rvNotification);
        tvNoData = findViewById(R.id.tvNoData);

        // Setup RecyclerView
        notificationAdapter = new NotificationAdapter(this, notificationList);
        rvNotification.setLayoutManager(new LinearLayoutManager(this));
        rvNotification.setAdapter(notificationAdapter);

        // Koneksi Firebase
        databaseRef = FirebaseDatabase.getInstance("https://siagawarga-aa282-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("tbl_laporan");

        getNotificationData();
    }

    private void getNotificationData() {
        String currentUserEmail;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            currentUserEmail = user.getEmail();
        } else {
            return;
        }

        final String finalUserEmail = currentUserEmail;

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ModelDatabase model = data.getValue(ModelDatabase.class);
                    if (model != null) {
                        // Filter pakai EMAIL
                        if (model.getEmail() != null && model.getEmail().equalsIgnoreCase(finalUserEmail)
                                && model.getStatus() != null && model.getStatus().equalsIgnoreCase("Selesai")) {
                            notificationList.add(model);
                        }
                    }
                }

                if (notificationList.isEmpty()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    rvNotification.setVisibility(View.GONE);
                } else {
                    tvNoData.setVisibility(View.GONE);
                    rvNotification.setVisibility(View.VISIBLE);
                    notificationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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