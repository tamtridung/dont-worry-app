package com.dontworry.app.domain.search

object ExcerptBuilder {
    fun fromText(text: String?, maxLength: Int = 180): String {
        if (text.isNullOrBlank()) return "No content preview available."
        val clean = text.replace("\\s+".toRegex(), " ").trim()
        return if (clean.length <= maxLength) clean else clean.take(maxLength).trimEnd() + "..."
    }
}
