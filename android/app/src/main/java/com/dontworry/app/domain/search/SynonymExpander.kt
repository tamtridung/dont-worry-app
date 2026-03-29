package com.dontworry.app.domain.search

import com.dontworry.app.domain.model.SynonymGroup

class SynonymExpander(groups: List<SynonymGroup>) {
    // Deterministic conflict handling: if a term appears in multiple groups,
    // keep the canonical form that is lexicographically smallest.
    private val canonicalByTerm: Map<String, String> = buildMap {
        groups.forEach { group ->
            val canonical = group.canonical
            group.terms.forEach { term ->
                val current = this[term]
                if (current == null || canonical < current) {
                    put(term, canonical)
                }
            }
        }
    }

    fun expand(queryTerms: List<String>): List<String> {
        if (queryTerms.isEmpty()) return emptyList()
        return queryTerms.map { term -> canonicalByTerm[term] ?: term }
    }
}
