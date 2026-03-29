package com.dontworry.app.domain.model

data class SynonymGroup(
    val canonical: String,
    val terms: Set<String>
)
