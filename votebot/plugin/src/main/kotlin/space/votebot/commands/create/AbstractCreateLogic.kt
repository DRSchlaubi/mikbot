package space.votebot.commands.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import kotlinx.datetime.Clock
import org.litote.kmongo.newId
import space.votebot.common.models.Poll
import space.votebot.common.models.merge
import space.votebot.core.VoteBotDatabase
import space.votebot.core.addExpirationListener
import space.votebot.core.updateMessages
import space.votebot.util.toPollMessage

suspend fun <A> EphemeralSlashCommandContext<A>.createVote()
        where A : Arguments, A : CreateSettings = createVote { arguments }

suspend fun <A : Arguments> EphemeralSlashCommandContext<A>.createVote(
    optionProvider: EphemeralSlashCommandContext<A>.() -> CreateSettings
) {
    val settings = optionProvider()
    val channel = (settings.channel ?: this.channel).asChannelOf<TopGuildMessageChannel>()

    val selfPermissions = channel.getEffectivePermissions(channel.kord.selfId)
    val requiredPermissions = Permissions(Permission.SendMessages, Permission.EmbedLinks)
    if (requiredPermissions !in selfPermissions) {
        discordError(translate("vote.create.missing_permissions.bot"))
    }

    val userPermissions = channel.getEffectivePermissions(user.id)
    if (requiredPermissions !in userPermissions) {
        discordError(translate("vote.create.missing_permissions.user"))
    }

    if (settings.answers.size > 25) {
        discordError(translate("vote.create.too_many_options"))
    }

    val toLongOption = settings.answers.firstOrNull {
        it.length > 50
    }

    if (toLongOption != null) {
        discordError(translate("vote.create.too_long_option", arrayOf(toLongOption)))
    }

    val finalSettings = if (settings.settings.complete) {
        settings.settings.merge(null)
    } else {
        val globalSettings = VoteBotDatabase.userSettings.findOneById(user.id)?.settings
        settings.settings.merge(globalSettings)
    }

    val message = channel.createMessage("New vote loading")

    val poll = Poll(
        newId<Poll>().toString(),
        safeGuild.id.value,
        user.id.value,
        settings.title,
        settings.answers.mapIndexed { index, option -> Poll.Option(index, option) },
        emptyMap(),
        emptyList(),
        listOf(message.toPollMessage()),
        Clock.System.now(),
        finalSettings
    )

    VoteBotDatabase.polls.save(poll)
    poll.updateMessages(channel.kord)
    if(finalSettings.deleteAfter != null) {
        poll.addExpirationListener(message.kord)
    }
}
