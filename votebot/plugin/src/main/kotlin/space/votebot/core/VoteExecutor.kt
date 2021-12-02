package space.votebot.core

import com.kotlindiscord.kord.extensions.events.EventContext
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import space.votebot.common.models.Poll

suspend fun VoteBotModule.voteExecutor() = event<GuildButtonInteractionCreateEvent> {
    action {
        onVote()
    }
}

private suspend fun EventContext<GuildButtonInteractionCreateEvent>.onVote() {
    val interaction = event.interaction

    val message = interaction.message ?: return
    val poll = VoteBotDatabase.polls.findByMessage(message) ?: return

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
                ack.followUpEphemeral { content = translate("vote.too_many_votes") }
                return
            }
        } else { // maxChanges >= 1
            val changes = poll.changes[userId] ?: 0
            if (changes >= settings.maxChanges) {
                ack.followUpEphemeral { content = translate("vote.too_many_changes") }
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
    newPoll.updateMessages(interaction.kord)
}