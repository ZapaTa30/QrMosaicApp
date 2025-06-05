package com.example.qrmosaicapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmosaicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            Toast.makeText(this, "Scan Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnGenerate.setOnClickListener {
            Toast.makeText(this, "Generate Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnLogout.setOnClickListener {
            Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnEncode.setOnClickListener {
            Toast.makeText(this, "Encode Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
