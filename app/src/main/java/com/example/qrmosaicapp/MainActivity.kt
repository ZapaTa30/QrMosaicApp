package com.example.qrmosaicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmosaicapp.auth.AuthManager
import com.example.qrmosaicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = AuthManager(this)

        // Login check
        if (!auth.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        binding.btnGenerate.setOnClickListener {
            startActivity(Intent(this, QRGeneratorActivity::class.java))
        }

        binding.btnEncode.setOnClickListener {
            startActivity(Intent(this, EncodeActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
