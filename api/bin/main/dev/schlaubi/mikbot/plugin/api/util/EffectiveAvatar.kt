package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.entity.User

public val User.effectiveAvatar: String
    get() = avatar?.url ?: defaultAvatar.url
