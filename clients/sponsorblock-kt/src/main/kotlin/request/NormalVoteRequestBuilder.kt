package dev.nycode.sponsorblock.request

import dev.nycode.sponsorblock.model.VoteType
import io.ktor.client.request.*

public class NormalVoteRequestBuilder(
    public val uuid: String,
    public val userId: String,
    public val type: VoteType
) : RequestBuilder {
    override fun HttpRequestBuilder.applyRequestBuilder() {
        parameter("UUID", uuid)
        parameter("userID", userId)
        parameter("type", type.num)
    }
}
