package com.example.warehouseapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var listStok: ListView
    private lateinit var btnCreateStok: FloatingActionButton
    private lateinit var btnLogout: Button
    private lateinit var labelHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listStok = findViewById(R.id.list_stok)
        btnCreateStok = findViewById(R.id.btn_create_stok)
        btnLogout = findViewById(R.id.btn_logout)
        labelHeader = findViewById(R.id.label_header)

        // Ambil nama pengguna dari SharedPreferences
        val sharedPreferences = getSharedPreferences("app_preference", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("name", "User")

        // Set teks pada TextView
        labelHeader.text = "Selamat Datang, $username!"

        btnLogout.setOnClickListener {
            logout()
        }

        btnCreateStok.setOnClickListener {
            Log.d(TAG, "Tombol + ditekan")
            val intent = Intent(this, CreateStokActivity::class.java)
            startActivity(intent)
        }

        fetchDataFromFirestore()

        listStok.setOnItemClickListener { adapterView, _, position, _ ->
            val item = adapterView.getItemAtPosition(position) as StokModel
            val intent = Intent(this, EditStokActivity::class.java).apply {
                putExtra("stokId", item.Id)
                putExtra("stokBarang", item.Barang)
                putExtra("stokKategori", item.Kategori)
                putExtra("stokUkuran", item.Ukuran)
                putExtra("stokStok", item.Stok.toString())
            }
            startActivity(intent)
        }

        listStok.setOnItemLongClickListener { adapterView, _, position, _ ->
            val item = adapterView.getItemAtPosition(position) as StokModel
            showDeleteConfirmationDialog(item)
            true
        }
    }

    private fun logout() {
        // Hapus data pengguna dari SharedPreferences
        val sharedPreferences = getSharedPreferences("app_preference", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Hapus semua data
        editor.apply()

        // Kembali ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Tutup MainActivity
    }

    override fun onResume() {
        super.onResume()
        fetchDataFromFirestore()  // Memperbarui tampilan dengan data terbaru
    }

    private fun fetchDataFromFirestore() {
        val db = Firebase.firestore
        db.collection("stok")
            .get()
            .addOnSuccessListener { result ->
                val items = ArrayList<StokModel>()

                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    items.add(
                        StokModel(
                            Id = document.id,
                            Barang = document.data["barang"] as? String ?: "Unknown",
                            Kategori = document.data["kategori"] as? String ?: "Unknown",
                            Ukuran = document.data["ukuran"] as? String ?: "Unknown",
                            Stok = document.data["stok"] as? Int ?: 0
                        )
                    )
                }

                val adapter = StokAdapter(this, R.layout.stok_item, items)
                listStok.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                Toast.makeText(this, "Gagal mengambil data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog(item: StokModel) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus item '${item.Barang}'?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                item.Id?.let { id ->
                    item.Barang?.let { deleteItem(id, it) }
                } ?: run {
                    Toast.makeText(this, "ID item tidak valid.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }

        val alert = dialogBuilder.create()
        alert.setTitle("Konfirmasi Hapus")
        alert.show()
    }

    private fun deleteItem(id: String, title: String) {
        val db = Firebase.firestore
        db.collection("stok").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menghapus item: $title", Toast.LENGTH_SHORT).show()
                fetchDataFromFirestore() // Memperbarui tampilan setelah penghapusan
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal menghapus item: $title.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Error deleting document.", exception)
            }
    }
}