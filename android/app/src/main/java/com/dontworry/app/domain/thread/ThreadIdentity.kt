package com.dontworry.app.domain.thread

import com.dontworry.app.domain.text.TextNormalizer

object ThreadIdentity {
    fun fromParts(threadId: String?, threadLink: String?, threadTitle: String): String {
        val id = threadId?.trim().orEmpty()
        if (id.isNotEmpty()) return "id:$id"

        val link = threadLink?.trim().orEmpty()
        if (link.isNotEmpty()) return "link:$link"

        return "title:${TextNormalizer.normalize(threadTitle)}"
    }
}
