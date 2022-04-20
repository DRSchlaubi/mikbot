package dev.nycode.sponsorblock.request

import io.ktor.client.request.HttpRequestBuilder

internal sealed interface RequestBuilder {
    fun HttpRequestBuilder.applyRequestBuilder()
}
