package com.example.autoslot

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

enum class RegisterResult { SUCCESS, USERNAME_TAKEN }

/**
 * Pengelola akun & sesi login berbasis SharedPreferences (file terpisah dari data aplikasi).
 * Password dan jawaban keamanan TIDAK disimpan sebagai teks asli, melainkan hash SHA-256 + salt.
 * Catatan: ini untuk keperluan pembelajaran; aplikasi produksi sebaiknya memakai autentikasi server.
 */
class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(AUTH_PREF_NAME, Context.MODE_PRIVATE)

    val currentUser: String? get() = prefs.getString(KEY_SESSION, null)
    val isLoggedIn: Boolean get() = currentUser != null

    // ---------------------------------------------------------------- util

    fun normalizeUsername(raw: String): String = raw.trim().lowercase()

    private fun normalizeAnswer(raw: String): String = raw.trim().lowercase()

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hash(salt: String, value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + value).toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun sameHash(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

    private fun loadAccounts(): JSONObject = try {
        JSONObject(prefs.getString(KEY_ACCOUNTS, "{}") ?: "{}")
    } catch (e: JSONException) {
        JSONObject()
    }

    private fun saveAccounts(accounts: JSONObject) {
        prefs.edit().putString(KEY_ACCOUNTS, accounts.toString()).apply()
    }

    private fun getAccount(username: String): JSONObject? =
        loadAccounts().optJSONObject(normalizeUsername(username))

    // ---------------------------------------------------------------- akun

    fun register(
        username: String,
        fullName: String,
        password: String,
        question: String,
        answer: String
    ): RegisterResult {
        val key = normalizeUsername(username)
        val accounts = loadAccounts()
        if (accounts.has(key)) return RegisterResult.USERNAME_TAKEN

        val salt = newSalt()
        val answerSalt = newSalt()
        accounts.put(key, JSONObject().apply {
            put("name", fullName.trim())
            put("salt", salt)
            put("hash", hash(salt, password))
            put("question", question)
            put("ansSalt", answerSalt)
            put("ansHash", hash(answerSalt, normalizeAnswer(answer)))
        })
        saveAccounts(accounts)
        return RegisterResult.SUCCESS
    }

    /** Mengembalikan true dan membuat sesi bila username + password benar. */
    fun login(username: String, password: String): Boolean {
        val account = getAccount(username) ?: return false
        val ok = sameHash(hash(account.getString("salt"), password), account.getString("hash"))
        if (ok) prefs.edit().putString(KEY_SESSION, normalizeUsername(username)).apply()
        return ok
    }

    fun logout() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    fun getFullName(username: String? = currentUser): String {
        if (username == null) return ""
        return getAccount(username)?.optString("name").orEmpty().ifEmpty { username }
    }

    // ---------------------------------------------------------------- lupa password

    fun getSecurityQuestion(username: String): String? =
        getAccount(username)?.optString("question")

    fun verifyAnswer(username: String, answer: String): Boolean {
        val account = getAccount(username) ?: return false
        val expected = account.getString("ansHash")
        val actual = hash(account.getString("ansSalt"), normalizeAnswer(answer))
        return sameHash(actual, expected)
    }

    fun resetPassword(username: String, newPassword: String): Boolean {
        val key = normalizeUsername(username)
        val accounts = loadAccounts()
        val account = accounts.optJSONObject(key) ?: return false
        val salt = newSalt()
        account.put("salt", salt)
        account.put("hash", hash(salt, newPassword))
        accounts.put(key, account)
        saveAccounts(accounts)
        return true
    }

    companion object {
        private const val AUTH_PREF_NAME = "autoslot_auth"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_SESSION = "session_user"

        private val usernameRegex = Regex("^[a-z0-9._]{4,20}$")
        fun isUsernameValid(normalized: String): Boolean = usernameRegex.matches(normalized)
        const val MIN_PASSWORD_LENGTH = 6
    }
}
