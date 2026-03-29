package com.dontworry.app.data.repo

import android.content.Context
import com.dontworry.app.data.assets.AssetReader
import com.dontworry.app.data.yaml.ThreadDetailsYamlParser
import com.dontworry.app.data.yaml.ThreadInfoYamlParser
import com.dontworry.app.domain.model.Response
import com.dontworry.app.domain.model.Thread
import com.dontworry.app.domain.text.TextNormalizer
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class ThreadRepository(context: Context) {
    private val appContext = context.applicationContext
    private val assetReader = AssetReader(appContext)
    private val infoParser = ThreadInfoYamlParser()
    private val detailsParser = ThreadDetailsYamlParser()
    private val cacheFile = File(
        appContext.filesDir,
        "thread-cache-${packageUpdateToken()}.bin"
    )

    fun loadThreads(): List<Thread> {
        readThreadsFromCache()?.let { cached ->
            if (cached.isNotEmpty()) return cached
        }

        val infoItems = runCatching {
            infoParser.parse(assetReader.readText("data/thread-info.yaml"))
        }.getOrDefault(emptyList())

        val detailItems = runCatching {
            detailsParser.parse(assetReader.readText("data/thread-details.yaml"))
        }.getOrDefault(emptyList())

        val infoByThreadId = infoItems
            .mapNotNull { info ->
                val id = extractThreadId(info.threadLink) ?: return@mapNotNull null
                id to info
            }
            .toMap()

        val infoByNormalizedTitle = infoItems.associateBy {
            TextNormalizer.normalize(it.threadTitle)
        }

        val threads = detailItems.map { detail ->
            val info = detail.threadId
                ?.let { infoByThreadId[it] }
                ?: infoByNormalizedTitle[TextNormalizer.normalize(detail.threadTitle)]
            val responses = detail.responses.filter { it.content.isNotBlank() }

            Thread(
                threadId = detail.threadId,
                threadTitle = detail.threadTitle,
                threadLink = info?.threadLink,
                threadContent = detail.threadContent,
                responses = responses
            )
        }

        if (threads.isNotEmpty()) {
            writeThreadsToCache(threads)
        }

        return threads
    }

    private fun readThreadsFromCache(): List<Thread>? {
        return runCatching {
            if (!cacheFile.exists()) return null

            ObjectInputStream(BufferedInputStream(FileInputStream(cacheFile))).use { input ->
                @Suppress("UNCHECKED_CAST")
                val cached = input.readObject() as? List<CachedThread> ?: return null
                cached.map { it.toDomain() }
            }
        }.getOrNull()
    }

    private fun writeThreadsToCache(threads: List<Thread>) {
        runCatching {
            val cached = threads.map { CachedThread.fromDomain(it) }
            ObjectOutputStream(BufferedOutputStream(FileOutputStream(cacheFile))).use { output ->
                output.writeObject(cached)
            }
        }
    }

    private fun extractThreadId(link: String?): String? {
        val trimmed = link?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        // Example: https://.../threads/86298-Massage-bi-tray-chan-chay-mau
        val afterThreads = trimmed.substringAfter("/threads/", missingDelimiterValue = "")
        if (afterThreads.isEmpty()) return null
        return afterThreads.substringBefore("-").takeIf { it.isNotBlank() }
    }

    private fun packageUpdateToken(): Long {
        return runCatching {
            appContext.packageManager
                .getPackageInfo(appContext.packageName, 0)
                .lastUpdateTime
        }.getOrDefault(0L)
    }

    private data class CachedResponse(
        val responser: String,
        val content: String
    ) : Serializable {
        fun toDomain(): Response = Response(responser = responser, content = content)

        companion object {
            fun fromDomain(response: Response): CachedResponse {
                return CachedResponse(
                    responser = response.responser,
                    content = response.content
                )
            }
        }
    }

    private data class CachedThread(
        val threadId: String?,
        val threadTitle: String,
        val threadLink: String?,
        val threadContent: String?,
        val responses: List<CachedResponse>
    ) : Serializable {
        fun toDomain(): Thread {
            return Thread(
                threadId = threadId,
                threadTitle = threadTitle,
                threadLink = threadLink,
                threadContent = threadContent,
                responses = responses.map { it.toDomain() }
            )
        }

        companion object {
            fun fromDomain(thread: Thread): CachedThread {
                return CachedThread(
                    threadId = thread.threadId,
                    threadTitle = thread.threadTitle,
                    threadLink = thread.threadLink,
                    threadContent = thread.threadContent,
                    responses = thread.responses.map { response ->
                        CachedResponse.fromDomain(response)
                    }
                )
            }
        }
    }
}
