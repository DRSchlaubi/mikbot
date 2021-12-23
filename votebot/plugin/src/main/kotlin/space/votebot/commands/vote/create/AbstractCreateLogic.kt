package space.votebot.commands.vote.create

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import kotlinx.datetime.Clock
import org.litote.kmongo.newId
import space.votebot.common.models.Poll
import space.votebot.common.models.merge
import space.votebot.core.VoteBotDatabase
import space.votebot.core.addExpirationListener
import space.votebot.core.addMessage
import space.votebot.core.findOneByGuild
import space.votebot.util.checkPermissions
import space.votebot.util.toPollMessage

suspend fun <A> EphemeralSlashCommandContext<A>.createVote()
        where A : Arguments, A : CreateSettings = createVote { arguments }

suspend fun <A : Arguments> EphemeralSlashCommandContext<A>.createVote(
    optionProvider: EphemeralSlashCommandContext<A>.() -> CreateSettings
) {
    val kord = getKoin().get<Kord>()
    val settings = optionProvider()
    val guildVoteChannel = VoteBotDatabase.guildSettings.findOneByGuild(guild!!.id)?.voteChannelId?.let {
        kord.getChannelOf<TopGuildMessageChannel>(it)
    }
    val channel = (guildVoteChannel ?: settings.channel ?: this.channel).asChannelOf<TopGuildMessageChannel>()

    checkPermissions(channel)

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

    if (finalSettings.publicResults && !attemptSendingDMs()) {
        return
    }

    val poll = Poll(
        newId<Poll>().toString(),
        safeGuild.id.value,
        user.id.value,
        settings.title,
        settings.answers.map { Poll.Option.ActualOption(null, it) },
        emptyMap(),
        emptyList(),
        emptyList(),
        Clock.System.now(),
        finalSettings
    )
    val message = poll.addMessage(channel, addButtons = true, addToDatabase = false)
    VoteBotDatabase.polls.save(poll.copy(messages = listOf(message.toPollMessage())))

    if (finalSettings.deleteAfter != null) {
        poll.addExpirationListener(channel.kord)
    }
}

private suspend fun <A : Arguments> EphemeralSlashCommandContext<A>.attemptSendingDMs(): Boolean {
    if (user.getDmChannelOrNull() == null) {
        val (agreed) = confirmation(
            yesWord = translate("vote.create.retry"),
            noWord = translate("vote.create.cancel"),
        ) {
            content = translate("vote.create.dms_disabled")
        }
        if (agreed) {
            return attemptSendingDMs()
        }
        return false
    }

    return true
}
