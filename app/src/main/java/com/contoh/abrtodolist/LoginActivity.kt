package com.contoh.abrtodolist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmailLogin: EditText
    private lateinit var editTextPasswordLogin: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewGoToRegister: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "UserAccounts"
    private val KEY_LOGGED_IN_USER_EMAIL = "loggedInUserEmail"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (isUserLoggedIn()) {
            navigateToToDoList()
            return
        }

        setContentView(R.layout.loginpage)
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin)
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister)
        buttonLogin.setOnClickListener {
            val email = editTextEmailLogin.text.toString().trim()
            val password = editTextPasswordLogin.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val storedPassword = sharedPreferences.getString(email, null)
            if (storedPassword != null && storedPassword == password) {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                val editor = sharedPreferences.edit()
                editor.putString(KEY_LOGGED_IN_USER_EMAIL, email)
                editor.putBoolean("isLoggedIn", true)
                editor.apply()
                navigateToToDoList()
            } else {
                Toast.makeText(this, "Email atau Password salah!", Toast.LENGTH_SHORT).show()
            }
        }

        textViewGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false) &&
                sharedPreferences.getString(KEY_LOGGED_IN_USER_EMAIL, null) != null
    }

    private fun navigateToToDoList() {
        val intent = Intent(this, ToDoListActivity::class.java)
        startActivity(intent)
    }
}