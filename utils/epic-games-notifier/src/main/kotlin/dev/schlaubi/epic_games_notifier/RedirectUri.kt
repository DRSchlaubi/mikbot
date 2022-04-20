package dev.schlaubi.epic_games_notifier

import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import io.ktor.http.*

val redirectUri = buildBotUrl { path("webhooks", "thanks") }
