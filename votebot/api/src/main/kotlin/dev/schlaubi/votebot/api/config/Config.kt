package dev.schlaubi.votebot.api.config

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.ktor.http.*

object Config : EnvironmentConfig() {
    val DISCORD_OAUTH_CLIENT_ID by environment
    val DISCORD_OAUTH_CLIENT_SECRET by environment

    val CORS_HOSTS by getEnv(listOf("localhost:3000")) { it.split(',') }

    val OAUTH_URIS by getEnv(listOf(Url("http://localhost:3000"))) { it.split(',').map(::Url) }

    // openssl genrsa -out private_key.pem 2048
    // openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
    // cat private_key.der | base64 > encoded.key
    val JWT_SIGNING_KEY by getEnv(Keys.keyPairFor(SignatureAlgorithm.RS256)) { TODO() }
}
