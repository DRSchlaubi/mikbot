package dev.schlaubi.musicbot.utils

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequest
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.google.api.services.youtube.model.Channel
import com.google.api.services.youtube.model.ChannelListResponse
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoListResponse
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.musicbot.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val client: YouTube = YouTube.Builder(
    GoogleNetHttpTransport.newTrustedTransport(),
    JacksonFactory.getDefaultInstance()
) { }
    .setApplicationName("groovybot-discord")
    .setYouTubeRequestInitializer(RequestInitializer())
    .build()

private suspend fun getVideosById(videoId: String): VideoListResponse {
    return withContext(Dispatchers.IO) {
        client.videos().list("snippet,localizations,contentDetails").setId(videoId).execute()
    }
}

private suspend fun getChannelsById(channelId: String): ChannelListResponse {
    return withContext(Dispatchers.IO) {
        client.channels().list("snippet").setId(channelId).execute()
    }
}

/**
 * Retrieves a [YouTube Video][Video] by its [videoId].
 */
suspend fun getVideoById(videoId: String): Video = getVideosById(videoId).items[0]

/**
 * Retrieves a [YouTube Channel][Channel] by its [channelId].
 */
suspend fun getFirstChannelById(channelId: String): Channel {
    return getChannelsById(channelId).items[0]
}

suspend fun searchForYouTubeMusicVideos(query: String) {
    val response = withContext(Dispatchers.IO) {
        client.search().list("snippet").apply {
            q = query
            videoCategoryId = "10" // Music category
        }.execute()
    }

    response.items.forEach {
        it.snippet
    }
}

suspend fun Track.findOnYoutube(): Video? {
    if (uri?.contains("youtu(?:be)?".toRegex()) == true) {
        return getVideoById(identifier)
    }
    return null
}

private class RequestInitializer : YouTubeRequestInitializer() {
    override fun initializeYouTubeRequest(youTubeRequest: YouTubeRequest<*>) {
        youTubeRequest.key = Config.YOUTUBE_API_KEY
    }
}
