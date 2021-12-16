package space.votebot.util

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.schlaubi.mikbot.plugin.api.util.discordError

suspend fun <A : Arguments> EphemeralSlashCommandContext<A>.checkPermissions(
    channel: TopGuildMessageChannel
) {
    val selfPermissions = channel.getEffectivePermissions(channel.kord.selfId)
    val requiredPermissions = Permissions(Permission.SendMessages, Permission.EmbedLinks)
    if (requiredPermissions !in selfPermissions) {
        discordError(translate("vote.create.missing_permissions.bot"))
    }

    val userPermissions = channel.getEffectivePermissions(user.id)
    if (requiredPermissions !in userPermissions) {
        discordError(translate("vote.create.missing_permissions.user"))
    }
}
