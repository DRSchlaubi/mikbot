package space.votebot.core

import com.kotlindiscord.kord.extensions.events.EventContext
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.dm
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import space.votebot.common.models.Poll
import space.votebot.util.reFetch

suspend fun Poll.close(kord: Kord, showChart: Boolean? = null, guild: GuildBehavior) {
    with(reFetch()) {
        updateMessages(kord, removeButtons = true, highlightWinner = true, guild = guild, showChart = showChart)

        VoteBotDatabase.polls.deleteOneById(id)

        if (settings.publicResults) {
            kord.getUser(Snowflake(authorId))?.dm {
                content = "This message contains the requested vote statistic for your poll: $id"

                addFile("votes.csv", generateCSVFile(kord))
            }
        }
    }
}

@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun VoteBotModule.voteExecutor() = event<GuildButtonInteractionCreateEvent> {
    action {
        onVote(kord.unsafe.guild(event.interaction.guildId))
    }
}

private suspend fun EventContext<GuildButtonInteractionCreateEvent>.onVote(guild: GuildBehavior) {
    val interaction = event.interaction

    val message = interaction.message ?: return
    val poll = VoteBotDatabase.polls.findOneByMessage(message) ?: return

    val option = interaction.componentId.substringAfter("vote_").toIntOrNull() ?: return

    val ack = interaction.acknowledgeEphemeralDeferredMessageUpdate()

    val userId = interaction.user.id.value
    val userVotes = poll.votes.asSequence()
        .filter { it.userId == userId }
        .sumOf { it.amount }

    val newPoll = if (userVotes > 0) {
        val settings = poll.settings
        if (settings.maxVotes == 1 && settings.maxChanges == 0) {
            ack.followUpEphemeral { content = translate("vote.voted_already") }
            return
        } else if (settings.maxChanges == 0) { // maxVotes > 1
            if (settings.maxVotes > userVotes) {
                val existingVote =
                    poll.votes.firstOrNull { it.userId == userId && it.forOption == option }
                        ?: Poll.Vote(
                            option,
                            userId,
                            0
                        )

                val newVote = existingVote.copy(amount = existingVote.amount + 1)

                poll.copy(votes = poll.votes - existingVote + newVote)
            } else {
                ack.followUpEphemeral { content = translate("vote.too_many_votes", arrayOf(settings.maxVotes)) }
                return
            }
        } else { // maxChanges >= 1
            val changes = poll.changes[userId] ?: 0
            if (changes >= settings.maxChanges) {
                ack.followUpEphemeral { content = translate("vote.too_many_changes", arrayOf(settings.maxChanges)) }
                return
            }
            val oldVote = poll.votes.first { it.userId == userId }
            val newVote = Poll.Vote(option, userId)

            poll.copy(
                votes = poll.votes - oldVote + newVote,
                changes = poll.changes + (userId to changes + 1)
            )
        }
    } else {
        poll.copy(votes = poll.votes + Poll.Vote(option, userId))
    }

    VoteBotDatabase.polls.save(newPoll)
    newPoll.updateMessages(interaction.kord, guild)
// TODO: discuss this
//    if (poll.settings.hideResults) {
//        ack.followUpEphemeral {
//            embeds.add(newPoll.toEmbed(ack.kord, highlightWinner = false, overwriteHideResults = true, guild = guild))
//        }
//    }
}
