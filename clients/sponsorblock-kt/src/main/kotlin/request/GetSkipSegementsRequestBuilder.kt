package dev.nycode.sponsorblock.request

import dev.nycode.sponsorblock.model.ActionType
import dev.nycode.sponsorblock.model.Category
import dev.nycode.sponsorblock.model.Service
import io.ktor.client.request.*

public class GetSkipSegementsRequestBuilder(
    public var categories: List<Category> = listOf(),
    public var requiredSegements: List<String> = listOf(),
    public var actionTypes: List<ActionType> = listOf(),
    public var service: Service? = null
) : RequestBuilder {
    override fun HttpRequestBuilder.applyRequestBuilder() {
        if (categories.isNotEmpty()) {
            parameter("categories", categories.joinToString(separator = ",", prefix = "[", postfix = "]") { """"$it"""" })
        }
    }
}
