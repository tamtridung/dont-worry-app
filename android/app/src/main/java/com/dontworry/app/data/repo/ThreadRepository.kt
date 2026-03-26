package com.dontworry.app.data.repo

import android.content.Context
import com.dontworry.app.data.assets.AssetReader
import com.dontworry.app.data.yaml.ThreadDetailsYamlParser
import com.dontworry.app.data.yaml.ThreadInfoYamlParser
import com.dontworry.app.domain.model.Thread
import com.dontworry.app.domain.text.TextNormalizer

class ThreadRepository(context: Context) {
    private val assetReader = AssetReader(context)
    private val infoParser = ThreadInfoYamlParser()
    private val detailsParser = ThreadDetailsYamlParser()

    fun loadThreads(): List<Thread> {
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

        return detailItems.map { detail ->
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
    }

    private fun extractThreadId(link: String?): String? {
        val trimmed = link?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        // Example: https://.../threads/86298-Massage-bi-tray-chan-chay-mau
        val afterThreads = trimmed.substringAfter("/threads/", missingDelimiterValue = "")
        if (afterThreads.isEmpty()) return null
        return afterThreads.substringBefore("-").takeIf { it.isNotBlank() }
    }
}
