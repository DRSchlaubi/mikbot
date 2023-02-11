package dev.schlaubi.votebot.api.error

import dev.schlaubi.votebot.api.authentication.AuthenticationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
private data class Error(val status: Int, val message: String? = null)

private suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String?) =
    respond(status, Error(status.value, message))

fun StatusPagesConfig.installErrorHandler() {
    exception<AuthenticationException> { call: ApplicationCall, cause ->
        call.respondError(HttpStatusCode.Unauthorized, cause.message)
    }
    exception<AuthenticationException> { call: ApplicationCall, cause ->
        call.respondError(HttpStatusCode.Unauthorized, cause.message)
    }
    exception<BadRequestException> { call: ApplicationCall, cause ->
        call.respondError(HttpStatusCode.BadRequest, cause.message)
    }

    exception<ContentTransformationException> { call: ApplicationCall, cause ->
        call.respondError(HttpStatusCode.BadRequest, cause.cause?.message ?: cause.message)
    }
}
