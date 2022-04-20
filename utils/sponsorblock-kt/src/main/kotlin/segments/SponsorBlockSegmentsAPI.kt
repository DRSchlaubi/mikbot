package dev.nycode.sponsorblock.segments

import dev.nycode.sponsorblock.SponsorBlockClient
import dev.nycode.sponsorblock.model.*
import dev.nycode.sponsorblock.request.CategoryVoteRequestBuilder
import dev.nycode.sponsorblock.request.CreateSegmentsRequestBuilder
import dev.nycode.sponsorblock.request.GetSkipSegementsRequestBuilder
import dev.nycode.sponsorblock.request.NormalVoteRequestBuilder
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlin.jvm.JvmInline
import kotlin.time.Duration

@JvmInline
public value class SponsorBlockSegmentsAPI(@PublishedApi internal val sponsorBlockClient: SponsorBlockClient) {
    public suspend inline fun getSkipSegments(
        videoId: String,
        builder: GetSkipSegementsRequestBuilder.() -> Unit = {}
    ): List<SkipSegment> =
        sponsorBlockClient.request("skipSegments") {
            parameter("videoID", videoId)
            GetSkipSegementsRequestBuilder().apply(builder).run {
                applyRequestBuilder()
            }
        }

    public suspend inline fun getSkipSegmentsByHash(
        sha256HashPrefix: String,
        builder: GetSkipSegementsRequestBuilder.() -> Unit = {}
    ): List<PrivacySkipSegment> =
        sponsorBlockClient.request("skipSegments", sha256HashPrefix) {
            with(GetSkipSegementsRequestBuilder().apply(builder)) {
                applyRequestBuilder()
            }
        }

    public suspend inline fun createSegments(
        videoId: String,
        userId: String,
        userAgent: String,
        service: Service? = null,
        videoDuration: Duration? = null,
        builder: CreateSegmentsRequestBuilder.() -> Unit
    ): Unit =
        sponsorBlockClient.request("skipSegments") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            setBody(CreateSegmentsRequestBuilder(videoId, userId, userAgent, service, videoDuration).apply(builder))
        }

    public suspend inline fun normalVote(uuid: String, userId: String, voteType: VoteType): Unit =
        sponsorBlockClient.request("voteOnSponsorTime") {
            method = HttpMethod.Post
            with(NormalVoteRequestBuilder(uuid, userId, voteType)) {
                applyRequestBuilder()
            }
        }

    public suspend inline fun categoryVote(uuid: String, userId: String, category: Category): Unit =
        sponsorBlockClient.request("voteOnSponsorTime") {
            method = HttpMethod.Post
            with(CategoryVoteRequestBuilder(uuid, userId, category)) {
                applyRequestBuilder()
            }
        }

    public suspend inline fun viewedVideoSegment(uuid: String): Unit =
        sponsorBlockClient.request("viewedVideoSponsorTime") {
            method = HttpMethod.Post
            parameter("UUID", uuid)
        }

    public suspend inline fun getUserInfoByLocalId(userId: String): UserInfo =
        sponsorBlockClient.request("userInfo") {
            parameter("userID", userId)
        }

    public suspend inline fun getUserInfoByPublicId(userId: String): UserInfo =
        sponsorBlockClient.request("userInfo") {
            parameter("publicUserID", userId)
        }
}
