package org.guy.library.util

import java.io.File
import java.nio.file.Files
import java.util.Base64

/**
 * Utility functions for handling files and encoding them for AI API payloads.
 */
object FileUtil {

    /**
     * Reads a file and encodes its contents to a Base64 string.
     */
    fun encodeFileToBase64(file: File): String {
        val bytes = Files.readAllBytes(file.toPath())
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Determines the MIME type of a file based on its extension.
     * Defaults to application/octet-stream if unknown.
     */
    fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }
    }
}
