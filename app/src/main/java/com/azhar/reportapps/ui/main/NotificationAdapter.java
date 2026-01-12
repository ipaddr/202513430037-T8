package com.azhar.reportapps.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<ModelDatabase> modelDatabaseList;

    public NotificationAdapter(Context context, List<ModelDatabase> modelDatabaseList) {
        this.context = context;
        this.modelDatabaseList = modelDatabaseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gunakan layout item_notification.xml yang sudah ada
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelDatabase model = modelDatabaseList.get(position);

        holder.tvKategori.setText(model.getKategori());
        holder.tvTanggal.setText(model.getTanggal());
        holder.tvStatus.setText("Status: " + model.getStatus());

        // Pesan khusus
        holder.tvPesan.setText("Laporan Anda telah ditangani oleh petugas.");

        // Memberikan warna teks Status agar terlihat jelas
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
    }

    @Override
    public int getItemCount() {
        return modelDatabaseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKategori, tvTanggal, tvStatus, tvPesan;
        CardView cvNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKategori = itemView.findViewById(R.id.tvKategori); // Pastikan ID ini ada di item_notification.xml
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvStatus = itemView.findViewById(R.id.tvStatus); // Tambahkan TextView status di layout jika perlu
            tvPesan = itemView.findViewById(R.id.tvIsiLaporan); // atau gunakan ID yang sesuai
            cvNotification = itemView.findViewById(R.id.cvLaporan);
        }
    }
}