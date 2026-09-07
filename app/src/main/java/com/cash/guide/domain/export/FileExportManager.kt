package com.cash.guide.domain.export

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.cash.guide.R
import java.io.File

object FileExportManager {

    const val MIME_PDF = "application/pdf"
    const val MIME_CSV = "text/csv"

    /**
     * Shares a file via Android's ACTION_SEND intent chooser.
     */
    fun shareFile(context: Context, file: File, mimeType: String, subject: String = "") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                if (subject.isNotBlank()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, context.getString(R.string.export_action_share)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.export_error, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens a file directly with the appropriate viewer application (PDF reader, Excel, etc.).
     */
    fun openFile(context: Context, file: File, mimeType: String, title: String = "") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(viewIntent, context.getString(R.string.export_action_open)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            // If no dedicated viewer is installed, fall back to sharing so user can pick Drive, WhatsApp, etc.
            shareFile(context, file, mimeType, title)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.export_error, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Deletes temporary export files older than 24 hours to keep cache compact.
     */
    fun cleanOldExports(context: Context) {
        runCatching {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) return
            val dayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            exportDir.listFiles()?.forEach { f ->
                if (f.lastModified() < dayAgo) {
                    f.delete()
                }
            }
        }
    }
}
