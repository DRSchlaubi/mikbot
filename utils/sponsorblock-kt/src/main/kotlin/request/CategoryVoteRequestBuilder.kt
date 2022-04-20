package dev.nycode.sponsorblock.request

import dev.nycode.sponsorblock.model.Category
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class CategoryVoteRequestBuilder(
    public val uuid: String,
    public val userId: String,
    public val category: Category
) : RequestBuilder {
    override fun HttpRequestBuilder.applyRequestBuilder() {
        parameter("UUID", uuid)
        parameter("userID", userId)
        parameter("category", Json.encodeToString(category))
    }
}
