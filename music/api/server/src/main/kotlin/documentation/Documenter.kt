@file:OptIn(ExperimentalSerializationApi::class)

package dev.schlaubi.mikmusic.api.documentation

import dev.kord.common.entity.UserPremium
import dev.schlaubi.mikbot.util_plugins.ktor.toJsonSchema
import dev.schlaubi.mikmusic.api.types.*
import io.bkbn.kompendium.core.metadata.*
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.serializer
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf

val customType = mapOf(
    typeOf<UserPremium>() to TypeDefinition.INT
)

data class SerialDescriptorElement(
    val name: String,
    val type: SerialDescriptor,
    val annotations: List<Annotation>,
    val isOptional: Boolean,
)

private val SerialDescriptorElement.description: String?
    get() = annotations.filterIsInstance<Description>().firstOrNull()?.value

inline fun <reified T> Route.applyDocs(method: HttpMethod) {
    val plugin = NotarizedResource<T>()
    // This means the plugin is already installed
    if (runCatching { plugin(plugin) }.isSuccess) return
    val format = application.plugin(Resources).resourcesFormat

    val descriptor = format.serializersModule.serializer<T>().descriptor

    install(plugin) {
        applyDocs(descriptor, method)
    }
}

fun NotarizedRoute.Config.applyDocs(serialDescriptor: SerialDescriptor, method: HttpMethod) {
    applyParameters(serialDescriptor)
    applyBodies(serialDescriptor, method)
}

private fun NotarizedRoute.Config.applyBodies(serialDescriptor: SerialDescriptor, httpMethod: HttpMethod) {
    post = applyBody(serialDescriptor, Post::descriptor, PostInfo::builder)
    get = applyBody(serialDescriptor, Get::descriptor, GetInfo::builder)
    patch = applyBody(serialDescriptor, Patch::descriptor, PatchInfo::builder)
    delete = applyBody(serialDescriptor, Delete::descriptor, DeleteInfo::builder)
    put = applyBody(serialDescriptor, Put::descriptor, PutInfo::builder)
}

private inline fun <reified Annotation, T : MethodInfo, Builder : MethodInfo.Builder<T>> applyBody(
    serialDescriptor: SerialDescriptor,
    crossinline verb: Annotation.() -> HttpVerb,
    factory: (Builder.() -> Unit) -> T,
): T? {
    val annotation = serialDescriptor.annotations.firstOrNull { it is Annotation } as? Annotation? ?: return null
    return factory {
        val httpVerb = annotation.verb()

        description(httpVerb.description)
        summary(httpVerb.summary)

        response {
            apply(httpVerb.response)
        }

        httpVerb.errors.forEach {
            canRespond { apply(it) }
        }

        if (this is MethodInfoWithRequest.Builder<*> && httpVerb.request.status != -1) {
            request {
                apply(httpVerb.request)
            }
        }
    }
}

private fun ResponseInfo.Builder.apply(response: HttpVerb.HttpBody) {
    description(response.description)
    responseCode(HttpStatusCode.fromValue(response.status))

    val responseBody = response.body
        .createType(response.typeParameters.map { KTypeProjection.invariant(it.starProjectedType) })
    responseType(responseBody)
}

private fun RequestInfo.Builder.apply(response: HttpVerb.HttpBody) {
    description(response.description)
    mediaTypes(response.mediaType)

    val responseBody = response.body
        .createType(response.typeParameters.map { KTypeProjection.invariant(it.starProjectedType) })
    requestType(responseBody)
}

private fun NotarizedRoute.Config.applyParameters(serialDescriptor: SerialDescriptor) {
    val resource = serialDescriptor.annotations.first { it is Resource } as Resource
    val path = resource.path

    parameters = (0 until serialDescriptor.elementsCount)
        .asSequence()
        .map {
            SerialDescriptorElement(
                serialDescriptor.getElementName(it),
                serialDescriptor.getElementDescriptor(it),
                serialDescriptor.getElementAnnotations(it),
                serialDescriptor.isElementOptional(it),
            )
        }
        // Filter out parent parameter
        .filter { it.type.annotations.none { annotation -> annotation is Resource } }
        .map {
            Parameter(
                it.name,
                if ("{${it.name}}" in path) Parameter.Location.path else Parameter.Location.query,
                it.type.toJsonSchema(),
                description = it.description,
                required = it.isOptional
            )
        }
        .toList()
}
