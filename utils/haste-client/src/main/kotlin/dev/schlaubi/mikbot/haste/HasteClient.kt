package dev.schlaubi.mikbot.haste

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Client for interacting with a [hastebin](https://github.com/toptal/haste-server) compatible server.
 *
 * @property url the base url of the haste server. e.g. https://pasta.with-rice.by.devs-from.asia/
 */
public class HasteClient(private val url: String) {

    private val httpClient: HttpClient = HttpClient(OkHttp) {
        install(JsonFeature)
    }

    /**
     * Creates a new haste.
     *
     * @param content the content to save as a haste.
     * @return the created haste.
     */
    public suspend fun createHaste(content: String): Haste {
        val (key) = httpClient.post<HasteResponse>(URLBuilder(url).pathComponents("documents").build()) {
            body = content
        }
        val hasteUrl = URLBuilder(url).pathComponents(key).buildString()
        return Haste(key, hasteUrl, this)
    }

    /**
     * Fetches a haste by its key.
     *
     * @param key the key of the haste to fetch.
     * @return the haste's content
     */
    public suspend fun getHasteContent(key: String): String {
        return httpClient.get(URLBuilder(url).pathComponents("raw", key).build())
    }
}
