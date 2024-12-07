package dev.schlaubi.mikmusic.api

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.GuildChannel
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikmusic.api.types.*
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.player.MusicPlayer

class ForbiddenException : RuntimeException("Insufficient permissions")

suspend fun QueuedTrack.toAPIQueuedTrack(guild: Guild): APIQueuedTrack {
    val chapters = (this as? ChapterQueuedTrack)?.chapters
    val member = guild.getMember(queuedBy)

    return APIQueuedTrack(
        member.toMinimalUser(),
        track,
        chapters
    )
}

fun Member.toMinimalUser() = MinimalUser(id, effectiveName, effectiveAvatar)

suspend fun Iterable<QueuedTrack>.mapToAPIQueuedTrack(guild: Guild) = map { it.toAPIQueuedTrack(guild) }

suspend fun MusicPlayer.toSelectedPlayer(user: Snowflake): Player {
    val guild = kord.getGuild(Snowflake(guildId))

    val playerState = toPlayerState(guild)
    val member = guild.getMemberOrNull(user)
    val userVoiceState = member?.getVoiceStateOrNull()
    val botVoiceState = guild.getMember(kord.selfId).getVoiceStateOrNull()

    val guildSettings = MusicSettingsDatabase.findGuild(guild)

    val djModePermission = !guildSettings.djMode || guildSettings.djRole in (member?.roleIds ?: emptyList())
    val channelMissmatch =
        botVoiceState != null && botVoiceState.channelId != null && botVoiceState.channelId != userVoiceState?.channelId

    return Player(
        playerState,
        queuedTracks.mapToAPIQueuedTrack(guild),
        Player.VoiceState(
            channelMissmatch,
            botVoiceState?.let {
                val channel = it.getChannelOrNull() ?: return@let null

                Channel(channel.id, channel.guildId, (channel as GuildChannel).name)
            },
            djModePermission
        )
    )
}

fun Player.requirePermission() {
    if (voiceState.channel == null || voiceState.channelMismatch || !voiceState.playerAvailable) {
        throw ForbiddenException()
    }
}

suspend fun MusicPlayer.toPlayerState(guild: Guild) = PlayerState(
    guild.toMinimalGuild(),
    currentTrack = playingTrack?.let {
        PlayerState.PlayingTrack(it.toAPIQueuedTrack(guild), player.positionDuration, player.volume)
    },
    SchedulerSettings(
        loopQueue,
        repeat,
        shuffle,
        null
    ),
    player.paused
)

fun Guild.toMinimalGuild() = MinimalGuild(
    id,
    name,
    icon?.cdnUrl?.toUrl(),
    splash?.cdnUrl?.toUrl()
)

fun GuildChannel.toChannel() = Channel(id, guildId, name)

fun UpdatablePlayerState.SchedulerSettings.toSchedulingOptions() = SchedulerSettings(
    loopQueue.value,
    repeat.value,
    shuffle.value,
    null
)
