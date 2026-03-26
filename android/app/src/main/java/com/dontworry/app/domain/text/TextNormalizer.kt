package com.dontworry.app.domain.text

import java.text.Normalizer

object TextNormalizer {
    fun normalize(input: String): String {
        val lowercase = input.lowercase()
        val noDiacritics = Normalizer.normalize(lowercase, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return noDiacritics
            .replace("[^a-z0-9\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
