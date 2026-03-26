package com.dontworry.app.domain.search

import com.dontworry.app.domain.model.Thread

data class IndexedDocument(
    val thread: Thread,
    val terms: Map<String, Int>,
    val length: Int
)

class SearchIndex private constructor(
    val documents: List<IndexedDocument>,
    private val docFrequencies: Map<String, Int>,
    val averageDocLength: Double
) {
    fun docFrequency(term: String): Int = docFrequencies[term] ?: 0

    companion object {
        fun fromThreads(threads: List<Thread>): SearchIndex {
            val documents = threads.map { thread ->
                val tokens = QueryTokenizer.tokenizeForIndex(thread.threadTitle, thread.threadContent)
                IndexedDocument(
                    thread = thread,
                    terms = tokens.groupingBy { it }.eachCount(),
                    length = tokens.size
                )
            }

            val docFrequencies = mutableMapOf<String, Int>()
            documents.forEach { doc ->
                doc.terms.keys.forEach { term ->
                    docFrequencies[term] = (docFrequencies[term] ?: 0) + 1
                }
            }

            val avgLength = if (documents.isEmpty()) 0.0 else documents.map { it.length }.average()
            return SearchIndex(documents, docFrequencies, avgLength)
        }
    }
}
