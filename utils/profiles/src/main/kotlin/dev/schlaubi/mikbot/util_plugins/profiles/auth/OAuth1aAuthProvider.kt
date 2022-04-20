package dev.schlaubi.mikbot.util_plugins.profiles.auth

import io.ktor.client.plugins.auth.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val OAuthTokenSecretAttributeKey = AttributeKey<String>("OAuthTokenSecret")

fun Auth.oauth1a(host: String, consumerSecret: String) {
    providers.add(OAuth1aAuthProvider(host, consumerSecret))
}

var HttpRequestBuilder.oAuthTokenSecret: String
    get() = attributes.getOrNull(OAuthTokenSecretAttributeKey) ?: error("Missing oauth token secret")
    set(value) = attributes.put(OAuthTokenSecretAttributeKey, value)

private class OAuth1aAuthProvider(private val host: String, private val consumerSecret: String) : AuthProvider {
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Please use sendWithoutRequest function instead")
    override val sendWithoutRequest: Boolean
        get() = error("Deprecated")

    override fun sendWithoutRequest(request: HttpRequestBuilder): Boolean {
        if (request.url.host != host) return false
        val headerRaw = request.headers[HttpHeaders.Authorization] ?: return false

        val header = parseAuthorizationHeader(headerRaw) as? HttpAuthHeader.Parameterized ?: return false
        if (header.authScheme != AuthScheme.OAuth) return false
        val oauthVersion = header.parameter("oauth_version")
        return oauthVersion == "1.0"
    }

    override fun isApplicable(auth: HttpAuthHeader): Boolean = false

    override suspend fun addRequestHeaders(request: HttpRequestBuilder, authHeader: HttpAuthHeader?) {
        val headerRaw = authHeader?.render() ?: return
        val header = parseAuthorizationHeader(headerRaw) as? HttpAuthHeader.Parameterized ?: return
        val headerParameters = header.parameters.associate { it.name to it.value }
        val requestParameters = request.url.parameters.entries().associate { (key, value) -> key to value.first() }
        val signingAlgorithm = sanitizeAlgorithmName(header.parameter("oauth_signature_method")!!)

        val parameters = (headerParameters + requestParameters)
            // Percent encode every key and value that will be signed.
            .map { (key, value) -> key.encodeURLParameter() to value.encodeURLParameter() }
            // Sort the list of parameters alphabetically [1] by encoded key [2].
            .sortedBy { (key) -> key }
            // For each key/value pair:
            // Append the encoded key to the output string.
            // Append the ‘=’ character to the output string.
            // Append the encoded value to the output string.
            // If there are more key/value pairs remaining, append a ‘&’ character to the output string.
            .joinToString("&") { (key, value) -> "$key=$value" }
        val signatureString = buildString {
            // Convert the HTTP Method to uppercase and set the output string equal to this value.
            append(request.method.value)
            // Append the ‘&’ character to the output string.
            append('&')
            // Percent encode the URL and append it to the output string.
            with(request.url) {
                // query needs to be cut off, so we make our custom string
                val urlString = buildString {
                    append(protocol.name)
                    append("://")
                    append(host)
                    append(encodedPath)
                }

                append(urlString.encodeURLParameter())
            }
            // Append the ‘&’ character to the output string.
            append('&')
            // Percent encode the parameter string and append it to the output string.
            append(parameters.encodeURLParameter())
        }

        val oAuthTokenSecret = request.oAuthTokenSecret.encodeURLParameter()
        val consumerSecret = this.consumerSecret.encodeURLParameter()
        // Both of these values need to be combined to form a signing key which will be used to generate the signature.
        // The signing key is simply the percent encoded consumer secret, followed by an ampersand character ‘&’,
        // followed by the percent encoded token secret
        val signingSecret = "$consumerSecret&$oAuthTokenSecret"

        val mac = Mac.getInstance(signingAlgorithm).apply {
            val secretKey = SecretKeySpec(signingSecret.toByteArray(), signingAlgorithm)
            init(secretKey)
        }

        // The output of the HMAC signing function is a binary string.
        // This needs to be base64 encoded to produce the signature string
        val headerValue = mac.doFinal(signatureString.toByteArray()).encodeBase64().encodeURLParameter()
        val signatureParameter = HeaderValueParam("oauth_signature", headerValue)

        // Finally, we take our old auth header and add the new signature as the 'oauth_signature' parameter
        request.headers[HttpHeaders.Authorization] =
            HttpAuthHeader.Parameterized(header.authScheme, header.parameters + signatureParameter).render()
    }

    override suspend fun refreshToken(response: HttpResponse): Boolean = true
}

private fun sanitizeAlgorithmName(name: String): String = "Hmac${name.substringAfter("HMAC-")}"
