package com.example.qrmosaicapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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

        // If not logged in, redirect to login
        if (!auth.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Logout icon
        findViewById<ImageView>(R.id.logoutIcon).setOnClickListener {
            auth.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Encode mosaic QR
        binding.btnEncode.setOnClickListener {
            startActivity(Intent(this, EncodeActivity::class.java))
        }

        // Decode mosaic QR
        binding.btnDecode.setOnClickListener {
            startActivity(Intent(this, DecodeActivity::class.java))
        }
    }
}
