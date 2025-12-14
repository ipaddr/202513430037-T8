package com.azhar.reportapps.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class ModelDatabase implements Serializable, Parcelable {

    private String uid; // ID Firebase (String)
    private String idUser; // ID Pelapor
    private String kategori;
    private String foto; // Nanti berisi URL Foto (Bukan Base64 lagi)
    private String namaPelapor;
    private String lokasi;
    private String tanggal;
    private String isiLaporan;
    private String status;
    private String telepon;

    // WAJIB ADA: Constructor Kosong untuk Firebase
    public ModelDatabase() { }

    // Getter & Setter
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getNamaPelapor() { return namaPelapor; }
    public void setNamaPelapor(String namaPelapor) { this.namaPelapor = namaPelapor; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getIsiLaporan() { return isiLaporan; }
    public void setIsiLaporan(String isiLaporan) { this.isiLaporan = isiLaporan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }

    // Parcelable Implementation (Agar bisa dikirim antar Activity)
    protected ModelDatabase(Parcel in) {
        uid = in.readString();
        idUser = in.readString();
        kategori = in.readString();
        foto = in.readString();
        namaPelapor = in.readString();
        lokasi = in.readString();
        tanggal = in.readString();
        isiLaporan = in.readString();
        status = in.readString();
        telepon = in.readString();
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
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(idUser);
        dest.writeString(kategori);
        dest.writeString(foto);
        dest.writeString(namaPelapor);
        dest.writeString(lokasi);
        dest.writeString(tanggal);
        dest.writeString(isiLaporan);
        dest.writeString(status);
        dest.writeString(telepon);
    }
}