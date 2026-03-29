package com.dontworry.app.data.assets

import android.content.Context

class AssetReader(private val context: Context) {
    fun readText(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }
}
