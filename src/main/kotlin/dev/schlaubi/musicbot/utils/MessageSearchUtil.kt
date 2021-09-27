package dev.schlaubi.musicbot.utils

import dev.kord.core.entity.Message

/**
 * Returns the first attachment url or the message content.
 */
val Message.attachmentOrContentQuery: String
    get() = attachments.firstOrNull()?.url ?: content
