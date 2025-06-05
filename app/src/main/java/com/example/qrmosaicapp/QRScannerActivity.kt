package com.example.qrmosaicapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmosaicapp.utils.CryptoUtils
import com.google.zxing.integration.android.IntentIntegrator

class QRScannerActivity : AppCompatActivity() {

    private val scannedChunks = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startQRScanner()
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setBeepEnabled(true)
        integrator.setPrompt("Scan mosaic QR part")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            scannedChunks.add(result.contents)

            // For simplicity, assume 1 QR for now
            try {
                val combined = scannedChunks.joinToString("")
                val decryptedBytes = CryptoUtils.decrypt(combined.toByteArray(), CryptoUtils.generateKey())
                Log.d("QRScanner", "Decrypted: ${String(decryptedBytes)}")
                Toast.makeText(this, "Decrypted content read.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error decoding QR", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
