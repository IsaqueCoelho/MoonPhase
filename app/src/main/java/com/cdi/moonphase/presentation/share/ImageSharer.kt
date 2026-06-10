package com.cdi.moonphase.presentation.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Persists a rendered card to app cache and opens the native Android share sheet
 * (`ACTION_SEND`, `image/png`) — the user's own confirmation is the share gesture (Tela 07).
 * Files land in `cache/shared_images`, exposed read-only through the app's [FileProvider].
 */
object ImageSharer {

    private const val SHARED_DIR = "shared_images"
    private const val MIME_PNG = "image/png"

    fun share(context: Context, bitmap: Bitmap, fileName: String, chooserTitle: String) {
        val uri = writeToCache(context, bitmap, fileName)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_PNG
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun writeToCache(context: Context, bitmap: Bitmap, fileName: String) =
        run {
            val dir = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
}
