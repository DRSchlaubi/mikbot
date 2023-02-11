package dev.schlaubi.votebot.api.models

import dev.kord.common.entity.DiscordPartialGuild
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import space.votebot.common.models.*

fun DiscordPartialGuild.toServer(voteCount: Long) =
    Server(id.value, name, voteCount.toInt(), icon.toString())

fun Guild.toServer(polls: List<PartialAPIPoll>) =
    Server(id.value, name, polls.size, data.icon, polls = polls)

fun User.toDiscordUser() = DiscordUser(
    id.value, username, null, discriminator, data.avatar, discriminator
)

fun Poll.toPartialAPIPoll(user: DiscordUser?) = PartialAPIPoll(
    id, guildId, user, votes.sumOf(Poll.Vote::amount)
)

fun Poll.toAPIPoll(author: DiscordUser?) = APIPoll(
    id,
    guildId,
    author,
    title,
    sumUp(),
    createdAt,
    settings
)
