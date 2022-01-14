package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.edit
import dev.kord.core.event.interaction.GuildComponentInteractionCreateEvent
import dev.kord.rest.builder.message.modify.actionRow
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.MusicPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun MusicModule.fixCommand() = ephemeralControlSlashCommand {
    name = "fix"
    description = "Troubleshoots audio playback issues"

    check {
        requireBotPermissions(Permission.ManageGuild)
    }

    action {
        val applicableRecoverySteps = RecoveryStep.steps.filter {
            with(it) {
                musicPlayer.applicable
            }
        }.iterator()

        var nextStep: RecoveryStep? = applicableRecoverySteps.next()
        while (nextStep != null) {
            val current: RecoveryStep = nextStep

            edit {
                content = translate("commands.fix.running_step", arrayOf(translate(current.nameKey)))
                components = mutableListOf()
            }

            with(current) {
                apply(musicPlayer)
            }

            edit {
                content = translate("commands.fix.ran_step", arrayOf(translate(current.nameKey)))

                if (applicableRecoverySteps.hasNext()) {
                    nextStep = applicableRecoverySteps.next()

                    actionRow {
                        interactionButton(ButtonStyle.Primary, "next_step") {
                            label = translate(nextStep!!.nameKey)
                        }

                        interactionButton(ButtonStyle.Success, "abort") {
                            label = translate("commands.fix.abort")
                        }
                    }
                } else {
                    edit {
                        content = translate("command.fix.troubleshooting_done")
                    }
                    nextStep = null
                }
            }

            val interaction = event.interaction
            val user = event.interaction.user

            val event = channel.kord.waitFor<GuildComponentInteractionCreateEvent> {
                this.interaction.message?.interaction?.id == interaction.id && this.interaction.user == user
            }

            event?.interaction?.acknowledgeEphemeralDeferredMessageUpdate()
            if (event?.interaction?.componentId != "next_step") {
                nextStep = null
                edit {
                    components = mutableListOf()
                }
            }
        }
    }
}

private interface RecoveryStep {
    val nameKey: String

    val MusicPlayer.applicable: Boolean
        get() = true

    suspend fun EphemeralSlashCommandContext<*>.apply(musicPlayer: MusicPlayer)

    companion object {
        val steps = listOf(
            UnPausePlayback, RestartPlayback, ReEstablishVoiceConnection,
            SwitchVoiceServers, SkipTrack
        )
    }
}

private object UnPausePlayback : RecoveryStep {

    override val nameKey: String = "commands.fix.step.unpause_plackback"
    override val MusicPlayer.applicable: Boolean
        get() = link.player.paused

    override suspend fun EphemeralSlashCommandContext<*>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.pause(false)
    }
}

private object RestartPlayback : RecoveryStep {

    override val nameKey: String = "commands.fix.step.restart_playback"

    override suspend fun EphemeralSlashCommandContext<*>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.player.pause(true)
        delay(500.milliseconds)
        musicPlayer.player.pause(false)
    }
}

private object ReEstablishVoiceConnection : RecoveryStep {

    override val nameKey: String = "commands.fix.step.re_establish_voice_connection"

    override suspend fun EphemeralSlashCommandContext<*>.apply(musicPlayer: MusicPlayer) {
        val state = musicPlayer.toState()
        val channelId = musicPlayer.link.lastChannelId!!

        musicPlayer.link.disconnectAudio()
        delay(200.milliseconds)
        musicPlayer.link.connectAudio(channelId)
        state.applyToPlayer(musicPlayer)
    }
}

private object SwitchVoiceServers : RecoveryStep {
    override val nameKey: String = "commands.fix.step.switch_voice_server"

    override suspend fun EphemeralSlashCommandContext<*>.apply(musicPlayer: MusicPlayer) {
        val channel = musicPlayer.getChannel()!!
        val currentRegion = channel.rtcRegion

        val availableRegions = (
            safeGuild.regions
                .map { it.id }.toList() - (currentRegion ?: "")
            )
        val fallbackRegion = availableRegions.random()

        channel.edit {
            rtcRegion = fallbackRegion
        }
        delay(5.seconds)
        channel.edit {
            rtcRegion = currentRegion
        }
    }
}

object SkipTrack : RecoveryStep {
    override val MusicPlayer.applicable: Boolean
        get() = queuedTracks.isNotEmpty()

    override val nameKey: String = "commands.fix.step.skip_track"

    override suspend fun EphemeralSlashCommandContext<*>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.skip()
    }
}
