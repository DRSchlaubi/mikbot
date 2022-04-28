package space.votebot.util

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.schlaubi.mikbot.plugin.api.util.discordError

suspend fun <A : Arguments> EphemeralSlashCommandContext<A>.checkPermissions(channel: GuildMessageChannel) {
    val selfPermissions = channel.getEffectivePermissions(channel.kord.selfId)
    val requiredPermissions = Permissions(Permission.SendMessages, Permission.EmbedLinks, Permission.ViewChannel)
    if (requiredPermissions !in selfPermissions) {
        discordError(translate("vote.create.missing_permissions.bot", arrayOf(channel.mention)))
    }

    val userPermissions = channel.getEffectivePermissions(user.id)
    if (requiredPermissions !in userPermissions) {
        discordError(translate("vote.create.missing_permissions.user", arrayOf(channel.mention)))
    }
}

private suspend fun GuildMessageChannel.getEffectivePermissions(user: Snowflake) = when (this) {
    is TopGuildMessageChannel -> getEffectivePermissions(user)
    is ThreadChannel -> parent.asChannel().getEffectivePermissions(user)
    else -> error("Could not determine permissions for channel type ${this::class.simpleName}")
}
