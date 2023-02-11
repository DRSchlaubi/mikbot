package dev.schlaubi.votebot.api.authentication

import io.jsonwebtoken.JwtException
import io.ktor.client.plugins.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

private const val VOTEBOT_AUTH_NAME = "VOTEBOT"


/**
 * Convenience getter for [ParsedJwt].
 *
 * @throws AuthenticationException if there was an error in the authentication pipeline
 * @see authenticateWithVoteBot
 */
fun ApplicationCall.votebotJWS(): ParsedJwt {
    if (authentication.allFailures.isNotEmpty()) {
        throw AuthenticationException(message = authentication.allFailures.first()::class.simpleName)
    }
    return principal()!!
}

/**
 * Adds a Authentication provider backed by Firebase auth to this [Route].
 *
 * @see ApplicationCall.votebotJWS
 */
fun Route.authenticateWithVoteBot(build: Route.() -> Unit): Route = authenticate(VOTEBOT_AUTH_NAME, build = build)

internal fun AuthenticationConfig.votebot() {
    val provider = VoteBotAuthenticationProvider(VoteBotAuthenticationProvider.Config(VOTEBOT_AUTH_NAME))

    register(provider)
}

class VoteBotAuthenticationProvider(config: Config) : AuthenticationProvider(config) {
    class Config(name: String?) : AuthenticationProvider.Config(name)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val header = context.call.request.parseAuthorizationHeader()
        if (header == null) {
            context.error("MISSING_HEADER", AuthenticationFailedCause.NoCredentials)
            return
        }

        if (header !is HttpAuthHeader.Single || header.authScheme != "Bearer") {
            context.error("WRONG_HEADER", AuthenticationFailedCause.NoCredentials)
            return
        }

        val token = header.blob

        val parsedJwt = try {
            TokenUtil.validateToken(token)
        } catch (e: Exception) {
            context.error("INVALID_TOKEN", AuthenticationFailedCause.InvalidCredentials)
            return
        }

        context.principal(parsedJwt)
    }
}
