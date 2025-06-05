package com.example.qrmosaicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmosaicapp.databinding.ActivityMainBinding

class HomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        binding.btnGenerate.setOnClickListener {
            startActivity(Intent(this, QRGeneratorActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            finish()
        }
    }
}
