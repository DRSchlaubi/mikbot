package dev.schlaubi.mikbot.eval.integration

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.Interaction

class ExecutionContext(
    val guild: Guild,
    val member: Member,
    val user: User,
    val interaction: Interaction,
    val channel: GuildMessageChannel
)
