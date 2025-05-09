package com.contoh.abrtodolist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var editTextUsernameRegister: EditText
    private lateinit var editTextEmailRegister: EditText
    private lateinit var editTextPasswordRegister: EditText
    private lateinit var editTextConfirmPasswordRegister: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewGoToLogin: TextView

    private lateinit var sharedPreferences: SharedPreferences

    private val TAG = "RegisterActivity"
    private val PREF_NAME = "UserAccounts"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registerpage)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        editTextUsernameRegister = findViewById(R.id.editTextUsernameRegister)
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister)
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister)
        editTextConfirmPasswordRegister = findViewById(R.id.editTextConfirmPasswordRegister)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin)

        imageViewProfile.setOnClickListener {
            Toast.makeText(this, "Fitur ganti gambar profil akan datang!", Toast.LENGTH_SHORT).show()
        }

        buttonRegister.setOnClickListener {
            val username = editTextUsernameRegister.text.toString().trim()
            val email = editTextEmailRegister.text.toString().trim()
            val password = editTextPasswordRegister.text.toString().trim()
            val confirmPassword = editTextConfirmPasswordRegister.text.toString().trim()

            Log.d(TAG, "Tombol Register diklik")
            Log.i(TAG, "Data input: Username: $username, Email: $email")

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Registrasi gagal: Ada field yang kosong")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Registrasi gagal: Format email tidak valid")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Registrasi gagal: Password tidak cocok")
                return@setOnClickListener
            }

            // Cek apakah email sudah terdaftar
            if (sharedPreferences.contains(email)) {
                Toast.makeText(this, "Email sudah terdaftar!", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Registrasi gagal: Email '$email' sudah terdaftar")
                return@setOnClickListener
            }

            val editor = sharedPreferences.edit()
            editor.putString(email, password)
            editor.apply()

            Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
            Log.i(TAG, "Registrasi berhasil untuk user: $username dengan email: $email")

            finish()
        }

        textViewGoToLogin.setOnClickListener {
            finish()
        }
    }
}