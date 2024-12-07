package dev.schlaubi.mikmusic.api.documentation

import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

@KtorDsl
inline fun <reified R : Any> Route.documentedPost(noinline body: suspend RoutingContext.(R) -> Unit) =
    documentedRoute<R>(HttpMethod.Post, body)

@KtorDsl
inline fun <reified R : Any> Route.documentedGet(noinline body: suspend RoutingContext.(R) -> Unit) =
    documentedRoute<R>(HttpMethod.Get, body)

@KtorDsl
inline fun <reified R : Any> Route.documentedPatch(noinline body: suspend RoutingContext.(R) -> Unit) =
    documentedRoute<R>(HttpMethod.Patch, body)

@KtorDsl
inline fun <reified R : Any> Route.documentedDelete(noinline body: suspend RoutingContext.(R) -> Unit) =
    documentedRoute<R>(HttpMethod.Delete, body)

@KtorDsl
inline fun <reified R : Any> Route.documentedPut(noinline body: suspend RoutingContext.(R) -> Unit) =
    documentedRoute<R>(HttpMethod.Put, body)

inline fun <reified R : Any> Route.documentedRoute(
    method: HttpMethod,
    noinline body: suspend RoutingContext.(R) -> Unit,
) {
    resource<R> {
        applyDocs<R>(method)
        method(method) {
            handle(body)
        }
    }
}
