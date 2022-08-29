package dev.schlaubi.mikbot.utils.roleselector.util

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.GuildEmoji


// This doesn't work rn and I am to lazy to figure it out. for now you can just use local guild emojis.

//suspend fun GuildEmoji?.orById(guildId: Snowflake?, emojiId: Snowflake?, kord: Kord): DiscordPartialEmoji? {
//    return if (this != null) DiscordPartialEmoji(
//        id = id,
//        name = name,
//        animated = isAnimated.optional()
//    ) else if (guildId != null && emojiId != null) {
//        val emoji = kord.rest.emoji.getEmoji(guildId, emojiId)
//        DiscordPartialEmoji(
//            emoji.id,
//            emoji.name,
//            emoji.animated
//        )
//    } else null
//}

fun GuildEmoji?.toPartialEmoji(): DiscordPartialEmoji? {
    return if (this != null) DiscordPartialEmoji(
        id = id,
        name = name,
        animated = isAnimated.optional()
    ) else null
}
