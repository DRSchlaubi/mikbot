package space.votebot.util

import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase

suspend fun Poll.reFetch() = VoteBotDatabase.polls.findOneById(id) ?: this
