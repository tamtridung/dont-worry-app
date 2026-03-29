package com.dontworry.app.domain.search

import com.dontworry.app.domain.model.Thread
import com.dontworry.app.domain.model.SynonymGroup

data class SearchResult(
    val thread: Thread,
    val score: Double,
    val excerpt: String
)

class SearchService(
    private val threads: List<Thread>,
    synonymGroups: List<SynonymGroup> = emptyList()
) {
    private val index = SearchIndex.fromThreads(threads)
    private val synonymExpander = SynonymExpander(synonymGroups)

    fun search(query: String, limit: Int = 10): List<SearchResult> {
        val terms = synonymExpander.expand(QueryTokenizer.tokenizeQuery(query))
        if (terms.isEmpty()) return emptyList()

        return index.documents
            .map { doc ->
                val score = Bm25Scorer.score(
                    queryTerms = terms,
                    termFrequencies = doc.terms,
                    docLength = doc.length,
                    averageDocLength = index.averageDocLength,
                    docFrequencyProvider = index::docFrequency,
                    totalDocuments = index.documents.size
                )
                SearchResult(
                    thread = doc.thread,
                    score = score,
                    excerpt = ExcerptBuilder.fromText(doc.thread.threadContent)
                )
            }
            .filter { it.score > 0.0 }
            .sortedByDescending { it.score }
            .take(limit)
    }
}
