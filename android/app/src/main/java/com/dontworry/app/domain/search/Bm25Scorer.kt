package com.dontworry.app.domain.search

import kotlin.math.ln

object Bm25Scorer {
    private const val K1 = 1.2
    private const val B = 0.75

    fun score(
        queryTerms: List<String>,
        termFrequencies: Map<String, Int>,
        docLength: Int,
        averageDocLength: Double,
        docFrequencyProvider: (String) -> Int,
        totalDocuments: Int
    ): Double {
        if (queryTerms.isEmpty() || docLength <= 0 || totalDocuments <= 0) return 0.0
        val safeAvg = if (averageDocLength <= 0.0) 1.0 else averageDocLength

        var score = 0.0
        for (term in queryTerms) {
            val tf = termFrequencies[term] ?: 0
            if (tf <= 0) continue

            val df = docFrequencyProvider(term)
            val idf = ln(((totalDocuments - df + 0.5) / (df + 0.5)) + 1.0)
            val numerator = tf * (K1 + 1.0)
            val denominator = tf + K1 * (1.0 - B + B * (docLength / safeAvg))
            score += idf * (numerator / denominator)
        }

        return score
    }
}
