package dev.schlaubi.mikbot.util_plugins.verification

import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import io.ktor.http.*

val Invitation.url: Url
    get() = buildBotUrl {
        path("invitations", id.toString(), "accept")
    }

