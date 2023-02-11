package dev.schlaubi.votebot.api.oauth

import kotlinx.serialization.Serializable

@Serializable
data class JWK(
    val kty: String,
    val kid: String,
    val use: String = "sig",
    val alg: String,
    val n: String,
    val e: String
)

@Serializable
data class JWKs(val keys: List<JWK>)
