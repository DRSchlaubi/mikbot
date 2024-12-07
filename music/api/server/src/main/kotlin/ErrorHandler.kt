package dev.schlaubi.mikmusic.api

import com.auth0.jwt.exceptions.JWTVerificationException
import dev.schlaubi.mikmusic.api.types.UnexclusiveSchedulingOptions
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.installErrorHandler() {
    exception<OAuth2Exception> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, cause.errorCode ?: cause.message ?: "")
    }

    exception<ForbiddenException> { call, _ ->
        call.respond(HttpStatusCode.Forbidden)
    }

    exception<UnexclusiveSchedulingOptions> { call, _ ->
        call.respond(HttpStatusCode.BadRequest, "Scheduling options need to be exclusive")
    }

    exception<JWTVerificationException> { call, _ ->
        call.respond(HttpStatusCode.Unauthorized)
    }
}
