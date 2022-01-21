package space.votebot.command

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.utils.focusedOption
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.mongodb.client.model.Filters.and
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.GuildAutoCompleteInteraction
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import info.debatty.java.stringsimilarity.Levenshtein
import org.litote.kmongo.eq
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase
import space.votebot.core.findOneByMessage
import space.votebot.util.jumpUrl

private val levenshtein = Levenshtein()

abstract class PollArguments(pollArgumentDescription: String) : Arguments() {
    val pollMessage by message {
        name = "poll"
        description = pollArgumentDescription

        autoComplete {
            val safeInput = focusedOption.safeInput

            val possible = VoteBotDatabase.polls
                .find(
                    and(
                        Poll::authorId eq user.id.value,
                        Poll::guildId eq (this as GuildAutoCompleteInteraction).guildId.value
                    )
                )
                .toList()
                .sortedBy { levenshtein.distance(safeInput, it.title, 50) }

            if (possible.isNotEmpty()) {
                suggestString {
                    possible.subList(0, 25.coerceAtMost(possible.size)).forEach {
                        choice(it.title, it.messages.first().jumpUrl)
                    }
                }
            }
        }
    }
}

suspend fun <A : PollArguments> SlashCommandContext<*, A>.poll(): Poll {
    val poll = VoteBotDatabase.polls.findOneByMessage(arguments.pollMessage)
        ?: discordError(translate("commands.generic.poll_not_found"))
    val user = getUser()
    if (user.id.value != poll.authorId &&
        getMember()?.run { asMember().hasPermission(Permission.ManageGuild) } != true
    ) {
        discordError(translate("commands.generic.no_permission"))
    }

    return poll
}
