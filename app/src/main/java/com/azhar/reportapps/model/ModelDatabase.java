package com.azhar.reportapps.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "tbl_laporan")
public class ModelDatabase implements Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "key")
    public String key;

    @ColumnInfo(name = "kategori")
    public String kategori;

    @ColumnInfo(name = "image")
    public String foto;

    @ColumnInfo(name = "nama")
    public String nama;

    // --- TAMBAHAN BARU ---
    @ColumnInfo(name = "email")
    public String email;
    // ---------------------

    @ColumnInfo(name = "lokasi")
    public String lokasi;

    @ColumnInfo(name = "tanggal")
    public String tanggal;

    @ColumnInfo(name = "isi_laporan")
    public String isiLaporan;

    @ColumnInfo(name = "telepon")
    public String telepon;

    @ColumnInfo(name = "status")
    public String status;

    public ModelDatabase() {
    }

    // Getter dan Setter untuk Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ... (Biarkan kode Parcelable di bawah ini tetap sama atau generate ulang) ...
    protected ModelDatabase(Parcel in) {
        uid = in.readInt();
        key = in.readString();
        kategori = in.readString();
        foto = in.readString();
        nama = in.readString();
        email = in.readString(); // Tambahkan ini
        lokasi = in.readString();
        tanggal = in.readString();
        isiLaporan = in.readString();
        telepon = in.readString();
        status = in.readString();
    }

    public static final Creator<ModelDatabase> CREATOR = new Creator<ModelDatabase>() {
        @Override
        public ModelDatabase createFromParcel(Parcel in) {
            return new ModelDatabase(in);
        }

        @Override
        public ModelDatabase[] newArray(int size) {
            return new ModelDatabase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(key);
        dest.writeString(kategori);
        dest.writeString(foto);
        dest.writeString(nama);
        dest.writeString(email); // Tambahkan ini
        dest.writeString(lokasi);
        dest.writeString(tanggal);
        dest.writeString(isiLaporan);
        dest.writeString(telepon);
        dest.writeString(status);
    }

    // Getter Setter Lama (Biarkan saja)
    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public String getIsiLaporan() { return isiLaporan; }
    public void setIsiLaporan(String isiLaporan) { this.isiLaporan = isiLaporan; }
    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}