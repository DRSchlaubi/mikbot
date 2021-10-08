package dev.schlaubi.musicbot.utils

import dev.kord.core.entity.User

val User.effectiveAvatar: String
    get() = avatar?.url ?: defaultAvatar.url
