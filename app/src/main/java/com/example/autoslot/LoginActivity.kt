package com.example.autoslot

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: AuthManager
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbShowPassword: CheckBox

    // Activity Result: menerima username dari RegisterActivity setelah daftar berhasil.
    private val registerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                etUsername.setText(result.data?.getStringExtra(RegisterActivity.EXTRA_USERNAME).orEmpty())
                etPassword.text.clear()
                etPassword.requestFocus()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = AuthManager(this)

        // Sudah punya sesi aktif -> langsung ke halaman utama.
        if (auth.isLoggedIn) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        cbShowPassword = findViewById(R.id.cbShowPassword)

        cbShowPassword.setOnCheckedChangeListener { _, checked ->
            etPassword.transformationMethod = if (checked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            etPassword.setSelection(etPassword.text.length)
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener { attemptLogin() }
        findViewById<TextView>(R.id.tvForgot).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            registerLauncher.launch(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        var valid = true
        if (username.isEmpty()) {
            etUsername.error = getString(R.string.error_username_empty)
            valid = false
        }
        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_empty)
            valid = false
        }
        if (!valid) return

        if (auth.login(username, password)) {
            Toast.makeText(
                this,
                getString(R.string.toast_welcome, auth.getFullName()),
                Toast.LENGTH_SHORT
            ).show()
            goToMain()
        } else {
            etPassword.text.clear()
            etPassword.error = getString(R.string.error_login_failed)
            etPassword.requestFocus()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
