package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.entity.Message

/**
 * Returns the first attachment url or the message content.
 */
public val Message.attachmentOrContentQuery: String
    get() = attachments.firstOrNull()?.url ?: content
