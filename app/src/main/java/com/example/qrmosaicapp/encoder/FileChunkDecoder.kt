package com.example.qrmosaicapp.encoder

import android.util.Base64
import java.io.File
import java.io.FileOutputStream

object FileChunkDecoder {

    fun decodeChunksToFile(chunks: List<String>, outputFile: File) {
        val outputStream = FileOutputStream(outputFile)
        for (chunk in chunks) {
            val decoded = Base64.decode(chunk, Base64.DEFAULT)
            outputStream.write(decoded)
        }
        outputStream.close()
    }
}
