package com.azhar.reportapps.ui.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.Serializable; // PENTING: Import ini ditambahkan
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    List<ModelDatabase> modelDatabase;
    Context mContext;
    HistoryAdapterCallback mAdapterCallback;
    private int lastPosition = -1;

    public HistoryAdapter(Context context, List<ModelDatabase> modelInput, HistoryAdapterCallback adapterCallback) {
        this.mContext = context;
        this.modelDatabase = modelInput;
        this.mAdapterCallback = adapterCallback;
    }

    public void setData(List<ModelDatabase> items) {
        modelDatabase.clear();
        modelDatabase.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ModelDatabase data = modelDatabase.get(position);

        holder.tvKategori.setText(data.getKategori());
        holder.tvNamaJalan.setText(data.getLokasi());
        holder.tvDate.setText(data.getTanggal());

        // Rapikan isi laporan
        String isi = data.getIsiLaporan();
        if (isi != null && isi.contains("\n\n(Pelapor:")) {
            isi = isi.split("\n\n\\(Pelapor:")[0];
        }
        holder.tvDeskripsi.setText(isi != null ? isi : "-");

        // Load Foto (File Lokal / Base64 / Strip)
        try {
            String fotoData = data.getFoto();
            if (fotoData == null || fotoData.equals("-") || fotoData.isEmpty()) {
                holder.imgHistory.setImageResource(R.drawable.ic_image_upload);
            } else if (fotoData.startsWith("/")) {
                Glide.with(mContext)
                        .load(new File(fotoData))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_image_upload)
                        .into(holder.imgHistory);
            } else {
                byte[] decodedString = Base64.decode(fotoData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgHistory.setImageBitmap(decodedByte);
            }
        } catch (Exception e) {
            holder.imgHistory.setImageResource(R.drawable.ic_image_upload);
        }

        // Warna Status
        String status = data.getStatus();
        if (status != null) {
            holder.tvStatus.setText(status);
            if (status.equalsIgnoreCase("Selesai")) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
                holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.white));
            } else if (status.equalsIgnoreCase("Proses")) {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_orange);
                holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                holder.tvStatus.setText("Baru");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_blue);
                holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.white));
            }
        }

        // KLIK ITEM -> BUKA DETAIL ACTIVITY
        holder.layoutItem.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, DetailActivity.class);
            // PERBAIKAN DI SINI: Cast ke (Serializable) agar tidak ambigu
            intent.putExtra("DATA_LAPORAN", (Serializable) data);
            mContext.startActivity(intent);
        });

        // (Opsional) Klik Lama -> Hapus
        holder.layoutItem.setOnLongClickListener(v -> {
            if (mAdapterCallback != null) mAdapterCallback.onDelete(data);
            return true;
        });

        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() { return modelDatabase.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvKategori, tvNamaJalan, tvDate, tvDeskripsi, tvStatus;
        public ImageView imgHistory;
        public CardView layoutItem;

        public ViewHolder(View itemView) {
            super(itemView);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvNamaJalan = itemView.findViewById(R.id.tvNamaJalan);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgHistory = itemView.findViewById(R.id.imgHistory);
            layoutItem = itemView.findViewById(R.id.layoutItem);
        }
    }

    public interface HistoryAdapterCallback {
        void onDelete(ModelDatabase modelDatabase);
    }
}