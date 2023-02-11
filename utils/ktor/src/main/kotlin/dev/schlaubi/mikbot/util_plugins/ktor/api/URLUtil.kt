package dev.schlaubi.mikbot.util_plugins.ktor.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
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

/**
 * Build URL of [resource].
 */
inline fun <reified T : Any> Application.buildBotUrl(resource: T, urlBuilder: URLBuilder.() -> Unit = {}): String {
    contract {
        callsInPlace(urlBuilder, InvocationKind.EXACTLY_ONCE)
    }

    val builder = URLBuilder(Config.WEB_SERVER_URL)
    href(resource, builder.apply(urlBuilder))

    return builder.buildString()
}
