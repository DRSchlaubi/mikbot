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

suspend fun getVideoById(videoId: String): VideoListResponse {
    return withContext(Dispatchers.IO) {
        client.videos().list("snippet,localizations,contentDetails").setId(videoId).execute()
    }
}

suspend fun getChannelById(channelId: String): ChannelListResponse {
    return withContext(Dispatchers.IO) {
        client.channels().list("snippet").setId(channelId).execute()
    }
}

suspend fun getFirstVideoById(videoId: String): Video {
    return getVideoById(videoId).items[0]
}

suspend fun getFirstChannelById(channelId: String): Channel {
    return getChannelById(channelId).items[0]
}

private class RequestInitializer : YouTubeRequestInitializer() {
    override fun initializeYouTubeRequest(youTubeRequest: YouTubeRequest<*>) {
        youTubeRequest.key = Config.YOUTUBE_API_KEY
    }
}
