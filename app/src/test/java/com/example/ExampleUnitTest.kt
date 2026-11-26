package com.example

import org.junit.Test
import org.junit.Assert.*
import java.net.HttpURLConnection
import java.net.URL

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testArchiveDownload() {
        val itemId = "gita-sanjeevani-gita-press-gorakhpur"
        val directUrl = "https://archive.org/download/$itemId/$itemId.pdf"
        println("Testing URL: $directUrl")
        try {
            var urlObj = URL(directUrl)
            var connection = urlObj.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
            connection.connect()

            var status = connection.responseCode
            println("Initial Response Code: $status")
            var redirectCount = 0
            while ((status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
                val newUrl = connection.getHeaderField("Location")
                println("Redirecting to: $newUrl")
                urlObj = URL(newUrl)
                connection = urlObj.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                status = connection.responseCode
                redirectCount++
            }

            println("Final Response Code: $status")
            println("Content Length: ${connection.contentLength}")
            println("Content Type: ${connection.contentType}")
            
            if (status != HttpURLConnection.HTTP_OK) {
                println("Failed! Trying metadata API...")
                // Let's test the metadata API format
                val metadataUrl = "https://archive.org/metadata/$itemId"
                val mUrlObj = URL(metadataUrl)
                val mConn = mUrlObj.openConnection() as HttpURLConnection
                mConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                println("Metadata API status: ${mConn.responseCode}")
                val text = mConn.inputStream.bufferedReader().use { it.readText() }
                println("Metadata JSON preview: ${text.take(500)}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail("Exception thrown: ${e.message}")
        }
    }
}


