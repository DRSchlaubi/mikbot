package dev.nycode.sponsorblock.request

import io.ktor.client.request.*

internal sealed interface RequestBuilder {
    fun HttpRequestBuilder.applyRequestBuilder()
}
