package com.example.qrmosaicapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

        // Login check: User not logged in, redirect to LoginActivity
        if (!auth.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Scan QR button
        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        // Generate QR button
        binding.btnGenerate.setOnClickListener {
            startActivity(Intent(this, QRGeneratorActivity::class.java))
        }

        // Encode button (future: camera/image encoding)
        binding.btnEncode.setOnClickListener {
            Toast.makeText(this, "Encode feature coming soon! (Camera & file encoding)", Toast.LENGTH_SHORT).show()
            // Yahan future me EncodeActivity ya dialog launch karein
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            auth.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
