package dev.schlaubi.mikbot.util_plugins.profiles

import dev.schlaubi.envconf.environment

object ProfileConfig {
    val GITHUB_CLIENT_ID by environment
    val GITHUB_CLIENT_SECRET by environment

    val GITLAB_CLIENT_ID by environment
    val GITLAB_CLIENT_SECRET by environment

    val TWITTER_CONSUMER_KEY by environment
    val TWITTER_CONSUMER_SECRET by environment

    val TWITTER_CLIENT_ID by environment
    val TWITTER_API_KEY by environment
    val TWITTER_API_SECRET by environment

    val TWITCH_CLIENT_ID by environment
    val TWITCH_CLIENT_SECRET by environment

    val DISCORD_CLIENT_ID by environment
    val DISCORD_CLIENT_SECRET by environment

    val REDDIT_CLIENT_ID by environment
    val REDDIT_CLIENT_SECRET by environment

    val INSTAGRAM_CLIENT_ID by environment
    val INSTAGRAM_CLIENT_SECRET by environment

    val SPOTIFY_CLIENT_ID by environment
    val SPOTIFY_CLIENT_SECRET by environment
}
