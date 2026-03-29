package com.dontworry.app.data.yaml

import org.yaml.snakeyaml.Yaml

data class ThreadInfoRecord(
    val threadTitle: String,
    val threadLink: String?,
    val createdBy: String?
)

class ThreadInfoYamlParser {
    fun parse(yamlText: String): List<ThreadInfoRecord> {
        val yaml = Yaml()
        val loaded = yaml.load<Any?>(yamlText) ?: return emptyList()
        val items = loaded as? List<*> ?: return emptyList()

        return items.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            val title = map["thread-title"]?.toString()?.trim().orEmpty()
            if (title.isEmpty()) return@mapNotNull null

            ThreadInfoRecord(
                threadTitle = title,
                threadLink = map["thread-link"]?.toString()?.trim()?.ifEmpty { null },
                createdBy = map["created-by"]?.toString()?.trim()?.ifEmpty { null }
            )
        }
    }
}
