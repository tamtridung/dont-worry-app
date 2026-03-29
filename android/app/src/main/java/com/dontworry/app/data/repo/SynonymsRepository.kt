package com.dontworry.app.data.repo

import android.content.Context
import com.dontworry.app.data.assets.AssetReader
import com.dontworry.app.data.yaml.SynonymsParseResult
import com.dontworry.app.data.yaml.SynonymsYamlParser

class SynonymsRepository(context: Context) {
    private val assetReader = AssetReader(context)
    private val parser = SynonymsYamlParser()

    fun load(): SynonymsParseResult {
        val yamlText = runCatching {
            assetReader.readText("synonyms/synonyms.yaml")
        }.getOrNull()
        return parser.parse(yamlText)
    }
}
