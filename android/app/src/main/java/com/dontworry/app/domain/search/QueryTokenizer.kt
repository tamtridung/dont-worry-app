package com.dontworry.app.domain.search

import com.dontworry.app.domain.text.TextNormalizer

object QueryTokenizer {
    fun tokenizeQuery(query: String): List<String> {
        val normalized = TextNormalizer.normalize(query)
        if (normalized.isBlank()) return emptyList()
        return normalized.split(" ").filter { it.isNotBlank() }
    }

    fun tokenizeForIndex(title: String, content: String?): List<String> {
        val normalized = TextNormalizer.normalize(title + " " + content.orEmpty())
        if (normalized.isBlank()) return emptyList()
        return normalized.split(" ").filter { it.isNotBlank() }
    }
}
