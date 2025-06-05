package com.example.qrmosaicapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.max

class DecodeActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnPick: Button
    private lateinit var btnScan: Button

    private val PICK_IMAGE_REQUEST = 201
    private val CAPTURE_IMAGE_REQUEST = 202

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decode)

        imageView = findViewById(R.id.imageViewOriginal)
        btnPick = findViewById(R.id.btnPickMosaic)
        btnScan = findViewById(R.id.btnScanMosaic)

        btnPick.setOnClickListener {
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickIntent, PICK_IMAGE_REQUEST)
        }

        btnScan.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val bitmap: Bitmap? = when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val uri = data.data
                    uri?.let { MediaStore.Images.Media.getBitmap(contentResolver, it) }
                }
                CAPTURE_IMAGE_REQUEST -> data.extras?.get("data") as? Bitmap
                else -> null
            }
            bitmap?.let { decodeMosaicQR(it) }
        }
    }

    // Detect and decode all QR codes in a mosaic image
    private fun decodeMosaicQR(bitmap: Bitmap) {
        val qrReader = MultiFormatReader()
        val width = bitmap.width
        val height = bitmap.height
        val grid = max(1, width / 400)
        val chunkList = mutableListOf<Pair<Int, ByteArray>>()
        // Scan each QR block in grid
        for (row in 0 until grid) {
            for (col in 0 until grid) {
                val x = col * 400
                val y = row * 400
                if (x + 400 > width || y + 400 > height) continue
                val qrBitmap = Bitmap.createBitmap(bitmap, x, y, 400, 400)
                val source = RGBLuminanceSource(qrBitmap.width, qrBitmap.height, qrBitmap.getPixels())
                val binarizer = HybridBinarizer(source)
                val binaryBitmap = BinaryBitmap(binarizer)
                try {
                    val result = qrReader.decode(binaryBitmap)
                    val bytes = android.util.Base64.decode(result.text, android.util.Base64.DEFAULT)
                    // chunkWithHeader: [idx, total, ...data]
                    val idx = bytes[0].toInt()
                    val chunk = bytes.copyOfRange(2, bytes.size)
                    chunkList.add(Pair(idx, chunk))
                } catch (e: Exception) {
                    // Not a QR code or decode failed, ignore
                }
            }
        }
        if (chunkList.isEmpty()) {
            Toast.makeText(this, "No QR codes found!", Toast.LENGTH_LONG).show()
            return
        }
        // Order by chunk index
        chunkList.sortBy { it.first }
        val originalBytes = ByteArrayOutputStream()
        for (pair in chunkList) originalBytes.write(pair.second)
        val data = originalBytes.toByteArray()
        // Try to display image
        val decodedBitmap = try {
            android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (e: Exception) {
            null
        }
        if (decodedBitmap != null) {
            imageView.setImageBitmap(decodedBitmap)
            Toast.makeText(this, "Decoded image!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Decoded data is not an image. Saved as file.", Toast.LENGTH_LONG).show()
            // Optionally: save file or let user share/download
        }
    }

    // Helper to get pixel array for QR decode
    private fun Bitmap.getPixels(): IntArray {
        val intArray = IntArray(width * height)
        getPixels(intArray, 0, width, 0, 0, width, height)
        return intArray
    }
}
