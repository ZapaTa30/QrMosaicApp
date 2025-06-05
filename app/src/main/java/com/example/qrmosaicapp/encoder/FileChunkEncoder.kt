package com.example.qrmosaicapp.encoder

import java.io.File

object FileChunkEncoder {
    fun encodeFileToChunks(file: File, chunkSize: Int = 800): List<String> {
        val bytes = file.readBytes()
        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        return base64.chunked(chunkSize)
    }

    fun decodeChunksToFile(chunks: List<String>, outputPath: String): File {
        val combined = chunks.joinToString("")
        val decoded = android.util.Base64.decode(combined, android.util.Base64.NO_WRAP)
        val outputFile = File(outputPath)
        outputFile.writeBytes(decoded)
        return outputFile
    }
}
