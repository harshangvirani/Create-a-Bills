package com.create.bills

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@RequiresApi(Build.VERSION_CODES.Q)
private fun savePdfToMediaStore(context: Context,fileUri: Uri, fileName: String) {
    if ((!File(fileUri.path).exists() || !File(fileUri.path).isFile)) {

        // Get ContentResolver
        val resolver = context.contentResolver

        // Create content values
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) // File name
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // MIME type
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOCUMENTS
            ) // Path relative to Documents folder
        }

        // Insert the file into MediaStore
        val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)
        if (uri == null) {
            uri?.let {
                // Open an output stream to the MediaStore URI
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                val inputStream: InputStream? = resolver.openInputStream(fileUri)

                // Copy the contents from the original PDF URI to MediaStore
                inputStream?.copyTo(outputStream!!)
                inputStream?.close()
                outputStream?.close()


                // Optionally, you can log the path to verify
                Log.d("PDF File Message","PDF saved to MediaStore: $uri")
            }
        }
    }
}

// For Android below API 29 (Scoped storage isn't enforced)
private fun savePdfToStorageLegacy(context: Context,fileUri: Uri, fileName: String) {
    val externalStorageDir =
        context.getExternalFilesDir(null)  // Get app-specific storage location
    val file = File(externalStorageDir, fileName)
    if (!file.exists()) {
        context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        Log.d("PDF File Message","PDF saved to legacy storage: ${file.absolutePath}")
    }
}