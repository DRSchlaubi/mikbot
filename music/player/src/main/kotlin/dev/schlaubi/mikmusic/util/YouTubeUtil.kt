package dev.schlaubi.mikmusic.util

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequest
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.google.api.services.youtube.model.Channel
import com.google.api.services.youtube.model.ChannelListResponse
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoListResponse
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikmusic.core.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val client: YouTube = YouTube.Builder(
    GoogleNetHttpTransport.newTrustedTransport(),
    GsonFactory.getDefaultInstance()
) { }
    .setApplicationName("mikmusic-discord")
    .setYouTubeRequestInitializer(RequestInitializer())
    .build()

private suspend fun getVideosById(videoId: String): VideoListResponse {
    return withContext(Dispatchers.IO) {
        client.videos().list(listOf("snippet", "localizations", "contentDetails")).setId(listOf(videoId)).execute()
    }
}

private suspend fun getChannelsById(channelId: String): ChannelListResponse {
    return withContext(Dispatchers.IO) {
        client.channels().list(listOf("snippet")).setId(listOf(channelId)).execute()
    }
}

/**
 * Retrieves a [YouTube Video][Video] by its [videoId].
 */
suspend fun getVideoById(videoId: String): Video = getVideosById(videoId).items[0]

/**
 * Retrieves a [YouTube Channel][Channel] by its [channelId].
 */
suspend fun getFirstChannelById(channelId: String): Channel = getChannelsById(channelId).items[0]

val Track.youtubeId: String?
    get() = if (info.sourceName == "youtube") info.identifier else null

suspend fun Track.findOnYoutube(): Video? = youtubeId?.let {
    getVideoById(it)
}

private class RequestInitializer : YouTubeRequestInitializer() {
    override fun initializeYouTubeRequest(youTubeRequest: YouTubeRequest<*>) {
        youTubeRequest.key = Config.YOUTUBE_API_KEY
    }
}
