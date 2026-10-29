package com.example.autoslot

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Reset password dua langkah: (1) cari akun -> (2) jawab pertanyaan keamanan + password baru. */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: AuthManager
    private lateinit var etUsername: EditText
    private lateinit var btnFind: Button
    private lateinit var llStep2: View
    private lateinit var tvQuestion: TextView
    private lateinit var etAnswer: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirm: EditText

    private var verifiedUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = AuthManager(this)
        etUsername = findViewById(R.id.etUsername)
        btnFind = findViewById(R.id.btnFind)
        llStep2 = findViewById(R.id.llStep2)
        tvQuestion = findViewById(R.id.tvQuestion)
        etAnswer = findViewById(R.id.etAnswer)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirm = findViewById(R.id.etConfirm)

        btnFind.setOnClickListener { findAccount() }
        findViewById<Button>(R.id.btnReset).setOnClickListener { resetPassword() }
    }

    private fun findAccount() {
        val username = auth.normalizeUsername(etUsername.text.toString())
        if (username.isEmpty()) {
            etUsername.error = getString(R.string.error_username_empty)
            return
        }
        val question = auth.getSecurityQuestion(username)
        if (question == null) {
            etUsername.error = getString(R.string.error_username_not_found)
            return
        }
        verifiedUser = username
        tvQuestion.text = question
        llStep2.visibility = View.VISIBLE
        etUsername.isEnabled = false
        btnFind.isEnabled = false
        etAnswer.requestFocus()
    }

    private fun resetPassword() {
        val username = verifiedUser ?: return
        val answer = etAnswer.text.toString().trim()
        val newPassword = etNewPassword.text.toString()
        val confirm = etConfirm.text.toString()

        var valid = true
        if (answer.isEmpty()) {
            etAnswer.error = getString(R.string.error_answer_short)
            valid = false
        }
        if (newPassword.length < AuthManager.MIN_PASSWORD_LENGTH) {
            etNewPassword.error = getString(R.string.error_password_short)
            valid = false
        }
        if (confirm != newPassword) {
            etConfirm.error = getString(R.string.error_confirm_mismatch)
            valid = false
        }
        if (!valid) return

        if (!auth.verifyAnswer(username, answer)) {
            etAnswer.error = getString(R.string.error_answer_wrong)
            etAnswer.requestFocus()
            return
        }

        auth.resetPassword(username, newPassword)
        Toast.makeText(this, R.string.toast_reset_success, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
