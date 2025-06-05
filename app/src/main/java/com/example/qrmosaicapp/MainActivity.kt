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

        if (!auth.isUserLoggedIn()) {
            auth.saveUser("offline@user.com", "1234")  // default dummy login
        }

        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        // btnEncode ko hata kar btnGenerate ka use karein
        binding.btnGenerate.setOnClickListener {
            startActivity(Intent(this, QRGeneratorActivity::class.java))
        }

        // Optional: Logout button ka bhi handler laga dein
        binding.btnLogout.setOnClickListener {
            // Yahan logout ka logic ya naya intent laga sakte hain
            auth.logout()
            // Example: LoginActivity dikhana
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
        }
    }
}
