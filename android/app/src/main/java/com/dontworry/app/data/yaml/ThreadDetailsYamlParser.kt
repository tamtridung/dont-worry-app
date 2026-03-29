package com.dontworry.app.data.yaml

import com.dontworry.app.domain.model.Response
import org.yaml.snakeyaml.Yaml

data class ThreadDetailsRecord(
    val threadId: String?,
    val threadTitle: String,
    val threadContent: String?,
    val responses: List<Response>
)

class ThreadDetailsYamlParser {
    fun parse(yamlText: String): List<ThreadDetailsRecord> {
        val yaml = Yaml()
        val loaded = yaml.load<Any?>(yamlText) ?: return emptyList()
        val items = loaded as? List<*> ?: return emptyList()

        return items.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            val title = map["thread-title"]?.toString()?.trim().orEmpty()
            if (title.isEmpty()) return@mapNotNull null

            val responsesRaw = map["responses"] as? List<*> ?: emptyList<Any>()
            val responses = responsesRaw.mapNotNull { parseResponse(it) }

            ThreadDetailsRecord(
                threadId = map["thread-id"]?.toString()?.trim()?.ifEmpty { null },
                threadTitle = title,
                threadContent = map["thread-content"]?.toString()?.trim()?.ifEmpty { null },
                responses = responses
            )
        }
    }

    private fun parseResponse(raw: Any?): Response? {
        val map = raw as? Map<*, *> ?: return null
        val responser = firstNonBlank(
            map["responser"],
            map["responder"],
            map["user"],
            map["name"]
        ) ?: "Anonymous"

        val content = firstNonBlank(
            map["response-content"],
            map["content"],
            map["response"],
            map["text"],
            map["body"],
            map["message"]
        ) ?: return null

        return Response(responser = responser, content = content)
    }

    private fun firstNonBlank(vararg values: Any?): String? {
        return values.firstNotNullOfOrNull { value ->
            value?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        }
    }
}
