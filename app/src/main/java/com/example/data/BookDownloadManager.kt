package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    object Completed : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object BookDownloadManager {
    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates

    fun getDownloadState(bookName: String): DownloadState {
        return _downloadStates.value[bookName] ?: DownloadState.Idle
    }

    fun isBookDownloaded(context: Context, bookName: String): Boolean {
        val file = getLocalFile(context, bookName)
        return file.exists() && file.length() > 1024 // Greater than 1KB to ensure it's not a dummy file
    }

    fun getLocalFile(context: Context, bookName: String): File {
        val safeName = bookName.replace(" ", "_").replace("/", "_").lowercase()
        val dir = File(context.filesDir, "downloaded_books")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "$safeName.pdf")
    }

    private fun configureConnection(connection: HttpURLConnection) {
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 20000
        connection.readTimeout = 20000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
        connection.setRequestProperty("Accept", "*/*")
    }

    suspend fun downloadBook(context: Context, bookName: String, archiveUrl: String) {
        val directUrl = getDirectPdfUrl(bookName, archiveUrl)
        val localFile = getLocalFile(context, bookName)

        updateState(bookName, DownloadState.Downloading(0))

        withContext(Dispatchers.IO) {
            try {
                var urlObj = URL(directUrl)
                var connection = urlObj.openConnection() as HttpURLConnection
                configureConnection(connection)
                connection.connect()

                var status = connection.responseCode
                var redirectCount = 0
                while ((status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
                    val newUrl = connection.getHeaderField("Location") ?: break
                    urlObj = URL(newUrl)
                    connection = urlObj.openConnection() as HttpURLConnection
                    configureConnection(connection)
                    status = connection.responseCode
                    redirectCount++
                }

                // If not found, try fallback to _text.pdf format
                if (status == HttpURLConnection.HTTP_NOT_FOUND && archiveUrl.contains("archive.org/details/")) {
                    val itemId = archiveUrl.substringAfter("details/").substringBefore("/").trim()
                    val fallbackUrl = "https://archive.org/download/$itemId/${itemId}_text.pdf"
                    
                    urlObj = URL(fallbackUrl)
                    connection = urlObj.openConnection() as HttpURLConnection
                    configureConnection(connection)
                    connection.connect()
                    status = connection.responseCode

                    redirectCount = 0
                    while ((status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
                        val newUrl = connection.getHeaderField("Location") ?: break
                        urlObj = URL(newUrl)
                        connection = urlObj.openConnection() as HttpURLConnection
                        configureConnection(connection)
                        status = connection.responseCode
                        redirectCount++
                    }
                }

                if (status != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP $status")
                }

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(localFile)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (inputStream.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        val progress = ((total * 100) / fileLength).toInt()
                        updateState(bookName, DownloadState.Downloading(progress))
                    } else {
                        val progress = (total % 100).toInt()
                        updateState(bookName, DownloadState.Downloading(progress))
                    }
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                updateState(bookName, DownloadState.Completed)
            } catch (e: Exception) {
                if (localFile.exists()) {
                    localFile.delete() // clean up partial file
                }
                updateState(bookName, DownloadState.Error(e.localizedMessage ?: "Connection Timeout"))
            }
        }
    }

    fun clearDownloadState(bookName: String) {
        val current = _downloadStates.value.toMutableMap()
        current.remove(bookName)
        _downloadStates.value = current
    }

    fun openDownloadedBook(context: Context, bookName: String) {
        val file = getLocalFile(context, bookName)
        if (!file.exists()) return

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (ex: Exception) {
                // If everything fails, standard web browser fallback or external viewer
            }
        }
    }

    fun deleteDownloadedBook(context: Context, bookName: String) {
        val file = getLocalFile(context, bookName)
        if (file.exists()) {
            file.delete()
        }
        val current = _downloadStates.value.toMutableMap()
        current.remove(bookName)
        _downloadStates.value = current
    }

    private fun updateState(bookName: String, state: DownloadState) {
        val current = _downloadStates.value.toMutableMap()
        current[bookName] = state
        _downloadStates.value = current
    }

    private fun getDirectPdfUrl(bookName: String, archiveUrl: String): String {
        if (archiveUrl.contains("archive.org/details/")) {
            val itemId = archiveUrl.substringAfter("details/").substringBefore("/").trim()
            return "https://archive.org/download/$itemId/$itemId.pdf"
        }
        return archiveUrl
    }
}
