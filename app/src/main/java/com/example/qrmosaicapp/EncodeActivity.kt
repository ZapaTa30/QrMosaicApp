package com.example.qrmosaicapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
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
    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button
    private var encryptionKey: SecretKey? = null

    private val PICK_IMAGE_REQUEST = 101
    private val CAPTURE_IMAGE_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encode)

        imageView = findViewById(R.id.imageViewQr)
        btnGallery = findViewById(R.id.btnPickFromGallery)
        btnCamera = findViewById(R.id.btnCaptureFromCamera)

        btnGallery.setOnClickListener {
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, PICK_IMAGE_REQUEST)
        }

        btnCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val uri: Uri? = data.data
                    uri?.let {
                        val file = uriToFile(it)
                        if (file != null) {
                            generateQRCode(file)
                        }
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val bitmap = data.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val file = bitmapToFile(it)
                        generateQRCode(file)
                    }
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "selected_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        return file
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "captured_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return file
    }

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
