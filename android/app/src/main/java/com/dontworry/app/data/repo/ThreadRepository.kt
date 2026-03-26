package com.dontworry.app.data.repo

import android.content.Context
import com.dontworry.app.data.assets.AssetReader
import com.dontworry.app.data.yaml.ThreadDetailsYamlParser
import com.dontworry.app.data.yaml.ThreadInfoYamlParser
import com.dontworry.app.domain.model.Thread
import com.dontworry.app.domain.thread.ThreadIdentity

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

        val infoByIdentity = infoItems.associateBy {
            ThreadIdentity.fromParts(
                threadId = null,
                threadLink = it.threadLink,
                threadTitle = it.threadTitle
            )
        }

        return detailItems.mapNotNull { detail ->
            val identity = ThreadIdentity.fromParts(
                threadId = detail.threadId,
                threadLink = null,
                threadTitle = detail.threadTitle
            )

            val info = infoByIdentity[identity]
            val responses = detail.responses.filter { it.content.isNotBlank() }
            if (responses.isEmpty()) return@mapNotNull null

            Thread(
                threadId = detail.threadId,
                threadTitle = detail.threadTitle,
                threadLink = info?.threadLink,
                threadContent = detail.threadContent,
                responses = responses
            )
        }
    }
}
