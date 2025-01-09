package com.example.warehouseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var btnViewData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Menginisialisasi tombol menggunakan findViewById
        btnViewData = findViewById(R.id.btn_view_data)

        // Set OnClickListener untuk tombol
        btnViewData.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Menutup HomeActivity agar tidak bisa kembali
        }
    }
}