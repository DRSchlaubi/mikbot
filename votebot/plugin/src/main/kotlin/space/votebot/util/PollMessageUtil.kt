package space.votebot.util

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildChannel
import space.votebot.common.models.Poll

suspend fun Message.toPollMessage() = Poll.Message(
    id.value, channelId.value, channel.asChannelOf<GuildChannel>().guildId.value
)

@OptIn(KordUnsafe::class, KordExperimental::class)
fun Poll.Message.toBehavior(kord: Kord) = kord.unsafe.message(
    Snowflake(channelId), Snowflake(messageId)
)

val Poll.Message.jumpUrl: String
    get() = "https://discord.com/channels/$guildId/$channelId/$messageId"
