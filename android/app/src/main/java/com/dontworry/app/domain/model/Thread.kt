package com.dontworry.app.domain.model

data class Response(
    val responser: String,
    val content: String
)

data class Thread(
    val threadId: String?,
    val threadTitle: String,
    val threadLink: String?,
    val threadContent: String?,
    val responses: List<Response>
)
