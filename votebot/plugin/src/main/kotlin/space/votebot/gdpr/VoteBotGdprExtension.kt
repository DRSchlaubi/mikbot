package space.votebot.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.pf4j.Extension
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase

@Extension
class VoteBotGdprExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(VotingsDataPoint, VotesDataPoint, DefaultSettingsDataPoints)
}

object VotingsDataPoint : PermanentlyStoredDataPoint("votebot", "gdpr.votings.name", "gdpr.votings.description") {
    override suspend fun deleteFor(user: User) {
        VoteBotDatabase.polls.deleteMany(Poll::authorId eq user.id.value)
    }

    override suspend fun requestFor(user: User): List<String> {
        val polls = VoteBotDatabase.polls.find(Poll::authorId eq user.id.value).toList()

        return polls.map { poll ->
            val options = poll.options.asSequence()
                .filterIsInstance<Poll.Option.ActualOption>()
                .map { it.option }
                .toList()
            val messages = poll.messages.map { it.messageId }.joinToString(", ")

            "Poll(name=${poll.title}, settings=${poll.settings}, options=${options}, messages=${messages}, createdAt=${poll.createdAt}"
        }
    }
}

object VotesDataPoint : PermanentlyStoredDataPoint("votebot", "gdpr.votes.name", "gdpr.votes.description") {
    override suspend fun deleteFor(user: User) {
        VoteBotDatabase.polls.deleteMany(Poll::votes / Poll.Vote::userId eq user.id.value)
    }

    override suspend fun requestFor(user: User): List<String> {
        val votes = VoteBotDatabase.polls.find(Poll::votes / Poll.Vote::userId eq user.id.value).toList()

        return votes.map { poll ->
            poll.votes
                .filter { it.userId == user.id.value }
                .joinToString(", ") { "Vote(option=${it.forOption}, pollId=${poll.id}" }
        }
    }
}

object DefaultSettingsDataPoints :
    PermanentlyStoredDataPoint("votebot", "gdpr.default_options.name", "gdpr.default_options.description") {
    override suspend fun deleteFor(user: User) {
        VoteBotDatabase.userSettings.deleteOneById(user.id)
    }

    override suspend fun requestFor(user: User): List<String> {
        val settings = VoteBotDatabase.userSettings.findOneById(user.id)

        return listOf(settings?.toString() ?: "None")
    }
}
