// Mendefinisikan package untuk kelas ini, sesuai dengan struktur proyek Anda.
package com.contoh.abrtodolist

// Impor kelas-kelas yang diperlukan dari Android SDK dan library pihak ketiga.
import android.content.Context // Digunakan untuk mengakses SharedPreferences dan resource sistem lainnya.
import android.content.Intent // Digunakan untuk navigasi antar Activity.
import androidx.appcompat.app.AppCompatActivity // Kelas dasar untuk Activity yang menggunakan fitur AppCompat.
import android.os.Bundle // Digunakan untuk menyimpan dan mengambil state Activity.
import android.util.Log // Digunakan untuk logging (mencatat pesan untuk debugging).
import android.view.Menu // Digunakan untuk membuat menu opsi di ActionBar/Toolbar.
import android.view.MenuItem // Merepresentasikan item dalam sebuah Menu.
import android.view.View // Kelas dasar untuk semua komponen UI. Digunakan di sini untuk mengatur visibilitas.
import android.widget.EditText // Komponen UI untuk input teks oleh pengguna.
import android.widget.Toast // Digunakan untuk menampilkan pesan singkat (notifikasi) kepada pengguna.
import androidx.appcompat.app.AlertDialog // Digunakan untuk menampilkan dialog konfirmasi atau input.
import androidx.appcompat.widget.Toolbar // Komponen UI yang berfungsi sebagai ActionBar kustom.
import androidx.recyclerview.widget.LinearLayoutManager // Mengatur item dalam RecyclerView secara linear (vertikal atau horizontal).
import androidx.recyclerview.widget.RecyclerView // Komponen UI yang efisien untuk menampilkan daftar data yang besar.
import com.google.android.material.floatingactionbutton.FloatingActionButton // Tombol aksi mengambang dari Material Design.
import com.google.gson.Gson // Library untuk konversi objek Java/Kotlin ke JSON dan sebaliknya.
import com.google.gson.reflect.TypeToken // Digunakan bersama Gson untuk mendapatkan tipe generik (misalnya List<ToDoItem>).

// Deklarasi kelas ToDoListActivity yang merupakan turunan dari AppCompatActivity.
// Ini adalah layar utama tempat pengguna akan melihat dan mengelola daftar tugas mereka.
class ToDoListActivity : AppCompatActivity() {

    // Deklarasi variabel untuk komponen UI dengan `private lateinit var`.
    // `lateinit` berarti variabel ini akan diinisialisasi nanti (biasanya di onCreate).
    private lateinit var toolbar: Toolbar // Toolbar di bagian atas layar.
    private lateinit var recyclerViewToDoItems: RecyclerView // Untuk menampilkan daftar tugas.
    private lateinit var fabAddTask: FloatingActionButton // Tombol (+) untuk menambah tugas baru.
    private lateinit var toDoAdapter: ToDoAdapter // Adapter untuk RecyclerView, menghubungkan data dengan tampilan.
    private val toDoList = mutableListOf<ToDoItem>() // Daftar (MutableList) yang akan menyimpan objek ToDoItem. Ini adalah sumber data utama untuk adapter.
    private lateinit var fabDeleteCheckedTasks: FloatingActionButton // Tombol (FAB) untuk menghapus tugas yang terpilih/diceklis.

    // Konstanta untuk nama file dan kunci SharedPreferences tempat data ToDo List disimpan.
    private val PREFS_NAME = "ToDoListPrefs" // Nama file SharedPreferences untuk data ToDo.
    private val KEY_TODO_LIST = "todo_list_data" // Kunci dasar untuk menyimpan list ToDo dalam SharedPreferences.

    // Variabel untuk menyimpan email pengguna yang sedang login.
    // Digunakan untuk memuat dan menyimpan ToDo List yang spesifik untuk pengguna tersebut.
    private lateinit var loggedInUserEmail: String

    // Konstanta untuk nama file SharedPreferences tempat data akun pengguna (login) disimpan.
    // Harus sama dengan yang digunakan di LoginActivity dan RegisterActivity.
    private val ACCOUNT_PREFS_NAME = "UserAccounts"


    // Fungsi `onCreate` dipanggil saat Activity pertama kali dibuat.
    // Tempat untuk inisialisasi UI, data, dan listener.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Memanggil implementasi `onCreate` dari kelas induk.
        setContentView(R.layout.todolistmainpage) // Mengatur layout XML untuk Activity ini.

        // Inisialisasi Toolbar.
        toolbar = findViewById(R.id.toolbar) // Mengambil referensi Toolbar dari layout XML.
        setSupportActionBar(toolbar) // Mengatur Toolbar ini sebagai ActionBar untuk Activity.
        supportActionBar?.setDisplayShowTitleEnabled(false) // Menyembunyikan judul default ActionBar karena kita mungkin menggunakan TextView kustom di Toolbar.

        // Inisialisasi komponen UI lainnya.
        recyclerViewToDoItems = findViewById(R.id.recyclerViewToDoItems) // Mengambil referensi RecyclerView.
        fabAddTask = findViewById(R.id.fabAddTask) // Mengambil referensi FAB untuk menambah tugas.
        fabDeleteCheckedTasks = findViewById(R.id.fabDeleteCheckedTasks) // Mengambil referensi FAB untuk menghapus tugas.

        // Mengambil email pengguna yang sedang login dari SharedPreferences akun.
        val accountPrefs = getSharedPreferences(ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE)
        // Mengambil nilai string dengan kunci "loggedInUserEmail". Jika tidak ditemukan, gunakan "default_user".
        // Operator `?:` (Elvis operator) menyediakan nilai default jika hasil di sebelah kiri null.
        loggedInUserEmail = accountPrefs.getString("loggedInUserEmail", "default_user") ?: "default_user"

        // Pengecekan jika email pengguna tidak berhasil diambil (masih "default_user").
        // Ini bisa terjadi jika ada masalah dengan alur login atau data SharedPreferences.
        if (loggedInUserEmail == "default_user") {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali.", Toast.LENGTH_LONG).show()
            performLogout() // Memanggil fungsi untuk melakukan logout dan kembali ke halaman login.
            return // Menghentikan eksekusi lebih lanjut dari `onCreate` karena sesi tidak valid.
        }

        // Setup RecyclerView dan memuat data tugas.
        setupRecyclerView() // Memanggil fungsi untuk mengkonfigurasi RecyclerView dan adapter-nya.
        loadToDoItemsFromPrefs() // Memanggil fungsi untuk memuat daftar tugas dari SharedPreferences.

        // Mengatur listener klik untuk FAB tambah tugas.
        fabAddTask.setOnClickListener {
            showAddTaskDialog() // Memanggil fungsi untuk menampilkan dialog penambahan tugas baru.
        }

        // Mengatur listener klik untuk FAB hapus tugas terpilih.
        fabDeleteCheckedTasks.setOnClickListener {
            deleteCheckedTasks() // Memanggil fungsi untuk menghapus tugas yang telah diceklis.
        }
    }


    // Fungsi untuk mengkonfigurasi RecyclerView dan adapter-nya.
    private fun setupRecyclerView() {
        // Membuat instance ToDoAdapter, memberikan daftar `toDoList` dan sebuah lambda.
        // Lambda ini akan dieksekusi setiap kali status checkbox pada item di adapter berubah.
        toDoAdapter = ToDoAdapter(toDoList) { item, isChecked ->
            // Mencatat ke Logcat saat item diceklis/tidak diceklis.
            Log.d("ToDoListActivity", "Item: ${item.text}, Checked: $isChecked for user $loggedInUserEmail")
            // Menyimpan perubahan (status item.isCompleted sudah diupdate di adapter) ke SharedPreferences.
            saveToDoItemsToPrefs()
            // Memperbarui visibilitas tombol hapus berdasarkan apakah ada item yang diceklis.
            updateDeleteButtonVisibility()
        }
        // Mengatur LayoutManager untuk RecyclerView (LinearLayoutManager akan menampilkan item secara vertikal).
        recyclerViewToDoItems.layoutManager = LinearLayoutManager(this)
        // Mengatur adapter untuk RecyclerView.
        recyclerViewToDoItems.adapter = toDoAdapter
    }

    // Fungsi untuk memperbarui visibilitas tombol/FAB hapus tugas terpilih.
    private fun updateDeleteButtonVisibility() {
        // Mengecek apakah ada setidaknya satu item di `toDoList` yang properti `isCompleted`-nya `true`.
        val hasCheckedItems = toDoList.any { it.isCompleted }
        // Jika ada item yang diceklis, tampilkan tombol hapus.
        if (hasCheckedItems) {
            fabDeleteCheckedTasks.visibility = View.VISIBLE
        } else { // Jika tidak ada, sembunyikan tombol hapus.
            fabDeleteCheckedTasks.visibility = View.GONE
        }
    }

    // Fungsi untuk menampilkan dialog penambahan tugas baru.
    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this) // Membuat builder untuk AlertDialog.
        builder.setTitle("Tambah Tugas Baru") // Mengatur judul dialog.

        val input = EditText(this) // Membuat EditText secara programatik untuk input teks tugas.
        input.hint = "Masukkan teks tugas" // Mengatur placeholder untuk EditText.
        builder.setView(input) // Menambahkan EditText ke dalam dialog.

        // Mengatur tombol positif ("Tambah") pada dialog.
        builder.setPositiveButton("Tambah") { dialog, _ ->
            val taskText = input.text.toString().trim() // Mengambil teks dari EditText dan menghapus spasi di awal/akhir.
            // Jika teks tugas tidak kosong:
            if (taskText.isNotEmpty()) {
                // Membuat objek ToDoItem baru.
                val newItem = ToDoItem(
                    id = System.currentTimeMillis(), // Menggunakan timestamp saat ini sebagai ID unik sementara.
                    text = taskText,
                    isCompleted = false // Tugas baru defaultnya belum selesai.
                )
                toDoList.add(newItem) // Menambahkan item baru ke `toDoList` utama.
                toDoAdapter.notifyItemInserted(toDoList.size - 1) // Memberi tahu adapter bahwa item baru telah ditambahkan di posisi terakhir.
                saveToDoItemsToPrefs() // Menyimpan daftar tugas yang sudah diperbarui ke SharedPreferences.
                updateDeleteButtonVisibility() // Memperbarui visibilitas tombol hapus.
                dialog.dismiss() // Menutup dialog.
            } else { // Jika teks tugas kosong, tampilkan pesan error.
                Toast.makeText(this, "Teks tugas tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        // Mengatur tombol negatif ("Batal") pada dialog.
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel() // Menutup dialog tanpa melakukan apa-apa.
        }
        builder.show() // Menampilkan dialog.
    }

    // Fungsi untuk menghapus tugas-tugas yang telah diceklis.
    private fun deleteCheckedTasks() {
        // Membuat daftar baru yang hanya berisi item-item yang `isCompleted`-nya `true`.
        val itemsToRemove = toDoList.filter { it.isCompleted }
        // Jika tidak ada item yang terpilih untuk dihapus, tampilkan pesan dan keluar dari fungsi.
        if (itemsToRemove.isEmpty()) {
            Toast.makeText(this, "Tidak ada tugas terpilih untuk dihapus.", Toast.LENGTH_SHORT).show()
            return
        }

        // Menampilkan dialog konfirmasi sebelum benar-benar menghapus.
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Anda yakin ingin menghapus ${itemsToRemove.size} tugas yang terpilih?")
            .setPositiveButton("Hapus") { _, _ -> // Jika pengguna memilih "Hapus":
                toDoList.removeAll(itemsToRemove.toSet()) // Menghapus semua item yang ada di `itemsToRemove` dari `toDoList`. Menggunakan toSet() bisa lebih efisien untuk removeAll.
                toDoAdapter.notifyDataSetChanged() // Memberi tahu adapter bahwa seluruh dataset mungkin telah berubah. Ini cara termudah untuk update UI.
                saveToDoItemsToPrefs() // Menyimpan daftar tugas yang sudah diperbarui ke SharedPreferences.
                updateDeleteButtonVisibility() // Memperbarui visibilitas tombol hapus.
                Toast.makeText(this, "${itemsToRemove.size} tugas dihapus.", Toast.LENGTH_SHORT).show() // Menampilkan pesan konfirmasi penghapusan.
            }
            .setNegativeButton("Batal", null) // Tombol "Batal" tidak melakukan apa-apa selain menutup dialog.
            .show() // Menampilkan dialog konfirmasi.
    }


    // Fungsi untuk menyimpan daftar `toDoList` ke SharedPreferences.
    private fun saveToDoItemsToPrefs() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE) // Mengambil instance SharedPreferences.
        val editor = sharedPreferences.edit() // Mendapatkan editor untuk melakukan perubahan.
        val gson = Gson() // Membuat instance Gson untuk konversi ke JSON.
        // Membuat kunci yang spesifik untuk pengguna yang sedang login.
        val userSpecificKey = "${KEY_TODO_LIST}_$loggedInUserEmail"
        val json = gson.toJson(toDoList) // Mengkonversi `toDoList` menjadi String JSON.
        editor.putString(userSpecificKey, json) // Menyimpan String JSON dengan kunci spesifik pengguna.
        editor.apply() // Menerapkan perubahan ke SharedPreferences (disimpan secara asinkron).
    }

    // Fungsi untuk memuat daftar tugas dari SharedPreferences.
    private fun loadToDoItemsFromPrefs() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE) // Mengambil instance SharedPreferences.
        val gson = Gson() // Membuat instance Gson untuk konversi dari JSON.
        val userSpecificKey = "${KEY_TODO_LIST}_$loggedInUserEmail" // Kunci spesifik pengguna.
        val json = sharedPreferences.getString(userSpecificKey, null) // Mengambil String JSON. Jika tidak ada, kembalikan null.
        // Mendapatkan tipe untuk deserialisasi List<ToDoItem> dari JSON.
        val type = object : TypeToken<MutableList<ToDoItem>>() {}.type

        toDoList.clear() // Mengosongkan `toDoList` saat ini sebelum memuat data baru.
        if (json != null) { // Jika ada data JSON yang tersimpan:
            try {
                // Mengkonversi String JSON kembali menjadi MutableList<ToDoItem>.
                val items: MutableList<ToDoItem> = gson.fromJson(json, type)
                toDoList.addAll(items) // Menambahkan semua item yang dimuat ke `toDoList`.
            } catch (e: Exception) { // Menangani jika terjadi error saat parsing JSON (misalnya, data korup).
                Log.e("ToDoListActivity", "Error parsing JSON for $loggedInUserEmail", e)
            }
        }

        // Jika adapter sudah diinisialisasi, beri tahu bahwa dataset telah berubah.
        if (::toDoAdapter.isInitialized) {
            toDoAdapter.notifyDataSetChanged()
        }
        // Selalu perbarui visibilitas tombol hapus setelah memuat data.
        updateDeleteButtonVisibility()
    }


    // Fungsi ini dipanggil untuk membuat menu opsi di Toolbar.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_todolist, menu) // Meng-inflate layout menu (menu_todolist.xml) ke dalam objek Menu.
        return true // Mengembalikan true untuk menandakan bahwa menu telah dibuat.
    }

    // Fungsi ini dipanggil ketika salah satu item menu dipilih oleh pengguna.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Menggunakan `when` untuk menentukan aksi berdasarkan ID item menu yang dipilih.
        return when (item.itemId) {
            R.id.action_settings -> { // Jika item "Settings" dipilih:
                Toast.makeText(this, "Settings diklik!", Toast.LENGTH_SHORT).show() // Tampilkan pesan Toast.
                true // Mengembalikan true untuk menandakan bahwa event telah ditangani.
            }
            R.id.action_logout -> { // Jika item "Logout" dipilih:
                // Menampilkan dialog konfirmasi sebelum logout.
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Anda yakin ingin logout?")
                    .setPositiveButton("Logout") { _, _ -> // Jika pengguna memilih "Logout" di dialog:
                        performLogout() // Memanggil fungsi untuk melakukan proses logout.
                    }
                    .setNegativeButton("Batal", null) // Tombol "Batal" tidak melakukan apa-apa.
                    .show() // Menampilkan dialog.
                true // Mengembalikan true.
            }
            // Jika ID item tidak cocok dengan kasus di atas, panggil implementasi dari kelas induk.
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Fungsi untuk melakukan proses logout pengguna.
    private fun performLogout() {
        // Mengambil SharedPreferences yang menyimpan data akun.
        val accountPrefs = getSharedPreferences(ACCOUNT_PREFS_NAME, MODE_PRIVATE)
        val editor = accountPrefs.edit() // Mendapatkan editor.
        editor.remove("loggedInUserEmail") // Menghapus email pengguna yang tersimpan.
        editor.putBoolean("isLoggedIn", false) // Mengatur status login menjadi false.
        editor.apply() // Menerapkan perubahan.

        // Membuat Intent untuk pindah ke LoginActivity.
        val intent = Intent(this, LoginActivity::class.java)
        // Mengatur flags agar semua activity sebelumnya di back stack dihapus.
        // Ini mencegah pengguna kembali ke ToDoListActivity setelah logout dengan tombol "Back".
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent) // Memulai LoginActivity.
        finish() // Menutup ToDoListActivity saat ini.
        Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show() // Menampilkan pesan logout.
    }
}