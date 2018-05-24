package util

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

object ImageUtils {
    fun saveBitmapToFile(context: Context,
                         bitmap: Bitmap,
                         filename: String) {
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        var bytes = stream.toByteArray()
        ImageUtils.saveBytesToFile(context, bytes, filename)
    }

    private fun saveBytesToFile(context: Context,
                                bytes: ByteArray,
                                filename: String) {
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}