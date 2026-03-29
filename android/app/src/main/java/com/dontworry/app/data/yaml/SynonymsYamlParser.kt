package com.dontworry.app.data.yaml

import com.dontworry.app.domain.model.SynonymGroup
import com.dontworry.app.domain.text.TextNormalizer
import org.yaml.snakeyaml.Yaml

data class SynonymsParseResult(
    val groups: List<SynonymGroup>,
    val enabled: Boolean,
    val warning: String? = null
)

class SynonymsYamlParser {
    fun parse(yamlText: String?): SynonymsParseResult {
        if (yamlText.isNullOrBlank()) {
            return SynonymsParseResult(groups = emptyList(), enabled = false, warning = "synonyms file missing")
        }

        return try {
            val yaml = Yaml()
            val loaded = yaml.load<Any?>(yamlText) as? Map<*, *>
                ?: return SynonymsParseResult(emptyList(), enabled = false, warning = "invalid YAML root")

            val items = loaded["synonyms"] as? List<*> ?: emptyList<Any>()
            val groups = items.mapNotNull { raw -> parseGroup(raw) }
            SynonymsParseResult(groups = groups, enabled = groups.isNotEmpty())
        } catch (_: Exception) {
            SynonymsParseResult(groups = emptyList(), enabled = false, warning = "failed to parse synonyms")
        }
    }

    private fun parseGroup(raw: Any?): SynonymGroup? {
        val map = raw as? Map<*, *> ?: return null
        val canonicalRaw = map["canonical"]?.toString()?.trim().orEmpty()
        if (canonicalRaw.isEmpty()) return null

        val termsRaw = map["terms"] as? List<*> ?: emptyList<Any>()
        val normalizedTerms = termsRaw.mapNotNull { item ->
            item?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(TextNormalizer::normalize)
        }.toMutableSet()

        normalizedTerms += TextNormalizer.normalize(canonicalRaw)
        if (normalizedTerms.isEmpty()) return null

        return SynonymGroup(
            canonical = TextNormalizer.normalize(canonicalRaw),
            terms = normalizedTerms
        )
    }
}
