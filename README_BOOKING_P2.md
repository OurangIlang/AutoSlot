# AutoSlot — File Booking Core (P2)

Paket ini mengumpulkan file yang berkaitan langsung dengan fitur Booking Core dari project AutoSlot.

## File inti Kotlin
- `MainActivity.kt` — form booking, validasi input, dialog, dan aksi menu.
- `Booking.kt` — model/kontrak data booking serta nama data yang dikirim melalui Intent.
- `Catalog.kt` — katalog layanan dan fungsi bantu validasi/kode booking.
- `PrefsManager.kt` — penyimpanan lokal data booking dan riwayat/preferensi yang dipakai bersama.

## Resource Android
- `activity_main.xml` — layout form booking.
- `menu_main.xml` — options menu halaman utama.
- `menu_context_form.xml` — context menu form.
- `menu_popup_quick.xml` — popup menu untuk pengisian cepat.
- `strings.xml` — resource string dari project asli yang mungkin dirujuk oleh layout dan kode booking.

## Cara menggunakan
File ini merupakan paket seleksi, bukan project Android Studio lengkap. Salin file ke folder dengan path yang sama pada project AutoSlot utama. Jangan mengganti seluruh `strings.xml` tanpa menggabungkan perubahan yang sudah ada di project kelompok.

## Catatan kerja tim
- Branch tugas P2: `feature/2-booking`.
- `Booking.kt` dan `PrefsManager.kt` merupakan kontrak bersama dengan P3 (Ticket & History). Koordinasikan perubahan nama field, Intent extras, atau API penyimpanan sebelum mengubahnya.
- Jangan push langsung ke `main` atau `develop`; buat Pull Request ke `develop` setelah diuji.
- `strings_booking.xml` tidak ditemukan pada ZIP project yang diberikan, jadi paket ini menyertakan `strings.xml` asli sebagai referensi resource saat ini, bukan membuat file baru yang belum ada di project.
