package dev.schlaubi.mikbot.util_plugins.ktor.api

import io.ktor.http.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Builds on URL based on [Config.WEB_SERVER_URL].
 */
inline fun buildBotUrl(urlBuilder: URLBuilder.() -> Unit): Url {
    contract {
        callsInPlace(urlBuilder, InvocationKind.EXACTLY_ONCE)
    }

    return URLBuilder(Config.WEB_SERVER_URL).apply(urlBuilder).build()
}
