package com.example.qrmosaicapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

class EncodeActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button

    private val PICK_IMAGE_REQUEST = 101
    private val CAPTURE_IMAGE_REQUEST = 102
    private val CHUNK_SIZE = 900 // bytes per QR code (safe)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encode)

        imageView = findViewById(R.id.imageViewMosaic)
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
                            createMosaicQR(file.readBytes())
                        }
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val bitmap = data.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val file = bitmapToFile(it)
                        createMosaicQR(file.readBytes())
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

    private fun createMosaicQR(data: ByteArray) {
        val qrList = splitAndGenerateQRCodes(data)
        // Create a grid mosaic image of QR codes
        val grid = ceil(sqrt(qrList.size.toDouble())).toInt()
        val mosaic = Bitmap.createBitmap(grid * 400, grid * 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mosaic)
        var i = 0
        for (row in 0 until grid) {
            for (col in 0 until grid) {
                if (i < qrList.size) {
                    canvas.drawBitmap(qrList[i], col * 400f, row * 400f, null)
                    i++
                }
            }
        }
        imageView.setImageBitmap(mosaic)
        Toast.makeText(this, "Mosaic QR generated (${qrList.size} QR codes)", Toast.LENGTH_LONG).show()
    }

    // Split data and generate QR bitmaps
    private fun splitAndGenerateQRCodes(data: ByteArray): List<Bitmap> {
        val qrList = mutableListOf<Bitmap>()
        var i = 0
        var idx = 0
        while (i < data.size) {
            val end = min(i + CHUNK_SIZE, data.size)
            val chunk = data.sliceArray(i until end)
            // Add index/total to chunk for ordering on decode
            val chunkWithHeader = byteArrayOf(idx.toByte(), (data.size / CHUNK_SIZE).toByte()) + chunk
            qrList.add(encodeQR(chunkWithHeader))
            i += CHUNK_SIZE
            idx++
        }
        return qrList
    }

    private fun encodeQR(bytes: ByteArray): Bitmap {
        val writer = QRCodeWriter()
        val str = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 400, 400)
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565)
        for (x in 0 until 400) {
            for (y in 0 until 400) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
            }
        }
        return bitmap
    }
}
