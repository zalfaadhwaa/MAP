package com.example.warehouseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
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
    private lateinit var spinnerKategori: Spinner
    private lateinit var spinnerUkuran: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listStok = findViewById(R.id.list_stok)
        btnCreateStok = findViewById(R.id.btn_create_stok)
        spinnerKategori = findViewById(R.id.spinner_kategori)
        spinnerUkuran = findViewById(R.id.spinner_ukuran)

        setupSpinners()

        btnCreateStok.setOnClickListener {
            Log.d(TAG, "Tombol + ditekan")
            val intent = Intent(this, CreateStokActivity::class.java)
            startActivity(intent)
        }

        fetchDataFromFirestore()

        listStok.setOnItemClickListener { adapterView, view, position, id ->
            val item = adapterView.getItemAtPosition(position) as StokModel

            val intent = Intent(this, EditStokActivity::class.java)
            intent.putExtra("stokId", item.Id)
            intent.putExtra("stokBarang", item.Barang)
            intent.putExtra("stokKategori", item.Kategori)
            intent.putExtra("stokUkuran", item.Ukuran)
            intent.putExtra("stokStok", item.Stok.toString())
            startActivity(intent)
        }
    }

    private fun setupSpinners() {
        // Data untuk kategori
        val kategoriList = arrayOf("Kaos", "Kemeja", "Celana", "Rok", "Hijab")
        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = kategoriAdapter

        // Data untuk ukuran
        val ukuranList = arrayOf("XS", "S", "M", "L", "XL", "XXL")
        val ukuranAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ukuranList)
        ukuranAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUkuran.adapter = ukuranAdapter
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
                            Barang = document.data["barang"] as? String,
                            Kategori = document.data["kategori"] as? String,
                            Ukuran = document.data["ukuran"] as? String,
                            Stok = document.data["stok"] as? Int ?: 0
                        )
                    )
                }

                val adapter = StokAdapter(this, R.layout.stok_item, items)
                listStok.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun deleteItem(id: String, title: String) {
        val db = Firebase.firestore
        db.collection("stok").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menghapus item: $title", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal menghapus item: $title.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Error deleting document.", exception)
                }
        }
}
