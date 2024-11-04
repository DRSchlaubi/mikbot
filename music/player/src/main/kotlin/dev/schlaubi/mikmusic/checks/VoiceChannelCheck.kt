package dev.schlaubi.mikmusic.checks

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.checks.hasRole
import dev.kordex.core.checks.memberFor
import dev.kordex.core.checks.types.CheckContext
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.mikbot.plugin.api.util.ifPassing
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlin.time.Duration.Companion.milliseconds

suspend fun <T : Event> CheckContext<T>.joinSameChannelCheck(extensibleBot: ExtensibleBot) {
    abstractMusicCheck {
        val botChannelMembers = guild.voiceStates.count { it.channelId == botChannel }
        if (botChannel == null || botChannelMembers == 1) {
            val lavalink = extensibleBot.findExtension<MusicModule>()!!.getMusicPlayer(guild)

            if (botChannel != null) {
                lavalink.disconnectAudio()
                delay(400.milliseconds) // wait for Discord API to propagate
            }
            lavalink.connectAudio(voiceChannel)
            lavalink.startLeaveTimeout() // start it here, so the bot leaves in case no track is queued

            guild.getMember(guild.kord.selfId).edit {
                deafened = true
            }
        } else if (voiceChannel != botChannel) {
            fail(MusicTranslations.Music.Checks.already_in_use)
        }
    }
}

suspend fun <T : InteractionCreateEvent> CheckContext<T>.musicControlCheck(ignoreDjMode: Boolean = false) {
    abstractMusicCheck(ignoreDjMode) {
        if (botChannel == null) {
            return@abstractMusicCheck fail(MusicTranslations.Music.Checks.no_running)
        }
        if (voiceChannel != botChannel) {
            fail(MusicTranslations.Music.Checks.no_running)
        }
    }
}

private suspend inline fun <T : Event> CheckContext<T>.abstractMusicCheck(
    ignoreDjMode: Boolean = false,
    block: MusicCheckContext.() -> Unit,
) {
    if (!passed) {
        return
    }

    val member = memberFor(event) ?: (event as? ComponentInteractionCreateEvent)?.let {
        it.interaction.message.getGuild().id.let { snow ->
            it.interaction.user.asMember(
                snow
            )
        }
    }
    val voiceChannel = member?.getVoiceStateOrNull()?.channelId
        ?: return fail(MusicTranslations.Music.Checks.not_in_vc)
    val guild = member.guild

    val guildSettings = MusicSettingsDatabase.findGuild(guild)
    if (guildSettings.djMode && !ignoreDjMode) {
        hasRole(guildSettings.djRole!!)
    }

    ifPassing {
        val bot = guild.getMember(member.kord.selfId)
        val botChannel = bot.getVoiceStateOrNull()?.channelId

        block(MusicCheckContext(voiceChannel, botChannel, guild))
    }
}

private data class MusicCheckContext(
    val voiceChannel: Snowflake,
    val botChannel: Snowflake?,
    val guild: GuildBehavior,
)
