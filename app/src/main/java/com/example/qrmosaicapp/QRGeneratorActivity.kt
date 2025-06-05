package com.example.qrmosaicapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmosaicapp.encoder.FileChunkEncoder
import com.example.qrmosaicapp.utils.CryptoUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import javax.crypto.SecretKey

class QRGeneratorActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private val PICK_FILE_REQUEST = 100
    private var encryptionKey: SecretKey? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView = ImageView(this)
        setContentView(imageView)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val fileUri: Uri? = data.data
            if (fileUri != null) {
                val file = uriToFile(fileUri)
                if (file != null) {
                    generateQRCode(file)
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val fileName = contentResolver.query(uri, null, null, null, null)?.use {
            it.moveToFirst()
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } ?: "tempFile"

        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        return file
    }

    private fun generateQRCode(file: File) {
        try {
            encryptionKey = CryptoUtils.generateKey()
            val encryptedChunks = FileChunkEncoder.encodeFileToChunks(file).map {
                CryptoUtils.encrypt(it.toByteArray(), encryptionKey!!)
            }

            val combined = encryptedChunks.joinToString("##")  // Delimiter

            val qrBitmap = encodeTextToQRCode(combined.take(1000))  // Truncated for now
            imageView.setImageBitmap(qrBitmap)
            Toast.makeText(this, "QR generated (1st chunk)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("QRGen", "Error: ${e.message}")
        }
    }

    private fun encodeTextToQRCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)

        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
            }
        }
        return bitmap
    }
}
