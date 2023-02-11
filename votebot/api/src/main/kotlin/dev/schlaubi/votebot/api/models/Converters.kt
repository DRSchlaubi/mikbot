package dev.schlaubi.votebot.api.models

import dev.kord.common.entity.DiscordPartialGuild
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import space.votebot.common.models.*

fun DiscordPartialGuild.toServer(voteCount: Long) =
    Server(id.toString(), name, voteCount.toInt(), icon.toString())

fun Guild.toServer(polls: List<PartialAPIPoll>) =
    Server(id.toString(), name, polls.size, data.icon, polls = polls)

fun User.toDiscordUser() = DiscordUser(
    id.toString(), username, null, discriminator, data.avatar
)

fun Poll.toPartialAPIPoll(user: DiscordUser?) = PartialAPIPoll(
    id, guildId.toString(), user, votes.sumOf(Poll.Vote::amount), title
)

fun Poll.toAPIPoll(author: DiscordUser?) = APIPoll(
    id,
    guildId.toString(),
    author,
    title,
    sumUp(),
    createdAt,
    settings
)
