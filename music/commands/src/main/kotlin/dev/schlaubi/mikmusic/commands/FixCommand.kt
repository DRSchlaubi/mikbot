package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.utils.waitFor
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.edit
import dev.kord.core.event.interaction.GuildComponentInteractionCreateEvent
import dev.kord.rest.builder.message.actionRow
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.MusicPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun MusicModule.fixCommand() = ephemeralControlSlashCommand {
    name = MusicTranslations.Commands.Fix.name
    description = MusicTranslations.Commands.Fix.description
    musicControlContexts()

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
                content = translate(MusicTranslations.Commands.Fix.runningStep, translate(current.nameKey))
                components = mutableListOf()
            }

            with(current) {
                apply(musicPlayer)
            }

            edit {
                content = translate(MusicTranslations.Commands.Fix.ranStep, translate(current.nameKey))

                if (applicableRecoverySteps.hasNext()) {
                    nextStep = applicableRecoverySteps.next()

                    actionRow {
                        interactionButton(ButtonStyle.Primary, "next_step") {
                            label = translate(nextStep!!.nameKey)
                        }

                        interactionButton(ButtonStyle.Success, "abort") {
                            label = translate(MusicTranslations.Commands.Fix.abort)
                        }
                    }
                } else {
                    edit {
                        content = translate(MusicTranslations.Command.Fix.troubleshootingDone)
                    }
                    nextStep = null
                }
            }

            val interaction = event.interaction
            val user = event.interaction.user

            val event = channel.kord.waitFor<GuildComponentInteractionCreateEvent>(timeout = null as Long?) {
                this.interaction.message.interactionMetadata?.id == interaction.id && this.interaction.user == user
            }

            event?.interaction?.deferEphemeralMessageUpdate()
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
    val nameKey: Key

    val MusicPlayer.applicable: Boolean
        get() = true

    suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer)

    companion object {
        val steps = listOf(
            ReJoinVoiceChannel, UnPausePlayback, RestartPlayback, ReEstablishVoiceConnection,
            SwitchVoiceServers, SkipTrack
        )
    }
}

private object ReJoinVoiceChannel : RecoveryStep {
    override val nameKey: Key = MusicTranslations.Commands.Fix.Step.reJoinChannel
    override val MusicPlayer.applicable: Boolean
        get() = link.lastChannelId == null || link.state == Link.State.NOT_CONNECTED

    override suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.connectAudio(member!!.getVoiceState().channelId!!)
    }
}

private object UnPausePlayback : RecoveryStep {
    override val nameKey: Key = MusicTranslations.Commands.Fix.Step.unpausePlackback
    override val MusicPlayer.applicable: Boolean
        get() = link.player.paused

    override suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.pause(false)
    }
}

private object RestartPlayback : RecoveryStep {

    override val nameKey: Key = MusicTranslations.Commands.Fix.Step.restartPlayback

    override suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.player.pause(true)
        delay(500.milliseconds)
        musicPlayer.player.pause(false)
    }
}

private object ReEstablishVoiceConnection : RecoveryStep {

    override val nameKey: Key = MusicTranslations.Commands.Fix.Step.reEstablishVoiceConnection

    override suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer) {
        val state = musicPlayer.toState()
        val channelId = musicPlayer.link.lastChannelId!!

        musicPlayer.stop()
        delay(200.milliseconds)
        musicPlayer.link.connectAudio(channelId)
        musicPlayer.applyState(state)
    }
}

private object SwitchVoiceServers : RecoveryStep {
    override val nameKey: Key = MusicTranslations.Commands.Fix.Step.switchVoiceServer

    override suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer) {
        val channel = musicPlayer.getChannel()!!
        val currentRegion = channel.rtcRegion

        val availableRegions = safeGuild.regions.map { it.id }.toList() - (currentRegion ?: "")
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

    override val nameKey: Key = MusicTranslations.Commands.Fix.Step.skipTrack

    override suspend fun EphemeralSlashCommandContext<*, *>.apply(musicPlayer: MusicPlayer) {
        musicPlayer.skip()
    }
}
