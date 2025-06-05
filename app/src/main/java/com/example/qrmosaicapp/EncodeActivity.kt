package com.example.qrmosaicapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrmosaicapp.utils.CryptoUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import javax.crypto.SecretKey

class EncodeActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var encryptionKey: SecretKey? = null
    private val PICK_IMAGE_REQUEST = 101
    private val CAPTURE_IMAGE_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView = ImageView(this)
        setContentView(imageView)

        // You can create a dialog here for "Camera" or "Gallery", for now pick from gallery
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                val file = uriToFile(uri)
                if (file != null) {
                    generateQRCode(file)
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data.extras != null) {
                val bitmap = data.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val file = bitmapToFile(it)
                    generateQRCode(file)
                }
            }
        }
    }

    // Convert Uri to File
    private fun uriToFile(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "selected_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        return file
    }

    // Convert Bitmap to File
    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "captured_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return file
    }

    // Generate and show QR code (first chunk only, for now)
    private fun generateQRCode(file: File) {
        try {
            encryptionKey = CryptoUtils.generateKey()
            val fileBytes = file.readBytes()
            val encrypted = CryptoUtils.encrypt(fileBytes, encryptionKey!!)
            val qrBitmap = encodeTextToQRCode(encrypted.take(1000).toByteArray()) // Only first 1000 chars for now
            imageView.setImageBitmap(qrBitmap)
            Toast.makeText(this, "QR generated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "QR generation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun encodeTextToQRCode(text: ByteArray): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(String(text), BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
            }
        }
        return bitmap
    }
}
