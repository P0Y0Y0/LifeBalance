# 🩺 LifeBalance App

**LifeBalance** adalah aplikasi Android yang dirancang untuk membantu pengguna menjaga keseimbangan gaya hidup sehat melalui pemantauan aktivitas harian seperti langkah kaki, berat badan, asupan air, dan makanan.  
Aplikasi ini memadukan berbagai fitur kesehatan dalam satu platform terintegrasi, dengan penyimpanan data lokal dan cloud secara sinkron.

---

## 📱 **Deskripsi Singkat Aplikasi**

LifeBalance membantu pengguna:
- **Mencatat langkah harian** menggunakan Step Counter Sensor.  
- **Melacak konsumsi air** dengan Water Tracker dan notifikasi pengingat.  
- **Mencatat berat badan & menghitung BMI otomatis.**  
- **Mencatat asupan makanan dan kalori** dengan integrasi Firestore.  
- **Mengatur rencana kesehatan / to-do list** melalui fitur Task (Room Database).  
- **Menampilkan data pengguna dan BMI di halaman profil.**

Aplikasi ini dibangun menggunakan arsitektur **MVVM (Model-View-ViewModel)** dan teknologi modern seperti:
- **Firebase Authentication** – login dan registrasi pengguna.  
- **Firebase Firestore** – penyimpanan data real-time berbasis cloud.  
- **Room Database** – penyimpanan data lokal (offline).  
- **Dagger Hilt** – dependency injection modular dan efisien.  
- **WorkManager & AlarmManager** – pengingat aktivitas aplikasi berjalan di background.  
- **Fused Location Provider** – untuk menampilkan lokasi pengguna secara akurat.  

---

## ⚙️ **Teknologi yang Digunakan**
|      Komponen     |              Deskripsi                  |
|-------------------|-----------------------------------------|
|     Kotlin        |       Bahasa pemrograman utama          |
| Firebase          |   Authentication & Firestore Database   |
| Room Database     |       Penyimpanan lokal offline         |
| MVVM Architecture |         Pemisahan logika & UI           |
| Dagger Hilt       |         Dependency Injection            |
| WorkManager       |  Alarm reminder berjalan di background  |
| Material Design   |  Tampilan antarmuka modern & konsisten  |

---

## 👥 **Anggota Kelompok**

|            Nama          |     NIM     |             Peran                |
|--------------------------|-------------|----------------------------------|
| Evan Yo                  | 00000068870 |         Design UI/UX             |
| Theon Anabel Deadora     | 00000076775 | Developer & Database Integration |
| Raihan Maulidar          | 00000104962 |        Logic & Testing           |
| Muhammad Thomas Pangukir | 00000109875 |    Dokumentasi & Deployment      |

---

## 🧠 **Tujuan Pengembangan**
Memberikan solusi kesehatan digital terpadu yang dapat digunakan siapa pun untuk menjaga keseimbangan hidup (Life Balance) secara konsisten dan efisien, tanpa perlu menggunakan banyak aplikasi terpisah.

---

## 📄 **Lisensi**
Aplikasi ini dikembangkan untuk tujuan **pendidikan dan penelitian**, bukan untuk distribusi komersial.

---

