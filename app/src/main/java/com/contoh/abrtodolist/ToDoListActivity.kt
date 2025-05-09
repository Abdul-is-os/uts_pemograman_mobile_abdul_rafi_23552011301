package com.contoh.abrtodolist

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ToDoListActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewToDoItems: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var toDoAdapter: ToDoAdapter
    private val toDoList = mutableListOf<ToDoItem>()
    private lateinit var fabDeleteCheckedTasks: FloatingActionButton
    private val PREFS_NAME = "ToDoListPrefs"
    private val KEY_TODO_LIST = "todo_list_data"
    private lateinit var loggedInUserEmail: String
    private val ACCOUNT_PREFS_NAME = "UserAccounts"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todolistmainpage)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recyclerViewToDoItems = findViewById(R.id.recyclerViewToDoItems)
        fabAddTask = findViewById(R.id.fabAddTask)
        fabDeleteCheckedTasks = findViewById(R.id.fabDeleteCheckedTasks)

        val accountPrefs = getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE)
        loggedInUserEmail = accountPrefs.getString("loggedInUserEmail", "default_user") ?: "default_user"

        if (loggedInUserEmail == "default_user") {

            Toast.makeText(this, "Sesi tidak valid, silakan login kembali.", Toast.LENGTH_LONG).show()
            performLogout()
            return
        }

        setupRecyclerView()
        loadToDoItemsFromPrefs()

        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        fabDeleteCheckedTasks.setOnClickListener {
            deleteCheckedTasks()
        }
    }


    private fun setupRecyclerView() {
        toDoAdapter = ToDoAdapter(toDoList) { item, isChecked ->
            Log.d("ToDoListActivity", "Item: ${item.text}, Checked: $isChecked for user $loggedInUserEmail")
            saveToDoItemsToPrefs()
            updateDeleteButtonVisibility()
        }
        recyclerViewToDoItems.layoutManager = LinearLayoutManager(this)
        recyclerViewToDoItems.adapter = toDoAdapter
    }

    private fun updateDeleteButtonVisibility() {
        val hasCheckedItems = toDoList.any { it.isCompleted }
        if (hasCheckedItems) {
            fabDeleteCheckedTasks.visibility = View.VISIBLE
        } else {
            fabDeleteCheckedTasks.visibility = View.GONE
        }
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambah Tugas Baru")

        val input = EditText(this)
        input.hint = "Masukkan teks tugas"
        builder.setView(input)

        builder.setPositiveButton("Tambah") { dialog, _ ->
            val taskText = input.text.toString().trim()
            if (taskText.isNotEmpty()) {
                val newItem = ToDoItem(
                    id = System.currentTimeMillis(),
                    text = taskText,
                    isCompleted = false
                )
                toDoList.add(newItem)
                toDoAdapter.notifyItemInserted(toDoList.size - 1)
                saveToDoItemsToPrefs()
                updateDeleteButtonVisibility()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Teks tugas tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun deleteCheckedTasks() {
        val itemsToRemove = toDoList.filter { it.isCompleted }
        if (itemsToRemove.isEmpty()) {
            Toast.makeText(this, "Tidak ada tugas terpilih untuk dihapus.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Anda yakin ingin menghapus ${itemsToRemove.size} tugas yang terpilih?")
            .setPositiveButton("Hapus") { _, _ ->
                toDoList.removeAll(itemsToRemove)
                toDoAdapter.notifyDataSetChanged()
                saveToDoItemsToPrefs()
                updateDeleteButtonVisibility()
                Toast.makeText(this, "${itemsToRemove.size} tugas dihapus.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun saveToDoItemsToPrefs() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val userSpecificKey = "${KEY_TODO_LIST}_$loggedInUserEmail"
        val json = gson.toJson(toDoList)
        editor.putString(userSpecificKey, json)
        editor.apply()
    }

    private fun loadToDoItemsFromPrefs() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val gson = Gson()
        val userSpecificKey = "${KEY_TODO_LIST}_$loggedInUserEmail"
        val json = sharedPreferences.getString(userSpecificKey, null)
        val type = object : TypeToken<MutableList<ToDoItem>>() {}.type

        toDoList.clear()
        if (json != null) {
            try {
                val items: MutableList<ToDoItem> = gson.fromJson(json, type)
                toDoList.addAll(items)
            } catch (e: Exception) {
                Log.e("ToDoListActivity", "Error parsing JSON for $loggedInUserEmail", e)
            }
        }

        if (::toDoAdapter.isInitialized) {
            toDoAdapter.notifyDataSetChanged()
        }
        updateDeleteButtonVisibility()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_todolist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings diklik!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Anda yakin ingin logout?")
                    .setPositiveButton("Logout") { _, _ ->
                        performLogout()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        val accountPrefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE)
        val editor = accountPrefs.edit()
        editor.remove("loggedInUserEmail")
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup ToDoListActivity
        Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show()
    }
}