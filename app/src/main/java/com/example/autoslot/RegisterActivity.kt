package com.example.autoslot

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: AuthManager
    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var spQuestion: Spinner
    private lateinit var etAnswer: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = AuthManager(this)
        etFullName = findViewById(R.id.etFullName)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirm = findViewById(R.id.etConfirm)
        spQuestion = findViewById(R.id.spQuestion)
        etAnswer = findViewById(R.id.etAnswer)

        findViewById<Button>(R.id.btnRegister).setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val fullName = etFullName.text.toString().trim()
        val username = auth.normalizeUsername(etUsername.text.toString())
        val password = etPassword.text.toString()
        val confirm = etConfirm.text.toString()
        val answer = etAnswer.text.toString().trim()

        var valid = true
        var firstInvalid: View? = null
        fun fail(view: View) {
            valid = false
            if (firstInvalid == null) firstInvalid = view
        }

        if (fullName.length < 3) {
            etFullName.error = getString(R.string.error_owner_short)
            fail(etFullName)
        }
        if (username.isEmpty()) {
            etUsername.error = getString(R.string.error_username_empty)
            fail(etUsername)
        } else if (!AuthManager.isUsernameValid(username)) {
            etUsername.error = getString(R.string.error_username_invalid)
            fail(etUsername)
        }
        if (password.length < AuthManager.MIN_PASSWORD_LENGTH) {
            etPassword.error = getString(R.string.error_password_short)
            fail(etPassword)
        }
        if (confirm != password) {
            etConfirm.error = getString(R.string.error_confirm_mismatch)
            fail(etConfirm)
        }
        if (spQuestion.selectedItemPosition == 0) {
            (spQuestion.selectedView as? TextView)?.error = getString(R.string.error_question_empty)
            fail(spQuestion)
        }
        if (answer.length < 2) {
            etAnswer.error = getString(R.string.error_answer_short)
            fail(etAnswer)
        }
        if (!valid) {
            Toast.makeText(this, R.string.toast_form_invalid, Toast.LENGTH_SHORT).show()
            firstInvalid?.requestFocus()
            return
        }

        val result = auth.register(
            username = username,
            fullName = fullName,
            password = password,
            question = spQuestion.selectedItem.toString(),
            answer = answer
        )
        when (result) {
            RegisterResult.USERNAME_TAKEN -> {
                etUsername.error = getString(R.string.error_username_taken)
                etUsername.requestFocus()
            }
            RegisterResult.SUCCESS -> {
                Toast.makeText(this, R.string.toast_register_success, Toast.LENGTH_LONG).show()
                setResult(RESULT_OK, Intent().putExtra(EXTRA_USERNAME, username))
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }
}
