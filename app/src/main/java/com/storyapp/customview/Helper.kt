package com.storyapp.customview

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helper {

    val pattern = Regex("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")

    private val FILENAME_FORMAT = "dd-MMM-yyyy"
    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT, Locale.getDefault()
    ).format(System.currentTimeMillis())

    fun formatDateToString(dateString: String): String {
        val inputDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val date: Date?
        var outputDate = ""

        try {
            date = inputDateFormat.parse(dateString)
            outputDate = outputDateFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return outputDate
    }

    private fun createCustomTempFile(context: Context): File {
        // get direktori penyimpanan eksternal utk storyApp di Pictures
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // make temporary file with time format
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    fun uriToFile(selectedImg: Uri, context: Context): File {
        // Mendapatkan ContentResolver dari konteks
        val contentResolver: ContentResolver = context.contentResolver
        // Membuat objek File menggunakan fungsi createCustomTempFile
        val myFile = createCustomTempFile(context)
        // Membuka InputStream dari Uri yang dipilih
        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        // Membuat OutputStream dari objek File
        val outputStream: OutputStream = FileOutputStream(myFile)
        // Menyalin data dari InputStream ke OutputStream menggunakan buffer
        val buffer = ByteArray(1024)
        var lenght: Int

        while (inputStream.read(buffer).also { lenght = it } > 0) outputStream.write(buffer, 0, lenght)
        outputStream.close()
        inputStream.close()

        // Mengembalikan objek File yang telah dibuat dan diisi dengan data dari Uri
        return myFile
    }

    fun createFile(application: Application): File {
        val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
            File(it, "story").apply { mkdirs() }
        }
        val outputDirectory = if (
            mediaDir != null && mediaDir.exists()
        ) mediaDir else application.filesDir

        return File(outputDirectory, "STORY-$timeStamp.jpg")
    }

    fun rotateBitmap(bitmap: Bitmap, isFromBackCamera: Boolean): Bitmap {
        val matrix = Matrix()

        if (isFromBackCamera) {
            matrix.postRotate(90f)
        } else {
            // Simpan orientasi asli
            matrix.postRotate(0f)
        }

        // Tidak ada pemrosesan skala yang diterapkan
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}