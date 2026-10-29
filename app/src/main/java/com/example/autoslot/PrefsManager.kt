package com.example.autoslot

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException

/**
 * Pembungkus SharedPreferences (key-value) untuk data lokal aplikasi.
 * Riwayat booking dan data pelanggan terakhir dipisah per akun yang sedang login.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val auth = AuthManager(context)

    private fun userKey(base: String): String = base + "_" + (auth.currentUser ?: "guest")

    // ---- Pengaturan aplikasi (global) ----
    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    /** Status onboarding: penanda aplikasi sudah pernah dibuka. */
    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    // ---- Data pelanggan yang diingat (per akun) ----
    var rememberMe: Boolean
        get() = prefs.getBoolean(userKey(KEY_REMEMBER), false)
        set(value) = prefs.edit().putBoolean(userKey(KEY_REMEMBER), value).apply()

    val lastOwner: String get() = prefs.getString(userKey(KEY_LAST_OWNER), "").orEmpty()
    val lastPlate: String get() = prefs.getString(userKey(KEY_LAST_PLATE), "").orEmpty()
    val lastVehicle: String get() = prefs.getString(userKey(KEY_LAST_VEHICLE), "").orEmpty()

    fun saveLastCustomer(owner: String, plate: String, vehicle: String) {
        val editor = prefs.edit()
        editor.putString(userKey(KEY_LAST_OWNER), owner)
        editor.putString(userKey(KEY_LAST_PLATE), plate)
        editor.putString(userKey(KEY_LAST_VEHICLE), vehicle)
        editor.apply()
    }

    // ---- Riwayat booking (string JSON array, per akun) ----
    fun getHistory(): MutableList<Booking> {
        val raw = prefs.getString(userKey(KEY_HISTORY), null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i -> Booking.fromJson(arr.getJSONObject(i)) }
        } catch (e: JSONException) {
            mutableListOf()
        }
    }

    private fun saveHistory(list: List<Booking>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(userKey(KEY_HISTORY), arr.toString()).apply()
    }

    fun addBooking(booking: Booking) {
        val list = getHistory()
        list.add(0, booking) // terbaru di atas
        saveHistory(list)
    }

    fun removeBooking(code: String) {
        saveHistory(getHistory().filterNot { it.code == code })
    }

    fun clearHistory() {
        prefs.edit().remove(userKey(KEY_HISTORY)).apply()
    }

    /** Menghapus seluruh data aplikasi (riwayat & pengaturan). Akun dan sesi login tidak ikut terhapus. */
    fun clearAll() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val PREF_NAME = "autoslot_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_REMEMBER = "remember_me"
        private const val KEY_LAST_OWNER = "last_owner"
        private const val KEY_LAST_PLATE = "last_plate"
        private const val KEY_LAST_VEHICLE = "last_vehicle"
        private const val KEY_HISTORY = "booking_history"
    }
}
