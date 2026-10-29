package com.example.autoslot

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Satu paket layanan servis beserta estimasi durasi (menit) dan biaya (rupiah). */
data class ServicePackage(val name: String, val durationMinutes: Int, val price: Int)

/** Data statis layanan + fungsi bantu format/validasi. */
object Catalog {

    // Urutan harus sama dengan array service_entries di strings.xml (setelah item placeholder).
    val services = listOf(
        ServicePackage("Ganti Oli Cepat", 30, 150_000),
        ServicePackage("Servis Ringan / Tune Up", 60, 250_000),
        ServicePackage("Servis Berkala Lengkap", 120, 500_000)
    )

    private val idLocale: Locale = Locale.forLanguageTag("id-ID")

    /** Contoh valid: B 1234 XYZ, D 5678 AB, AB 12 C */
    private val plateRegex = Regex("^[A-Z]{1,2} ?[0-9]{1,4} ?[A-Z]{0,3}$")

    fun normalizePlate(raw: String): String =
        raw.trim().uppercase().replace(Regex("\\s+"), " ")

    fun isPlateValid(normalizedPlate: String): Boolean = plateRegex.matches(normalizedPlate)

    fun formatRupiah(amount: Int): String =
        "Rp " + NumberFormat.getInstance(idLocale).format(amount)

    fun formatDuration(minutes: Int): String {
        if (minutes < 60) return "$minutes menit"
        val h = minutes / 60
        val m = minutes % 60
        return if (m == 0) "$minutes menit ($h jam)" else "$minutes menit ($h jam $m menit)"
    }

    fun formatDate(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", idLocale).format(Date(millis))

    /** Kode booking = prefix + jam + plat nomor, contoh: AS-1000-B1234XYZ */
    fun makeBookingCode(slot: String, plate: String): String =
        "AS-" + slot.replace(":", "") + "-" + plate.filter { it.isLetterOrDigit() }
}
