@file:OptIn(ExperimentalSerializationApi::class)

package dev.schlaubi.mikmusic.api.types

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

object EmptyBody

@SerialInfo
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Description(val value: String)

@MustBeDocumented
@Target()
annotation class HttpVerb(
    val description: String,
    val summary: String,
    val response: HttpBody,
    val request: HttpBody = HttpBody("", status = -1),
    val errors: Array<HttpBody> = [],
    val auth: String = "BearerAuth"
) {
    annotation class HttpBody(
        val description: String,
        val mediaType: String = "application/json",
        val body: KClass<*> = EmptyBody::class,
        val status: Int = 200,
        val typeParameters: Array<KClass<*>> = [],
    )
}

@SerialInfo
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Post(val descriptor: HttpVerb)

@SerialInfo
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Get(val descriptor: HttpVerb)

@SerialInfo
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Delete(val descriptor: HttpVerb)

@SerialInfo
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Patch(val descriptor: HttpVerb)

@SerialInfo
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Put(val descriptor: HttpVerb)
