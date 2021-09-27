package dev.schlaubi.musicbot.module.music.checks

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.hasRole
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.MessageCommandInteraction
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.musicbot.core.audio.LavalinkManager
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.ifPassing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlin.time.Duration

suspend fun <T : Event> CheckContext<T>.joinSameChannelCheck(extensibleBot: ExtensibleBot) {
    abstractMusicCheck {
        val botChannelMembers = guild.voiceStates.count { it.channelId == botChannel }
        if (botChannel == null || botChannelMembers == 1) {
            val lavalink = extensibleBot.findExtension<LavalinkManager>()!!.getLink(guild)

            if (botChannel != null) {
                lavalink.disconnectAudio()
                delay(Duration.milliseconds(400)) // wait for Discord API to propagate
            }
            lavalink.connectAudio(voiceChannel)

            guild.getMember(guild.kord.selfId).edit {
                deafened = true
            }
        } else if (voiceChannel != botChannel) {
            fail(translateM("music.checks.already_in_use"))
        }
    }
}

suspend fun <T : InteractionCreateEvent> CheckContext<T>.musicControlCheck() {
    abstractMusicCheck {
        if (botChannel == null) {
            return@abstractMusicCheck fail(translateM("music.checks.no_running"))
        }
        if (voiceChannel != botChannel) {
            fail(translateM("music.checks.not_in_same_vc"))
        }
    }
}

private suspend inline fun <T : Event> CheckContext<T>.abstractMusicCheck(
    block: MusicCheckContext.() -> Unit
) {
    if (!passed) {
        return
    }

    val member = memberFor(event) ?: (event as? ComponentInteractionCreateEvent)?.let {
        it.interaction.message?.getGuild()?.id?.let { snow ->
            it.interaction.user.asMember(
                snow
            )
        }
    }
    val voiceChannel = member?.getVoiceStateOrNull()?.channelId
        ?: return fail(translateM("music.checks.not_in_vc"))
    val guild = member.guild

    val guildSettings = database.guildSettings.findGuild(guild)
    if (guildSettings.djMode) {
        hasRole(guildSettings.djRole!!)
    }

    ifPassing {
        val bot = guild.getMember(member.kord.selfId)
        val botChannel = bot.getVoiceStateOrNull()?.channelId

        block(MusicCheckContext(voiceChannel, botChannel, guild))
    }
}

private fun CheckContext<*>.translateM(key: String) = translate(key, "music")

@JvmRecord
private data class MusicCheckContext(
    val voiceChannel: Snowflake,
    val botChannel: Snowflake?,
    val guild: GuildBehavior
)
