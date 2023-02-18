package dev.schlaubi.musicbot.core.plugin

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.pf4j.update.FileDownloader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.writeBytes

object KtorHttpFileDownloader : FileDownloader {
    private val client = HttpClient()

    override fun downloadFile(fileUrl: URL): Path = runBlocking {
        val destination = withContext(Dispatchers.IO) {
            Files.createTempDirectory("pf4j-update-downloader")
        }
        val url = URLBuilder(fileUrl.toString()).apply {
            encodedPath = encodedPath.replace("//", "/")
        }.build()

        val bytes = client.get(url).body<ByteArray>()

        val downloadedFile = destination / url.encodedPath.substringAfterLast('/')

        downloadedFile.createFile()
        downloadedFile.writeBytes(bytes)

        downloadedFile
    }
}
